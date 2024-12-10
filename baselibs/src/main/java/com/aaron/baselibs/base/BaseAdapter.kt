package com.aaron.baselibs.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T: ViewBinding>(val datas: MutableList<out Any>):
    RecyclerView.Adapter<BaseViewHolder<T>>() {

    private lateinit var _binding: T;
    public val binding get() = _binding;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        _binding = getViewBinding(parent, viewType)

        operationLayoutSize(_binding)

        return BaseViewHolder(_binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {

        try{
            operationHolder(holder, position)

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    abstract fun getViewBinding(parent: ViewGroup, viewType: Int): T

    abstract fun operationHolder(holder: BaseViewHolder<T>, position: Int)

    abstract fun operationLayoutSize(viewBinding: T);

    override fun getItemCount(): Int {
        return datas?.let {
            it.size
        }?:0
    }
}