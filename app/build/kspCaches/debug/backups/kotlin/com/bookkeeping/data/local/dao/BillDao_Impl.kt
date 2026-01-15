package com.bookkeeping.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.bookkeeping.`data`.local.entity.BillEntity
import javax.`annotation`.processing.Generated
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
public class BillDao_Impl(
  __db: RoomDatabase,
) : BillDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBillEntity: EntityInsertAdapter<BillEntity>

  private val __deleteAdapterOfBillEntity: EntityDeleteOrUpdateAdapter<BillEntity>

  private val __updateAdapterOfBillEntity: EntityDeleteOrUpdateAdapter<BillEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfBillEntity = object : EntityInsertAdapter<BillEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `bills` (`id`,`amount`,`type`,`categoryId`,`categoryName`,`categoryIcon`,`categoryColor`,`note`,`billDate`,`createdAt`,`syncStatus`,`userId`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BillEntity) {
        statement.bindText(1, entity.id)
        statement.bindDouble(2, entity.amount)
        statement.bindText(3, entity.type)
        statement.bindText(4, entity.categoryId)
        statement.bindText(5, entity.categoryName)
        statement.bindText(6, entity.categoryIcon)
        statement.bindText(7, entity.categoryColor)
        statement.bindText(8, entity.note)
        statement.bindLong(9, entity.billDate)
        statement.bindLong(10, entity.createdAt)
        statement.bindText(11, entity.syncStatus)
        statement.bindText(12, entity.userId)
      }
    }
    this.__deleteAdapterOfBillEntity = object : EntityDeleteOrUpdateAdapter<BillEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `bills` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: BillEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfBillEntity = object : EntityDeleteOrUpdateAdapter<BillEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `bills` SET `id` = ?,`amount` = ?,`type` = ?,`categoryId` = ?,`categoryName` = ?,`categoryIcon` = ?,`categoryColor` = ?,`note` = ?,`billDate` = ?,`createdAt` = ?,`syncStatus` = ?,`userId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: BillEntity) {
        statement.bindText(1, entity.id)
        statement.bindDouble(2, entity.amount)
        statement.bindText(3, entity.type)
        statement.bindText(4, entity.categoryId)
        statement.bindText(5, entity.categoryName)
        statement.bindText(6, entity.categoryIcon)
        statement.bindText(7, entity.categoryColor)
        statement.bindText(8, entity.note)
        statement.bindLong(9, entity.billDate)
        statement.bindLong(10, entity.createdAt)
        statement.bindText(11, entity.syncStatus)
        statement.bindText(12, entity.userId)
        statement.bindText(13, entity.id)
      }
    }
  }

  public override suspend fun insertBill(bill: BillEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfBillEntity.insert(_connection, bill)
  }

  public override suspend fun insertBills(bills: List<BillEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfBillEntity.insert(_connection, bills)
  }

  public override suspend fun deleteBill(bill: BillEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfBillEntity.handle(_connection, bill)
  }

  public override suspend fun updateBill(bill: BillEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfBillEntity.handle(_connection, bill)
  }

  public override fun getAllBills(userId: String): Flow<List<BillEntity>> {
    val _sql: String =
        "SELECT * FROM bills WHERE userId = ? AND syncStatus != 'PENDING_DELETE' ORDER BY billDate DESC"
    return createFlow(__db, false, arrayOf("bills")) { _connection ->
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
        val _columnIndexOfBillDate: Int = getColumnIndexOrThrow(_stmt, "billDate")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<BillEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BillEntity
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
          val _tmpBillDate: Long
          _tmpBillDate = _stmt.getLong(_columnIndexOfBillDate)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              BillEntity(_tmpId,_tmpAmount,_tmpType,_tmpCategoryId,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpNote,_tmpBillDate,_tmpCreatedAt,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBillById(billId: String): BillEntity? {
    val _sql: String = "SELECT * FROM bills WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, billId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfCategoryIcon: Int = getColumnIndexOrThrow(_stmt, "categoryIcon")
        val _columnIndexOfCategoryColor: Int = getColumnIndexOrThrow(_stmt, "categoryColor")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfBillDate: Int = getColumnIndexOrThrow(_stmt, "billDate")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: BillEntity?
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
          val _tmpBillDate: Long
          _tmpBillDate = _stmt.getLong(_columnIndexOfBillDate)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _result =
              BillEntity(_tmpId,_tmpAmount,_tmpType,_tmpCategoryId,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpNote,_tmpBillDate,_tmpCreatedAt,_tmpSyncStatus,_tmpUserId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingUploadBills(userId: String): List<BillEntity> {
    val _sql: String = "SELECT * FROM bills WHERE userId = ? AND syncStatus = 'PENDING_UPLOAD'"
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
        val _columnIndexOfBillDate: Int = getColumnIndexOrThrow(_stmt, "billDate")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<BillEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BillEntity
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
          val _tmpBillDate: Long
          _tmpBillDate = _stmt.getLong(_columnIndexOfBillDate)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              BillEntity(_tmpId,_tmpAmount,_tmpType,_tmpCategoryId,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpNote,_tmpBillDate,_tmpCreatedAt,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingDeleteBills(userId: String): List<BillEntity> {
    val _sql: String = "SELECT * FROM bills WHERE userId = ? AND syncStatus = 'PENDING_DELETE'"
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
        val _columnIndexOfBillDate: Int = getColumnIndexOrThrow(_stmt, "billDate")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _result: MutableList<BillEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BillEntity
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
          val _tmpBillDate: Long
          _tmpBillDate = _stmt.getLong(_columnIndexOfBillDate)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          _item =
              BillEntity(_tmpId,_tmpAmount,_tmpType,_tmpCategoryId,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpNote,_tmpBillDate,_tmpCreatedAt,_tmpSyncStatus,_tmpUserId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBillById(billId: String) {
    val _sql: String = "DELETE FROM bills WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, billId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsDeleted(billId: String) {
    val _sql: String = "UPDATE bills SET syncStatus = 'PENDING_DELETE' WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, billId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateSyncStatus(billId: String, status: String) {
    val _sql: String = "UPDATE bills SET syncStatus = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindText(_argIndex, billId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllBillsForUser(userId: String) {
    val _sql: String = "DELETE FROM bills WHERE userId = ?"
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
