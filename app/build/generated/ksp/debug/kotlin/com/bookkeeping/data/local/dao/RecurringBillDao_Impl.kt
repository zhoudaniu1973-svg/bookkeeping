package com.bookkeeping.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.bookkeeping.`data`.local.entity.RecurringBillEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
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
public class RecurringBillDao_Impl(
  __db: RoomDatabase,
) : RecurringBillDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRecurringBillEntity: EntityInsertAdapter<RecurringBillEntity>

  private val __updateAdapterOfRecurringBillEntity: EntityDeleteOrUpdateAdapter<RecurringBillEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfRecurringBillEntity = object : EntityInsertAdapter<RecurringBillEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `recurring_bills` (`id`,`amount`,`type`,`categoryId`,`categoryName`,`categoryIcon`,`categoryColor`,`note`,`frequency`,`startDate`,`nextDueDate`,`isActive`,`createdAt`,`syncStatus`,`userId`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RecurringBillEntity) {
        statement.bindText(1, entity.id)
        statement.bindDouble(2, entity.amount)
        statement.bindText(3, entity.type)
        statement.bindText(4, entity.categoryId)
        statement.bindText(5, entity.categoryName)
        statement.bindText(6, entity.categoryIcon)
        statement.bindText(7, entity.categoryColor)
        statement.bindText(8, entity.note)
        statement.bindText(9, entity.frequency)
        statement.bindLong(10, entity.startDate)
        statement.bindLong(11, entity.nextDueDate)
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(12, _tmp.toLong())
        statement.bindLong(13, entity.createdAt)
        statement.bindText(14, entity.syncStatus)
        statement.bindText(15, entity.userId)
      }
    }
    this.__updateAdapterOfRecurringBillEntity = object :
        EntityDeleteOrUpdateAdapter<RecurringBillEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `recurring_bills` SET `id` = ?,`amount` = ?,`type` = ?,`categoryId` = ?,`categoryName` = ?,`categoryIcon` = ?,`categoryColor` = ?,`note` = ?,`frequency` = ?,`startDate` = ?,`nextDueDate` = ?,`isActive` = ?,`createdAt` = ?,`syncStatus` = ?,`userId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: RecurringBillEntity) {
        statement.bindText(1, entity.id)
        statement.bindDouble(2, entity.amount)
        statement.bindText(3, entity.type)
        statement.bindText(4, entity.categoryId)
        statement.bindText(5, entity.categoryName)
        statement.bindText(6, entity.categoryIcon)
        statement.bindText(7, entity.categoryColor)
        statement.bindText(8, entity.note)
        statement.bindText(9, entity.frequency)
        statement.bindLong(10, entity.startDate)
        statement.bindLong(11, entity.nextDueDate)
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(12, _tmp.toLong())
        statement.bindLong(13, entity.createdAt)
        statement.bindText(14, entity.syncStatus)
        statement.bindText(15, entity.userId)
        statement.bindText(16, entity.id)
      }
    }
  }

  public override suspend fun insert(entity: RecurringBillEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfRecurringBillEntity.insert(_connection, entity)
  }

  public override suspend fun update(entity: RecurringBillEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfRecurringBillEntity.handle(_connection, entity)
  }

  public override fun getAllRecurringBills(userId: String): Flow<List<RecurringBillEntity>> {
    val _sql: String =
        "SELECT * FROM recurring_bills WHERE userId = ? AND syncStatus != 'PENDING_DELETE' ORDER BY nextDueDate ASC"
    return createFlow(__db, false, arrayOf("recurring_bills")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfCategoryIcon: Int = getColumnIndexOrThrow(_stmt, "categoryIcon")
        val _columnIndexOfCategoryColor: Int = getColumnIndexOrThrow(_stmt, "categoryColor")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfNextDueDate: Int = getColumnIndexOrThrow(_stmt, "nextDueDate")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<RecurringBillEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RecurringBillEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCategoryId: String
          _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpCategoryIcon: String
          _tmpCategoryIcon = _stmt.getText(_columnIndexOfCategoryIcon)
          val _tmpCategoryColor: String
          _tmpCategoryColor = _stmt.getText(_columnIndexOfCategoryColor)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpStartDate: Long
          _tmpStartDate = _stmt.getLong(_columnIndexOfStartDate)
          val _tmpNextDueDate: Long
          _tmpNextDueDate = _stmt.getLong(_columnIndexOfNextDueDate)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              RecurringBillEntity(_tmpId,_tmpAmount,_tmpType,_tmpCategoryId,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpNote,_tmpFrequency,_tmpStartDate,_tmpNextDueDate,_tmpIsActive,_tmpCreatedAt,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveRecurringBills(userId: String): List<RecurringBillEntity> {
    val _sql: String =
        "SELECT * FROM recurring_bills WHERE userId = ? AND isActive = 1 AND syncStatus != 'PENDING_DELETE'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfCategoryIcon: Int = getColumnIndexOrThrow(_stmt, "categoryIcon")
        val _columnIndexOfCategoryColor: Int = getColumnIndexOrThrow(_stmt, "categoryColor")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfNextDueDate: Int = getColumnIndexOrThrow(_stmt, "nextDueDate")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<RecurringBillEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RecurringBillEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCategoryId: String
          _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpCategoryIcon: String
          _tmpCategoryIcon = _stmt.getText(_columnIndexOfCategoryIcon)
          val _tmpCategoryColor: String
          _tmpCategoryColor = _stmt.getText(_columnIndexOfCategoryColor)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpStartDate: Long
          _tmpStartDate = _stmt.getLong(_columnIndexOfStartDate)
          val _tmpNextDueDate: Long
          _tmpNextDueDate = _stmt.getLong(_columnIndexOfNextDueDate)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              RecurringBillEntity(_tmpId,_tmpAmount,_tmpType,_tmpCategoryId,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpNote,_tmpFrequency,_tmpStartDate,_tmpNextDueDate,_tmpIsActive,_tmpCreatedAt,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getDueRecurringBills(userId: String, todayTimestamp: Long):
      List<RecurringBillEntity> {
    val _sql: String =
        "SELECT * FROM recurring_bills WHERE userId = ? AND isActive = 1 AND nextDueDate <= ? AND syncStatus != 'PENDING_DELETE'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, todayTimestamp)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfCategoryIcon: Int = getColumnIndexOrThrow(_stmt, "categoryIcon")
        val _columnIndexOfCategoryColor: Int = getColumnIndexOrThrow(_stmt, "categoryColor")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfNextDueDate: Int = getColumnIndexOrThrow(_stmt, "nextDueDate")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<RecurringBillEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RecurringBillEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCategoryId: String
          _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpCategoryIcon: String
          _tmpCategoryIcon = _stmt.getText(_columnIndexOfCategoryIcon)
          val _tmpCategoryColor: String
          _tmpCategoryColor = _stmt.getText(_columnIndexOfCategoryColor)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpStartDate: Long
          _tmpStartDate = _stmt.getLong(_columnIndexOfStartDate)
          val _tmpNextDueDate: Long
          _tmpNextDueDate = _stmt.getLong(_columnIndexOfNextDueDate)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              RecurringBillEntity(_tmpId,_tmpAmount,_tmpType,_tmpCategoryId,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpNote,_tmpFrequency,_tmpStartDate,_tmpNextDueDate,_tmpIsActive,_tmpCreatedAt,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): RecurringBillEntity? {
    val _sql: String = "SELECT * FROM recurring_bills WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfCategoryIcon: Int = getColumnIndexOrThrow(_stmt, "categoryIcon")
        val _columnIndexOfCategoryColor: Int = getColumnIndexOrThrow(_stmt, "categoryColor")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfNextDueDate: Int = getColumnIndexOrThrow(_stmt, "nextDueDate")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: RecurringBillEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCategoryId: String
          _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpCategoryIcon: String
          _tmpCategoryIcon = _stmt.getText(_columnIndexOfCategoryIcon)
          val _tmpCategoryColor: String
          _tmpCategoryColor = _stmt.getText(_columnIndexOfCategoryColor)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpStartDate: Long
          _tmpStartDate = _stmt.getLong(_columnIndexOfStartDate)
          val _tmpNextDueDate: Long
          _tmpNextDueDate = _stmt.getLong(_columnIndexOfNextDueDate)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _result =
              RecurringBillEntity(_tmpId,_tmpAmount,_tmpType,_tmpCategoryId,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpNote,_tmpFrequency,_tmpStartDate,_tmpNextDueDate,_tmpIsActive,_tmpCreatedAt,_tmpSyncStatus,_tmpUserId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateNextDueDate(id: String, nextDueDate: Long) {
    val _sql: String =
        "UPDATE recurring_bills SET nextDueDate = ?, syncStatus = 'PENDING_UPLOAD' WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, nextDueDate)
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setActive(id: String, isActive: Boolean) {
    val _sql: String =
        "UPDATE recurring_bills SET isActive = ?, syncStatus = 'PENDING_UPLOAD' WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (isActive) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsDeleted(id: String) {
    val _sql: String = "UPDATE recurring_bills SET syncStatus = 'PENDING_DELETE' WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String) {
    val _sql: String = "DELETE FROM recurring_bills WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllForUser(userId: String) {
    val _sql: String = "DELETE FROM recurring_bills WHERE userId = ?"
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
