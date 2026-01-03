package com.bookkeeping.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    
    private val auth = FirebaseAuth.getInstance()
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
        
    val isLoggedIn: Boolean
        get() = currentUser != null
        
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { 
                Result.success(it) 
            } ?: Result.failure(Exception("注册失败：用户为空"))
        } catch (e: Exception) {
            Result.failure(parseAuthException(e))
        }
    }
    
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { 
                Result.success(it) 
            } ?: Result.failure(Exception("登录失败：用户为空"))
        } catch (e: Exception) {
            Result.failure(parseAuthException(e))
        }
    }
    
    fun logout() {
        auth.signOut()
    }
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(parseAuthException(e))
        }
    }
    
    private fun parseAuthException(e: Exception): Exception {
        val message = when {
            e.message?.contains("email address is badly formatted") == true -> "邮箱格式不正确"
            e.message?.contains("password is invalid") == true -> "密码错误"
            e.message?.contains("no user record") == true -> "该邮箱未注册"
            e.message?.contains("email address is already in use") == true -> "该邮箱已被注册"
            e.message?.contains("password should be at least 6 characters") == true -> "密码至少需要6位字符"
            e.message?.contains("network error") == true -> "网络连接失败，请检查网络"
            else -> e.message ?: "未知错误"
        }
        return Exception(message)
    }
}
