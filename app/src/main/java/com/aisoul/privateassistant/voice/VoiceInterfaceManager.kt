package com.aisoul.privateassistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Voice Interface Manager
 * Handles speech-to-text and text-to-speech functionality
 */
class VoiceInterfaceManager private constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "VoiceInterfaceManager"
        
        @Volatile
        private var INSTANCE: VoiceInterfaceManager? = null
        
        fun getInstance(context: Context): VoiceInterfaceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoiceInterfaceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private val voiceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Voice recognition state
    private val _speechRecognitionState = MutableStateFlow(SpeechRecognitionState.IDLE)
    val speechRecognitionState: StateFlow<SpeechRecognitionState> = _speechRecognitionState.asStateFlow()
    
    // Text-to-speech state
    private val _textToSpeechState = MutableStateFlow(TextToSpeechState.IDLE)
    val textToSpeechState: StateFlow<TextToSpeechState> = _textToSpeechState.asStateFlow()
    
    // Voice settings
    var isVoiceInputEnabled = true
    var isVoiceOutputEnabled = true
    var speechLanguage = Locale.getDefault()
    var speechRate = 1.0f
    var speechPitch = 1.0f
    
    /**
     * Speech recognition states
     */
    enum class SpeechRecognitionState {
        IDLE,
        LISTENING,
        PROCESSING,
        ERROR
    }
    
    /**
     * Text-to-speech states
     */
    enum class TextToSpeechState {
        IDLE,
        INITIALIZING,
        SPEAKING,
        ERROR
    }
    
    /**
     * Voice recognition result
     */
    sealed class VoiceRecognitionResult {
        data class Success(val text: String, val confidence: Float) : VoiceRecognitionResult()
        data class Error(val message: String, val code: Int) : VoiceRecognitionResult()
        object Cancelled : VoiceRecognitionResult()
    }
    
    /**
     * Text-to-speech result
     */
    sealed class TTSResult {
        object Success : TTSResult()
        data class Error(val message: String) : TTSResult()
        object NotInitialized : TTSResult()
    }
    
    /**
     * Initialize voice interface components
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.Main) {
        try {
            Log.d(TAG, "Initializing voice interface...")
            
            // Initialize speech recognizer
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                Log.d(TAG, "Speech recognizer initialized")
            } else {
                Log.w(TAG, "Speech recognition not available on this device")
            }
            
            // Initialize text-to-speech
            initializeTTS()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize voice interface", e)
            false
        }
    }
    
    /**
     * Initialize Text-to-Speech
     */
    private fun initializeTTS() {
        _textToSpeechState.value = TextToSpeechState.INITIALIZING
        
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(speechLanguage)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Language not supported: ${speechLanguage.displayName}")
                        tts.setLanguage(Locale.ENGLISH)
                    }
                    
                    tts.setSpeechRate(speechRate)
                    tts.setPitch(speechPitch)
                    
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _textToSpeechState.value = TextToSpeechState.SPEAKING
                        }
                        
                        override fun onDone(utteranceId: String?) {
                            _textToSpeechState.value = TextToSpeechState.IDLE
                        }
                        
                        override fun onError(utteranceId: String?) {
                            _textToSpeechState.value = TextToSpeechState.ERROR
                            Log.e(TAG, "TTS error for utterance: $utteranceId")
                        }
                    })
                    
                    _textToSpeechState.value = TextToSpeechState.IDLE
                    Log.d(TAG, "Text-to-speech initialized successfully")
                }
            } else {
                _textToSpeechState.value = TextToSpeechState.ERROR
                Log.e(TAG, "Text-to-speech initialization failed")
            }
        }
    }
    
    /**
     * Start voice recognition
     */
    suspend fun startVoiceRecognition(): VoiceRecognitionResult = withContext(Dispatchers.Main) {
        if (!isVoiceInputEnabled) {
            return@withContext VoiceRecognitionResult.Error("Voice input is disabled", -1)
        }
        
        val recognizer = speechRecognizer ?: return@withContext VoiceRecognitionResult.Error(
            "Speech recognizer not available", -1
        )
        
        _speechRecognitionState.value = SpeechRecognitionState.LISTENING
        
        val result = CompletableDeferred<VoiceRecognitionResult>()
        
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
            }
            
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech")
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Voice level changed
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }
            
            override fun onEndOfSpeech() {
                Log.d(TAG, "End of speech")
                _speechRecognitionState.value = SpeechRecognitionState.PROCESSING
            }
            
            override fun onError(error: Int) {
                val errorMessage = getErrorString(error)
                Log.e(TAG, "Speech recognition error: $errorMessage")
                _speechRecognitionState.value = SpeechRecognitionState.ERROR
                result.complete(VoiceRecognitionResult.Error(errorMessage, error))
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    val bestMatch = matches[0]
                    val confidence = confidences?.get(0) ?: 0.5f
                    
                    Log.d(TAG, "Speech recognition result: $bestMatch (confidence: $confidence)")
                    _speechRecognitionState.value = SpeechRecognitionState.IDLE
                    result.complete(VoiceRecognitionResult.Success(bestMatch, confidence))
                } else {
                    _speechRecognitionState.value = SpeechRecognitionState.ERROR
                    result.complete(VoiceRecognitionResult.Error("No speech detected", -1))
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                // Partial results received
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Recognition event
            }
        }
        
        recognizer.setRecognitionListener(recognitionListener)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLanguage.toString())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            _speechRecognitionState.value = SpeechRecognitionState.ERROR
            result.complete(VoiceRecognitionResult.Error("Failed to start recognition: ${e.message}", -1))
        }
        
        result.await()
    }
    
    /**
     * Stop voice recognition
     */
    fun stopVoiceRecognition() {
        speechRecognizer?.stopListening()
        _speechRecognitionState.value = SpeechRecognitionState.IDLE
    }
    
    /**
     * Speak text using text-to-speech
     */
    suspend fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH): TTSResult = withContext(Dispatchers.Main) {
        if (!isVoiceOutputEnabled) {
            return@withContext TTSResult.Error("Voice output is disabled")
        }
        
        val tts = textToSpeech ?: return@withContext TTSResult.NotInitialized
        
        try {
            val utteranceId = "aisoul_${System.currentTimeMillis()}"
            val result = tts.speak(text, queueMode, null, utteranceId)
            
            if (result == TextToSpeech.SUCCESS) {
                Log.d(TAG, "Started speaking: ${text.take(50)}...")
                TTSResult.Success
            } else {
                Log.e(TAG, "Failed to start TTS")
                TTSResult.Error("Failed to start text-to-speech")
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS error", e)
            TTSResult.Error("TTS error: ${e.message}")
        }
    }
    
    /**
     * Stop text-to-speech
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        _textToSpeechState.value = TextToSpeechState.IDLE
    }
    
    /**
     * Get error string from speech recognition error code
     */
    private fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech input"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        
        voiceScope.cancel()
        
        Log.d(TAG, "Voice interface cleaned up")
    }
}