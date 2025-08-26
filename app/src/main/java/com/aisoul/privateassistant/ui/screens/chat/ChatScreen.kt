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
import com.aisoul.privateassistant.ai.ModelInfo

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

    val voiceInterface = remember { VoiceInterfaceManager.getInstance(context) }
    
    // Voice states
    val speechRecognitionState by voiceInterface.speechRecognitionState.collectAsState()
    val textToSpeechState by voiceInterface.textToSpeechState.collectAsState()
    var isVoiceEnabled by remember { mutableStateOf(true) }
    
    // Chat state
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    
    // Get available models and check for loaded models
    var hasLoadedModels by remember { mutableStateOf(false) }
    var loadedModelName by remember { mutableStateOf("") }
    var isLoadingModels by remember { mutableStateOf(false) }
    
    // Check for loaded models periodically
    LaunchedEffect(Unit) {
        while (true) {
            scope.launch {
                val availableModels = aiModelManager.getAvailableModels()
                val loadedModel = availableModels.find { it.isLoaded }
                hasLoadedModels = loadedModel != null
                loadedModelName = loadedModel?.type?.displayName ?: ""
                
                Log.d("ChatScreen", "Model check: hasLoadedModels=$hasLoadedModels, modelName=$loadedModelName")
                Log.d("ChatScreen", "Available models: ${availableModels.map { "${it.type.displayName}(loaded=${it.isLoaded})" }}")
            }
            kotlinx.coroutines.delay(2000) // Check every 2 seconds
        }
    }
    
    // Auto-load models on startup
    LaunchedEffect(Unit) {
        scope.launch {
            if (!hasLoadedModels && !isLoadingModels) {
                isLoadingModels = true
                try {
                    // Try to load available models
                    val modelTypes = listOf(AIModelManager.ModelType.GEMMA_2B, AIModelManager.ModelType.GEMMA_7B)
                    for (modelType in modelTypes) {
                        val result = aiModelManager.loadModel(modelType)
                        if (result is AIModelManager.ModelLoadResult.Success) {
                            Log.d("ChatScreen", "Successfully loaded ${modelType.displayName}")
                            break
                        } else {
                            Log.w("ChatScreen", "Failed to load ${modelType.displayName}: $result")
                        }
                    }
                } finally {
                    isLoadingModels = false
                }
            }
        }
    }
    
    // Initialize voice interface
    LaunchedEffect(Unit) {
        scope.launch {
            voiceInterface.initialize()
        }
    }
    
    // Send message function - only works with real models
    fun sendMessage() {
        if (messageText.trim().isEmpty() || isProcessing || !hasLoadedModels) return
        
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
        
        // Generate AI response - only real AI inference
        scope.launch {
            try {
                val response = when (val result = aiInferenceEngine.generateResponse(
                    input = currentInput,
                    conversationHistory = messages.takeLast(6).map { it.content }
                )) {
                    is AIInferenceEngine.InferenceResult.Success -> {
                        ChatMessage(
                            content = result.response,
                            isFromUser = false,
                            processingTimeMs = result.processingTime,
                            confidence = result.confidence,
                            modelUsed = result.modelUsed
                        )
                    }
                    is AIInferenceEngine.InferenceResult.Error -> {
                        ChatMessage(
                            content = "AI Error: ${result.message}. Please check that you have a valid MediaPipe model file (.bin) downloaded and try again.",
                            isFromUser = false,
                            modelUsed = "Error Handler"
                        )
                    }
                    is AIInferenceEngine.InferenceResult.NoModelAvailable -> {
                        ChatMessage(
                            content = "No AI models available. Please visit the Models tab to download a MediaPipe-compatible model (.bin file) to enable AI conversations.",
                            isFromUser = false,
                            modelUsed = "Model Required"
                        )
                    }
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
                    content = "Sorry, I encountered an error processing your message: ${e.message}. Please ensure you have a valid MediaPipe model downloaded.",
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
        if (!isVoiceEnabled || !voiceInterface.isVoiceInputEnabled || !hasLoadedModels) return
        
        scope.launch {
            when (val result = voiceInterface.startVoiceRecognition()) {
                is VoiceInterfaceManager.VoiceRecognitionResult.Success -> {
                    messageText = result.text
                    // Auto-send if confidence is high enough AND we have loaded models
                    if (result.confidence > 0.7f && hasLoadedModels) {
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
                loadedModelName = loadedModelName,
                messageCount = messages.size,
                isLoadingModels = isLoadingModels,
                availableModels = aiModelManager.getAvailableModels()
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
        
        // Input Bar Card - disabled when no models loaded
        ChatInputCard(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendMessage = { sendMessage() },
            onVoiceInput = { startVoiceInput() },
            isProcessing = isProcessing,
            isVoiceEnabled = isVoiceEnabled,
            speechRecognitionState = speechRecognitionState,
            onToggleVoice = { isVoiceEnabled = !isVoiceEnabled },
            isModelAvailable = hasLoadedModels // Disable input when no models
        )
    }
}

@Composable
fun ChatHeaderCard(
    hasLoadedModels: Boolean,
    loadedModelName: String,
    messageCount: Int,
    isLoadingModels: Boolean,
    availableModels: List<ModelInfo> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasLoadedModels) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Dynamic title based on status
            val statusText = when {
                isLoadingModels -> "AI Soul - Loading Models..."
                hasLoadedModels -> "AI Soul - ${loadedModelName}"
                else -> "AI Soul - Model Required"
            }
            
            val descriptionText = when {
                isLoadingModels -> "Initializing AI models, please wait..."
                hasLoadedModels -> "Advanced AI processing active with ${loadedModelName}"
                else -> "No AI models loaded - Please download a MediaPipe model (.bin) in the Models tab"
            }
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasLoadedModels) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Text(
                text = descriptionText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasLoadedModels) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                },
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Additional guidance for non-loaded models
            if (!hasLoadedModels && !isLoadingModels) {
                Text(
                    text = "⚠️ AI chat is disabled until you download a MediaPipe model. Visit the Models tab to download a Gemma or Phi model.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Message count
            if (messageCount > 0) {
                Text(
                    text = "Messages: $messageCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasLoadedModels) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
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
    onToggleVoice: () -> Unit,
    isModelAvailable: Boolean = true // New parameter to disable input when no models
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
                            else -> if (isModelAvailable) "🎤 Voice Ready" else "🔇 Voice Disabled (No Model)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (speechRecognitionState) {
                            VoiceInterfaceManager.SpeechRecognitionState.LISTENING -> Color.Green
                            VoiceInterfaceManager.SpeechRecognitionState.PROCESSING -> MaterialTheme.colorScheme.primary
                            VoiceInterfaceManager.SpeechRecognitionState.ERROR -> Color.Red
                            else -> if (isModelAvailable) MaterialTheme.colorScheme.onSurfaceVariant else Color.Red
                        }
                    )
                    
                    IconButton(
                        onClick = onToggleVoice,
                        modifier = Modifier.size(32.dp),
                        enabled = isModelAvailable // Disable voice toggle when no models
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
                // Voice input button - disabled when no models
                if (isVoiceEnabled) {
                    IconButton(
                        onClick = onVoiceInput,
                        enabled = !isProcessing && speechRecognitionState == VoiceInterfaceManager.SpeechRecognitionState.IDLE && isModelAvailable
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
                            } else if (isModelAvailable) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                // Text input field - disabled when no models
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            if (isVoiceEnabled) {
                                if (isModelAvailable) "Type or speak your message..." else "⚠️ Download a model to enable chat"
                            } else {
                                if (isModelAvailable) stringResource(R.string.chat_placeholder) else "⚠️ Download a model to enable chat"
                            }
                        ) 
                    },
                    enabled = !isProcessing && isModelAvailable, // Disable when no models
                    maxLines = 4
                )
                
                // Spacer
                Spacer(modifier = Modifier.width(8.dp))
                
                // Send button - disabled when no models
                IconButton(
                    onClick = onSendMessage,
                    enabled = !isProcessing && messageText.trim().isNotEmpty() && isModelAvailable
                ) {
                    Icon(
                        Icons.Filled.Send, 
                        contentDescription = "Send",
                        tint = if (!isProcessing && messageText.trim().isNotEmpty() && isModelAvailable) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
                }
            }
            
            // Warning when no models are available
            if (!isModelAvailable) {
                Text(
                    text = "⚠️ AI chat disabled - Please download a MediaPipe model (.bin) in the Models tab",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                )
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