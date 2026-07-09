import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlin.collections.ifEmpty

fun JsonElement?.asDouble(defaultValue: Double = 0.0): Double {
    val primitive = this as? JsonPrimitive ?: return defaultValue

    return primitive.doubleOrNull
        ?: primitive.contentOrNull?.toDoubleOrNull()
        ?: defaultValue
}

fun JsonElement?.asNullableDouble(): Double? {
    val primitive = this as? JsonPrimitive ?: return null

    return primitive.doubleOrNull
        ?: primitive.contentOrNull?.toDoubleOrNull()
}

fun JsonElement?.asInt(defaultValue: Int = 0): Int {
    val primitive = this as? JsonPrimitive ?: return defaultValue

    return primitive.intOrNull
        ?: primitive.contentOrNull?.toIntOrNull()
        ?: defaultValue
}

fun JsonElement?.asStringMap(): Map<String, String>? {
    val jsonObject = this as? JsonObject ?: return null

    return jsonObject
        .mapNotNull { entry ->
            val value = entry.value as? JsonPrimitive
            val content = value?.contentOrNull

            if (content == null) {
                null
            } else {
                entry.key to content
            }
        }
        .toMap()
        .ifEmpty { null }
}