package com.aaron.petsocial.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.aaron.baselibs.base.BaseAdapter
import com.aaron.baselibs.base.BaseViewHolder
import com.aaron.baselibs.utils.fromat
import com.aaron.petsocial.R
import com.aaron.petsocial.databinding.ItemChatBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import java.util.Date

class ChatAdapter(val layoutInflater: LayoutInflater, datas: MutableList<EMConversation>): BaseAdapter<ItemChatBinding>(datas) {
    override fun getViewBinding(parent: ViewGroup, viewType: Int): ItemChatBinding = ItemChatBinding.inflate(layoutInflater, parent, false)

    override fun operationLayoutSize(viewBinding: ItemChatBinding) {

    }

    override fun operationHolder(holder: BaseViewHolder<ItemChatBinding>, position: Int) {

        val conversation: EMConversation = datas.get(position) as EMConversation

        val conversationId = conversation.conversationId()
        val message: EMMessage = conversation.lastMessage

        binding?.let {
            val option: RequestOptions = RequestOptions()
            option.placeholder(layoutInflater.context.resources.getDrawable(R.drawable.ic_chat_normal))
            option.error(layoutInflater.context.resources.getDrawable(R.drawable.ic_chat_normal))
            Glide.with(layoutInflater.context).load("").apply(option).into(it.chatIv)

            it.chatName.text = message.from
            it.lastMsg.text = message.body.toString()
            it.chatTime.text = Date(message.msgTime).fromat("MM-dd HH:mm")

        }
    }
}