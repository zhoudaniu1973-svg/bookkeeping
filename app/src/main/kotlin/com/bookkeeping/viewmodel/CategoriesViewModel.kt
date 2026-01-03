package com.bookkeeping.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val isLoading: Boolean = false,
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val error: String? = null
)

class CategoriesViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _uiState = mutableStateOf(CategoriesUiState())
    val uiState: State<CategoriesUiState> = _uiState
    
    init {
        loadCategories()
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    expenseCategories = categories.filter { it.type == BillType.EXPENSE },
                    incomeCategories = categories.filter { it.type == BillType.INCOME }
                )
            }
        }
    }
    
    fun addCategory(name: String, icon: String, color: String, type: BillType, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val newCategory = Category(
                    name = name,
                    icon = icon,
                    color = color,
                    type = type,
                    isDefault = false
                )
                repository.addCategory(newCategory)
                onSuccess()
                // loadCategories will be triggered by Flow update
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    fun updateCategory(category: Category, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.updateCategory(category)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(categoryId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
