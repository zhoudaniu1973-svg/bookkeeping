package com.bookkeeping.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.data.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryStat(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val billType: BillType = BillType.EXPENSE,
    val totalAmount: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val error: String? = null
)

class StatisticsViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _uiState = mutableStateOf(StatisticsUiState())
    val uiState: State<StatisticsUiState> = _uiState
    
    init {
        loadData()
    }
    
    fun setBillType(type: BillType) {
        _uiState.value = _uiState.value.copy(billType = type)
        loadData()
    }
    
    fun previousMonth() {
        var year = _uiState.value.currentYear
        var month = _uiState.value.currentMonth - 1
        if (month < 1) {
            month = 12
            year--
        }
        _uiState.value = _uiState.value.copy(currentYear = year, currentMonth = month)
        loadData()
    }
    
    fun nextMonth() {
        var year = _uiState.value.currentYear
        var month = _uiState.value.currentMonth + 1
        if (month > 12) {
            month = 1
            year++
        }
        _uiState.value = _uiState.value.copy(currentYear = year, currentMonth = month)
        loadData()
    }
    
    // Explicitly public reload method if needed by UI pull-to-refresh
    fun reload() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getBills().collect { bills ->
                try {
                    val year = _uiState.value.currentYear
                    val month = _uiState.value.currentMonth
                    val type = _uiState.value.billType
                    
                    // Filter bills
                    val filteredBills = bills.filter { bill ->
                        val cal = Calendar.getInstance().apply { time = bill.billDate }
                        cal.get(Calendar.YEAR) == year && 
                        (cal.get(Calendar.MONTH) + 1) == month &&
                        bill.type == type
                    }
                    
                    // Calculate total
                    val total = filteredBills.sumOf { it.amount }
                    
                    // Group by category and calculate stats
                    val stats = if (total > 0) {
                        filteredBills.groupBy { it.categoryId }
                            .mapNotNull { (categoryId, billsInCategory) ->
                                if (billsInCategory.isEmpty()) return@mapNotNull null
                                
                                val categoryTotal = billsInCategory.sumOf { it.amount }
                                val percentage = (categoryTotal / total).toFloat()
                                
                                // Construct a Category object (assuming we have basic info from the first bill)
                                // ideally we should fetch Category from DB, but for now we use what's on the bill
                                // or reconstruction. 
                                // Since Bill stores categoryName/Icon/Color directly, we can use that.
                                val firstBill = billsInCategory.first()
                                val category = Category(
                                    id = categoryId,
                                    name = firstBill.categoryName,
                                    icon = firstBill.categoryIcon,
                                    color = "#4A90D9",
                                    type = type
                                )
                                
                                CategoryStat(category, categoryTotal, percentage)
                            }
                            .sortedByDescending { it.amount }
                    } else {
                        emptyList()
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        totalAmount = total,
                        categoryStats = stats
                    )
                } catch (e: Exception) {
                     _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}
