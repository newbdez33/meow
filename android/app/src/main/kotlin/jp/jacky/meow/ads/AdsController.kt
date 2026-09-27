package jp.jacky.meow.ads

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gathers the consent Google requires, then starts the ads SDK (AdsController.swift without the
 * tracking prompt, which Android does not have). The banner renders only while [canRequestAds].
 */
class AdsController(private val consent: ConsentGateway) {
    private val _canRequestAds = MutableStateFlow(false)
    /** True once consent allows ad requests. */
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)
    /** True when regulations require an entry point for changing consent. */
    val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    private var isStarting = false
    private var isSdkStarted = false

    /** Runs once per launch from the first screen. A second call while one is running is ignored. */
    suspend fun start(activity: Activity) {
        if (isStarting) return
        isStarting = true
        try {
            try {
                consent.requestConsentInfoUpdate(activity)
                consent.loadAndShowConsentFormIfRequired(activity)
            } catch (e: ConsentException) {
                // The consent state from the previous launch stays in force; it may still allow ads.
                Log.w(TAG, "Consent update failed: ${e.message}")
            }
            _isPrivacyOptionsRequired.value = consent.isPrivacyOptionsRequired
            if (consent.canRequestAds) startSdkIfNeeded()
            _canRequestAds.value = consent.canRequestAds
        } finally {
            isStarting = false
        }
    }

    /** Shows the privacy options form so the user can change an earlier choice. */
    suspend fun presentPrivacyOptions(activity: Activity) {
        try {
            consent.showPrivacyOptionsForm(activity)
        } catch (e: ConsentException) {
            Log.w(TAG, "Privacy options form failed: ${e.message}")
        }
        if (consent.canRequestAds) startSdkIfNeeded()
        _canRequestAds.value = consent.canRequestAds
    }

    private suspend fun startSdkIfNeeded() {
        if (isSdkStarted) return
        isSdkStarted = true
        consent.initializeSdk()
    }

    private companion object {
        const val TAG = "MeowAds"
    }
}
