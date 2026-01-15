package com.bookkeeping.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.data.model.Frequency
import com.bookkeeping.data.model.RecurringBill
import com.bookkeeping.data.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class RecurringBillUiState(
    val isLoading: Boolean = false,
    val recurringBills: List<RecurringBill> = emptyList(),
    val error: String? = null
)

data class AddRecurringBillUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val editingId: String = "",
    val billType: BillType = BillType.EXPENSE,
    val amount: String = "",
    val selectedCategory: Category? = null,
    val note: String = "",
    val frequency: Frequency = Frequency.MONTHLY,
    val startDate: Date = Date(),
    val isActive: Boolean = true,
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val error: String? = null
)

/**
 * 周期性账单 ViewModel
 */
class RecurringBillViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _listState = mutableStateOf(RecurringBillUiState())
    val listState: State<RecurringBillUiState> = _listState
    
    private val _addState = mutableStateOf(AddRecurringBillUiState())
    val addState: State<AddRecurringBillUiState> = _addState
    
    init {
        loadRecurringBills()
        loadCategories()
        checkAndGenerateDueBills()
    }
    
    /**
     * 加载周期性账单列表
     */
    fun loadRecurringBills() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)
            repository.getRecurringBills().collect { bills ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    recurringBills = bills
                )
            }
        }
    }
    
    /**
     * 加载分类
     */
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { categories ->
                _addState.value = _addState.value.copy(
                    expenseCategories = categories.filter { it.type == BillType.EXPENSE },
                    incomeCategories = categories.filter { it.type == BillType.INCOME }
                )
            }
        }
    }
    
    /**
     * 检查并生成到期账单
     * 在 ViewModel 初始化时自动调用
     */
    fun checkAndGenerateDueBills() {
        viewModelScope.launch {
            try {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time
                
                repository.getRecurringBills().collect { bills ->
                    bills.filter { it.isActive && it.nextDueDate <= today }
                        .forEach { recurring ->
                            // 生成账单
                            val bill = recurring.toBill()
                            repository.addBill(bill)
                            
                            // 更新下次执行日期
                            val updated = recurring.copy(
                                nextDueDate = recurring.calculateNextDueDate()
                            )
                            repository.updateRecurringBill(updated)
                        }
                    return@collect // 只处理一次
                }
            } catch (e: Exception) {
                // 静默处理错误
            }
        }
    }
    
    // ==================== 添加/编辑状态管理 ====================
    
    fun setBillType(type: BillType) {
        _addState.value = _addState.value.copy(billType = type, selectedCategory = null)
    }
    
    fun setAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _addState.value = _addState.value.copy(amount = amount)
        }
    }
    
    fun setCategory(category: Category) {
        _addState.value = _addState.value.copy(selectedCategory = category)
    }
    
    fun setNote(note: String) {
        _addState.value = _addState.value.copy(note = note)
    }
    
    fun setFrequency(frequency: Frequency) {
        _addState.value = _addState.value.copy(frequency = frequency)
    }
    
    fun setStartDate(date: Date) {
        _addState.value = _addState.value.copy(startDate = date)
    }
    
    fun setActive(active: Boolean) {
        _addState.value = _addState.value.copy(isActive = active)
    }
    
    /**
     * 加载编辑数据
     */
    fun loadForEdit(recurring: RecurringBill) {
        val categories = if (recurring.type == BillType.EXPENSE) 
            _addState.value.expenseCategories else _addState.value.incomeCategories
        val category = categories.find { it.id == recurring.categoryId }
        
        _addState.value = _addState.value.copy(
            isEditMode = true,
            editingId = recurring.id,
            billType = recurring.type,
            amount = recurring.amount.toString(),
            selectedCategory = category,
            note = recurring.note,
            frequency = recurring.frequency,
            startDate = recurring.startDate,
            isActive = recurring.isActive
        )
    }
    
    /**
     * 保存周期性账单
     */
    fun save(onSuccess: () -> Unit) {
        val state = _addState.value
        val amountVal = state.amount.toDoubleOrNull()
        
        if (amountVal == null || amountVal <= 0) {
            _addState.value = state.copy(error = "请输入有效金额")
            return
        }
        if (state.selectedCategory == null) {
            _addState.value = state.copy(error = "请选择分类")
            return
        }
        
        viewModelScope.launch {
            _addState.value = state.copy(isLoading = true)
            try {
                val recurring = RecurringBill(
                    id = if (state.isEditMode) state.editingId else "",
                    amount = amountVal,
                    type = state.billType,
                    categoryId = state.selectedCategory.id,
                    categoryName = state.selectedCategory.name,
                    categoryIcon = state.selectedCategory.icon,
                    categoryColor = state.selectedCategory.color,
                    note = state.note,
                    frequency = state.frequency,
                    startDate = state.startDate,
                    nextDueDate = state.startDate, // 首次执行日期
                    isActive = state.isActive
                )
                
                if (state.isEditMode) {
                    repository.updateRecurringBill(recurring)
                } else {
                    repository.addRecurringBill(recurring)
                }
                
                _addState.value = _addState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _addState.value = _addState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    /**
     * 切换启用状态
     */
    fun toggleActive(recurring: RecurringBill) {
        viewModelScope.launch {
            try {
                val updated = recurring.copy(isActive = !recurring.isActive)
                repository.updateRecurringBill(updated)
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = e.message)
            }
        }
    }
    
    /**
     * 删除周期性账单
     */
    fun delete(recurringId: String) {
        viewModelScope.launch {
            try {
                repository.deleteRecurringBill(recurringId)
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = e.message)
            }
        }
    }
    
    /**
     * 重置添加状态
     */
    fun resetAddState() {
        _addState.value = AddRecurringBillUiState(
            expenseCategories = _addState.value.expenseCategories,
            incomeCategories = _addState.value.incomeCategories
        )
    }
    
    fun clearError() {
        _addState.value = _addState.value.copy(error = null)
        _listState.value = _listState.value.copy(error = null)
    }
}
