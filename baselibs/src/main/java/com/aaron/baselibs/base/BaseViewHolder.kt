package com.aaron.baselibs.base

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class BaseViewHolder<T: ViewBinding>(viewBinding: T) : RecyclerView.ViewHolder(viewBinding.root) {
}