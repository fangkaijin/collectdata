package com.aaron.baselibs.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T: ViewBinding>: Fragment() {

    private var _binding: T? = null

    val xBing: T?
        get() {
            try{

                return this::_binding.get()

            }catch (e: Exception){
                e.printStackTrace()
            }

            return null
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = getBinding(inflater, container)

        return _binding?.root?:null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseView(_binding)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        //销毁
        _binding = null

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    abstract fun getBinding(inflater: LayoutInflater,
                            container: ViewGroup?): T?
    abstract fun parseView(binding: T?)
}