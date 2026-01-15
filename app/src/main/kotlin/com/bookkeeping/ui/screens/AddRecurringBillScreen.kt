package com.bookkeeping.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.data.model.Frequency
import com.bookkeeping.ui.theme.ExpenseRed
import com.bookkeeping.ui.theme.IncomeGreen
import com.bookkeeping.viewmodel.RecurringBillViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 添加/编辑周期性账单界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringBillScreen(
    viewModel: RecurringBillViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.addState
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    
    // 错误提示
    LaunchedEffect(state.error) {
        state.error?.let {
            // Show error (could use Snackbar)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "编辑周期账单" else "添加周期账单") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save { onBack() } },
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 收支类型切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = state.billType == BillType.EXPENSE,
                    onClick = { viewModel.setBillType(BillType.EXPENSE) },
                    label = { Text("支出") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRed.copy(alpha = 0.2f)
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                FilterChip(
                    selected = state.billType == BillType.INCOME,
                    onClick = { viewModel.setBillType(BillType.INCOME) },
                    label = { Text("收入") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreen.copy(alpha = 0.2f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 金额输入
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.setAmount(it) },
                label = { Text("金额") },
                prefix = { Text("¥") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 周期选择
            Text("重复周期", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Frequency.entries.forEach { freq ->
                    FilterChip(
                        selected = state.frequency == freq,
                        onClick = { viewModel.setFrequency(freq) },
                        label = { 
                            Text(when(freq) {
                                Frequency.DAILY -> "每日"
                                Frequency.WEEKLY -> "每周"
                                Frequency.MONTHLY -> "每月"
                                Frequency.YEARLY -> "每年"
                            })
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 开始日期
            Text("开始日期", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { time = state.startDate }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val newDate = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }.time
                                viewModel.setStartDate(newDate)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(dateFormat.format(state.startDate))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 分类选择
            Text("选择分类", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            
            val categories = if (state.billType == BillType.EXPENSE) 
                state.expenseCategories else state.incomeCategories
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = state.selectedCategory?.id == category.id,
                        onClick = { viewModel.setCategory(category) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 备注
            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.setNote(it) },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 启用开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("立即启用")
                Switch(
                    checked = state.isActive,
                    onCheckedChange = { viewModel.setActive(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 保存按钮
            Button(
                onClick = { viewModel.save { onBack() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(category.icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}
