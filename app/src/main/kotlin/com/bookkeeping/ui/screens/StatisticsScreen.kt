package com.bookkeeping.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookkeeping.data.model.BillType
import com.bookkeeping.ui.theme.*
import com.bookkeeping.viewmodel.CategoryStat
import com.bookkeeping.viewmodel.StatisticsViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header (Month Selector)
        HomeHeader(
            currentYear = uiState.currentYear,
            currentMonth = uiState.currentMonth,
            income = 0.0, // Not needed here, or could show total
            expense = 0.0,
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() }
        )

        // Content
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Type Switcher & Total
                TypeSwitcherAndTotal(
                    selectedType = uiState.billType,
                    totalAmount = uiState.totalAmount,
                    onTypeSelected = { viewModel.setBillType(it) }
                )
                
                // Pie Chart
                if (uiState.categoryStats.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PieChart(
                            stats = uiState.categoryStats,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                    
                    // List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.categoryStats) { stat ->
                            CategoryStatItem(stat)
                        }
                    }
                } else {
                    // Empty State
                     Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                         Text("本月暂无数据", color = TextTertiary)
                    }
                }
            }
        }
    }
}

@Composable
fun TypeSwitcherAndTotal(
    selectedType: BillType,
    totalAmount: Double,
    onTypeSelected: (BillType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(2.dp)
        ) {
            TabButton(
                text = "支出",
                selected = selectedType == BillType.EXPENSE,
                onClick = { onTypeSelected(BillType.EXPENSE) },
                modifier = Modifier.width(100.dp)
            )
            TabButton(
                text = "收入",
                selected = selectedType == BillType.INCOME,
                onClick = { onTypeSelected(BillType.INCOME) },
                modifier = Modifier.width(100.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = if (selectedType == BillType.EXPENSE) "共支出" else "共收入", style = BodySmall, color = TextSecondary)
        Text(
            text = "¥${String.format("%.2f", totalAmount)}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (selectedType == BillType.EXPENSE) TextPrimary else IncomeGreen
        )
    }
}

@Composable
fun PieChart(
    stats: List<CategoryStat>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val strokeWidth = 30.dp.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        
        var startAngle = -90f
        
        stats.forEach { stat ->
            val sweepAngle = stat.percentage * 360f
            val color = try {
                 Color(android.graphics.Color.parseColor(stat.category.color))
            } catch (e: Exception) {
                Color.Gray
            }
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false, // Ring style
                style = Stroke(width = strokeWidth),
                topLeft = Offset(center.x - radius + strokeWidth/2, center.y - radius + strokeWidth/2),
                size = Size((radius - strokeWidth/2) * 2, (radius - strokeWidth/2) * 2)
            )
            
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryStatItem(stat: CategoryStat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
         Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = Color(android.graphics.Color.parseColor(stat.category.color)).copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stat.category.icon)
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Name & Bar
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stat.category.name, style = BodyMedium)
                Text("${String.format("%.1f", stat.percentage * 100)}%", style = BodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Progress Bar
            LinearProgressIndicator(
                progress = stat.percentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.Transparent, RoundedCornerShape(3.dp)), // Clip rounds the indicator
                color = try { Color(android.graphics.Color.parseColor(stat.category.color)) } catch (e:Exception) { Primary },
                trackColor = Background
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Amount
        Text(
            text = "¥${String.format("%.2f", stat.amount)}",
            style = BodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
