package org.example.project.data.chat

expect object ChatReadStateStorage {
    fun initialize(platformContext: Any? = null)
    fun load(userId: Int): Map<Int, Int>
    fun save(userId: Int, readStateByConversationId: Map<Int, Int>)
    fun clear(userId: Int)
}
