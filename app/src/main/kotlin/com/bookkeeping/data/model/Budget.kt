package com.bookkeeping.data.model

import java.util.Date

/**
 * 预算数据模型
 */
data class Budget(
    val id: String = "",
    val amount: Double = 0.0,
    val categoryId: String = "",
    val categoryName: String = "",
    val year: Int = 0,
    val month: Int = 0,
    val createdAt: Date = Date()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "amount" to amount,
        "categoryId" to categoryId,
        "categoryName" to categoryName,
        "year" to year,
        "month" to month,
        "createdAt" to createdAt
    )
    
    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Budget {
            return Budget(
                id = id,
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                categoryId = map["categoryId"] as? String ?: "",
                categoryName = map["categoryName"] as? String ?: "",
                year = (map["year"] as? Number)?.toInt() ?: 0,
                month = (map["month"] as? Number)?.toInt() ?: 0,
                createdAt = (map["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
            )
        }
    }
}
