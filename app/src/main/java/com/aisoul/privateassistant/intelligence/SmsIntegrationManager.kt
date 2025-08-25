package com.aisoul.privateassistant.intelligence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.aisoul.privateassistant.ai.AIInferenceEngine
import com.aisoul.privateassistant.data.database.AppDatabase
import com.aisoul.privateassistant.data.entities.SmsRecord
import com.aisoul.privateassistant.data.entities.ContactInfo
import com.aisoul.privateassistant.data.entities.SmartResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * SMS Integration with Smart Response Suggestions
 * Analyzes SMS conversations and provides AI-powered response suggestions
 */
class SmsIntegrationManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: SmsIntegrationManager? = null
        
        fun getInstance(context: Context): SmsIntegrationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SmsIntegrationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val SMS_PERMISSION = Manifest.permission.READ_SMS
        private const val CONTACTS_PERMISSION = Manifest.permission.READ_CONTACTS
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val aiInferenceEngine = AIInferenceEngine.getInstance(context)
    
    // State management
    private val _integrationState = MutableStateFlow(IntegrationState.IDLE)
    val integrationState: StateFlow<IntegrationState> = _integrationState.asStateFlow()
    
    private val _smartResponses = MutableStateFlow<List<SmartResponse>>(emptyList())
    val smartResponses: StateFlow<List<SmartResponse>> = _smartResponses.asStateFlow()
    
    private val _conversationAnalysis = MutableStateFlow<ConversationAnalysis?>(null)
    val conversationAnalysis: StateFlow<ConversationAnalysis?> = _conversationAnalysis.asStateFlow()
    
    enum class IntegrationState {
        IDLE,
        PERMISSION_REQUIRED,
        ANALYZING,
        GENERATING_RESPONSES,
        COMPLETE,
        ERROR
    }
    
    enum class ResponseType {
        QUICK_REPLY,
        CONTEXTUAL,
        FOLLOW_UP,
        SCHEDULING,
        CONFIRMATION,
        POLITE_DECLINE
    }
    
    enum class ConversationTone {
        FORMAL,
        CASUAL,
        FRIENDLY,
        BUSINESS,
        FAMILY,
        URGENT
    }
    
    data class SmsMessage(
        val id: Long,
        val address: String,
        val body: String,
        val timestamp: Long,
        val type: Int, // 1 = received, 2 = sent
        val contactName: String? = null,
        val threadId: Long = 0
    )
    
    data class ConversationThread(
        val threadId: Long,
        val contactName: String?,
        val contactNumber: String,
        val messages: List<SmsMessage>,
        val lastMessageTime: Long,
        val messageCount: Int,
        val tone: ConversationTone,
        val isActive: Boolean
    )
    
    data class ConversationAnalysis(
        val totalThreads: Int,
        val activeThreads: Int,
        val avgResponseTime: Long,
        val topContacts: List<Pair<String, Int>>,
        val commonKeywords: List<String>,
        val timePatterns: Map<Int, Int>,
        val urgentMessages: Int
    )
    
    data class ResponseSuggestion(
        val text: String,
        val type: ResponseType,
        val confidence: Float,
        val context: String,
        val tone: ConversationTone
    )
    
    /**
     * Initialize SMS integration and check permissions
     */
    suspend fun initialize(): Boolean {
        if (!hasRequiredPermissions()) {
            _integrationState.value = IntegrationState.PERMISSION_REQUIRED
            return false
        }
        
        _integrationState.value = IntegrationState.ANALYZING
        
        try {
            // Analyze existing conversations
            analyzeConversations()
            
            _integrationState.value = IntegrationState.COMPLETE
            return true
        } catch (e: Exception) {
            _integrationState.value = IntegrationState.ERROR
            return false
        }
    }
    
    /**
     * Generate smart response suggestions for a conversation
     */
    suspend fun generateSmartResponses(
        threadId: Long,
        lastMessage: String,
        contactNumber: String,
        conversationHistory: List<String> = emptyList()
    ): List<ResponseSuggestion> {
        if (!hasRequiredPermissions()) {
            return emptyList()
        }
        
        _integrationState.value = IntegrationState.GENERATING_RESPONSES
        
        try {
            // Get conversation context
            val thread = getConversationThread(threadId)
            val tone = detectConversationTone(thread?.messages ?: emptyList())
            val context = analyzeMessageContext(lastMessage, conversationHistory)
            
            // Generate response suggestions
            val suggestions = mutableListOf<ResponseSuggestion>()
            
            // Quick replies
            suggestions.addAll(generateQuickReplies(lastMessage, tone))
            
            // Contextual responses
            suggestions.addAll(generateContextualResponses(lastMessage, context, tone))
            
            // Follow-up questions
            suggestions.addAll(generateFollowUpQuestions(lastMessage, tone))
            
            // Scheduling responses (if time-related)
            if (isTimeRelated(lastMessage)) {
                suggestions.addAll(generateSchedulingResponses(lastMessage, tone))
            }
            
            // Confirmation responses (if question asked)
            if (isQuestion(lastMessage)) {
                suggestions.addAll(generateConfirmationResponses(lastMessage, tone))
            }
            
            // Store suggestions
            val smartResponses = suggestions.map { suggestion ->
                SmartResponse(
                    id = 0,
                    threadId = threadId,
                    contactNumber = contactNumber,
                    responseText = suggestion.text,
                    responseType = suggestion.type.name,
                    confidence = suggestion.confidence,
                    context = suggestion.context,
                    tone = suggestion.tone.name,
                    timestamp = System.currentTimeMillis()
                )
            }
            
            database.smsDao().insertSmartResponses(smartResponses)
            _smartResponses.value = smartResponses
            
            _integrationState.value = IntegrationState.COMPLETE
            return suggestions
            
        } catch (e: Exception) {
            _integrationState.value = IntegrationState.ERROR
            return emptyList()
        }
    }
    
    /**
     * Analyze all SMS conversations for patterns and insights
     */
    private suspend fun analyzeConversations() {
        val allMessages = readAllSmsMessages()
        val threads = groupMessagesByThread(allMessages)
        
        // Store SMS records
        val smsRecords = allMessages.map { msg ->
            SmsRecord(
                id = 0,
                messageId = msg.id,
                threadId = msg.threadId,
                address = msg.address,
                body = msg.body,
                timestamp = msg.timestamp,
                type = msg.type,
                contactName = msg.contactName
            )
        }
        
        database.smsDao().insertSmsRecords(smsRecords)
        
        // Generate conversation analysis
        val analysis = generateConversationAnalysis(threads, allMessages)
        _conversationAnalysis.value = analysis
    }
    
    /**
     * Read all SMS messages from device
     */
    private fun readAllSmsMessages(): List<SmsMessage> {
        if (!hasRequiredPermissions()) return emptyList()
        
        val messages = mutableListOf<SmsMessage>()
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.THREAD_ID
        )
        
        val cursor: Cursor? = context.contentResolver.query(
            uri, projection, null, null, 
            "${Telephony.Sms.DATE} DESC LIMIT 1000"
        )
        
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms._ID))
                val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: ""
                val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: ""
                val timestamp = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                val type = it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                val threadId = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                
                val contactName = getContactName(address)
                
                messages.add(SmsMessage(id, address, body, timestamp, type, contactName, threadId))
            }
        }
        
        return messages
    }
    
    /**
     * Group messages by conversation thread
     */
    private fun groupMessagesByThread(messages: List<SmsMessage>): List<ConversationThread> {
        return messages.groupBy { it.threadId }
            .map { (threadId, threadMessages) ->
                val sortedMessages = threadMessages.sortedBy { it.timestamp }
                val lastMessage = sortedMessages.lastOrNull()
                val contactNumber = threadMessages.firstOrNull()?.address ?: ""
                val contactName = threadMessages.firstOrNull()?.contactName
                val tone = detectConversationTone(threadMessages)
                val isActive = lastMessage?.timestamp ?: 0 > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                
                ConversationThread(
                    threadId = threadId,
                    contactName = contactName,
                    contactNumber = contactNumber,
                    messages = sortedMessages,
                    lastMessageTime = lastMessage?.timestamp ?: 0,
                    messageCount = threadMessages.size,
                    tone = tone,
                    isActive = isActive
                )
            }
            .sortedByDescending { it.lastMessageTime }
    }
    
    /**
     * Get conversation thread by ID
     */
    private fun getConversationThread(threadId: Long): ConversationThread? {
        val messages = readAllSmsMessages().filter { it.threadId == threadId }
        if (messages.isEmpty()) return null
        
        return groupMessagesByThread(messages).firstOrNull { it.threadId == threadId }
    }
    
    /**
     * Detect conversation tone from message history
     */
    private fun detectConversationTone(messages: List<SmsMessage>): ConversationTone {
        val recentMessages = messages.takeLast(10)
        val combinedText = recentMessages.joinToString(" ") { it.body }.lowercase()
        
        return when {
            combinedText.contains("meeting") || combinedText.contains("schedule") || 
            combinedText.contains("appointment") || combinedText.contains("business") -> ConversationTone.BUSINESS
            
            combinedText.contains("urgent") || combinedText.contains("asap") || 
            combinedText.contains("emergency") || combinedText.contains("immediately") -> ConversationTone.URGENT
            
            combinedText.contains("love") || combinedText.contains("family") || 
            combinedText.contains("mom") || combinedText.contains("dad") -> ConversationTone.FAMILY
            
            combinedText.contains("dear") || combinedText.contains("regards") || 
            combinedText.contains("sincerely") -> ConversationTone.FORMAL
            
            combinedText.contains("hey") || combinedText.contains("awesome") || 
            combinedText.contains("cool") || combinedText.contains("thanks") -> ConversationTone.FRIENDLY
            
            else -> ConversationTone.CASUAL
        }
    }
    
    /**
     * Analyze message context for response generation
     */
    private fun analyzeMessageContext(message: String, history: List<String>): String {
        val keywords = mutableListOf<String>()
        val messageWords = message.lowercase().split(Regex("\\W+"))
        
        // Extract meaningful keywords
        keywords.addAll(messageWords.filter { it.length > 3 && !isStopWord(it) })
        
        // Add context from history
        val historyKeywords = history.flatMap { it.lowercase().split(Regex("\\W+")) }
            .filter { it.length > 3 && !isStopWord(it) }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
        
        keywords.addAll(historyKeywords)
        
        return keywords.distinct().joinToString(", ")
    }
    
    /**
     * Generate quick reply suggestions
     */
    private suspend fun generateQuickReplies(message: String, tone: ConversationTone): List<ResponseSuggestion> {
        val replies = mutableListOf<ResponseSuggestion>()
        val messageText = message.lowercase()
        
        when (tone) {
            ConversationTone.CASUAL, ConversationTone.FRIENDLY -> {
                when {
                    messageText.contains("how are you") -> {
                        replies.add(ResponseSuggestion("I'm doing great, thanks! How about you?", ResponseType.QUICK_REPLY, 0.9f, "greeting", tone))
                        replies.add(ResponseSuggestion("All good here! What's up?", ResponseType.QUICK_REPLY, 0.8f, "greeting", tone))
                    }
                    messageText.contains("thank") -> {
                        replies.add(ResponseSuggestion("You're welcome! 😊", ResponseType.QUICK_REPLY, 0.9f, "gratitude", tone))
                        replies.add(ResponseSuggestion("No problem at all!", ResponseType.QUICK_REPLY, 0.8f, "gratitude", tone))
                    }
                    messageText.contains("sorry") -> {
                        replies.add(ResponseSuggestion("No worries!", ResponseType.QUICK_REPLY, 0.9f, "apology", tone))
                        replies.add(ResponseSuggestion("It's all good!", ResponseType.QUICK_REPLY, 0.8f, "apology", tone))
                    }
                }
            }
            ConversationTone.BUSINESS, ConversationTone.FORMAL -> {
                when {
                    messageText.contains("meeting") -> {
                        replies.add(ResponseSuggestion("I'll check my calendar and get back to you.", ResponseType.QUICK_REPLY, 0.9f, "scheduling", tone))
                        replies.add(ResponseSuggestion("Thank you for the invitation. I'll confirm shortly.", ResponseType.QUICK_REPLY, 0.8f, "scheduling", tone))
                    }
                    messageText.contains("thank") -> {
                        replies.add(ResponseSuggestion("You're very welcome.", ResponseType.QUICK_REPLY, 0.9f, "gratitude", tone))
                        replies.add(ResponseSuggestion("My pleasure.", ResponseType.QUICK_REPLY, 0.8f, "gratitude", tone))
                    }
                }
            }
            ConversationTone.URGENT -> {
                replies.add(ResponseSuggestion("Got it, I'll handle this right away.", ResponseType.QUICK_REPLY, 0.9f, "urgent_response", tone))
                replies.add(ResponseSuggestion("Understood. Taking care of it now.", ResponseType.QUICK_REPLY, 0.8f, "urgent_response", tone))
            }
            else -> {
                // Generic quick replies
                if (isQuestion(message)) {
                    replies.add(ResponseSuggestion("Let me think about that and get back to you.", ResponseType.QUICK_REPLY, 0.7f, "generic_question", tone))
                }
            }
        }
        
        return replies
    }
    
    /**
     * Generate contextual responses using AI
     */
    private suspend fun generateContextualResponses(message: String, context: String, tone: ConversationTone): List<ResponseSuggestion> {
        val responses = mutableListOf<ResponseSuggestion>()
        
        try {
            // Create AI prompt for response generation
            val prompt = buildString {
                append("Generate a contextual SMS response for this message: \"$message\"\n")
                append("Conversation tone: ${tone.name.lowercase()}\n")
                append("Context keywords: $context\n")
                append("Response should be:")
                when (tone) {
                    ConversationTone.FORMAL -> append(" formal and professional")
                    ConversationTone.CASUAL -> append(" casual and relaxed")
                    ConversationTone.FRIENDLY -> append(" warm and friendly")
                    ConversationTone.BUSINESS -> append(" professional and to-the-point")
                    ConversationTone.FAMILY -> append(" warm and personal")
                    ConversationTone.URGENT -> append(" prompt and action-oriented")
                }
                append("\nKeep it under 160 characters.")
            }
            
            val aiResponse = aiInferenceEngine.generateResponse(prompt, emptyList())
            
            when (aiResponse) {
                is AIInferenceEngine.InferenceResult.Success -> {
                    responses.add(ResponseSuggestion(
                        text = aiResponse.response.take(160),
                        type = ResponseType.CONTEXTUAL,
                        confidence = aiResponse.confidence,
                        context = context,
                        tone = tone
                    ))
                }
                else -> {
                    // Fallback to template-based responses
                    responses.addAll(generateTemplateResponses(message, tone))
                }
            }
        } catch (e: Exception) {
            // Fallback to template-based responses
            responses.addAll(generateTemplateResponses(message, tone))
        }
        
        return responses
    }
    
    /**
     * Generate follow-up questions
     */
    private fun generateFollowUpQuestions(message: String, tone: ConversationTone): List<ResponseSuggestion> {
        val questions = mutableListOf<ResponseSuggestion>()
        val messageText = message.lowercase()
        
        when {
            messageText.contains("event") || messageText.contains("party") -> {
                questions.add(ResponseSuggestion("What time does it start?", ResponseType.FOLLOW_UP, 0.8f, "event_details", tone))
                questions.add(ResponseSuggestion("Should I bring anything?", ResponseType.FOLLOW_UP, 0.7f, "event_preparation", tone))
            }
            messageText.contains("plan") || messageText.contains("meet") -> {
                questions.add(ResponseSuggestion("When works best for you?", ResponseType.FOLLOW_UP, 0.8f, "scheduling", tone))
                questions.add(ResponseSuggestion("Where should we meet?", ResponseType.FOLLOW_UP, 0.7f, "location", tone))
            }
            messageText.contains("problem") || messageText.contains("issue") -> {
                questions.add(ResponseSuggestion("Do you need any help with that?", ResponseType.FOLLOW_UP, 0.8f, "assistance", tone))
                questions.add(ResponseSuggestion("How can I support you?", ResponseType.FOLLOW_UP, 0.7f, "support", tone))
            }
        }
        
        return questions
    }
    
    /**
     * Generate scheduling-related responses
     */
    private fun generateSchedulingResponses(message: String, tone: ConversationTone): List<ResponseSuggestion> {
        val responses = mutableListOf<ResponseSuggestion>()
        
        when (tone) {
            ConversationTone.BUSINESS, ConversationTone.FORMAL -> {
                responses.add(ResponseSuggestion("I'll check my availability and confirm.", ResponseType.SCHEDULING, 0.9f, "professional_scheduling", tone))
                responses.add(ResponseSuggestion("Let me review my calendar and get back to you.", ResponseType.SCHEDULING, 0.8f, "calendar_check", tone))
            }
            else -> {
                responses.add(ResponseSuggestion("Sounds good! Let me check when I'm free.", ResponseType.SCHEDULING, 0.8f, "casual_scheduling", tone))
                responses.add(ResponseSuggestion("I'll look at my schedule and let you know!", ResponseType.SCHEDULING, 0.7f, "schedule_check", tone))
            }
        }
        
        return responses
    }
    
    /**
     * Generate confirmation responses for questions
     */
    private fun generateConfirmationResponses(message: String, tone: ConversationTone): List<ResponseSuggestion> {
        val responses = mutableListOf<ResponseSuggestion>()
        
        when (tone) {
            ConversationTone.FORMAL, ConversationTone.BUSINESS -> {
                responses.add(ResponseSuggestion("Yes, that works for me.", ResponseType.CONFIRMATION, 0.8f, "formal_confirmation", tone))
                responses.add(ResponseSuggestion("I can confirm that.", ResponseType.CONFIRMATION, 0.7f, "professional_confirmation", tone))
                responses.add(ResponseSuggestion("I'll need to decline, but thank you.", ResponseType.POLITE_DECLINE, 0.6f, "polite_decline", tone))
            }
            else -> {
                responses.add(ResponseSuggestion("Yes, definitely!", ResponseType.CONFIRMATION, 0.9f, "enthusiastic_confirmation", tone))
                responses.add(ResponseSuggestion("Sure thing!", ResponseType.CONFIRMATION, 0.8f, "casual_confirmation", tone))
                responses.add(ResponseSuggestion("Sorry, can't make it this time.", ResponseType.POLITE_DECLINE, 0.7f, "casual_decline", tone))
            }
        }
        
        return responses
    }
    
    /**
     * Generate template-based responses as fallback
     */
    private fun generateTemplateResponses(message: String, tone: ConversationTone): List<ResponseSuggestion> {
        val responses = mutableListOf<ResponseSuggestion>()
        
        when (tone) {
            ConversationTone.CASUAL -> {
                responses.add(ResponseSuggestion("Got it, thanks!", ResponseType.CONTEXTUAL, 0.6f, "generic", tone))
                responses.add(ResponseSuggestion("Sounds good to me!", ResponseType.CONTEXTUAL, 0.5f, "generic", tone))
            }
            ConversationTone.FORMAL -> {
                responses.add(ResponseSuggestion("Thank you for the information.", ResponseType.CONTEXTUAL, 0.6f, "generic", tone))
                responses.add(ResponseSuggestion("I appreciate you letting me know.", ResponseType.CONTEXTUAL, 0.5f, "generic", tone))
            }
            else -> {
                responses.add(ResponseSuggestion("Thanks for letting me know!", ResponseType.CONTEXTUAL, 0.6f, "generic", tone))
            }
        }
        
        return responses
    }
    
    /**
     * Generate conversation analysis
     */
    private fun generateConversationAnalysis(threads: List<ConversationThread>, allMessages: List<SmsMessage>): ConversationAnalysis {
        val activeThreads = threads.count { it.isActive }
        val topContacts: List<Pair<String, Int>> = threads.map { thread ->
            (thread.contactName ?: thread.contactNumber) to thread.messageCount
        }.sortedByDescending { it.second }.take(5)
        
        val allText = allMessages.joinToString(" ") { it.body }
        val commonKeywords = extractCommonKeywords(allText)
        
        val timePatterns = allMessages.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }.mapValues { it.value.size }
        
        val urgentMessages = allMessages.count { msg ->
            val text = msg.body.lowercase()
            text.contains("urgent") || text.contains("asap") || text.contains("emergency")
        }
        
        // Simple response time calculation
        val responseDelays = mutableListOf<Long>()
        threads.forEach { thread ->
            val sortedMessages = thread.messages.sortedBy { it.timestamp }
            for (i in 1 until sortedMessages.size) {
                val prev = sortedMessages[i - 1]
                val curr = sortedMessages[i]
                if (prev.type != curr.type) { // Different types (received vs sent)
                    responseDelays.add(curr.timestamp - prev.timestamp)
                }
            }
        }
        
        val avgResponseTime = if (responseDelays.isNotEmpty()) {
            responseDelays.average().toLong()
        } else 0L
        
        return ConversationAnalysis(
            totalThreads = threads.size,
            activeThreads = activeThreads,
            avgResponseTime = avgResponseTime,
            topContacts = topContacts,
            commonKeywords = commonKeywords,
            timePatterns = timePatterns,
            urgentMessages = urgentMessages
        )
    }
    
    /**
     * Extract common keywords from text
     */
    private fun extractCommonKeywords(text: String): List<String> {
        return text.lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 3 && !isStopWord(it) }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { (_, count) -> count }
            .take(10)
            .map { (word, _) -> word }
    }
    
    /**
     * Get contact name from phone number
     */
    private fun getContactName(phoneNumber: String): String? {
        if (!hasContactsPermission()) return null
        
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        
        return null
    }
    
    // Utility methods
    
    private fun hasRequiredPermissions(): Boolean {
        return hasSmsPermission() && hasContactsPermission()
    }
    
    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, SMS_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, CONTACTS_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun isQuestion(message: String): Boolean {
        return message.contains("?") || 
               message.lowercase().startsWith("what") ||
               message.lowercase().startsWith("when") ||
               message.lowercase().startsWith("where") ||
               message.lowercase().startsWith("who") ||
               message.lowercase().startsWith("why") ||
               message.lowercase().startsWith("how") ||
               message.lowercase().startsWith("can") ||
               message.lowercase().startsWith("could") ||
               message.lowercase().startsWith("would") ||
               message.lowercase().startsWith("should") ||
               message.lowercase().startsWith("do you") ||
               message.lowercase().startsWith("are you")
    }
    
    private fun isTimeRelated(message: String): Boolean {
        val timeKeywords = listOf("time", "when", "schedule", "meet", "appointment", "calendar", "date", "tomorrow", "today", "tonight", "morning", "afternoon", "evening")
        return timeKeywords.any { message.lowercase().contains(it) }
    }
    
    private fun isStopWord(word: String): Boolean {
        val stopWords = setOf("the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from", "this", "that", "these", "those", "you", "your", "have", "has", "had", "will", "would", "could", "should", "can", "may", "might", "must", "shall", "i", "me", "my", "we", "us", "our", "they", "them", "their")
        return stopWords.contains(word.lowercase())
    }
}