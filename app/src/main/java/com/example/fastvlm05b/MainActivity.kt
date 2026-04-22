package com.example.fastvlm05b

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.fastvlm05b.adapter.ChatAdapter
import com.example.fastvlm05b.databinding.ActivityMainBinding
import com.example.fastvlm05b.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.setImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupInputBar()
        observeUiState()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupInputBar() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty() || viewModel.uiState.value.selectedImageUri != null) {
                viewModel.sendMessage(text)
                binding.etMessage.text.clear()
                hideKeyboard()
            }
        }

        binding.btnAttachImage.setOnClickListener {
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnRemoveImage.setOnClickListener {
            viewModel.setImage(null)
        }

        binding.btnBackend.setOnClickListener {
            showBackendMenu(it)
        }
    }

    private fun showBackendMenu(view: View) {
        val popup = androidx.appcompat.widget.PopupMenu(this, view)
        popup.menu.add("CPU")
        popup.menu.add("GPU")
        popup.menu.add("NPU")
        popup.setOnMenuItemClickListener { item ->
            val backend = item.title.toString()
            binding.btnBackend.text = backend
            viewModel.initializeEngine(backend)
            true
        }
        popup.show()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvStatus.text = "Status: ${state.status}"
                
                chatAdapter.submitList(state.messages) {
                    if (state.messages.isNotEmpty()) {
                        binding.chatRecyclerView.scrollToPosition(state.messages.size - 1)
                    }
                }

                if (state.selectedImageUri != null) {
                    binding.imagePreviewContainer.visibility = View.VISIBLE
                    binding.ivPreview.load(state.selectedImageUri)
                } else {
                    binding.imagePreviewContainer.visibility = View.GONE
                }

                binding.btnSend.isEnabled = !state.isGenerating
                binding.etMessage.isEnabled = !state.isGenerating
                binding.btnAttachImage.isEnabled = !state.isGenerating
                
                // Opacity feedback
                binding.btnSend.alpha = if (state.isGenerating) 0.5f else 1.0f
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etMessage.windowToken, 0)
    }
}
