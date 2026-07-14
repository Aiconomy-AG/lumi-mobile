package org.example.project.data.calls

expect object ClientInstanceIdStorage {
    fun initialize(platformContext: Any? = null)
    fun getOrCreate(): String
}
