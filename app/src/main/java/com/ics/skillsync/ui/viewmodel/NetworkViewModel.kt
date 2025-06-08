package com.ics.skillsync.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class NetworkViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun initialize(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewModelScope.launch {
                    Log.d("NetworkViewModel", "Conexión a internet disponible")
                    _isConnected.value = true
                }
            }

            override fun onLost(network: Network) {
                viewModelScope.launch {
                    Log.d("NetworkViewModel", "Conexión a internet perdida")
                    _isConnected.value = false
                }
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                viewModelScope.launch {
                    _isConnected.value = hasInternet && hasValidated
                    Log.d("NetworkViewModel", "Estado de conexión actualizado: ${_isConnected.value}")
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
            checkInitialConnection()
        } catch (e: Exception) {
            Log.e("NetworkViewModel", "Error al registrar el callback de red", e)
            _isConnected.value = false
        }
    }

    private fun checkInitialConnection() {
        try {
            val network = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            
            _isConnected.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
            
            Log.d("NetworkViewModel", "Estado inicial de conexión: ${_isConnected.value}")
        } catch (e: Exception) {
            Log.e("NetworkViewModel", "Error al verificar la conexión inicial", e)
            _isConnected.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.e("NetworkViewModel", "Error al desregistrar el callback de red", e)
        }
    }
} 