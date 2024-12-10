package com.aaron.baselibs.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BaseViewModel<T>: ViewModel() {

    private val _stateStringFlow = MutableStateFlow<String>("")
    val stateFlow = _stateStringFlow.asStateFlow()

    private val __stateApiFlow = MutableStateFlow<BaseBean<T>>(BaseBean());
    val stateApiFlow = __stateApiFlow.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        //清空资源
    }
}