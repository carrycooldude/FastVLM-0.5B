package com.example.fastvlm05b.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastvlm05b.engine.InferenceEngine
import com.example.fastvlm05b.model.ChatMessage
import com.example.fastvlm05b.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val selectedImageUri: Uri? = null,
    val status: String = "Idle"
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val inferenceEngine = InferenceEngine(application)
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Auto-initialize with GPU first for stability, then user can try NPU
        initializeEngine("GPU")
    }

    fun initializeEngine(backend: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(status = "Initializing $backend...") }
            try {
                inferenceEngine.initialize(backend)
                _uiState.update { it.copy(status = "Ready ($backend)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(status = "Error: ${e.message}") }
            }
        }
    }

    fun setImage(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun sendMessage(text: String) {
        val currentImage = _uiState.value.selectedImageUri
        val userMessage = ChatMessage(text = text, role = Role.USER, imageUri = currentImage)
        
        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage,
                isGenerating = true,
                selectedImageUri = null // Clear image after sending
            )
        }

        viewModelScope.launch {
            val assistantMessageId = java.util.UUID.randomUUID().toString()
            var assistantText = ""
            
            // Add an empty assistant message to the list
            _uiState.update { 
                it.copy(messages = it.messages + ChatMessage(id = assistantMessageId, text = "", role = Role.ASSISTANT))
            }

            try {
                inferenceEngine.sendMessage(text, currentImage).collect { chunk ->
                    assistantText += chunk
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) msg.copy(text = assistantText) else msg
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                assistantText += "\n[Error: ${e.message}]"
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map { msg ->
                            if (msg.id == assistantMessageId) msg.copy(text = assistantText) else msg
                        }
                    )
                }
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        inferenceEngine.close()
    }
}
