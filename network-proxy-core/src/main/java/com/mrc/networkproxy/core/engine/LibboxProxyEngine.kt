package com.mrc.networkproxy.core.engine

import android.util.Log
import com.mrc.networkproxy.core.config.LocalProxyEndpoint
import com.mrc.networkproxy.core.config.ProxyStartRequest
import io.nekohasekai.libbox.CommandServer
import io.nekohasekai.libbox.CommandServerHandler
import io.nekohasekai.libbox.ConnectionOwner
import io.nekohasekai.libbox.InterfaceUpdateListener
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.LocalDNSTransport
import io.nekohasekai.libbox.NetworkInterfaceIterator
import io.nekohasekai.libbox.Notification
import io.nekohasekai.libbox.OverrideOptions
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.SetupOptions
import io.nekohasekai.libbox.StringIterator
import io.nekohasekai.libbox.SystemProxyStatus
import io.nekohasekai.libbox.TunOptions
import io.nekohasekai.libbox.WIFIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LibboxProxyEngine : ProxyEngine {

    override suspend fun start(request: ProxyStartRequest): RunningProxyEngine =
        withContext(Dispatchers.IO) {
            LibboxRuntime.setupIfNeeded(request)

            val handler = AndroidProxyCommandHandler()
            val platform = LocalProxyPlatformInterface()
            val commandServer = CommandServer(handler, platform)

            try {
                commandServer.start()
                commandServer.startOrReloadService(request.singBoxConfigJson, OverrideOptions())
            } catch (throwable: Throwable) {
                runCatching { commandServer.closeService() }
                runCatching { commandServer.close() }
                throw ProxyEngineStartException(
                    "Failed to start libbox proxy engine: ${throwable.message}",
                    throwable
                )
            }

            LibboxRunningProxyEngine(
                endpoint = request.endpoint,
                commandServer = commandServer
            )
        }
}

private object LibboxRuntime {

    private val mutex = Mutex()
    private var isSetup = false

    suspend fun setupIfNeeded(request: ProxyStartRequest) {
        if (isSetup) return

        mutex.withLock {
            if (isSetup) return

            val context = request.applicationContext
            val baseDir = context.filesDir.resolve("network-proxy").apply { mkdirs() }
            val workingDir = context.noBackupFilesDir.resolve("network-proxy").apply { mkdirs() }
            val tempDir = context.cacheDir.resolve("network-proxy").apply { mkdirs() }

            Libbox.setDefaultAppId(context.packageName)
            Libbox.setup(
                SetupOptions().apply {
                    basePath = baseDir.path
                    workingPath = workingDir.path
                    tempPath = tempDir.path
                    fixAndroidStack = true
                    logMaxLines = 1000
                    debug = false
                }
            )

            isSetup = true
        }
    }
}

private class LibboxRunningProxyEngine(
    override val endpoint: LocalProxyEndpoint,
    private val commandServer: CommandServer
) : RunningProxyEngine {

    override suspend fun stop() {
        withContext(Dispatchers.IO) {
            runCatching { commandServer.closeService() }
                .onFailure { Log.w(TAG, "closeService failed", it) }
            commandServer.close()
        }
    }

    private companion object {
        private const val TAG = "LibboxProxyEngine"
    }
}

private class AndroidProxyCommandHandler : CommandServerHandler {

    override fun getSystemProxyStatus(): SystemProxyStatus =
        SystemProxyStatus().apply {
            available = false
            enabled = false
        }

    override fun serviceReload() = Unit

    override fun serviceStop() = Unit

    override fun setSystemProxyEnabled(isEnabled: Boolean) = Unit

    override fun writeDebugMessage(message: String?) {
        Log.d(TAG, message.orEmpty())
    }

    private companion object {
        private const val TAG = "LibboxProxyEngine"
    }
}

private class LocalProxyPlatformInterface : PlatformInterface {

    override fun autoDetectInterfaceControl(fd: Int) = Unit

    override fun clearDNSCache() = Unit

    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener?) = Unit

    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String?,
        sourcePort: Int,
        destinationAddress: String?,
        destinationPort: Int
    ): ConnectionOwner? = null

    override fun getInterfaces(): NetworkInterfaceIterator = EmptyNetworkInterfaceIterator

    override fun includeAllNetworks(): Boolean = false

    override fun localDNSTransport(): LocalDNSTransport? = null

    override fun openTun(options: TunOptions?): Int {
        throw UnsupportedOperationException("TUN is not supported by network-proxy-core.")
    }

    override fun readWIFIState(): WIFIState? = null

    override fun sendNotification(notification: Notification?) = Unit

    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener?) = Unit

    override fun systemCertificates(): StringIterator = EmptyStringIterator

    override fun underNetworkExtension(): Boolean = false

    override fun usePlatformAutoDetectInterfaceControl(): Boolean = false

    override fun useProcFS(): Boolean = true
}

private object EmptyStringIterator : StringIterator {
    override fun hasNext(): Boolean = false

    override fun len(): Int = 0

    override fun next(): String = throw NoSuchElementException()
}

private object EmptyNetworkInterfaceIterator : NetworkInterfaceIterator {
    override fun hasNext(): Boolean = false

    override fun next(): io.nekohasekai.libbox.NetworkInterface = throw NoSuchElementException()
}
