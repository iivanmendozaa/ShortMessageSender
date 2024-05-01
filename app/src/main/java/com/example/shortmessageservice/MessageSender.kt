package com.example.shortmessageservice

import android.telephony.SmsManager

class MessageSender {

    fun sendSMS(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        println("SMS SENT")
    }
}