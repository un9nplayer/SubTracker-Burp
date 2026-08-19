package com.agniops.subtracker.api

import com.google.gson.annotations.SerializedName

data class SubdomainEntry(
    @SerializedName("subdomain") val subdomain: String? = "",
    @SerializedName("ip") val ip: String? = "",
    @SerializedName("cloudflare") val cloudflare: Boolean = false
)

data class QuotaMeta(
    @SerializedName("quota_remaining") val quotaRemaining: Any? = "-",
    @SerializedName("daily_quota") val dailyQuota: Any? = "1000"
)

data class ScanResult(
    @SerializedName("domain") val domain: String? = "",
    @SerializedName("subdomains_count") val count: Int = 0,
    @SerializedName("subdomains") val subdomains: List<SubdomainEntry>? = emptyList(),
    @SerializedName("meta") val meta: QuotaMeta? = QuotaMeta()
)
