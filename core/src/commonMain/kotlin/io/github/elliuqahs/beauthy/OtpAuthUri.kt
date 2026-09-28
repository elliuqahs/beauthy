package io.github.elliuqahs.beauthy

/**
 * The kind of one-time password an [OtpAuthUri] describes.
 */
public enum class OtpType {
    /** Time-based, see [Totp]. */
    TOTP,

    /** Counter-based, see [Hotp]. */
    HOTP
}

/**
 * An account described by an `otpauth://` URI, the format carried by authenticator QR codes.
 *
 * Format reference: [Key Uri Format](https://github.com/google/google-authenticator/wiki/Key-Uri-Format).
 *
 * ```kotlin
 * val uri = OtpAuthUri.parse("otpauth://totp/GitHub:alice?secret=JBSWY3DPEHPK3PXP&issuer=GitHub")
 * val code = uri.toTotp().generate()
 *
 * val qrContent = OtpAuthUri(
 *     type = OtpType.TOTP,
 *     secret = Secret.generate(),
 *     accountName = "alice@example.com",
 *     issuer = "Example"
 * ).toUriString()
 * ```
 *
 * Per the format, neither [issuer] nor [accountName] may contain a colon.
 *
 * [toString] hides the secret so instances are safe to log.
 *
 * @property secret Base32-encoded shared secret, normalized to uppercase without spaces or padding
 * @property counter initial counter; only meaningful for [OtpType.HOTP]
 * @property period time step in seconds; only meaningful for [OtpType.TOTP]
 * @throws IllegalArgumentException if [secret] is not valid Base32, [issuer] or [accountName] contains a colon,
 *   or [digits], [period] or [counter] is out of range
 */
