package com.bookkeeping.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.ui.theme.*
import com.bookkeeping.viewmodel.BillUiState
import com.bookkeeping.viewmodel.BillViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillScreen(
    viewModel: BillViewModel,
    isEditMode: Boolean,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState
    
    LaunchedEffect(uiState.error) {
        if (uiState.error == "账单未找到") {
             // Handle appropriately, maybe go back
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "编辑账单" else "记一笔") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                // Type Selector (Income/Expense)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    TabButton(
                        text = "支出",
                        selected = uiState.billType == BillType.EXPENSE,
                        onClick = { viewModel.setBillType(BillType.EXPENSE) },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "收入",
                        selected = uiState.billType == BillType.INCOME,
                        onClick = { viewModel.setBillType(BillType.INCOME) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Amount Input
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.setAmount(it) },
                    label = { Text("金额") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = AmountLarge
                )
                
                // Category Grid
                Text(
                    text = "分类",
                    style = TitleSmall,
                    modifier = Modifier.padding(16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    val categories = if (uiState.billType == BillType.EXPENSE) uiState.expenseCategories else uiState.incomeCategories
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            selected = uiState.selectedCategory?.id == category.id || (uiState.selectedCategory == null && category.isDefault && false), // Simplified selection logic
                            onClick = { viewModel.setCategory(category) }
                        )
                    }
                }
                
                // Note & Date
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = { viewModel.setNote(it) },
                        label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(uiState.billDate)}",
                        modifier = Modifier.clickable { 
                            // TODO: Show DatePicker
                        }
                    )
                }
                
                // Save Button
                Button(
                    onClick = { 
                        viewModel.saveBill { 
                            onBack() 
                        } 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(if (isEditMode) "更新" else "保存")
                }
                
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Primary else Color.Transparent,
            contentColor = if (selected) Color.White else TextSecondary
        ),
        elevation = null,
        modifier = modifier
    ) {
        Text(text)
    }
}

@Composable
fun CategoryItem(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = if (selected) Primary else Background,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(category.icon)
            }
        }
        Text(
            text = category.name,
            style = BodySmall,
            color = if (selected) Primary else TextSecondary
        )
    }
}
