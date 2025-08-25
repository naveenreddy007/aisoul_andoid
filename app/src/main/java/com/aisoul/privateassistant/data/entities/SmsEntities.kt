package com.aisoul.privateassistant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SMS record entity for SMS analysis
 */
@Entity(tableName = "sms_records")
data class SmsRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val type: Int, // 1 = received, 2 = sent
    val contactName: String?
)

/**
 * Contact information entity
 */
@Entity(tableName = "contact_info")
data class ContactInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val displayName: String,
    val communicationFrequency: Int,
    val lastContact: Long,
    val relationship: String?
)

/**
 * Smart response entity for SMS suggestions
 */
@Entity(tableName = "smart_responses")
data class SmartResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long,
    val contactNumber: String,
    val responseText: String,
    val responseType: String,
    val confidence: Float,
    val context: String,
    val tone: String,
    val timestamp: Long
)