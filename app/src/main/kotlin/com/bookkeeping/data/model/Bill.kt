package com.bookkeeping.data.model

import java.util.Date

/**
 * 账单类型枚举
 */
enum class BillType {
    INCOME,   // 收入
    EXPENSE   // 支出
}

/**
 * 账单数据模型
 * 
 * @param id 账单ID
 * @param amount 金额
 * @param type 类型（收入/支出）
 * @param categoryId 分类ID
 * @param categoryName 分类名称
 * @param categoryIcon 分类图标
 * @param note 备注
 * @param billDate 账单日期
 * @param createdAt 创建时间
 */
data class Bill(
    val id: String = "",
    val amount: Double = 0.0,
    val type: BillType = BillType.EXPENSE,
    val categoryId: String = "",
    val categoryName: String = "",
    val categoryIcon: String = "",
    val note: String = "",
    val billDate: Date = Date(),
    val createdAt: Date = Date()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "amount" to amount,
        "type" to type.name,
        "categoryId" to categoryId,
        "categoryName" to categoryName,
        "categoryIcon" to categoryIcon,
        "note" to note,
        "billDate" to billDate,
        "createdAt" to createdAt
    )
    
    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Bill {
            return Bill(
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
                note = map["note"] as? String ?: "",
                billDate = (map["billDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                createdAt = (map["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
            )
        }
    }
}
