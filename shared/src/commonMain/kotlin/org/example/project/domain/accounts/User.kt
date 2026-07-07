package org.example.project.data.accounts

import org.example.project.domain.accounts.AccountRole

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val password: String,
    val team: String,
    val role: AccountRole,
    val isOnline: Boolean
) {
    val initials: String
        get() = fullName
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
}
