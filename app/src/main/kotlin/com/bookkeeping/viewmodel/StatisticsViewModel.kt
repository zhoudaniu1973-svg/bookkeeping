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

// 统计周期枚举
enum class StatsPeriod {
    MONTH,  // 按月统计
    YEAR    // 按年统计
}

data class CategoryStat(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val statsPeriod: StatsPeriod = StatsPeriod.MONTH, // 统计周期
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val billType: BillType = BillType.EXPENSE,
    val totalAmount: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
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
    
    // 设置统计周期（月/年）
    fun setStatsPeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(statsPeriod = period)
        loadData()
    }
    
    fun setBillType(type: BillType) {
        _uiState.value = _uiState.value.copy(billType = type)
        loadData()
    }
    
    // 上一个周期（根据当前模式决定是上个月还是上一年）
    fun previousPeriod() {
        val state = _uiState.value
        if (state.statsPeriod == StatsPeriod.MONTH) {
            var year = state.currentYear
            var month = state.currentMonth - 1
            if (month < 1) {
                month = 12
                year--
            }
            _uiState.value = state.copy(currentYear = year, currentMonth = month)
        } else {
            // 年模式：减一年
            _uiState.value = state.copy(currentYear = state.currentYear - 1)
        }
        loadData()
    }
    
    // 下一个周期
    fun nextPeriod() {
        val state = _uiState.value
        if (state.statsPeriod == StatsPeriod.MONTH) {
            var year = state.currentYear
            var month = state.currentMonth + 1
            if (month > 12) {
                month = 1
                year++
            }
            _uiState.value = state.copy(currentYear = year, currentMonth = month)
        } else {
            // 年模式：加一年
            _uiState.value = state.copy(currentYear = state.currentYear + 1)
        }
        loadData()
    }
    
    // 保留旧方法名以兼容现有调用
    fun previousMonth() = previousPeriod()
    fun nextMonth() = nextPeriod()
    
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
                    val period = _uiState.value.statsPeriod
                    
                    // 根据统计周期过滤账单
                    val periodBills = bills.filter { bill ->
                        val cal = Calendar.getInstance().apply { time = bill.billDate }
                        val billYear = cal.get(Calendar.YEAR)
                        val billMonth = cal.get(Calendar.MONTH) + 1
                        
                        if (period == StatsPeriod.MONTH) {
                            billYear == year && billMonth == month
                        } else {
                            // 年模式：只匹配年份
                            billYear == year
                        }
                    }
                    
                    // 计算总收入和总支出
                    val totalIncome = periodBills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
                    val totalExpense = periodBills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
                    
                    // 按类型过滤用于分类统计
                    val filteredBills = periodBills.filter { it.type == type }
                    
                    // 计算选中类型的总额
                    val total = filteredBills.sumOf { it.amount }
                    
                    // 按分类分组并计算统计信息
                    val stats = if (total > 0) {
                        filteredBills.groupBy { it.categoryId }
                            .mapNotNull { (categoryId, billsInCategory) ->
                                if (billsInCategory.isEmpty()) return@mapNotNull null
                                
                                val categoryTotal = billsInCategory.sumOf { it.amount }
                                val percentage = (categoryTotal / total).toFloat()
                                
                                val firstBill = billsInCategory.first()
                                val category = Category(
                                    id = categoryId,
                                    name = firstBill.categoryName,
                                    icon = firstBill.categoryIcon,
                                    color = firstBill.categoryColor,
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
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
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
