package com.bookkeeping

import android.app.Application
import com.bookkeeping.data.local.AppDatabase
import com.bookkeeping.data.repository.BillRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * 应用程序入口
 * 负责初始化全局依赖
 */
class BookkeepingApp : Application() {
    
    // 懒加载数据库实例
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
    
    // 懒加载 Repository 实例
    val billRepository: BillRepository by lazy {
        BillRepository(
            billDao = database.billDao(),
            categoryDao = database.categoryDao()
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 Firebase
        FirebaseApp.initializeApp(this)
        
        // 启用 Firebase Firestore 离线持久化
        // 这允许应用在离线时仍能读取缓存数据
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)  // 启用离线缓存
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)  // 不限制缓存大小
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}
