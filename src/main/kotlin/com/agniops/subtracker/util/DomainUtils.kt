package com.agniops.subtracker.util

object DomainUtils {

    private val VALID_HOSTNAME_RE = Regex(
        "^(?:[a-zA-Z0-9]" +
        "(?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+" +
        "[a-zA-Z]{2,}$"
    )

    private const val MAX_DOMAIN_LEN = 253

    private val MULTI_TLDS = setOf(
        "co.uk", "com.au", "net.au", "org.uk", "gov.uk", "ac.uk",
        "org.in", "co.in", "gen.in", "edu.in", "com.br", "co.jp", "com.tw", "co.nz"
    )

    fun sanitiseHost(rawHost: String?): String {
        if (rawHost.isNullOrBlank()) return ""
        var h = rawHost.trim().lowercase()
        if (h.startsWith("https://")) h = h.substring(8)
        if (h.startsWith("http://")) h = h.substring(7)
        if (h.contains(":") && !h.startsWith("[")) {
            h = h.split(":")[0]
        }
        return h.split("/")[0]
    }

    fun validateDomain(domain: String?): Pair<Boolean, String?> {
        if (domain.isNullOrBlank()) {
            return Pair(false, "Domain must not be empty.")
        }
        if (domain.length > MAX_DOMAIN_LEN) {
            return Pair(false, "Domain exceeds maximum length (253 chars).")
        }
        if (!VALID_HOSTNAME_RE.matches(domain)) {
            return Pair(false, "Domain contains invalid characters or format.")
        }
        return Pair(true, null)
    }

    fun extractRootDomain(hostname: String?): String {
        if (hostname.isNullOrBlank()) return ""
        var host = hostname.lowercase().replace("www.", "")
        val parts = host.split(".")
        if (parts.size <= 2) return host

        val lastTwo = "${parts[parts.size - 2]}.${parts[parts.size - 1]}"
        if (MULTI_TLDS.contains(lastTwo) && parts.size > 2) {
            return "${parts[parts.size - 3]}.$lastTwo"
        }
        return lastTwo
    }
}
