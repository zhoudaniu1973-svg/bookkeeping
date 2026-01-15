package com.bookkeeping.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category

/**
 * 分类 Room 实体
 * 用于本地数据库存储
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: String,           // BillType 枚举转字符串存储
    val isDefault: Boolean,
    val syncStatus: String = SyncStatus.SYNCED.name,
    val userId: String = ""
) {
    /**
     * 转换为领域模型
     */
    fun toModel(): Category = Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = try { BillType.valueOf(type) } catch (e: Exception) { BillType.EXPENSE },
        isDefault = isDefault
    )
    
    companion object {
        /**
         * 从领域模型创建实体
         */
        fun fromModel(
            category: Category,
            userId: String,
            syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
        ): CategoryEntity = CategoryEntity(
            id = category.id.ifEmpty { java.util.UUID.randomUUID().toString() },
            name = category.name,
            icon = category.icon,
            color = category.color,
            type = category.type.name,
            isDefault = category.isDefault,
            syncStatus = syncStatus.name,
            userId = userId
        )
    }
}
