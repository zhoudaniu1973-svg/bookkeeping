package com.bookkeeping.data.local.dao

import androidx.room.*
import com.bookkeeping.data.local.entity.RecurringBillEntity
import kotlinx.coroutines.flow.Flow

/**
 * 周期性账单 DAO
 */
@Dao
interface RecurringBillDao {
    
    /**
     * 获取用户所有周期性账单
     */
    @Query("SELECT * FROM recurring_bills WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY nextDueDate ASC")
    fun getAllRecurringBills(userId: String): Flow<List<RecurringBillEntity>>
    
    /**
     * 获取所有活跃的周期性账单（用于自动生成）
     */
    @Query("SELECT * FROM recurring_bills WHERE userId = :userId AND isActive = 1 AND syncStatus != 'PENDING_DELETE'")
    suspend fun getActiveRecurringBills(userId: String): List<RecurringBillEntity>
    
    /**
     * 获取到期的周期性账单（nextDueDate <= 今天）
     */
    @Query("SELECT * FROM recurring_bills WHERE userId = :userId AND isActive = 1 AND nextDueDate <= :todayTimestamp AND syncStatus != 'PENDING_DELETE'")
    suspend fun getDueRecurringBills(userId: String, todayTimestamp: Long): List<RecurringBillEntity>
    
    /**
     * 根据ID获取
     */
    @Query("SELECT * FROM recurring_bills WHERE id = :id")
    suspend fun getById(id: String): RecurringBillEntity?
    
    /**
     * 插入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecurringBillEntity)
    
    /**
     * 更新
     */
    @Update
    suspend fun update(entity: RecurringBillEntity)
    
    /**
     * 更新下次执行日期
     */
    @Query("UPDATE recurring_bills SET nextDueDate = :nextDueDate, syncStatus = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun updateNextDueDate(id: String, nextDueDate: Long)
    
    /**
     * 切换启用状态
     */
    @Query("UPDATE recurring_bills SET isActive = :isActive, syncStatus = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun setActive(id: String, isActive: Boolean)
    
    /**
     * 标记删除
     */
    @Query("UPDATE recurring_bills SET syncStatus = 'PENDING_DELETE' WHERE id = :id")
    suspend fun markAsDeleted(id: String)
    
    /**
     * 真正删除
     */
    @Query("DELETE FROM recurring_bills WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * 清空用户数据
     */
    @Query("DELETE FROM recurring_bills WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
