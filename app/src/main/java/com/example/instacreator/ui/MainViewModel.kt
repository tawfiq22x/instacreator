package com.example.instacreator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.instacreator.data.database.AccountDatabase
import com.example.instacreator.data.database.AccountEntity
import com.example.instacreator.utils.CookieExtractor
import com.example.instacreator.utils.SmsRetrieverHelper
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.instagram4j.Instagram4j
import org.instagram4j.requests.payload.AccountCreateResponse

data class UIState(
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isWaitingForSms: Boolean = false,
    val statusMessage: String = "",
    val cookies: Map<String, String> = emptyMap(),
    val history: List<AccountEntity> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val accountDao = AccountDatabase.getDatabase(application).accountDao()
    private var igClient: Instagram4j? = null
    private val fixedPhone = "+251707142648"
    private val fixedDob = Triple("1", "1", "2000")

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    fun updateFullName(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value)
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun createAccount(fullName: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Creating account...")
            try {
                igClient = Instagram4j(username, password).apply {
                    setApi(org.instagram4j.Instagram4j.Api.MOBILE)
                }

                val signupResult = igClient!!.account()
                    .create(username, password, fullName, fixedPhone, fixedDob.first, fixedDob.second, fixedDob.third)
                    .get()

                when {
                    signupResult.status == "ok" -> onSuccess(signupResult)
                    signupResult.status == "fail" && signupResult.challenge != null -> onChallenge(signupResult)
                    else -> onError("Registration failed: ${signupResult.message}")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }

    private suspend fun onSuccess(result: AccountCreateResponse) {
        igClient?.login()?.get()
        val cookies = CookieExtractor.extract(igClient!!)
        saveAccount(_uiState.value.username, cookies)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            statusMessage = "✅ Account created successfully!",
            cookies = cookies
        )
    }

    private suspend fun onChallenge(result: AccountCreateResponse) {
        val challenge = result.challenge
        when {
            challenge?.stepName == "verify_phone" -> {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "📱 OTP sent. Auto-reading SMS...",
                    isWaitingForSms = true
                )
                SmsRetrieverHelper.startSmsListener(getApplication()) { otp ->
                    viewModelScope.launch {
                        verifyOtp(otp)
                    }
                }
            }
            else -> onError("Manual challenge required: ${challenge?.stepName}")
        }
    }

    private suspend fun verifyOtp(otp: String) {
        try {
            val verifyResult = igClient?.account()
                ?.verifyPhone(otp, fixedPhone)
                ?.get()
            if (verifyResult?.status == "ok") {
                onSuccess(verifyResult)
            } else {
                onError("Invalid or expired OTP")
            }
        } catch (e: Exception) {
            onError("Verification failed: ${e.message}")
        }
    }

    private fun saveAccount(username: String, cookies: Map<String, String>) {
        viewModelScope.launch {
            val entity = AccountEntity(
                username = username,
                fullName = _uiState.value.fullName,
                cookiesJson = Gson().toJson(cookies),
                timestamp = System.currentTimeMillis()
            )
            accountDao.insert(entity)
            _uiState.value = _uiState.value.copy(history = accountDao.getAll())
        }
    }

    private fun onError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            statusMessage = "❌ $message"
        )
    }
}