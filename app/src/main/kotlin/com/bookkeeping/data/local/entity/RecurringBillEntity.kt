package com.bookkeeping.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Frequency
import com.bookkeeping.data.model.RecurringBill
import java.util.Date

/**
 * 周期性账单 Room 实体
 */
@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val type: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val note: String,
    val frequency: String,
    val startDate: Long,
    val nextDueDate: Long,
    val isActive: Boolean,
    val createdAt: Long,
    val syncStatus: String = SyncStatus.SYNCED.name,
    val userId: String = ""
) {
    /**
     * 转换为领域模型
     */
    fun toModel(): RecurringBill = RecurringBill(
        id = id,
        amount = amount,
        type = try { BillType.valueOf(type) } catch (e: Exception) { BillType.EXPENSE },
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
        note = note,
        frequency = try { Frequency.valueOf(frequency) } catch (e: Exception) { Frequency.MONTHLY },
        startDate = Date(startDate),
        nextDueDate = Date(nextDueDate),
        isActive = isActive,
        createdAt = Date(createdAt)
    )
    
    companion object {
        fun fromModel(
            recurring: RecurringBill,
            userId: String,
            syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
        ): RecurringBillEntity = RecurringBillEntity(
            id = recurring.id.ifEmpty { java.util.UUID.randomUUID().toString() },
            amount = recurring.amount,
            type = recurring.type.name,
            categoryId = recurring.categoryId,
            categoryName = recurring.categoryName,
            categoryIcon = recurring.categoryIcon,
            categoryColor = recurring.categoryColor,
            note = recurring.note,
            frequency = recurring.frequency.name,
            startDate = recurring.startDate.time,
            nextDueDate = recurring.nextDueDate.time,
            isActive = recurring.isActive,
            createdAt = recurring.createdAt.time,
            syncStatus = syncStatus.name,
            userId = userId
        )
    }
}
