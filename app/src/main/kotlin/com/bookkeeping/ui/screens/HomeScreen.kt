package com.bookkeeping.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.data.model.Bill
import com.bookkeeping.data.model.BillType
import com.bookkeeping.ui.theme.*
import com.bookkeeping.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddBill: () -> Unit,
    onEditBill: (Bill) -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState
    
    // Bottom Navigation State
    var selectedItem by remember { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "明细") },
                    label = { Text("明细") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = "统计") },
                    label = { Text("统计") },
                    selected = selectedItem == 1,
                    onClick = { 
                        selectedItem = 1
                        onNavigateToStatistics()
                        // Reset back to 0 because Statistics is a separate screen in our nav graph currently
                        // Ideally we'd use nested navigation for bottom bar
                        selectedItem = 0 
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "分类") },
                    label = { Text("分类") },
                    selected = selectedItem == 2,
                    onClick = { 
                        selectedItem = 2
                        onNavigateToCategories()
                        selectedItem = 0
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "我的") },
                    label = { Text("我的") },
                    selected = selectedItem == 3,
                    onClick = { 
                        selectedItem = 3
                        onNavigateToSettings()
                        selectedItem = 0
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddBill) {
                Icon(Icons.Filled.Add, contentDescription = "记一笔")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)
            ) {
                // Header (Month Selector & Summary)
                HomeHeader(
                    currentYear = uiState.currentYear,
                    currentMonth = uiState.currentMonth,
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
                
                // Bill List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    uiState.groupedBills.forEach { (key, bills) ->
                        item {
                            DateHeader(key.first, key.second, key.third)
                        }
                        items(bills) { bill ->
                            BillItem(
                                bill = bill,
                                onClick = { onEditBill(bill) },
                                onDelete = { viewModel.deleteBill(bill.id) }
                            )
                        }
                    }
                    
                    if (uiState.bills.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("本月暂无账单", color = TextTertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader(
    currentYear: Int,
    currentMonth: Int,
    income: Double,
    expense: Double,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        shape = androidx.compose.ui.graphics.RectangleShape // Rectangular header
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPreviousMonth) {
                    Text("<", color = Color.White, fontSize = 18.sp)
                }
                Text(
                    text = "${currentYear}年${currentMonth}月",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onNextMonth) {
                    Text(">", color = Color.White, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Summary
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("本月支出", color = Color.White.copy(alpha = 0.8f))
                    Text("¥${String.format("%.2f", expense)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("本月收入", color = Color.White.copy(alpha = 0.8f))
                    Text("¥${String.format("%.2f", income)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DateHeader(year: Int, month: Int, day: Int) {
    val dateStr = "${month}月${day}日"
    Text(
        text = dateStr,
        style = BodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Background)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillItem(
    bill: Bill,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("删除账单") },
            text = { Text("确定要删除这条账单吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDialog = false
                    }
                ) {
                    Text("删除", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDialog = true }
            ),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Background,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(bill.categoryIcon.ifEmpty { "?" })
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(bill.categoryName, style = TitleSmall)
                if (bill.note.isNotEmpty()) {
                    Text(bill.note, style = BodySmall)
                }
            }
            
            Text(
                text = if (bill.type == BillType.EXPENSE) "-${String.format("%.2f", bill.amount)}" else "+${String.format("%.2f", bill.amount)}",
                color = if (bill.type == BillType.EXPENSE) TextPrimary else IncomeGreen,
                style = AmountMedium
            )
        }
    }
}
