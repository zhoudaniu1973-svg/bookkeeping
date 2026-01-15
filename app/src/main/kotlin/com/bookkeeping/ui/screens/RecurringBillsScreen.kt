package com.bookkeeping.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.RecurringBill
import com.bookkeeping.ui.theme.Background
import com.bookkeeping.ui.theme.ExpenseRed
import com.bookkeeping.ui.theme.IncomeGreen
import com.bookkeeping.viewmodel.RecurringBillViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 周期性账单列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringBillsScreen(
    onBack: () -> Unit,
    onAddNew: () -> Unit,
    onEdit: (RecurringBill) -> Unit,
    viewModel: RecurringBillViewModel = viewModel()
) {
    val state by viewModel.listState
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("周期性账单") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.resetAddState()
                    onAddNew()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        },
        containerColor = Background
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.recurringBills.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无周期性账单", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击右下角按钮添加", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(state.recurringBills, key = { it.id }) { recurring ->
                    RecurringBillItem(
                        recurring = recurring,
                        onClick = { onEdit(recurring) },
                        onToggleActive = { viewModel.toggleActive(recurring) },
                        onDelete = { viewModel.delete(recurring.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecurringBillItem(
    recurring: RecurringBill,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (recurring.isActive) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Color(android.graphics.Color.parseColor(recurring.categoryColor)).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(recurring.categoryIcon, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = recurring.categoryName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    if (!recurring.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "已暂停",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${recurring.getFrequencyLabel()} · 下次: ${dateFormat.format(recurring.nextDueDate)}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                if (recurring.note.isNotEmpty()) {
                    Text(
                        text = recurring.note,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            
            // 金额
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (recurring.type == BillType.EXPENSE) "-" else "+"}¥${String.format("%.2f", recurring.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (recurring.type == BillType.EXPENSE) ExpenseRed else IncomeGreen
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 操作按钮
            Column {
                IconButton(
                    onClick = onToggleActive,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (recurring.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (recurring.isActive) "暂停" else "启用",
                        tint = if (recurring.isActive) Color.Gray else IncomeGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = ExpenseRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除确认") },
            text = { Text("确定要删除这个周期性账单吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("删除", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
