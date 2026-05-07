
Add a new instance of LoggerInterceptor to your okhttp client
use one of ext functions depend on where your in a activity or fragment or compose screen to show logger fab 

for adding logger fab to compose you can use DebugFloatingButton (composable) to MaterialTheme
replace content to {
content()
// 👇 Add debug FAB overlay only in debug mode
DebugFloatingButton()
}


a lightweight Chucker-like SDK
with Compose support
internal logger UI
public interception API
optional UI entrypoints


OkHttp Interceptor
↓
LoggerStore (singleton state holder)
↓
LoggerViewModel
↓
LoggerScreen
↓
LoggerActivity



You can later add:

export logs
share logs
websocket inspector
room persistence
network timeline
curl generation
request replay


_networkLogs.update { it + entry }

This becomes expensive.

Later optimize with:

persistent collections
ring buffer
max log count


PUBLIC API
────────────────────────
ComposeLogger
LoggerInterceptor
LoggerFab
LoggerActivity

INTERNAL
────────────────────────
LoggerGraph
LoggerStore
LoggerViewModel
LoggerScreen


No ViewModel.
No Hilt.
No DI.

This is VERY viable for logger SDKs.


Compose app:
OkHttpClient.Builder()
.addInterceptor(ComposeLogger.interceptor)

LoggerFab()


XML app:
OkHttpClient.Builder()
.addInterceptor(ComposeLogger.interceptor)

installLoggerOverlay()