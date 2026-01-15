package com.bookkeeping.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.bookkeeping.`data`.local.entity.CategoryEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CategoryDao_Impl(
  __db: RoomDatabase,
) : CategoryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCategoryEntity: EntityInsertAdapter<CategoryEntity>

  private val __updateAdapterOfCategoryEntity: EntityDeleteOrUpdateAdapter<CategoryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCategoryEntity = object : EntityInsertAdapter<CategoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `categories` (`id`,`name`,`icon`,`color`,`type`,`isDefault`,`syncStatus`,`userId`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.icon)
        statement.bindText(4, entity.color)
        statement.bindText(5, entity.type)
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.syncStatus)
        statement.bindText(8, entity.userId)
      }
    }
    this.__updateAdapterOfCategoryEntity = object : EntityDeleteOrUpdateAdapter<CategoryEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `categories` SET `id` = ?,`name` = ?,`icon` = ?,`color` = ?,`type` = ?,`isDefault` = ?,`syncStatus` = ?,`userId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.icon)
        statement.bindText(4, entity.color)
        statement.bindText(5, entity.type)
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.syncStatus)
        statement.bindText(8, entity.userId)
        statement.bindText(9, entity.id)
      }
    }
  }

  public override suspend fun insertCategory(category: CategoryEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCategoryEntity.insert(_connection, category)
  }

  public override suspend fun insertCategories(categories: List<CategoryEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCategoryEntity.insert(_connection, categories)
  }

  public override suspend fun updateCategory(category: CategoryEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfCategoryEntity.handle(_connection, category)
  }

  public override fun getAllCategories(userId: String): Flow<List<CategoryEntity>> {
    val _sql: String =
        "SELECT * FROM categories WHERE userId = ? AND syncStatus != 'PENDING_DELETE'"
    return createFlow(__db, false, arrayOf("categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIcon: Int = getColumnIndexOrThrow(_stmt, "icon")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIcon: String
          _tmpIcon = _stmt.getText(_columnIndexOfIcon)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              CategoryEntity(_tmpId,_tmpName,_tmpIcon,_tmpColor,_tmpType,_tmpIsDefault,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCategoryById(categoryId: String): CategoryEntity? {
    val _sql: String = "SELECT * FROM categories WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, categoryId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIcon: Int = getColumnIndexOrThrow(_stmt, "icon")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: CategoryEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIcon: String
          _tmpIcon = _stmt.getText(_columnIndexOfIcon)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _result =
              CategoryEntity(_tmpId,_tmpName,_tmpIcon,_tmpColor,_tmpType,_tmpIsDefault,_tmpSyncStatus,_tmpUserId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingUploadCategories(userId: String): List<CategoryEntity> {
    val _sql: String = "SELECT * FROM categories WHERE userId = ? AND syncStatus = 'PENDING_UPLOAD'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIcon: Int = getColumnIndexOrThrow(_stmt, "icon")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIcon: String
          _tmpIcon = _stmt.getText(_columnIndexOfIcon)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              CategoryEntity(_tmpId,_tmpName,_tmpIcon,_tmpColor,_tmpType,_tmpIsDefault,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingDeleteCategories(userId: String): List<CategoryEntity> {
    val _sql: String = "SELECT * FROM categories WHERE userId = ? AND syncStatus = 'PENDING_DELETE'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIcon: Int = getColumnIndexOrThrow(_stmt, "icon")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIcon: String
          _tmpIcon = _stmt.getText(_columnIndexOfIcon)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              CategoryEntity(_tmpId,_tmpName,_tmpIcon,_tmpColor,_tmpType,_tmpIsDefault,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCategoryCount(userId: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM categories WHERE userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsDeleted(categoryId: String) {
    val _sql: String = "UPDATE categories SET syncStatus = 'PENDING_DELETE' WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, categoryId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCategoryById(categoryId: String) {
    val _sql: String = "DELETE FROM categories WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, categoryId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateSyncStatus(categoryId: String, status: String) {
    val _sql: String = "UPDATE categories SET syncStatus = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindText(_argIndex, categoryId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllCategoriesForUser(userId: String) {
    val _sql: String = "DELETE FROM categories WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
