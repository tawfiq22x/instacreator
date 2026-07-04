package com.example.instacreator.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsMessage
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

object SmsRetrieverHelper {
    private var smsBroadcastReceiver: SmsBroadcastReceiver? = null

    fun startSmsListener(context: Context, onOtpReceived: (String) -> Unit) {
        val client = SmsRetriever.getClient(context)
        val task = client.startSmsRetriever()

        task.addOnSuccessListener {
            val receiver = SmsBroadcastReceiver(onOtpReceived)
            smsBroadcastReceiver = receiver
            context.registerReceiver(receiver, IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION))
        }
        task.addOnFailureListener {
            // fallback
        }
    }

    private class SmsBroadcastReceiver(private val onOtpReceived: (String) -> Unit) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
                when (status?.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val message = extras?.getParcelable(SmsRetriever.EXTRA_SMS_MESSAGE) as? SmsMessage
                        message?.messageBody?.let { sms ->
                            val otp = Regex("\\b\\d{6}\\b").find(sms)?.value
                            otp?.let { onOtpReceived(it) }
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        // handle timeout
                    }
                }
            }
        }
    }
}
