package com.example.appsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            // Importante: El número puede llegar nulo en el primer "evento" del sistema
            if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
                val prefs = context.getSharedPreferences("AppSMSPrefs", Context.MODE_PRIVATE)
                val targetNumber = prefs.getString("targetNumber", "") ?: ""
                val message = prefs.getString("autoMessage", "") ?: ""

                // Limpiamos ambos números para que la comparación sea exitosa
                val cleanIncoming = incomingNumber.replace(Regex("[^0-9]"), "")
                val cleanTarget = targetNumber.replace(Regex("[^0-9]"), "")

                if (cleanTarget.isNotEmpty() && cleanIncoming.contains(cleanTarget)) {
                    try {
                        val smsManager = context.getSystemService(SmsManager::class.java)
                        smsManager.sendTextMessage(incomingNumber, null, message, null, null)
                        Toast.makeText(context, "Enviando respuesta a $incomingNumber...", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e("AppSMS", "Error: ${e.message}")
                    }
                }
            }
        }
    }
}