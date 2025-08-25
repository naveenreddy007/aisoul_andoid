package com.aisoul.privateassistant.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.aisoul.privateassistant.R
import com.aisoul.privateassistant.ui.theme.AISoulTheme
import com.aisoul.privateassistant.core.demo.DemoModeManager

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val coroutineScope = rememberCoroutineScope()
    
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(
        listOf(
            ChatMessage("1", "Hello! I'm your AI Soul assistant. All processing happens locally on your device for complete privacy.", false),
            ChatMessage("2", "Demo Mode Active - Install an AI model from the Models tab to get started!", false)
        )
    ) }
    var isGeneratingResponse by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI Soul - Demo Mode",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.local_processing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Install a real AI model from the Models tab for intelligent responses!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
        }

        // Input area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.chat_placeholder)) },
                    singleLine = false,
                    maxLines = 3,
                    enabled = !isGeneratingResponse
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank() && !isGeneratingResponse) {
                            val userMessage = ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                content = messageText,
                                isFromUser = true
                            )
                            
                            messages = messages + userMessage
                            val inputText = messageText
                            messageText = ""
                            isGeneratingResponse = true
                            
                            // Add loading message
                            val loadingMessage = ChatMessage(
                                id = (System.currentTimeMillis() + 1).toString(),
                                content = "AI is thinking...",
                                isFromUser = false,
                                isLoading = true
                            )
                            messages = messages + loadingMessage
                            
                            // Generate response
                            coroutineScope.launch {
                                try {
                                    // Simple demo response
                                    val response = "Demo Mode: I received your message '$inputText'. Install an AI model from the Models tab to get real responses!"
                                    
                                    // Remove loading message and add real response
                                    messages = messages.dropLast(1) + ChatMessage(
                                        id = (System.currentTimeMillis() + 2).toString(),
                                        content = response,
                                        isFromUser = false
                                    )
                                } catch (e: Exception) {
                                    // Handle error
                                    messages = messages.dropLast(1) + ChatMessage(
                                        id = (System.currentTimeMillis() + 2).toString(),
                                        content = "Sorry, I encountered an error. Please try again.",
                                        isFromUser = false
                                    )
                                } finally {
                                    isGeneratingResponse = false
                                }
                            }
                        }
                    },
                    enabled = messageText.isNotBlank() && !isGeneratingResponse
                ) {
                    if (isGeneratingResponse) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.isFromUser -> MaterialTheme.colorScheme.primary
                    message.isLoading -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (message.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = message.content,
                    color = if (message.isFromUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
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