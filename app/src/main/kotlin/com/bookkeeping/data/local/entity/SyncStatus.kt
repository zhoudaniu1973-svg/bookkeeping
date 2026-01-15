package com.bookkeeping.data.local.entity

/**
 * 同步状态枚举
 * 用于追踪本地数据与云端的同步状态
 */
enum class SyncStatus {
    SYNCED,           // 已同步到云端
    PENDING_UPLOAD,   // 待上传（新增或修改）
    PENDING_DELETE    // 待删除
}
