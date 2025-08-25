package com.aisoul.privateassistant.data.dao

import androidx.room.*
import com.aisoul.privateassistant.data.entities.SmsRecord
import com.aisoul.privateassistant.data.entities.ContactInfo
import com.aisoul.privateassistant.data.entities.SmartResponse

/**
 * DAO for SMS integration operations
 */
@Dao
interface SmsDao {
    
    // SMS Records
    @Query("SELECT * FROM sms_records ORDER BY timestamp DESC")
    suspend fun getAllSmsRecords(): List<SmsRecord>
    
    @Query("SELECT * FROM sms_records WHERE threadId = :threadId ORDER BY timestamp ASC")
    suspend fun getSmsRecordsByThread(threadId: Long): List<SmsRecord>
    
    @Query("SELECT * FROM sms_records WHERE address = :address ORDER BY timestamp DESC")
    suspend fun getSmsRecordsByAddress(address: String): List<SmsRecord>
    
    @Query("SELECT * FROM sms_records WHERE timestamp > :afterTimestamp ORDER BY timestamp DESC")
    suspend fun getRecentSmsRecords(afterTimestamp: Long): List<SmsRecord>
    
    @Query("SELECT * FROM sms_records WHERE body LIKE :searchTerm ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchSmsRecords(searchTerm: String, limit: Int): List<SmsRecord>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsRecord(smsRecord: SmsRecord): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsRecords(smsRecords: List<SmsRecord>)
    
    @Update
    suspend fun updateSmsRecord(smsRecord: SmsRecord)
    
    @Delete
    suspend fun deleteSmsRecord(smsRecord: SmsRecord)
    
    @Query("DELETE FROM sms_records WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldSmsRecords(beforeTimestamp: Long): Int
    
    // Contact Info
    @Query("SELECT * FROM contact_info ORDER BY communicationFrequency DESC")
    suspend fun getAllContacts(): List<ContactInfo>
    
    @Query("SELECT * FROM contact_info WHERE phoneNumber = :phoneNumber")
    suspend fun getContactByPhone(phoneNumber: String): ContactInfo?
    
    @Query("SELECT * FROM contact_info WHERE displayName LIKE :searchTerm")
    suspend fun searchContacts(searchTerm: String): List<ContactInfo>
    
    @Query("SELECT * FROM contact_info ORDER BY communicationFrequency DESC LIMIT :limit")
    suspend fun getTopContacts(limit: Int): List<ContactInfo>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactInfo): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactInfo>)
    
    @Update
    suspend fun updateContact(contact: ContactInfo)
    
    @Delete
    suspend fun deleteContact(contact: ContactInfo)
    
    // Smart Responses
    @Query("SELECT * FROM smart_responses WHERE threadId = :threadId ORDER BY timestamp DESC")
    suspend fun getSmartResponsesByThread(threadId: Long): List<SmartResponse>
    
    @Query("SELECT * FROM smart_responses WHERE contactNumber = :contactNumber ORDER BY timestamp DESC")
    suspend fun getSmartResponsesByContact(contactNumber: String): List<SmartResponse>
    
    @Query("SELECT * FROM smart_responses WHERE responseType = :responseType ORDER BY confidence DESC")
    suspend fun getSmartResponsesByType(responseType: String): List<SmartResponse>
    
    @Query("SELECT * FROM smart_responses ORDER BY confidence DESC LIMIT :limit")
    suspend fun getTopSmartResponses(limit: Int): List<SmartResponse>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartResponse(smartResponse: SmartResponse): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartResponses(smartResponses: List<SmartResponse>)
    
    @Update
    suspend fun updateSmartResponse(smartResponse: SmartResponse)
    
    @Delete
    suspend fun deleteSmartResponse(smartResponse: SmartResponse)
    
    @Query("DELETE FROM smart_responses WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldSmartResponses(beforeTimestamp: Long): Int
}