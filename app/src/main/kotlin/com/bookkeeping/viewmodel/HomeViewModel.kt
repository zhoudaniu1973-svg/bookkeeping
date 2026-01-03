package com.bookkeeping.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookkeeping.data.model.Bill
import com.bookkeeping.data.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val isLoading: Boolean = false,
    val bills: List<Bill> = emptyList(),
    val groupedBills: Map<Triple<Int, Int, Int>, List<Bill>> = emptyMap(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _uiState = mutableStateOf(HomeUiState())
    val uiState: State<HomeUiState> = _uiState
    
    init {
        loadBills()
    }
    
    fun loadBills() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getBills().collect { bills ->
                val currentYear = _uiState.value.currentYear
                val currentMonth = _uiState.value.currentMonth
                
                // 筛选当前月份
                val filteredBills = bills.filter { bill ->
                    val cal = Calendar.getInstance().apply { time = bill.billDate }
                    cal.get(Calendar.YEAR) == currentYear && (cal.get(Calendar.MONTH) + 1) == currentMonth
                }
                
                // 计算收支
                var income = 0.0
                var expense = 0.0
                filteredBills.forEach { 
                    if (it.type == com.bookkeeping.data.model.BillType.INCOME) income += it.amount
                    else expense += it.amount
                }
                
                // 按日期分组
                val grouped = filteredBills.groupBy { 
                    val cal = Calendar.getInstance().apply { time = it.billDate }
                    Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                }.toSortedMap(compareByDescending { Triple(it.first, it.second, it.third).toString() }) // 简单排序，实际可能需要更复杂的比较器
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bills = filteredBills,
                    groupedBills = grouped,
                    totalIncome = income,
                    totalExpense = expense
                )
            }
        }
    }
    
    fun deleteBill(billId: String) {
        viewModelScope.launch {
            repository.deleteBill(billId)
            loadBills() // 通常 Flow 会自动更新，但如果需要手动刷新也可以
        }
    }
    
    fun previousMonth() {
        var year = _uiState.value.currentYear
        var month = _uiState.value.currentMonth - 1
        if (month < 1) {
            month = 12
            year--
        }
        _uiState.value = _uiState.value.copy(currentYear = year, currentMonth = month)
        loadBills()
    }
    
    fun nextMonth() {
        var year = _uiState.value.currentYear
        var month = _uiState.value.currentMonth + 1
        if (month > 12) {
            month = 1
            year++
        }
        _uiState.value = _uiState.value.copy(currentYear = year, currentMonth = month)
        loadBills()
    }
}
