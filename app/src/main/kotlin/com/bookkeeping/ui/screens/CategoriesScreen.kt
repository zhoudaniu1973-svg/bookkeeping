package com.bookkeeping.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookkeeping.data.model.BillType
import com.bookkeeping.data.model.Category
import com.bookkeeping.ui.theme.*
import com.bookkeeping.viewmodel.CategoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = viewModel()
) {
    val uiState by viewModel.uiState
    var selectedType by remember { mutableStateOf(BillType.EXPENSE) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) } // Null means adding new

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingCategory = null
                        showEditDialog = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Category")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            // Type Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "支出",
                    selected = selectedType == BillType.EXPENSE,
                    onClick = { selectedType = BillType.EXPENSE },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "收入",
                    selected = selectedType == BillType.INCOME,
                    onClick = { selectedType = BillType.INCOME },
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (uiState.isLoading) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val categories = if (selectedType == BillType.EXPENSE) uiState.expenseCategories else uiState.incomeCategories
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryListItem(
                            category = category,
                            onClick = {
                                editingCategory = category
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showEditDialog) {
        CategoryEditDialog(
            initialCategory = editingCategory,
            type = selectedType,
            onDismiss = { showEditDialog = false },
            onSave = { name, icon, color ->
                if (editingCategory == null) {
                    viewModel.addCategory(name, icon, color, selectedType) {
                        showEditDialog = false
                    }
                } else {
                    viewModel.updateCategory(editingCategory!!.copy(name = name, icon = icon, color = color)) {
                        showEditDialog = false
                    }
                }
            },
            onDelete = { categoryId ->
                viewModel.deleteCategory(categoryId)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun CategoryListItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Surface(
                shape = CircleShape,
                color = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { Primary },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(category.icon, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(category.name, style = BodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CategoryEditDialog(
    initialCategory: Category?,
    type: BillType,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(initialCategory?.icon ?: "🍔") }
    var selectedColor by remember { mutableStateOf(initialCategory?.color ?: "#4A90D9") }
    
    val icons = listOf("🍔", "🚗", "🛒", "🎮", "🏠", "📱", "💊", "📚", "📦", "💰", "💵", "🎁", "📈", "💼", "✈️", "🐶", "👶", "🚬", "🍷", "💪")
    val colors = listOf("#FF6B6B", "#4A90D9", "#9B59B6", "#E67E22", "#27AE60", "#3498DB", "#E74C3C", "#1ABC9C", "#95A5A6", "#52C41A", "#FAAD14", "#722ED1", "#13C2C2", "#F5222D", "#EB2F96")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Background)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (initialCategory == null) "新增分类" else "编辑分类",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Icon Preview
                Surface(
                    shape = CircleShape,
                    color = try { Color(android.graphics.Color.parseColor(selectedColor)) } catch (e: Exception) { Primary },
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(selectedIcon, style = MaterialTheme.typography.headlineMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("选择图标", style = BodySmall, modifier = Modifier.align(Alignment.Start))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 40.dp),
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icons) { icon ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selectedIcon == icon) Primary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icon)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("选择颜色", style = BodySmall, modifier = Modifier.align(Alignment.Start))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 40.dp),
                    modifier = Modifier.height(100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(try { Color(android.graphics.Color.parseColor(color)) } catch (e: Exception) { Color.Gray })
                                .clickable { selectedColor = color }
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    imageVector = Icons.Filled.Add, // Using Add as checkmark placeholder or just dot
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (initialCategory != null && !initialCategory.isDefault) {
                        TextButton(onClick = { onDelete(initialCategory.id) }) {
                            Text("删除", color = Error)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp)) // Spacer to keep Save button on right
                    }
                    
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        Button(
                            onClick = { onSave(name, selectedIcon, selectedColor) },
                            enabled = name.isNotBlank()
                        ) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}
