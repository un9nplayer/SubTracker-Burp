# AgniOps SubTracker — Burp Suite Extension (Kotlin & Montoya API)

[![Burp Suite Montoya API](https://img.shields.io/badge/Burp%20Suite-Montoya%20API-orange.svg)](https://portswigger.net/burp/documentation/desktop/extensions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**AgniOps SubTracker** brings active subdomain discovery directly inside **Burp Suite** (Community & Professional) using modern **Kotlin** and PortSwigger's **Montoya API**.

> 💻 **CLI Version Available**: Prefer terminal tools? Check out the **SubTracker CLI tool** on GitHub: [github.com/un9nplayer/SubTracker](https://github.com/un9nplayer/SubTracker).

---

## 🚀 Features

- 🌐 **Native Burp Suite Tab**: Custom dark-themed `SubTracker` UI tab integrated into Burp Suite's main navigation.
- 🎯 **Context Menu Integration**: Right-click any request or host in **Proxy HTTP history**, **Site map**, or **Repeater** → **"Send host to SubTracker"**.
- ⚡ **Burp-Mediated HTTP Client**: Outbound requests route through Burp's native HTTP engine (`MontoyaApi.http().sendRequest()`), automatically respecting your upstream proxy and TLS settings.
- 🔄 **Non-Blocking Async Execution**: Active scans run on background worker threads to keep Burp's UI fluid and responsive.
- ☁ **Cloudflare Detection**: Highlights and badges subdomains protected by Cloudflare reverse proxies.
- 🔒 **Persistent Settings**: API Key and Root Domain extraction preferences persist across Burp sessions via Montoya's native `Preferences` API.
- 📊 **Real-Time Quota Display**: Live daily API search quota monitoring.
- 📤 **Multi-Format Export**: Native file dialog export to **JSON**, **CSV** (RFC 4180 formula-injection safe), or **TXT**.

---

## 🔍 Active API Enumeration vs. Passive Discovery

Unlike existing passive extensions (e.g. *Asset Discovery* or *Subdomain Extractor*) which extract assets already observed in your session traffic, **AgniOps SubTracker performs active external API enumeration**. It queries the AgniOps Intelligence Node to surface hidden target subdomains before you ever visit them.

---

## 📦 1-Click Installation Guide

### Prerequisites
- **Burp Suite**: Community or Professional Edition (2024.x or later).
- **Java Runtime**: Java 21 or later (Burp Suite's bundled JRE works out of the box).
- **AgniOps API Key**: Get your API key at [app.agniops.in](https://app.agniops.in).

### How to Load
1. Download `AgniOps-SubTracker-1.0.0.jar` from the [Releases](https://github.com/un9nplayer/SubTracker-Burp/releases) or build it locally.
2. Open **Burp Suite**.
3. Go to the **Extensions** tab → **Installed** sub-tab.
4. Click **Add**.
5. Set **Extension type**: `Java`.
6. Click **Select file...** and pick `AgniOps-SubTracker-1.0.0.jar`.
7. Click **Next** → **Close**.
8. A new tab named **`SubTracker`** will appear in Burp's top navigation bar!

---

## 🛠️ Building from Source

```bash
# Clone the repository
git clone https://github.com/un9nplayer/SubTracker-Burp.git
cd SubTracker-Burp

# Build the Fat JAR with Gradle
./gradlew shadowJar

# Compiled standalone JAR will be located at:
# build/libs/AgniOps-SubTracker-1.0.0.jar
```

---

## 🔑 Usage

### 1. Initial Setup
1. Open the **SubTracker** tab in Burp.
2. Enter your AgniOps API Key (`at_live_...`).
3. Click **Save Key**. (Saved persistently across Burp restarts).

### 2. Manual Subdomain Discovery
1. Type a domain (e.g. `example.com`).
2. Toggle **Root domain mode** ON/OFF as preferred (extracts `example.com` from `open.example.com`).
3. Click **Scan Subdomains**.

### 3. Context Menu Shortcut
1. In Proxy history or Site map, right-click any request.
2. Select **"Send 'target.com' to SubTracker"**.
3. Switch to the `SubTracker` tab — domain will auto-populate ready for scanning!

---

## 🔒 Security & Privacy Disclosures

- **Target Domain Transmission**: Only the target domain string is sent to `https://app.agniops.in/api/v1/subdomains/scan`.
- **API Key Security**: Sent strictly via `X-API-Key` header. API keys are excluded from Burp logging streams.
- **No Interception**: SubTracker does not inspect, read, or transmit HTTP request/response bodies, session tokens, or non-target proxy traffic.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
