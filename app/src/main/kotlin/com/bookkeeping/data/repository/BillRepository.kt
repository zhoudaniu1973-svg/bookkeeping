package com.bookkeeping.data.repository

import android.util.Log
import com.bookkeeping.data.local.dao.BillDao
import com.bookkeeping.data.local.dao.CategoryDao
import com.bookkeeping.data.local.entity.BillEntity
import com.bookkeeping.data.local.entity.CategoryEntity
import com.bookkeeping.data.local.entity.SyncStatus
import com.bookkeeping.data.model.Bill
import com.bookkeeping.data.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * 统一的账单仓库
 * 实现离线优先策略：本地读写 + 云端同步
 */
class BillRepository(
    private val billDao: BillDao,
    private val categoryDao: CategoryDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    companion object {
        private const val TAG = "BillRepository"
    }
    
    private val userId: String
        get() = auth.currentUser?.uid ?: ""
    
    private val isLoggedIn: Boolean
        get() = auth.currentUser != null
    
    // ==================== 账单操作 ====================
    
    /**
     * 获取账单列表（离线优先）
     * 返回本地数据的 Flow，后台自动同步云端数据
     */
    fun getBills(): Flow<List<Bill>> {
        // 后台触发云端同步
        if (isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                syncBillsFromCloud()
            }
        }
        // 返回本地数据流
        return billDao.getAllBills(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    /**
     * 添加账单
     * 本地立即保存，后台异步同步到云端
     */
    suspend fun addBill(bill: Bill): String {
        val entity = BillEntity.fromModel(bill, userId, SyncStatus.PENDING_UPLOAD)
        billDao.insertBill(entity)
        
        // 后台同步到云端
        if (isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                syncBillToCloud(entity)
            }
        }
        
        return entity.id
    }
    
    /**
     * 更新账单
     */
    suspend fun updateBill(bill: Bill) {
        val entity = BillEntity.fromModel(bill, userId, SyncStatus.PENDING_UPLOAD)
        billDao.updateBill(entity)
        
        if (isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                syncBillToCloud(entity)
            }
        }
    }
    
    /**
     * 删除账单
     * 先标记为待删除，同步后真正删除
     */
    suspend fun deleteBill(billId: String) {
        billDao.markAsDeleted(billId)
        
        if (isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    userDoc().collection("bills").document(billId).delete().await()
                    billDao.deleteBillById(billId)
                    Log.d(TAG, "账单已从云端删除: $billId")
                } catch (e: Exception) {
                    Log.e(TAG, "云端删除失败，保留本地待删除标记", e)
                }
            }
        }
    }
    
    // ==================== 分类操作 ====================
    
    /**
     * 获取分类列表
     */
    fun getCategories(): Flow<List<Category>> {
        if (isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                syncCategoriesFromCloud()
            }
        }
        return categoryDao.getAllCategories(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    /**
     * 添加分类
     */
    suspend fun addCategory(category: Category): String {
        val entity = CategoryEntity.fromModel(category, userId, SyncStatus.PENDING_UPLOAD)
        categoryDao.insertCategory(entity)
        
        if (isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                syncCategoryToCloud(entity)
            }
        }
        
        return entity.id
    }
    
    /**
     * 检查是否有分类，没有则初始化默认分类
     */
    suspend fun initDefaultCategoriesIfNeeded() {
        val count = categoryDao.getCategoryCount(userId)
        if (count == 0) {
            // 插入默认支出分类
            Category.getDefaultExpenseCategories().forEach { category ->
                val entity = CategoryEntity.fromModel(category, userId, SyncStatus.PENDING_UPLOAD)
                categoryDao.insertCategory(entity)
            }
            // 插入默认收入分类
            Category.getDefaultIncomeCategories().forEach { category ->
                val entity = CategoryEntity.fromModel(category, userId, SyncStatus.PENDING_UPLOAD)
                categoryDao.insertCategory(entity)
            }
            Log.d(TAG, "已初始化默认分类")
            
            // 同步到云端
            if (isLoggedIn) {
                syncAllPendingData()
            }
        }
    }
    
    // ==================== 同步操作 ====================
    
    /**
     * 同步所有待上传数据到云端
     */
    suspend fun syncAllPendingData() {
        if (!isLoggedIn) return
        
        try {
            // 同步待上传的账单
            val pendingBills = billDao.getPendingUploadBills(userId)
            pendingBills.forEach { syncBillToCloud(it) }
            
            // 同步待删除的账单
            val pendingDeleteBills = billDao.getPendingDeleteBills(userId)
            pendingDeleteBills.forEach { bill ->
                try {
                    userDoc().collection("bills").document(bill.id).delete().await()
                    billDao.deleteBillById(bill.id)
                } catch (e: Exception) {
                    Log.e(TAG, "删除账单同步失败: ${bill.id}", e)
                }
            }
            
            // 同步分类
            val pendingCategories = categoryDao.getPendingUploadCategories(userId)
            pendingCategories.forEach { syncCategoryToCloud(it) }
            
            Log.d(TAG, "待上传数据同步完成")
        } catch (e: Exception) {
            Log.e(TAG, "同步失败", e)
        }
    }
    
    /**
     * 从云端同步账单到本地
     */
    private suspend fun syncBillsFromCloud() {
        try {
            val snapshot = userDoc()
                .collection("bills")
                .orderBy("billDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val cloudBills = snapshot.documents.map { doc ->
                Bill.fromMap(doc.id, doc.data ?: emptyMap())
            }
            
            // 将云端数据插入本地（已同步状态）
            cloudBills.forEach { bill ->
                val entity = BillEntity.fromModel(bill, userId, SyncStatus.SYNCED)
                // 使用 REPLACE 策略，云端数据覆盖本地
                billDao.insertBill(entity.copy(id = bill.id))
            }
            
            Log.d(TAG, "从云端同步了 ${cloudBills.size} 条账单")
        } catch (e: Exception) {
            Log.e(TAG, "从云端同步账单失败（可能离线）", e)
        }
    }
    
    /**
     * 从云端同步分类到本地
     */
    private suspend fun syncCategoriesFromCloud() {
        try {
            val snapshot = userDoc()
                .collection("categories")
                .get()
                .await()
            
            val cloudCategories = snapshot.documents.map { doc ->
                Category.fromMap(doc.id, doc.data ?: emptyMap())
            }
            
            cloudCategories.forEach { category ->
                val entity = CategoryEntity.fromModel(category, userId, SyncStatus.SYNCED)
                categoryDao.insertCategory(entity.copy(id = category.id))
            }
            
            Log.d(TAG, "从云端同步了 ${cloudCategories.size} 个分类")
        } catch (e: Exception) {
            Log.e(TAG, "从云端同步分类失败（可能离线）", e)
        }
    }
    
    /**
     * 同步单个账单到云端
     */
    private suspend fun syncBillToCloud(entity: BillEntity) {
        try {
            val bill = entity.toModel()
            userDoc().collection("bills").document(entity.id).set(bill.toMap()).await()
            billDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            Log.d(TAG, "账单已同步到云端: ${entity.id}")
        } catch (e: Exception) {
            Log.e(TAG, "账单同步失败（将在网络恢复后重试）: ${entity.id}", e)
        }
    }
    
    /**
     * 同步单个分类到云端
     */
    private suspend fun syncCategoryToCloud(entity: CategoryEntity) {
        try {
            val category = entity.toModel()
            userDoc().collection("categories").document(entity.id).set(category.toMap()).await()
            categoryDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            Log.d(TAG, "分类已同步到云端: ${entity.id}")
        } catch (e: Exception) {
            Log.e(TAG, "分类同步失败: ${entity.id}", e)
        }
    }
    
    private fun userDoc() = firestore.collection("users").document(userId)
}
