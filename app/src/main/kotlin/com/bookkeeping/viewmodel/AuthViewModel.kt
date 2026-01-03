package com.bookkeeping.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookkeeping.data.repository.AuthRepository
import com.bookkeeping.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val firebaseRepository = FirebaseRepository()
    
    var currentUser by mutableStateOf<FirebaseUser?>(null)
        private set
        
    var isLoading by mutableStateOf(true)
        private set
        
    init {
        viewModelScope.launch {
            repository.authStateFlow().collect { user ->
                currentUser = user
                isLoading = false
                
                // 如果用户刚登录且没有分类数据，初始化默认分类
                if (user != null) {
                    try {
                        if (!firebaseRepository.hasCategories()) {
                            firebaseRepository.initDefaultCategories()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    fun register(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            repository.register(email, password)
                .onSuccess { 
                    onSuccess()
                }
                .onFailure { 
                    isLoading = false
                    onError(it.message ?: "注册失败") 
                }
        }
    }
    
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            repository.login(email, password)
                .onSuccess { 
                    onSuccess()
                }
                .onFailure { 
                    isLoading = false
                    onError(it.message ?: "登录失败") 
                }
        }
    }
    
    fun logout() {
        repository.logout()
    }
}
