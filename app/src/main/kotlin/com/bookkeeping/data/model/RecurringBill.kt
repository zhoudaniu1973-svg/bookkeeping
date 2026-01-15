package com.bookkeeping.data.model

import java.util.Calendar
import java.util.Date

/**
 * 周期频率枚举
 */
enum class Frequency {
    DAILY,    // 每日
    WEEKLY,   // 每周
    MONTHLY,  // 每月
    YEARLY    // 每年
}

/**
 * 周期性账单数据模型
 * 用于自动记录房租、订阅、工资等周期性收支
 * 
 * @param id 唯一标识
 * @param amount 金额
 * @param type 类型（收入/支出）
 * @param categoryId 分类ID
 * @param categoryName 分类名称
 * @param categoryIcon 分类图标
 * @param categoryColor 分类颜色
 * @param note 备注
 * @param frequency 周期频率
 * @param startDate 开始日期
 * @param nextDueDate 下次执行日期
 * @param isActive 是否启用
 * @param createdAt 创建时间
 */
data class RecurringBill(
    val id: String = "",
    val amount: Double = 0.0,
    val type: BillType = BillType.EXPENSE,
    val categoryId: String = "",
    val categoryName: String = "",
    val categoryIcon: String = "",
    val categoryColor: String = "#4A90D9",
    val note: String = "",
    val frequency: Frequency = Frequency.MONTHLY,
    val startDate: Date = Date(),
    val nextDueDate: Date = Date(),
    val isActive: Boolean = true,
    val createdAt: Date = Date()
) {
    /**
     * 转换为 Map 用于 Firebase 存储
     */
    fun toMap(): Map<String, Any> = mapOf(
        "amount" to amount,
        "type" to type.name,
        "categoryId" to categoryId,
        "categoryName" to categoryName,
        "categoryIcon" to categoryIcon,
        "categoryColor" to categoryColor,
        "note" to note,
        "frequency" to frequency.name,
        "startDate" to startDate,
        "nextDueDate" to nextDueDate,
        "isActive" to isActive,
        "createdAt" to createdAt
    )
    
    /**
     * 转换为普通账单（用于自动生成）
     */
    fun toBill(billDate: Date = nextDueDate): Bill = Bill(
        amount = amount,
        type = type,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
        note = "[周期] $note",
        billDate = billDate,
        createdAt = Date()
    )
    
    /**
     * 计算下一个执行日期
     */
    fun calculateNextDueDate(): Date {
        val calendar = Calendar.getInstance().apply { time = nextDueDate }
        when (frequency) {
            Frequency.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            Frequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            Frequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            Frequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }
        return calendar.time
    }
    
    /**
     * 获取周期的中文描述
     */
    fun getFrequencyLabel(): String = when (frequency) {
        Frequency.DAILY -> "每日"
        Frequency.WEEKLY -> "每周"
        Frequency.MONTHLY -> "每月"
        Frequency.YEARLY -> "每年"
    }
    
    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): RecurringBill {
            return RecurringBill(
                id = id,
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                type = try { 
                    BillType.valueOf(map["type"] as? String ?: "EXPENSE") 
                } catch (e: Exception) { 
                    BillType.EXPENSE 
                },
                categoryId = map["categoryId"] as? String ?: "",
                categoryName = map["categoryName"] as? String ?: "",
                categoryIcon = map["categoryIcon"] as? String ?: "",
                categoryColor = map["categoryColor"] as? String ?: "#4A90D9",
                note = map["note"] as? String ?: "",
                frequency = try {
                    Frequency.valueOf(map["frequency"] as? String ?: "MONTHLY")
                } catch (e: Exception) {
                    Frequency.MONTHLY
                },
                startDate = (map["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                nextDueDate = (map["nextDueDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                isActive = map["isActive"] as? Boolean ?: true,
                createdAt = (map["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
            )
        }
    }
}
