package sport1.mobile.android.app

import android.content.Context
import androidx.core.content.edit

object IabConsentStore {

    // --- TCF v2 (GDPR) keys ---
    private const val K_TCF_APPLIES = "IABTCF_gdprApplies"            // 0/1 or -1 unknown
    private const val K_TCF_TCSTRING = "IABTCF_TCString"              // the TC string
    private const val K_TCF_ADDTL = "IABTCF_AddtlConsent"             // Google AC string (optional)

    // Optional but good to set if you know them (omit if you don't):
    private const val K_TCF_PURPOSE_CONSENTS = "IABTCF_PurposeConsents"                    // e.g. "110000000000000000000000"
    private const val K_TCF_PURPOSE_LI = "IABTCF_PurposeLegitimateInterests"
    private const val K_TCF_SF_OPTINS = "IABTCF_SpecialFeaturesOptIns"                     // e.g. "10" for SF1+SF2
    private const val K_TCF_CMP_ID = "IABTCF_CmpSdkID"
    private const val K_TCF_CMP_VER = "IABTCF_CmpSdkVersion"
    private const val K_TCF_CONSENT_SCREEN = "IABTCF_ConsentScreen"
    private const val K_TCF_CONSENT_LANG = "IABTCF_ConsentLanguage"
    private const val K_TCF_PUB_CC = "IABTCF_PublisherCC"

    // --- GPP (IAB Global Privacy Platform) optional keys ---
    private const val K_GPP_STRING = "IABGPP_HDR_GppString"           // full GPP string (if you use GPP)
    private const val K_GPP_SID = "IABGPP_GppSID"                     // comma-separated section IDs, e.g. "2" for TCF v2

    // --- CCPA/US Privacy (optional) ---
    private const val K_USP = "IABUSPrivacy_String"                   // e.g. "1YNN" / "1---"

    fun updateTcString(
        context: Context,
        tcString: String,
        gdprApplies: Int = 1,             // 1=yes, 0=no, -1=unknown
        addtlConsent: String? = null,     // e.g. "1~7.12.35.66"
        gppString: String? = null,        // if you also maintain GPP
        gppSid: String? = null,           // e.g. "2" when TCFv2 present in GPP
        usPrivacy: String? = null         // if you also maintain CCPA
    ) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit(commit = false) {
            putInt(K_TCF_APPLIES, gdprApplies)
            putString(K_TCF_TCSTRING, tcString)

            if (addtlConsent != null) putString(K_TCF_ADDTL, addtlConsent) else remove(K_TCF_ADDTL)
            if (gppString != null) putString(K_GPP_STRING, gppString) else remove(K_GPP_STRING)
            if (gppSid != null) putString(K_GPP_SID, gppSid) else remove(K_GPP_SID)
            if (usPrivacy != null) putString(K_USP, usPrivacy) else remove(K_USP)

            // If you have these values, set them; otherwise omit (don’t write junk defaults)
            // putString(K_TCF_PURPOSE_CONSENTS, "110000000000000000000000")
            // putString(K_TCF_PURPOSE_LI, "000000000000000000000000")
            // putString(K_TCF_SF_OPTINS, "10")
            // putInt(K_TCF_CMP_ID, 0)
            // putInt(K_TCF_CMP_VER, 0)
            // putInt(K_TCF_CONSENT_SCREEN, 0)
            // putString(K_TCF_CONSENT_LANG, "EN")
            // putString(K_TCF_PUB_CC, "DE")
        }
        // Many SDKs hot-read these keys; if yours caches, re-init/refresh adapters after this call.
    }
}