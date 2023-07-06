package com.example.todoapp.network.workers

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import javax.inject.Inject


class OnInternetConnectionWorker @Inject constructor(private val context: Context) {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallback

    fun runWork(doOnAvailable: () -> Unit) {
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                doOnAvailable()
            }
        }

        val networkRequest = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            networkCallback
        )
    }

    fun stopWork() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
