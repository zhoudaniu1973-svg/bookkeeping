package com.bookkeeping.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bookkeeping.data.local.dao.BillDao
import com.bookkeeping.data.local.dao.CategoryDao
import com.bookkeeping.data.local.dao.RecurringBillDao
import com.bookkeeping.data.local.entity.BillEntity
import com.bookkeeping.data.local.entity.CategoryEntity
import com.bookkeeping.data.local.entity.RecurringBillEntity

/**
 * 应用本地数据库
 * 使用 Room 实现离线数据存储
 */
@Database(
    entities = [BillEntity::class, CategoryEntity::class, RecurringBillEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun billDao(): BillDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringBillDao(): RecurringBillDao
    
    companion object {
        private const val DATABASE_NAME = "bookkeeping_db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 获取数据库单例
         * 使用双重检查锁确保线程安全
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                // 数据库升级时的回退策略：销毁并重建
                // 生产环境建议使用迁移策略
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
