package com.bookkeeping.data.repository

import com.bookkeeping.data.model.Bill
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.data.model.Budget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirebaseRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("用户未登录")
    
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
}
