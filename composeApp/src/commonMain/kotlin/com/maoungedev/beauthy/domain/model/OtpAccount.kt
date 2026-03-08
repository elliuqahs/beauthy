package com.maoungedev.beauthy.domain.model

enum class OtpType {
    TOTP,
    HOTP
}

data class OtpAccount(
    val id: String,
    val issuer: String,
    val accountName: String,
    val secret: String,
    val type: OtpType = OtpType.TOTP,
    val digits: Int = 6,
    val period: Int = 30,
    val counter: Long = 0
) {
    override fun toString(): String =
        "OtpAccount(id=$id, issuer=$issuer, accountName=$accountName, secret=***, type=$type, digits=$digits, period=$period, counter=$counter)"
}
