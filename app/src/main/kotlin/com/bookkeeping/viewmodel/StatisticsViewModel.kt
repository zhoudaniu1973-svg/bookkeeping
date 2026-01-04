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
    val percentage: Float,
    val bills: List<com.bookkeeping.data.model.Bill> = emptyList() // 该分类下的账单列表
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
    
    // 分类名称到颜色的映射表，用于兼容旧数据
    private val categoryColorMap = mapOf(
        // 支出分类
        "餐饮" to "#FF6B6B",
        "交通" to "#4A90D9",
        "购物" to "#9B59B6",
        "娱乐" to "#E67E22",
        "居住" to "#27AE60",
        "通讯" to "#3498DB",
        "医疗" to "#E74C3C",
        "教育" to "#1ABC9C",
        "其他" to "#95A5A6",
        // 收入分类
        "工资" to "#52C41A",
        "奖金" to "#FAAD14",
        "投资" to "#722ED1",
        "兼职" to "#13C2C2"
    )
    
    // 获取分类颜色，优先使用存储的颜色，若为默认值则根据名称匹配
    private fun getCategoryColor(storedColor: String, categoryName: String, type: BillType): String {
        // 如果存储的颜色不是默认值，直接使用
        if (storedColor != "#4A90D9") {
            return storedColor
        }
        // 否则根据分类名称查找对应颜色
        return categoryColorMap[categoryName] 
            ?: if (type == BillType.INCOME) "#52C41A" else "#4A90D9"
    }
    
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
                                // 使用颜色映射来兼容旧数据
                                val resolvedColor = getCategoryColor(
                                    firstBill.categoryColor,
                                    firstBill.categoryName,
                                    type
                                )
                                val category = Category(
                                    id = categoryId,
                                    name = firstBill.categoryName,
                                    icon = firstBill.categoryIcon,
                                    color = resolvedColor,
                                    type = type
                                )
                                
                                CategoryStat(category, categoryTotal, percentage, billsInCategory)
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
