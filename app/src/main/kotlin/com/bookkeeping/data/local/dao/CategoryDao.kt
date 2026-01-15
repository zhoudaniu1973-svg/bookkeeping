package com.bookkeeping.data.local.dao

import androidx.room.*
import com.bookkeeping.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 分类数据访问对象
 */
@Dao
interface CategoryDao {
    
    /**
     * 获取指定用户的所有分类
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND syncStatus != 'PENDING_DELETE'")
    fun getAllCategories(userId: String): Flow<List<CategoryEntity>>
    
    /**
     * 根据ID获取分类
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
    
    /**
     * 插入分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    /**
     * 批量插入分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    /**
     * 更新分类
     */
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    /**
     * 标记为待删除
     */
    @Query("UPDATE categories SET syncStatus = 'PENDING_DELETE' WHERE id = :categoryId")
    suspend fun markAsDeleted(categoryId: String)
    
    /**
     * 根据ID删除分类
     */
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)
    
    /**
     * 获取待同步的分类
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND syncStatus = 'PENDING_UPLOAD'")
    suspend fun getPendingUploadCategories(userId: String): List<CategoryEntity>
    
    /**
     * 获取待删除的分类
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND syncStatus = 'PENDING_DELETE'")
    suspend fun getPendingDeleteCategories(userId: String): List<CategoryEntity>
    
    /**
     * 更新同步状态
     */
    @Query("UPDATE categories SET syncStatus = :status WHERE id = :categoryId")
    suspend fun updateSyncStatus(categoryId: String, status: String)
    
    /**
     * 检查用户是否有分类数据
     */
    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    suspend fun getCategoryCount(userId: String): Int
    
    /**
     * 清空用户分类
     */
    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllCategoriesForUser(userId: String)
}
