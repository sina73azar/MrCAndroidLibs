# Network Proxy Core

Android library foundation for proxying selected OkHttp and Retrofit clients through a local VLESS-backed tunnel.

This module currently provides:

- VLESS URI parsing.
- sing-box compatible JSON config generation.
- A singleton proxy lifecycle API.
- A libbox-backed sing-box engine.
- A pluggable `ProxyEngine` boundary for alternative engines.

The default engine uses `net.clever-vpn:libbox-android`.
