package com.bookkeeping.data.local.dao

import androidx.room.*
import com.bookkeeping.data.local.entity.BillEntity
import com.bookkeeping.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * 账单数据访问对象
 * 提供本地数据库的 CRUD 操作
 */
@Dao
interface BillDao {
    
    /**
     * 获取指定用户的所有账单，按日期降序排列
     */
    @Query("SELECT * FROM bills WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY billDate DESC")
    fun getAllBills(userId: String): Flow<List<BillEntity>>
    
    /**
     * 根据ID获取单个账单
     */
    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillById(billId: String): BillEntity?
    
    /**
     * 插入账单（如果已存在则替换）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity)
    
    /**
     * 批量插入账单
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<BillEntity>)
    
    /**
     * 更新账单
     */
    @Update
    suspend fun updateBill(bill: BillEntity)
    
    /**
     * 删除账单
     */
    @Delete
    suspend fun deleteBill(bill: BillEntity)
    
    /**
     * 根据ID删除账单
     */
    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteBillById(billId: String)
    
    /**
     * 标记账单为待删除状态（软删除，等待同步后真正删除）
     */
    @Query("UPDATE bills SET syncStatus = 'PENDING_DELETE' WHERE id = :billId")
    suspend fun markAsDeleted(billId: String)
    
    /**
     * 获取所有待同步的账单（新增/修改）
     */
    @Query("SELECT * FROM bills WHERE userId = :userId AND syncStatus = 'PENDING_UPLOAD'")
    suspend fun getPendingUploadBills(userId: String): List<BillEntity>
    
    /**
     * 获取所有待删除的账单
     */
    @Query("SELECT * FROM bills WHERE userId = :userId AND syncStatus = 'PENDING_DELETE'")
    suspend fun getPendingDeleteBills(userId: String): List<BillEntity>
    
    /**
     * 更新同步状态
     */
    @Query("UPDATE bills SET syncStatus = :status WHERE id = :billId")
    suspend fun updateSyncStatus(billId: String, status: String)
    
    /**
     * 清空指定用户的所有账单（用于登出时清理）
     */
    @Query("DELETE FROM bills WHERE userId = :userId")
    suspend fun deleteAllBillsForUser(userId: String)
}
