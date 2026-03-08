package com.maoungedev.beauthy.data.dto

import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.model.OtpType
import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val id: String,
    val issuer: String,
    val accountName: String,
    val secret: String,
    val type: String = "totp",
    val digits: Int = 6,
    val period: Int = 30,
    val counter: Long = 0
) {
    fun toDomain(): OtpAccount = OtpAccount(
        id = id,
        issuer = issuer,
        accountName = accountName,
        secret = secret,
        type = if (type == "hotp") OtpType.HOTP else OtpType.TOTP,
        digits = digits,
        period = period,
        counter = counter
    )

    companion object {
        fun fromDomain(account: OtpAccount): AccountDto = AccountDto(
            id = account.id,
            issuer = account.issuer,
            accountName = account.accountName,
            secret = account.secret,
            type = if (account.type == OtpType.HOTP) "hotp" else "totp",
            digits = account.digits,
            period = account.period,
            counter = account.counter
        )
    }
}
