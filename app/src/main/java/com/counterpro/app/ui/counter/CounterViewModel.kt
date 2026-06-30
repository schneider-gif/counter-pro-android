package com.counterpro.app.ui.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.counterpro.app.data.repository.CountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CounterViewModel(private val repo: CountRepository) : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private val _label = MutableStateFlow("Contagem")
    val label = _label.asStateFlow()

    fun increment() { _count.value++ }
    fun decrement() { if (_count.value > 0) _count.value-- }
    fun reset() { _count.value = 0 }
    fun setLabel(l: String) { _label.value = l }

    fun save() {
        viewModelScope.launch {
            repo.save(_label.value, _count.value)
            _count.value = 0
        }
    }
}
