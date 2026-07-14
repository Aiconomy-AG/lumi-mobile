package org.example.project.data.calls

import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID

actual object ClientInstanceIdStorage {
    private const val KEY_ID = "lumi_installation_id"

    actual fun initialize(platformContext: Any?) = Unit

    actual fun getOrCreate(): String {
        val defaults = NSUserDefaults.standardUserDefaults
        val existing = defaults.stringForKey(KEY_ID)
        if (existing != null) return "ios-$existing"
        val id = NSUUID().UUIDString()
        defaults.setObject(id, KEY_ID)
        return "ios-$id"
    }
}
