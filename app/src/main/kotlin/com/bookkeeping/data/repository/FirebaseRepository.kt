package com.bookkeeping.data.repository

import android.util.Log
import com.bookkeeping.data.model.Bill
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.data.model.Budget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

/**
 * Firebase 数据仓库
 * 
 * 离线支持说明：
 * - Firebase Firestore 已在 BookkeepingApp 中启用离线持久化
 * - 离线时数据会自动缓存到本地，在线时自动同步
 * - 写操作会立即应用到本地缓存，网络恢复后自动同步到云端
 */
class FirebaseRepository {
    
    companion object {
        private const val TAG = "FirebaseRepository"
    }
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // 安全获取 userId，未登录时返回空字符串
    private val userId: String
        get() = auth.currentUser?.uid ?: ""
    
    // 检查是否已登录
    private val isLoggedIn: Boolean
        get() = auth.currentUser != null
    
    private fun userDoc() = firestore.collection("users").document(userId)
    
    // Bills
    fun getBills(): Flow<List<Bill>> = callbackFlow {
        val listener = userDoc()
            .collection("bills")
            .orderBy("billDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bills = snapshot?.documents?.map { doc ->
                    Bill.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(bills)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun addBill(bill: Bill): String {
        val docRef = userDoc().collection("bills").add(bill.toMap()).await()
        return docRef.id
    }
    
    suspend fun updateBill(bill: Bill) {
        userDoc().collection("bills").document(bill.id).set(bill.toMap()).await()
    }
    
    suspend fun deleteBill(billId: String) {
        userDoc().collection("bills").document(billId).delete().await()
    }
    
    // Categories
    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val listener = userDoc()
            .collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.map { doc ->
                    Category.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun initDefaultCategories() {
        val batch = firestore.batch()
        val categoriesRef = userDoc().collection("categories")
        
        Category.getDefaultExpenseCategories().forEach { category ->
            batch.set(categoriesRef.document(), category.toMap())
        }
        Category.getDefaultIncomeCategories().forEach { category ->
            batch.set(categoriesRef.document(), category.toMap())
        }
        
        batch.commit().await()
    }
    
    suspend fun hasCategories(): Boolean {
        val snapshot = userDoc().collection("categories").limit(1).get().await()
        return !snapshot.isEmpty
    }
    
    suspend fun addCategory(category: Category): String {
        val docRef = userDoc().collection("categories").add(category.toMap()).await()
        return docRef.id
    }
    
    suspend fun updateCategory(category: Category) {
        userDoc().collection("categories").document(category.id).set(category.toMap()).await()
    }
    
    suspend fun deleteCategory(categoryId: String) {
        userDoc().collection("categories").document(categoryId).delete().await()
    }
    
    // Budgets
    fun getBudgets(): Flow<List<Budget>> = callbackFlow {
        val listener = userDoc()
            .collection("budgets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val budgets = snapshot?.documents?.map { doc ->
                    Budget.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(budgets)
            }
        awaitClose { listener.remove() }
    }
    
    // ==================== 周期性账单 ====================
    
    /**
     * 获取所有周期性账单
     */
    fun getRecurringBills(): Flow<List<com.bookkeeping.data.model.RecurringBill>> = callbackFlow {
        val listener = userDoc()
            .collection("recurring_bills")
            .orderBy("nextDueDate", Query.Direction.ASC)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.map { doc ->
                    com.bookkeeping.data.model.RecurringBill.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }
    
    /**
     * 添加周期性账单
     */
    suspend fun addRecurringBill(recurring: com.bookkeeping.data.model.RecurringBill): String {
        val docRef = userDoc().collection("recurring_bills").add(recurring.toMap()).await()
        return docRef.id
    }
    
    /**
     * 更新周期性账单
     */
    suspend fun updateRecurringBill(recurring: com.bookkeeping.data.model.RecurringBill) {
        userDoc().collection("recurring_bills").document(recurring.id).set(recurring.toMap()).await()
    }
    
    /**
     * 删除周期性账单
     */
    suspend fun deleteRecurringBill(recurringId: String) {
        userDoc().collection("recurring_bills").document(recurringId).delete().await()
    }
}
