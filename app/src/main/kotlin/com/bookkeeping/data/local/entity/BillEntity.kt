package com.bookkeeping.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bookkeeping.data.model.Bill
import com.bookkeeping.data.model.BillType
import java.util.Date

/**
 * 账单 Room 实体
 * 用于本地数据库存储，支持离线操作
 */
@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val type: String,           // BillType 枚举转字符串存储
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val note: String,
    val billDate: Long,         // Date 转换为时间戳存储
    val createdAt: Long,
    val syncStatus: String = SyncStatus.SYNCED.name,  // 同步状态
    val userId: String = ""     // 用户ID，用于多用户隔离
) {
    /**
     * 转换为领域模型
     */
    fun toModel(): Bill = Bill(
        id = id,
        amount = amount,
        type = try { BillType.valueOf(type) } catch (e: Exception) { BillType.EXPENSE },
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
        note = note,
        billDate = Date(billDate),
        createdAt = Date(createdAt)
    )
    
    companion object {
        /**
         * 从领域模型创建实体
         * @param bill 领域模型
         * @param userId 当前用户ID
         * @param syncStatus 同步状态，默认为待上传
         */
        fun fromModel(
            bill: Bill, 
            userId: String,
            syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
        ): BillEntity = BillEntity(
            id = bill.id.ifEmpty { java.util.UUID.randomUUID().toString() },
            amount = bill.amount,
            type = bill.type.name,
            categoryId = bill.categoryId,
            categoryName = bill.categoryName,
            categoryIcon = bill.categoryIcon,
            categoryColor = bill.categoryColor,
            note = bill.note,
            billDate = bill.billDate.time,
            createdAt = bill.createdAt.time,
            syncStatus = syncStatus.name,
            userId = userId
        )
    }
}
