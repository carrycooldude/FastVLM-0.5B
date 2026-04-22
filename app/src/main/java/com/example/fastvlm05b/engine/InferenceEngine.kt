package com.example.fastvlm05b.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.Contents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * InferenceEngine for FastVLM 0.5B using LiteRT-LM API.
 * Optimized for S25 Ultra (Snapdragon 8 Elite) NPU acceleration.
 */
class InferenceEngine(private val context: Context) {

    init {
        try {
            // Loading these explicitly helps resolve symbols for the dispatch library
            System.loadLibrary("LiteRt")
            System.loadLibrary("LiteRtDispatch_Qualcomm")
            Log.i(TAG, "Native libraries loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Could not load native libraries explicitly: ${e.message}")
        }
    }

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var currentBackend: String = "NPU"

    companion object {
        private const val TAG = "InferenceEngine"
    }

    /**
     * Initializes the engine.
     * Supports JIT and AOT workflows based on the model provided.
     * 
     * @param backend "NPU", "GPU", or "CPU"
     * @param modelPath Optional override for model path (e.g. for AOT feature delivery)
     */
    suspend fun initialize(backend: String = "NPU", modelPathOverride: String? = null) {
        withContext(Dispatchers.IO) {
            close()
            currentBackend = backend
            
            val modelPath = modelPathOverride ?: ModelDownloader.getModelPath(context, backend)
            val nativeLibDir = context.applicationInfo.nativeLibraryDir
            
            Log.i(TAG, "Initializing LiteRT-LM engine on $backend")
            Log.d(TAG, "Model path: $modelPath")
            Log.d(TAG, "Native library dir: $nativeLibDir")

            // Set log severity
            Engine.setNativeMinLogSeverity(LogSeverity.ERROR)

            // Configure backends
            val backendEnum = when (backend) {
                "NPU" -> Backend.NPU(nativeLibraryDir = nativeLibDir)
                "GPU" -> Backend.GPU()
                else -> Backend.CPU()
            }

            // For FastVLM, vision tasks are heavy, use NPU if available
            val visionBackendEnum = if (backend == "NPU") {
                Backend.NPU(nativeLibraryDir = nativeLibDir)
            } else {
                Backend.GPU() // Fallback to GPU for vision
            }

            val config = EngineConfig(
                modelPath = modelPath,
                backend = backendEnum,
                visionBackend = visionBackendEnum,
            )

            try {
                engine = Engine(config)
                engine!!.initialize()
                
                // Configure generation parameters for better quality
                val conversationConfig = ConversationConfig(
                    systemInstruction = Contents.of("You are a helpful, precise vision-language assistant. " +
                            "Describe images accurately and answer questions based strictly on the provided visual information."),
                    samplerConfig = SamplerConfig(
                        temperature = 0.4, // Double
                        topK = 40,
                        topP = 0.95 // Double
                    )
                )
                
                conversation = engine!!.createConversation(conversationConfig)
                Log.i(TAG, "Engine initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Engine initialization failed", e)
                throw e
            }
        }
    }

    /**
     * Sends a multimodal message (Image + Text).
     */
    fun sendMessage(text: String, imageUri: Uri? = null): Flow<String> = flow {
        val conv = conversation ?: throw IllegalStateException("Engine not initialized")
        
        val message = if (imageUri != null) {
            val imagePath = copyImageToTemp(imageUri)
            Message.of(
                Content.ImageFile(imagePath),
                Content.Text(text)
            )
        } else {
            Message.of(text)
        }

        conv.sendMessageAsync(message).collect { responseMessage ->
            val textChunk = responseMessage.contents.contents
                .filterIsInstance<Content.Text>()
                .joinToString("") { it.text }
            emit(textChunk)
        }
    }.flowOn(Dispatchers.IO)

    private fun copyImageToTemp(uri: Uri): String {
        val tempFile = File(context.cacheDir, "input_image_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Could not open image URI")
        return tempFile.absolutePath
    }

    fun isInitialized(): Boolean = engine != null

    fun close() {
        conversation?.close()
        engine?.close()
        conversation = null
        engine = null
    }
}
