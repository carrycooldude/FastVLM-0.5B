package com.example.fastvlm05b.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.fastvlm05b.databinding.ItemMessageAssistantBinding
import com.example.fastvlm05b.databinding.ItemMessageUserBinding
import com.example.fastvlm05b.model.ChatMessage
import com.example.fastvlm05b.model.Role
import io.noties.markwon.Markwon
import java.util.Date

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffCallback()) {
    
    private var markwon: Markwon? = null

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ASSISTANT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).role) {
            Role.USER -> VIEW_TYPE_USER
            else -> VIEW_TYPE_ASSISTANT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> UserViewHolder(ItemMessageUserBinding.inflate(inflater, parent, false))
            else -> AssistantViewHolder(ItemMessageAssistantBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is AssistantViewHolder -> holder.bind(message)
        }
    }

    class UserViewHolder(private val binding: ItemMessageUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvUserMessage.text = message.text
            val timeFormat = DateFormat.getTimeFormat(itemView.context)
            binding.tvUserTimestamp.text = timeFormat.format(Date(message.timestamp))

            if (message.imageUri != null) {
                binding.ivUserImage.visibility = View.VISIBLE
                binding.ivUserImage.load(message.imageUri) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(24f))
                }
            } else {
                binding.ivUserImage.visibility = View.GONE
            }
        }
    }

    class AssistantViewHolder(private val binding: ItemMessageAssistantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            if (message.text.isEmpty()) {
                binding.tvAssistantMessage.visibility = View.GONE
                binding.tvTypingIndicator.visibility = View.VISIBLE
            } else {
                binding.tvAssistantMessage.visibility = View.VISIBLE
                
                // Initialize Markwon if needed
                if (markwon == null) {
                    markwon = Markwon.create(itemView.context)
                }
                
                markwon?.setMarkdown(binding.tvAssistantMessage, message.text)
                binding.tvTypingIndicator.visibility = View.GONE
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
    }
}