public class OtpAuthUri(
    public val type: OtpType,
    secret: String,
    public val accountName: String,
    public val issuer: String? = null,
    public val algorithm: HmacAlgorithm = HmacAlgorithm.SHA1,
    public val digits: Int = 6,
    public val period: Int = 30,
    public val counter: Long = 0
) {
    public val secret: String = secret.filterNot { it.isWhitespace() }.uppercase().trimEnd('=')

    init {
        require(Base32.isValid(this.secret)) { "secret is not valid Base32" }
        // The label format "Issuer:account" cannot represent a colon inside either part.
        require(':' !in accountName) { "accountName must not contain ':'" }
        require(issuer == null || ':' !in issuer) { "issuer must not contain ':'" }
        requireDigits(digits)
        require(period > 0) { "period must be positive, was $period" }
        require(counter >= 0) { "counter must not be negative, was $counter" }
    }

    /**
     * Creates a [Totp] for this account.
     *
     * @param clockOffsetMillis see [Totp.clockOffsetMillis]
     * @throws IllegalStateException if [type] is not [OtpType.TOTP]
     */
    public fun toTotp(clockOffsetMillis: Long = 0): Totp {
        check(type == OtpType.TOTP) { "Cannot create Totp from a $type URI" }
        return Totp(secret, algorithm, digits, period, clockOffsetMillis)
    }

    /**
     * Creates a [Hotp] for this account. Start generating from [counter].
     *
     * @throws IllegalStateException if [type] is not [OtpType.HOTP]
     */
    public fun toHotp(): Hotp {
        check(type == OtpType.HOTP) { "Cannot create Hotp from a $type URI" }
        return Hotp(secret, algorithm, digits)
    }

    /**
     * Formats this account as an `otpauth://` URI, suitable for encoding in a QR code.
     */
    public fun toUriString(): String {
        val label = if (issuer.isNullOrEmpty()) {
            percentEncode(accountName)
        } else {
            percentEncode(issuer) + ":" + percentEncode(accountName)
        }
        val params = buildList {
            add("secret" to secret)
            if (!issuer.isNullOrEmpty()) add("issuer" to issuer)
            add("algorithm" to algorithm.name)
            add("digits" to digits.toString())
            when (type) {
                OtpType.TOTP -> add("period" to period.toString())
                OtpType.HOTP -> add("counter" to counter.toString())
            }
        }
        val query = params.joinToString("&") { (key, value) -> "$key=${percentEncode(value)}" }
        return "otpauth://${type.name.lowercase()}/$label?$query"
    }

    override fun equals(other: Any?): Boolean =
        other is OtpAuthUri &&
            type == other.type &&
            secret == other.secret &&
            accountName == other.accountName &&
            issuer == other.issuer &&
            algorithm == other.algorithm &&
            digits == other.digits &&
            period == other.period &&
            counter == other.counter

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + accountName.hashCode()
        result = 31 * result + (issuer?.hashCode() ?: 0)
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + digits
        result = 31 * result + period
        result = 31 * result + counter.hashCode()
        return result
    }

    override fun toString(): String =
        "OtpAuthUri(type=$type, accountName=$accountName, issuer=$issuer, secret=***, " +
            "algorithm=$algorithm, digits=$digits, period=$period, counter=$counter)"

    public companion object {
        private const val SCHEME = "otpauth://"

        /**
         * Parses an `otpauth://totp/...` or `otpauth://hotp/...` URI.
         *
         * The issuer is taken from the `issuer` parameter, falling back to the label
         * prefix (`Issuer:account`). Missing `algorithm`, `digits`, `period` and `counter`
         * parameters take their defaults.
         *
         * @throws IllegalArgumentException if [uri] is not a valid `otpauth://` URI
         */
        public fun parse(uri: String): OtpAuthUri {
            val trimmed = uri.trim()
            require(trimmed.startsWith(SCHEME, ignoreCase = true)) { "URI must start with $SCHEME" }
            val rest = trimmed.substring(SCHEME.length)

            val slash = rest.indexOf('/')
            require(slash != -1) { "URI is missing a label" }
            val type = when (rest.substring(0, slash).lowercase()) {
                "totp" -> OtpType.TOTP
                "hotp" -> OtpType.HOTP
                else -> throw IllegalArgumentException("Unsupported OTP type: ${rest.substring(0, slash)}")
            }

            val afterType = rest.substring(slash + 1)
            val question = afterType.indexOf('?')
            require(question != -1) { "URI is missing query parameters" }
            val rawLabel = afterType.substring(0, question)
            val params = parseQuery(afterType.substring(question + 1))

            // The issuer prefix is separated by a literal or percent-encoded colon.
            val literalColon = rawLabel.indexOf(':')
            val encodedColon = rawLabel.indexOf("%3A", ignoreCase = true)
            val (labelIssuer, accountName) = when {
                encodedColon != -1 && (literalColon == -1 || encodedColon < literalColon) ->
                    percentDecode(rawLabel.substring(0, encodedColon)).trim() to
                        percentDecode(rawLabel.substring(encodedColon + 3)).trim()
                literalColon != -1 ->
                    percentDecode(rawLabel.substring(0, literalColon)).trim() to
                        percentDecode(rawLabel.substring(literalColon + 1)).trim()
                else -> null to percentDecode(rawLabel).trim()
            }

            val secret = params["secret"]
            require(!secret.isNullOrBlank()) { "URI is missing the secret parameter" }

            val algorithm = params["algorithm"]?.let { value ->
                HmacAlgorithm.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                    ?: throw IllegalArgumentException("Unsupported algorithm: $value")
            } ?: HmacAlgorithm.SHA1

            return OtpAuthUri(
                type = type,
                secret = secret,
                accountName = accountName,
                issuer = params["issuer"]?.takeIf { it.isNotBlank() } ?: labelIssuer?.takeIf { it.isNotEmpty() },
                algorithm = algorithm,
                digits = params.intParam("digits") ?: 6,
                period = params.intParam("period") ?: 30,
                counter = params["counter"]?.let {
                    it.toLongOrNull() ?: throw IllegalArgumentException("Invalid counter: $it")
                } ?: 0
            )
        }

        /**
         * Like [parse], but returns `null` instead of throwing for invalid input.
         */
        public fun parseOrNull(uri: String): OtpAuthUri? =
            try {
                parse(uri)
            } catch (_: IllegalArgumentException) {
                null
            }

        private fun Map<String, String>.intParam(name: String): Int? =
            this[name]?.let { it.toIntOrNull() ?: throw IllegalArgumentException("Invalid $name: $it") }

        private fun parseQuery(query: String): Map<String, String> =
            query.split('&')
                .filter { it.isNotEmpty() }
                .associate { param ->
                    val eq = param.indexOf('=')
                    if (eq == -1) {
                        percentDecode(param).lowercase() to ""
                    } else {
                        percentDecode(param.substring(0, eq)).lowercase() to percentDecode(param.substring(eq + 1))
                    }
                }
    }
}

private const val UNRESERVED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
private const val HEX = "0123456789ABCDEF"

private fun percentEncode(value: String): String {
    val sb = StringBuilder()
    for (byte in value.encodeToByteArray()) {
        val b = byte.toInt() and 0xFF
        if (b < 0x80 && b.toChar() in UNRESERVED) {
            sb.append(b.toChar())
        } else {
            sb.append('%').append(HEX[b shr 4]).append(HEX[b and 0x0F])
        }
    }
    return sb.toString()
}

/**
 * Decodes `%XX` escapes as UTF-8. Malformed escapes are kept literally.
 */
private fun percentDecode(value: String): String {
    if ('%' !in value) return value
    val sb = StringBuilder(value.length)
    val pending = ArrayList<Byte>()
    fun flush() {
        if (pending.isNotEmpty()) {
            sb.append(pending.toByteArray().decodeToString())
            pending.clear()
        }
    }
    var i = 0
    while (i < value.length) {
        val escaped = if (value[i] == '%' && i + 2 < value.length) {
            val high = HEX.indexOf(value[i + 1].uppercaseChar())
            val low = HEX.indexOf(value[i + 2].uppercaseChar())
            if (high != -1 && low != -1) (high shl 4) or low else null
        } else {
            null
        }
        if (escaped != null) {
            pending.add(escaped.toByte())
            i += 3
        } else {
            flush()
            sb.append(value[i])
            i++
        }
    }
    flush()
    return sb.toString()
}
