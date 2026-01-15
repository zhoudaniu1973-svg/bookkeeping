package com.bookkeeping.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.bookkeeping.`data`.local.dao.BillDao
import com.bookkeeping.`data`.local.dao.BillDao_Impl
import com.bookkeeping.`data`.local.dao.CategoryDao
import com.bookkeeping.`data`.local.dao.CategoryDao_Impl
import com.bookkeeping.`data`.local.dao.RecurringBillDao
import com.bookkeeping.`data`.local.dao.RecurringBillDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _billDao: Lazy<BillDao> = lazy {
    BillDao_Impl(this)
  }

  private val _categoryDao: Lazy<CategoryDao> = lazy {
    CategoryDao_Impl(this)
  }

  private val _recurringBillDao: Lazy<RecurringBillDao> = lazy {
    RecurringBillDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2,
        "48bd6b532281327e850d38da066fd284", "05678c3e4d763dfc8976b209f42e2ec0") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `bills` (`id` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `categoryIcon` TEXT NOT NULL, `categoryColor` TEXT NOT NULL, `note` TEXT NOT NULL, `billDate` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, `userId` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `color` TEXT NOT NULL, `type` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, `userId` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `recurring_bills` (`id` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `categoryIcon` TEXT NOT NULL, `categoryColor` TEXT NOT NULL, `note` TEXT NOT NULL, `frequency` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `nextDueDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, `userId` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '48bd6b532281327e850d38da066fd284')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `bills`")
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
        connection.execSQL("DROP TABLE IF EXISTS `recurring_bills`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsBills: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBills.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("amount", TableInfo.Column("amount", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("categoryId", TableInfo.Column("categoryId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("categoryName", TableInfo.Column("categoryName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("categoryIcon", TableInfo.Column("categoryIcon", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("categoryColor", TableInfo.Column("categoryColor", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("note", TableInfo.Column("note", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("billDate", TableInfo.Column("billDate", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("syncStatus", TableInfo.Column("syncStatus", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBills.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBills: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBills: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBills: TableInfo = TableInfo("bills", _columnsBills, _foreignKeysBills,
            _indicesBills)
        val _existingBills: TableInfo = read(connection, "bills")
        if (!_infoBills.equals(_existingBills)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |bills(com.bookkeeping.data.local.entity.BillEntity).
              | Expected:
              |""".trimMargin() + _infoBills + """
              |
              | Found:
              |""".trimMargin() + _existingBills)
        }
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("icon", TableInfo.Column("icon", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("color", TableInfo.Column("color", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("isDefault", TableInfo.Column("isDefault", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("syncStatus", TableInfo.Column("syncStatus", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories,
            _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.bookkeeping.data.local.entity.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        val _columnsRecurringBills: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRecurringBills.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("amount", TableInfo.Column("amount", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("categoryId", TableInfo.Column("categoryId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("categoryName", TableInfo.Column("categoryName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("categoryIcon", TableInfo.Column("categoryIcon", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("categoryColor", TableInfo.Column("categoryColor", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("note", TableInfo.Column("note", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("frequency", TableInfo.Column("frequency", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("startDate", TableInfo.Column("startDate", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("nextDueDate", TableInfo.Column("nextDueDate", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("syncStatus", TableInfo.Column("syncStatus", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRecurringBills: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRecurringBills: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoRecurringBills: TableInfo = TableInfo("recurring_bills", _columnsRecurringBills,
            _foreignKeysRecurringBills, _indicesRecurringBills)
        val _existingRecurringBills: TableInfo = read(connection, "recurring_bills")
        if (!_infoRecurringBills.equals(_existingRecurringBills)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |recurring_bills(com.bookkeeping.data.local.entity.RecurringBillEntity).
              | Expected:
              |""".trimMargin() + _infoRecurringBills + """
              |
              | Found:
              |""".trimMargin() + _existingRecurringBills)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "bills", "categories",
        "recurring_bills")
  }

  public override fun clearAllTables() {
    super.performClear(false, "bills", "categories", "recurring_bills")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(BillDao::class, BillDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CategoryDao::class, CategoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RecurringBillDao::class, RecurringBillDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun billDao(): BillDao = _billDao.value

  public override fun categoryDao(): CategoryDao = _categoryDao.value

  public override fun recurringBillDao(): RecurringBillDao = _recurringBillDao.value
}
