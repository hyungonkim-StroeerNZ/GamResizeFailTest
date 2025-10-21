package sport1.mobile.android.app

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.BannerParameters
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.api.data.BidInfo
import org.prebid.mobile.api.data.InitializationStatus
import org.prebid.mobile.api.original.OnFetchDemandResult
import java.io.IOException
import kotlin.coroutines.resume

class MainActivity : AppCompatActivity() {

    val adSizes = AdSizeGenerator.generateAdSizes()
    val configIds = ConfigIdGenerator.generateConfigIds()
    val dpfUnitIds = DfpUnitIdGenerator.generateDfpUnitIds()

    var viewList : LinearLayout? = null

    @RequiresPermission(Manifest.permission.INTERNET)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var initialized = false
        CoroutineScope(Dispatchers.IO).launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {
                Log.i("MainActivity", "Google Mobile Ads SDK initialized.")
                initialized = true
            }
        }

        viewList = findViewById(R.id.linearView)

        initPrebidOnce()

        val reloadButton = findViewById<Button>(R.id.reloadButton)
        reloadButton.setOnClickListener {
            lifecycleScope.launch {
                if(!initialized){
                    Toast.makeText(this@MainActivity, "SDK not initialized yet. wait for a while", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                for(view in viewList!!.children) {
                    if(view is AdManagerAdView) {
                        view.destroy()
                    }
                }
                viewList!!.removeAllViews()
                for(slotName in configIds.keys) {
                    val result = startPrebidFetch(slotName)
                    Log.i("MainActivity", "Download result for $slotName: $result")
                    callDfp(slotName, result)
                }
            }
        }
    }

    val client = OkHttpClient()

    private suspend fun downloadJson(url: String): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        "Failed: HTTP ${response.code}"
                    } else {
                        response.body?.string() ?: "Empty body"
                    }
                }
            } catch (e: IOException) {
                "Error: ${e.message}"
            }
        }
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    fun callDfp(slotName: String, keyValues : Map<String, String>) {
        val requestBuilder = AdManagerAdRequest.Builder()
        for (key in keyValues.keys) {
            requestBuilder.addCustomTargeting(key, keyValues[key]!!)
        }

        requestBuilder.setHttpTimeoutMillis(7000)
        requestBuilder.addCustomTargeting("androidYlSdkVersion", listOf("7.3.0"))

        val request = requestBuilder.build()

        val gamView = AdManagerAdView(this)
        gamView.adUnitId = dpfUnitIds[slotName]!!
        gamView.setAdSizes(*adSizes[slotName]!!.toTypedArray())
        gamView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(var1: LoadAdError) {
            }

            override fun onAdImpression() {
            }

            override fun onAdLoaded() {
            }
        }
        gamView.loadAd(request)
        viewList!!.addView(gamView)
    }


    private fun initPrebidOnce() {
        // Minimal init; adjust to your PBS host/account
        PrebidMobile.setPrebidServerAccountId("sport1")
        PrebidMobile.setShareGeoLocation(false)
        PrebidMobile.setTimeoutMillis(7000) // 2000ms by default; change if needed
        PrebidMobile.setPbsDebug(false)
        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=sport1.mobile.android.apps&hl=de&gl=US")
        TargetingParams.setOmidPartnerName( "Google")
        TargetingParams.setOmidPartnerVersion("213502000.213502000")
        IabConsentStore.updateTcString(
            this,
            "CQZltMAQZltMAAGABCENCAFgAP_gAEPgAAYgKEBBJCpdTWFAMDJ1QJsAQYBV19gBIEQABACAAyAFAAKA4JQCwWECEAQAACACAQAAo1ABIABEEABEQEAAIAAEAABEAAQQgABIIABAAAEQQgBAAAgAAAAAEAAIAAAJMAAAkACAIQKEEEAgAIAgKgCAAIAAAACAAAMADEA4ABAAAAIAoohAgAIAEAKAAAEAAQgAAAAAAAAAAABAQAAAIAAAACAABBAyAUACQAFQAfABHAD7AIQARwBCADcwIGAHAgAICjwkCYACoAHAAPAAggBkAGgATAA_ACEgEMARIAjgBNADDgH2AfoBFACNAEiALmAXoAxQBtADcAHEASIAmkBQ4C8wGGgNXAayA2MBuYDkwHjgQTAhCBC4CaoFCAgAQAlIBNAD-gKPDAAQFHiAAICjx0CkACoAHAAQQAyADQAJgAYgA_QCGAIkATQAwwBowD7AP0AikBHQEiALEAXOAvIC9AGKANoAbgA4gCEAEXgJEATIAmkBQ4C3QF5gMNAZYA00BqoDVwHJgPHAf2BAECWgE1QKEDgBIAFwAoAB8AIiASkAmgB_QFHgMmIQCwAxAB-AIoASkAuYBigDaAJpAaqA8cB_ZAACAWIlAQAA4AEwAMUAhgCJAEcAPwAuYBigDiAIQAReAkQBeYEAQIQgTVJAAwALgKPAZYUgQgAVAA4ACAAGgATAAxAB-gEMARIA0YB-AH7AR0BIgC5gF5AMUAbQA3ABxAEXgJEATSAocBeYDDQGWANZAcmA8UB44D-wIJgQhAhyBNUoAKAAUABcAGQAUAAtgEpALEAXUBR4DJi0AMAvQChwHjgA.YAAAAAAAAAAA",
            )

        PrebidMobile.initializeSdk(this, "https://s2s.yieldlove-ad-serving.net/openrtb2/auction", object : org.prebid.mobile.rendering.listeners.SdkInitializationListener {
            override fun onInitializationComplete(status: InitializationStatus) {
                Log.i("MainActivity", "Prebid SDK initialization complete")
            }
        })
    }

    private suspend fun startPrebidFetch(slotName : String) : MutableMap<String, String>{
        // Example banner ad unit (stored request configId from PBS)
        val configId = configIds[slotName]!!
        val size = adSizes[slotName]!![0]

        val adUnit = BannerAdUnit(configId, size.width, size.height)

        for(adSize in adSizes[slotName]!!) {
            if(adSize != size) {
                adUnit.addAdditionalSize(adSize.width, adSize.height)
            }
        }
        val array = JSONArray()
        array.put("7.2.0")
        OrtbJsonHelper.addOrUpdate(array, "app", "ext", "data", "androidYlSdkVersion")
        OrtbJsonHelper.addOrUpdate("CQZltMAQZltMAAGABCENCAFgAP_gAEPgAAYgKEBBJCpdTWFAMDJ1QJsAQYBV19gBIEQABACAAyAFAAKA4JQCwWECEAQAACACAQAAo1ABIABEEABEQEAAIAAEAABEAAQQgABIIABAAAEQQgBAAAgAAAAAEAAIAAAJMAAAkACAIQKEEEAgAIAgKgCAAIAAAACAAAMADEA4ABAAAAIAoohAgAIAEAKAAAEAAQgAAAAAAAAAAABAQAAAIAAAACAABBAyAUACQAFQAfABHAD7AIQARwBCADcwIGAHAgAICjwkCYACoAHAAPAAggBkAGgATAA_ACEgEMARIAjgBNADDgH2AfoBFACNAEiALmAXoAxQBtADcAHEASIAmkBQ4C8wGGgNXAayA2MBuYDkwHjgQTAhCBC4CaoFCAgAQAlIBNAD-gKPDAAQFHiAAICjx0CkACoAHAAQQAyADQAJgAYgA_QCGAIkATQAwwBowD7AP0AikBHQEiALEAXOAvIC9AGKANoAbgA4gCEAEXgJEATIAmkBQ4C3QF5gMNAZYA00BqoDVwHJgPHAf2BAECWgE1QKEDgBIAFwAoAB8AIiASkAmgB_QFHgMmIQCwAxAB-AIoASkAuYBigDaAJpAaqA8cB_ZAACAWIlAQAA4AEwAMUAhgCJAEcAPwAuYBigDiAIQAReAkQBeYEAQIQgTVJAAwALgKPAZYUgQgAVAA4ACAAGgATAAxAB-gEMARIA0YB-AH7AR0BIgC5gF5AMUAbQA3ABxAEXgJEATSAocBeYDDQGWANZAcmA8UB44D-wIJgQhAhyBNUoAKAAUABcAGQAUAAtgEpALEAXUBR4DJi0AMAvQChwHjgA.YAAAAAAAAAAA",
            "user", "ext","consent")
        OrtbJsonHelper.apply()

        val bannerParameters = BannerParameters()
        bannerParameters.api = listOf(
            org.prebid.mobile.Signals.Api.MRAID_1,
            org.prebid.mobile.Signals.Api.MRAID_2,
            org.prebid.mobile.Signals.Api.MRAID_3,
            org.prebid.mobile.Signals.Api.OMID_1
        )
        adUnit.bannerParameters = bannerParameters

        adUnit.pbAdSlot = dpfUnitIds[slotName]!!

        return suspendCancellableCoroutine { cont ->
            adUnit.fetchDemand(object : OnFetchDemandResult {
                override fun onComplete(bidInfo: BidInfo) {
                    Log.i("Stroeer", "Bidding result = ${bidInfo.targetingKeywords.toString()}")

                    val result = mutableMapOf<String, String>()
                    if (bidInfo.targetingKeywords != null && bidInfo.targetingKeywords!!.isNotEmpty()) {
                        result["yl_app_bidder"] = bidInfo.targetingKeywords?.get("hb_bidder") as String
                        result["yl_app_pb"] = bidInfo.targetingKeywords?.get("hb_pb") as String
                        result["yl_app_env"] = bidInfo.targetingKeywords?.get("hb_env") as String
                        result["yl_app_cache_id"] = bidInfo.targetingKeywords?.get("hb_cache_id") as String
                        result["yl_app_size"] = bidInfo.targetingKeywords?.get("hb_size") as String
                    }
                    cont.resume(result)
                }
            })
        }
    }

}