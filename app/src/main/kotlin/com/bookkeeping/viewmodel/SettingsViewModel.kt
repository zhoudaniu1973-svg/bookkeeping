package com.bookkeeping.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookkeeping.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class SettingsViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                // 1. Fetch all bills
                val bills = repository.getBills().first()
                
                if (bills.isEmpty()) {
                    // TODO: Show toast "No data to export"
                    return@launch
                }

                // 2. Build CSV content
                val sb = StringBuilder()
                // UTF-8 BOM for Excel compatibility
                sb.append('\uFEFF')
                sb.append("日期,类型,分类,金额,备注\n")
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                
                bills.forEach { bill ->
                    val date = dateFormat.format(bill.billDate)
                    val type = if (bill.type == com.bookkeeping.data.model.BillType.EXPENSE) "支出" else "收入"
                    // Escape special characters if needed, for simple CSV usually just quotes if comma exists
                    val note = bill.note.replace(",", "，").replace("\n", " ")
                    
                    sb.append("$date,$type,${bill.categoryName},${bill.amount},$note\n")
                }

                // 3. Write to file
                val fileName = "bookkeeping_backup_${System.currentTimeMillis()}.csv"
                val file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { 
                    it.write(sb.toString().toByteArray())
                }

                // 4. Share file
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "导出账单数据")
                // Need FLAG_ACTIVITY_NEW_TASK because we might use Application Context in some cases, 
                // but usually this is called from Activity context passed in. 
                // However, safe to check context type or just start it.
                // Since we pass 'context' from UI composable, it is usually Activity context.
                context.startActivity(chooser)

            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Handle error state
            }
        }
    }
}
