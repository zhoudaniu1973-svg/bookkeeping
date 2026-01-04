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
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookkeeping.data.model.BillType
import com.bookkeeping.ui.theme.*
import com.bookkeeping.viewmodel.CategoryStat
import com.bookkeeping.viewmodel.StatisticsViewModel
import com.bookkeeping.viewmodel.StatsPeriod

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
        // 统计专用 Header，支持月/年切换
        StatisticsHeader(
            statsPeriod = uiState.statsPeriod,
            currentYear = uiState.currentYear,
            currentMonth = uiState.currentMonth,
            income = uiState.totalIncome,
            expense = uiState.totalExpense,
            onPeriodChange = { viewModel.setStatsPeriod(it) },
            onPreviousPeriod = { viewModel.previousPeriod() },
            onNextPeriod = { viewModel.nextPeriod() }
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
                            .height(320.dp)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 饼图与中心数据叠加显示
                        PieChartWithCenter(
                            stats = uiState.categoryStats,
                            totalAmount = uiState.totalAmount,
                            isExpense = uiState.billType == BillType.EXPENSE,
                            modifier = Modifier.size(300.dp)
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
                    val emptyText = if (uiState.statsPeriod == StatsPeriod.MONTH) "本月暂无数据" else "本年暂无数据"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emptyText, color = TextTertiary)
                    }
                }
            }
        }
    }
}

/**
 * 统计页面专用 Header
 * 支持月/年周期切换
 */
@Composable
fun StatisticsHeader(
    statsPeriod: StatsPeriod,
    currentYear: Int,
    currentMonth: Int,
    income: Double,
    expense: Double,
    onPeriodChange: (StatsPeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        shape = androidx.compose.ui.graphics.RectangleShape
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 周期切换按钮（月/年）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(2.dp)
                ) {
                    TextButton(
                        onClick = { onPeriodChange(StatsPeriod.MONTH) },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (statsPeriod == StatsPeriod.MONTH) Color.White else Color.Transparent,
                            contentColor = if (statsPeriod == StatsPeriod.MONTH) Primary else Color.White
                        ),
                        modifier = Modifier.width(60.dp)
                    ) {
                        Text("月")
                    }
                    TextButton(
                        onClick = { onPeriodChange(StatsPeriod.YEAR) },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (statsPeriod == StatsPeriod.YEAR) Color.White else Color.Transparent,
                            contentColor = if (statsPeriod == StatsPeriod.YEAR) Primary else Color.White
                        ),
                        modifier = Modifier.width(60.dp)
                    ) {
                        Text("年")
                    }
                }
            }
            
            // 日期选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPreviousPeriod) {
                    Text("<", color = Color.White, fontSize = 18.sp)
                }
                Text(
                    text = if (statsPeriod == StatsPeriod.MONTH) 
                        "${currentYear}年${currentMonth}月" 
                    else 
                        "${currentYear}年",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onNextPeriod) {
                    Text(">", color = Color.White, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 收支汇总
            val periodLabel = if (statsPeriod == StatsPeriod.MONTH) "本月" else "本年"
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${periodLabel}支出", color = Color.White.copy(alpha = 0.8f))
                    Text("¥${String.format("%.2f", expense)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("${periodLabel}收入", color = Color.White.copy(alpha = 0.8f))
                    Text("¥${String.format("%.2f", income)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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

/**
 * 带中心数据展示的饼图组件
 * 在饼图中心显示总金额
 */
@Composable
fun PieChartWithCenter(
    stats: List<CategoryStat>,
    totalAmount: Double,
    isExpense: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 外层饼图
        PieChart(
            stats = stats,
            modifier = Modifier.fillMaxSize()
        )
        
        // 中心数据展示
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isExpense) "总支出" else "总收入",
                style = BodySmall,
                color = TextSecondary
            )
            Text(
                text = "¥${String.format("%.2f", totalAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) TextPrimary else IncomeGreen
            )
            // 显示分类数量
            Text(
                text = "${stats.size}个分类",
                style = BodySmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
fun PieChart(
    stats: List<CategoryStat>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        // 饼图半径缩小，为外围标签留出空间
        val radius = size.minDimension / 2 * 0.55f
        val strokeWidth = 28.dp.toPx()
        val center = Offset(canvasWidth / 2, canvasHeight / 2)
        
        // 引线起点在圆环外边缘
        val lineStartRadius = radius + strokeWidth / 2 + 4.dp.toPx()
        // 引线终点（标签位置）
        val labelRadius = radius + strokeWidth / 2 + 35.dp.toPx()
        
        // 用于绘制标签文字的 Paint
        val textPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#333333")
            textSize = 11.sp.toPx()
            isAntiAlias = true
        }
        
        // 用于绘制百分比的 Paint（加粗）
        val percentPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#666666")
            textSize = 10.sp.toPx()
            isAntiAlias = true
        }
        
        var startAngle = -90f
        
        stats.forEach { stat ->
            val sweepAngle = stat.percentage * 360f
            val color = try {
                 Color(android.graphics.Color.parseColor(stat.category.color))
            } catch (e: Exception) {
                Color.Gray
            }
            
            // 绘制扇形弧
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            // 所有分类都显示引线标签
            if (stat.percentage > 0) {
                val midAngle = startAngle + sweepAngle / 2
                val angleRad = Math.toRadians(midAngle.toDouble())
                val cos = kotlin.math.cos(angleRad).toFloat()
                val sin = kotlin.math.sin(angleRad).toFloat()
                
                // 引线起点（饼图边缘）
                val lineStartX = center.x + lineStartRadius * cos
                val lineStartY = center.y + lineStartRadius * sin
                
                // 引线中点（拐角）
                val lineMidX = center.x + labelRadius * cos
                val lineMidY = center.y + labelRadius * sin
                
                // 引线终点（水平延伸）
                val isRightSide = cos >= 0
                val lineEndX = if (isRightSide) lineMidX + 15.dp.toPx() else lineMidX - 15.dp.toPx()
                val lineEndY = lineMidY
                
                // 绘制引线（两段）
                drawLine(
                    color = color,
                    start = Offset(lineStartX, lineStartY),
                    end = Offset(lineMidX, lineMidY),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = color,
                    start = Offset(lineMidX, lineMidY),
                    end = Offset(lineEndX, lineEndY),
                    strokeWidth = 1.5.dp.toPx()
                )
                
                // 绘制小圆点
                drawCircle(
                    color = color,
                    radius = 3.dp.toPx(),
                    center = Offset(lineStartX, lineStartY)
                )
                
                // 设置文字对齐方向
                textPaint.textAlign = if (isRightSide) Paint.Align.LEFT else Paint.Align.RIGHT
                percentPaint.textAlign = if (isRightSide) Paint.Align.LEFT else Paint.Align.RIGHT
                
                // 标签文字位置
                val textX = if (isRightSide) lineEndX + 4.dp.toPx() else lineEndX - 4.dp.toPx()
                
                // 绘制分类名称
                drawContext.canvas.nativeCanvas.drawText(
                    stat.category.name,
                    textX,
                    lineEndY - 2.dp.toPx(),
                    textPaint
                )
                
                // 绘制百分比
                drawContext.canvas.nativeCanvas.drawText(
                    "${String.format("%.1f", stat.percentage * 100)}%",
                    textX,
                    lineEndY + percentPaint.textSize + 2.dp.toPx(),
                    percentPaint
                )
            }
            
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
                progress = { stat.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.Transparent, RoundedCornerShape(3.dp)),
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
