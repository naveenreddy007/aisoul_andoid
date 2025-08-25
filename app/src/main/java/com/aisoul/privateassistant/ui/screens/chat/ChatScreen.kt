package com.aisoul.privateassistant.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.util.Log
import com.aisoul.privateassistant.R
import com.aisoul.privateassistant.ai.AIInferenceEngine
import com.aisoul.privateassistant.ai.AIModelManager
import com.aisoul.privateassistant.core.demo.DemoModeManager
import com.aisoul.privateassistant.voice.VoiceInterfaceManager
import com.aisoul.privateassistant.ui.theme.AISoulTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat message data class
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isProcessing: Boolean = false,
    val processingTimeMs: Long? = null,
    val confidence: Float? = null,
    val modelUsed: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // AI components
    val aiInferenceEngine = remember { AIInferenceEngine.getInstance(context) }
    val aiModelManager = remember { AIModelManager.getInstance(context) }
    val demoModeManager = remember { DemoModeManager.getInstance(context) }
    val voiceInterface = remember { VoiceInterfaceManager.getInstance(context) }
    
    // Voice states
    val speechRecognitionState by voiceInterface.speechRecognitionState.collectAsState()
    val textToSpeechState by voiceInterface.textToSpeechState.collectAsState()
    var isVoiceEnabled by remember { mutableStateOf(true) }
    
    // Chat state
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    
    // Get available models
    var hasLoadedModels by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            val availableModels = aiModelManager.getAvailableModels()
            hasLoadedModels = availableModels.any { it.isLoaded }
            
            // Initialize voice interface
            voiceInterface.initialize()
        }
    }
    
    // Send message function
    fun sendMessage() {
        if (messageText.trim().isEmpty() || isProcessing) return
        
        val userMessage = ChatMessage(
            content = messageText.trim(),
            isFromUser = true
        )
        
        messages = messages + userMessage
        val currentInput = messageText.trim()
        messageText = ""
        isProcessing = true
        
        // Scroll to bottom
        scope.launch {
            listState.animateScrollToItem(messages.size)
        }
        
        // Generate AI response
        scope.launch {
            try {
                val response = if (hasLoadedModels && !demoModeManager.isDemoModeEnabled) {
                    // Use real AI inference
                    val result = aiInferenceEngine.generateResponse(
                        input = currentInput,
                        conversationHistory = messages.takeLast(6).map { it.content }
                    )
                    
                    when (result) {
                        is AIInferenceEngine.InferenceResult.Success -> {
                            ChatMessage(
                                content = result.response,
                                isFromUser = false,
                                processingTimeMs = result.processingTime,
                                confidence = result.confidence,
                                modelUsed = "TensorFlow Lite"
                            )
                        }
                        else -> {
                            // Fallback to demo mode
                            val demoResponse = demoModeManager.generateDemoResponse(currentInput)
                            ChatMessage(
                                content = demoResponse,
                                isFromUser = false,
                                modelUsed = "Demo Mode"
                            )
                        }
                    }
                } else {
                    // Use demo mode
                    val demoResponse = demoModeManager.generateDemoResponse(currentInput)
                    ChatMessage(
                        content = demoResponse,
                        isFromUser = false,
                        modelUsed = "Demo Mode"
                    )
                }
                
                messages = messages + response
                
                // Auto-speak response if voice output is enabled
                if (isVoiceEnabled && voiceInterface.isVoiceOutputEnabled) {
                    voiceInterface.speak(response.content)
                }
                
                // Scroll to bottom
                scope.launch {
                    listState.animateScrollToItem(messages.size)
                }
                
            } catch (e: Exception) {
                // Error handling
                val errorMessage = ChatMessage(
                    content = "Sorry, I encountered an error processing your message. Please try again.",
                    isFromUser = false,
                    modelUsed = "Error Handler"
                )
                messages = messages + errorMessage
            } finally {
                isProcessing = false
            }
        }
    }
    
    // Voice input function
    fun startVoiceInput() {
        if (!isVoiceEnabled || !voiceInterface.isVoiceInputEnabled) return
        
        scope.launch {
            when (val result = voiceInterface.startVoiceRecognition()) {
                is VoiceInterfaceManager.VoiceRecognitionResult.Success -> {
                    messageText = result.text
                    // Auto-send if confidence is high enough
                    if (result.confidence > 0.7f) {
                        sendMessage()
                    }
                }
                is VoiceInterfaceManager.VoiceRecognitionResult.Error -> {
                    Log.e("ChatScreen", "Voice recognition error: ${result.message}")
                }
                VoiceInterfaceManager.VoiceRecognitionResult.Cancelled -> {
                    Log.d("ChatScreen", "Voice recognition cancelled")
                }
            }
        }
    }
    // Main Container - Column that fills the entire screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Card
        ChatHeaderCard(
            hasLoadedModels = hasLoadedModels,
            isDemoMode = demoModeManager.isDemoModeEnabled,
            messageCount = messages.size
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Message List - LazyColumn that takes remaining space
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
            
            // Processing indicator
            if (isProcessing) {
                item {
                    ProcessingIndicator()
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Input Bar Card
        ChatInputCard(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendMessage = { sendMessage() },
            onVoiceInput = { startVoiceInput() },
            isProcessing = isProcessing,
            isVoiceEnabled = isVoiceEnabled,
            speechRecognitionState = speechRecognitionState,
            onToggleVoice = { isVoiceEnabled = !isVoiceEnabled }
        )
    }
}

@Composable
fun ChatHeaderCard(
    hasLoadedModels: Boolean,
    isDemoMode: Boolean,
    messageCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title
            Text(
                text = when {
                    hasLoadedModels && !isDemoMode -> "AI Soul - Local Processing"
                    else -> "AI Soul - Demo Mode"
                },
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Description - Local processing
            Text(
                text = stringResource(R.string.local_processing),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Status description
            Text(
                text = when {
                    hasLoadedModels && !isDemoMode -> "AI model active - Real intelligence processing"
                    else -> "Install a real AI model from the Models tab for intelligent responses!"
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Message count
            if (messageCount > 0) {
                Text(
                    text = "Messages: $messageCount",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            Icon(
                Icons.Filled.SmartToy,
                contentDescription = "AI",
                modifier = Modifier
                    .padding(end = 8.dp, top = 4.dp)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(
                    start = if (message.isFromUser) 48.dp else 0.dp,
                    end = if (!message.isFromUser) 48.dp else 0.dp
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isFromUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = timeFormatter.format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isFromUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                    
                    // Model info and processing time
                    if (!message.isFromUser && message.modelUsed != null) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = message.modelUsed,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            
                            if (message.processingTimeMs != null) {
                                Text(
                                    text = "${message.processingTimeMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (message.isFromUser) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "User",
                modifier = Modifier
                    .padding(start = 8.dp, top = 4.dp)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProcessingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            Icons.Filled.SmartToy,
            contentDescription = "AI",
            modifier = Modifier
                .padding(end = 8.dp, top = 4.dp)
                .size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI is thinking...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChatInputCard(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onVoiceInput: () -> Unit,
    isProcessing: Boolean,
    isVoiceEnabled: Boolean,
    speechRecognitionState: VoiceInterfaceManager.SpeechRecognitionState,
    onToggleVoice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Voice controls row
            if (isVoiceEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (speechRecognitionState) {
                            VoiceInterfaceManager.SpeechRecognitionState.LISTENING -> "🎤 Listening..."
                            VoiceInterfaceManager.SpeechRecognitionState.PROCESSING -> "⚡ Processing..."
                            VoiceInterfaceManager.SpeechRecognitionState.ERROR -> "❌ Voice Error"
                            else -> "🎤 Voice Ready"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (speechRecognitionState) {
                            VoiceInterfaceManager.SpeechRecognitionState.LISTENING -> Color.Green
                            VoiceInterfaceManager.SpeechRecognitionState.PROCESSING -> MaterialTheme.colorScheme.primary
                            VoiceInterfaceManager.SpeechRecognitionState.ERROR -> Color.Red
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    IconButton(
                        onClick = onToggleVoice,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (isVoiceEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                            contentDescription = "Toggle Voice",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                HorizontalDivider(thickness = 1.dp)
            }
            
            // Input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice input button
                if (isVoiceEnabled) {
                    IconButton(
                        onClick = onVoiceInput,
                        enabled = !isProcessing && speechRecognitionState == VoiceInterfaceManager.SpeechRecognitionState.IDLE
                    ) {
                        Icon(
                            if (speechRecognitionState == VoiceInterfaceManager.SpeechRecognitionState.LISTENING) {
                                Icons.Filled.MicOff
                            } else {
                                Icons.Filled.Mic
                            },
                            contentDescription = "Voice Input",
                            tint = if (speechRecognitionState == VoiceInterfaceManager.SpeechRecognitionState.LISTENING) {
                                Color.Red
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                // Text input field
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            if (isVoiceEnabled) "Type or speak your message..." 
                            else stringResource(R.string.chat_placeholder)
                        ) 
                    },
                    enabled = !isProcessing,
                    maxLines = 4
                )
                
                // Spacer
                Spacer(modifier = Modifier.width(8.dp))
                
                // Send button
                IconButton(
                    onClick = onSendMessage,
                    enabled = !isProcessing && messageText.trim().isNotEmpty()
                ) {
                    Icon(
                        Icons.Filled.Send, 
                        contentDescription = "Send",
                        tint = if (!isProcessing && messageText.trim().isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    AISoulTheme {
        ChatScreen()
    }
}