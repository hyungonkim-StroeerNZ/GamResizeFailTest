package sport1.mobile.android.app

import com.google.android.gms.ads.AdSize

object AdSizeGenerator {
    private val adSize320x50 = AdSize(320, 50)
    private val adSize320x75 = AdSize(320, 75)
    private val adSize320x100 = AdSize(320, 100)
    private val adSize320x150 = AdSize(320, 150)
    private val adSize300x250 = AdSize(300, 250)
    private val adSize300x600 = AdSize(300, 600)
    private val adSize37x31 = AdSize(37, 31)
    private val adSize37x32 = AdSize(37, 32)
    private val adSize37x33 = AdSize(37, 33)
    private val adSize37x34 = AdSize(37, 34)
    private val adSize37x35 = AdSize(37, 35)
    private val adSize37x36 = AdSize(37, 36)
    private val adSize88x99 = AdSize(88, 99)

    fun generateAdSizes(): Map<String, List<AdSize>> {
        val b1 = listOf(adSize37x31, adSize320x50, adSize320x75, adSize320x100, adSize320x150)
        val b2 = listOf(adSize37x32, adSize300x600, adSize320x50, adSize320x75, adSize320x100, adSize320x150, adSize300x250)
        val b3 = listOf(adSize37x33, adSize320x50, adSize320x75, adSize320x100, adSize320x150, adSize300x250)
        val b4 = listOf(adSize37x34, adSize320x50, adSize320x75, adSize320x100, adSize320x150, adSize300x250)
        val b5 = listOf(adSize37x35, adSize88x99, adSize320x50, adSize320x75, adSize320x100, adSize320x150, adSize300x250)
        val b6 = listOf(adSize37x36, adSize320x50, adSize320x75, adSize320x100, adSize320x150, adSize300x250)

        return mapOf("b1" to b1, "b2" to b2, "b3" to b3, "b4" to b4, "b5" to b5, "b6" to b6)
    }
}