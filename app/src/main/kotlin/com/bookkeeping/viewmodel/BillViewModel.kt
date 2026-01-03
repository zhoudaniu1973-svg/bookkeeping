package com.bookkeeping.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookkeeping.data.model.Bill
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

data class BillUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val editingBillId: String = "",
    val billType: BillType = BillType.EXPENSE,
    val amount: String = "",
    val selectedCategory: Category? = null,
    val note: String = "",
    val billDate: Date = Date(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val error: String? = null
)

class BillViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _uiState = mutableStateOf(BillUiState())
    val uiState: State<BillUiState> = _uiState
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(
                    expenseCategories = categories.filter { it.type == BillType.EXPENSE },
                    incomeCategories = categories.filter { it.type == BillType.INCOME }
                )
            }
        }
    }
    
    fun setBillType(type: BillType) {
        _uiState.value = _uiState.value.copy(billType = type, selectedCategory = null)
    }
    
    fun setAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.value = _uiState.value.copy(amount = amount)
        }
    }
    
    fun setCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }
    
    fun setDate(date: Date) {
        _uiState.value = _uiState.value.copy(billDate = date)
    }
    
    fun loadBill(billId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 简单起见，这里直接从所有 bills 列表里找，或者需要 Repo 提供 getBillById
                val allBills = repository.getBills().first() 
                val bill = allBills.find { it.id == billId }
                
                if (bill != null) {
                    val categories = if (bill.type == BillType.EXPENSE) _uiState.value.expenseCategories else _uiState.value.incomeCategories
                    val category = categories.find { it.id == bill.categoryId } ?: categories.firstOrNull()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditMode = true,
                        editingBillId = billId,
                        billType = bill.type,
                        amount = bill.amount.toString(),
                        selectedCategory = category,
                        note = bill.note,
                        billDate = bill.billDate
                    )
                } else {
                     _uiState.value = _uiState.value.copy(isLoading = false, error = "账单未找到")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    fun saveBill(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amountVal = state.amount.toDoubleOrNull()
        
        if (amountVal == null || amountVal <= 0) {
            _uiState.value = state.copy(error = "请输入有效金额")
            return
        }
        if (state.selectedCategory == null) {
            _uiState.value = state.copy(error = "请选择分类")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            try {
                val bill = Bill(
                    id = if (state.isEditMode) state.editingBillId else "",
                    amount = amountVal,
                    type = state.billType,
                    categoryId = state.selectedCategory.id,
                    categoryName = state.selectedCategory.name,
                    categoryIcon = state.selectedCategory.icon,
                    note = state.note,
                    billDate = state.billDate
                )
                
                if (state.isEditMode) {
                    repository.updateBill(bill)
                } else {
                    repository.addBill(bill)
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    fun reset() {
        _uiState.value = BillUiState(
            expenseCategories = _uiState.value.expenseCategories,
            incomeCategories = _uiState.value.incomeCategories
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
