package com.bookkeeping.data.model

/**
 * 分类数据模型
 */
data class Category(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val color: String = "#4A90D9",
    val type: BillType = BillType.EXPENSE,
    val isDefault: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "icon" to icon,
        "color" to color,
        "type" to type.name,
        "isDefault" to isDefault
    )
    
    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Category {
            return Category(
                id = id,
                name = map["name"] as? String ?: "",
                icon = map["icon"] as? String ?: "",
                color = map["color"] as? String ?: "#4A90D9",
                type = try { 
                    BillType.valueOf(map["type"] as? String ?: "EXPENSE") 
                } catch (e: Exception) { 
                    BillType.EXPENSE 
                },
                isDefault = map["isDefault"] as? Boolean ?: false
            )
        }
        
        fun getDefaultExpenseCategories(): List<Category> = listOf(
            Category(name = "餐饮", icon = "🍔", color = "#FF6B6B", type = BillType.EXPENSE, isDefault = true),
            Category(name = "交通", icon = "🚗", color = "#4A90D9", type = BillType.EXPENSE, isDefault = true),
            Category(name = "购物", icon = "🛒", color = "#9B59B6", type = BillType.EXPENSE, isDefault = true),
            Category(name = "娱乐", icon = "🎮", color = "#E67E22", type = BillType.EXPENSE, isDefault = true),
            Category(name = "居住", icon = "🏠", color = "#27AE60", type = BillType.EXPENSE, isDefault = true),
            Category(name = "通讯", icon = "📱", color = "#3498DB", type = BillType.EXPENSE, isDefault = true),
            Category(name = "医疗", icon = "💊", color = "#E74C3C", type = BillType.EXPENSE, isDefault = true),
            Category(name = "教育", icon = "📚", color = "#1ABC9C", type = BillType.EXPENSE, isDefault = true),
            Category(name = "其他", icon = "📦", color = "#95A5A6", type = BillType.EXPENSE, isDefault = true)
        )
        
        fun getDefaultIncomeCategories(): List<Category> = listOf(
            Category(name = "工资", icon = "💵", color = "#52C41A", type = BillType.INCOME, isDefault = true),
            Category(name = "奖金", icon = "🎁", color = "#FAAD14", type = BillType.INCOME, isDefault = true),
            Category(name = "投资", icon = "📈", color = "#722ED1", type = BillType.INCOME, isDefault = true),
            Category(name = "兼职", icon = "💼", color = "#13C2C2", type = BillType.INCOME, isDefault = true),
            Category(name = "其他", icon = "💰", color = "#52C41A", type = BillType.INCOME, isDefault = true)
        )
    }
}
