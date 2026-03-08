package com.maoungedev.beauthy.core.uri

import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.model.OtpType

object OtpAuthParser {

    fun parse(uri: String, id: String): OtpAccount? {
        val otpType: OtpType
        val withoutScheme: String

        when {
            uri.startsWith("otpauth://totp/") -> {
                otpType = OtpType.TOTP
                withoutScheme = uri.removePrefix("otpauth://totp/")
            }
            uri.startsWith("otpauth://hotp/") -> {
                otpType = OtpType.HOTP
                withoutScheme = uri.removePrefix("otpauth://hotp/")
            }
            else -> return null
        }

        val questionIndex = withoutScheme.indexOf('?')
        if (questionIndex == -1) return null

        val label = percentDecode(withoutScheme.take(questionIndex))
        val queryString = withoutScheme.substring(questionIndex + 1)
        val params = parseQueryParams(queryString)

        val secret = params["secret"]?.uppercase()?.replace(" ", "") ?: return null
        if (secret.isBlank()) return null

        val issuer = params["issuer"] ?: extractIssuerFromLabel(label)
        val accountName = extractAccountFromLabel(label)
        val digits = params["digits"]?.toIntOrNull() ?: 6
        val period = params["period"]?.toIntOrNull() ?: 30
        val counter = params["counter"]?.toLongOrNull() ?: 0

        return OtpAccount(
            id = id,
            issuer = issuer,
            accountName = accountName,
            secret = secret,
            type = otpType,
            digits = digits,
            period = period,
            counter = counter
        )
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        return query.split("&").mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) {
                percentDecode(parts[0]) to percentDecode(parts[1])
            } else null
        }.toMap()
    }

    private fun extractIssuerFromLabel(label: String): String {
        val colonIndex = label.indexOf(':')
        return if (colonIndex != -1) label.substring(0, colonIndex).trim()
        else label.trim()
    }

    private fun extractAccountFromLabel(label: String): String {
        val colonIndex = label.indexOf(':')
        return if (colonIndex != -1) label.substring(colonIndex + 1).trim()
        else label.trim()
    }

    private fun percentDecode(value: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < value.length) {
            if (value[i] == '%' && i + 2 < value.length) {
                val hex = value.substring(i + 1, i + 3)
                val code = hex.toIntOrNull(16)
                if (code != null) {
                    sb.append(code.toChar())
                    i += 3
                    continue
                }
            }
            sb.append(value[i])
            i++
        }
        return sb.toString()
    }
}
