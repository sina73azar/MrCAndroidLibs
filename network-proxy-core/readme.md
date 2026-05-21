# Network Proxy Core

Android library foundation for proxying selected OkHttp and Retrofit clients through a local VLESS-backed tunnel.

This module currently provides:

- VLESS URI parsing.
- sing-box compatible JSON config generation.
- A singleton proxy lifecycle API.
- A pluggable `ProxyEngine` boundary for sing-box, Xray, or a future Rust backend.

It does not yet bundle a native proxy engine. Calls to `NetworkProxy.start(...)` require a real `ProxyEngine` implementation.
