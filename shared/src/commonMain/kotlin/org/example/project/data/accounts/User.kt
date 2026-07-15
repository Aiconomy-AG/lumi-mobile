package org.example.project.data.accounts

import org.example.project.domain.accounts.AccountRole

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: AccountRole,
    val status: String,
    val phoneNumber: String,
    val languageFlag: String,
    val isActive: Boolean,
    val photoUrl: String? = null,
) {
    val initials: String
        get() = name
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
}
