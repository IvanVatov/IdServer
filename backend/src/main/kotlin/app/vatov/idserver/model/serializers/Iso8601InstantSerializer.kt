package app.vatov.idserver.model.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

val UTC_ZONE_ID: ZoneId = ZoneId.ofOffset("UTC", ZoneOffset.UTC)

fun String.iso8601ToInstant(): Instant {
    val temporalAccessor = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this)
    return Instant.from(temporalAccessor)
}

fun Instant.toIso8601(): String {
    return this.atZone(UTC_ZONE_ID)
        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Date::class)
object Iso8601InstantSerializer : KSerializer<Instant> {

    override fun deserialize(decoder: Decoder): Instant {
        return decoder.decodeString().iso8601ToInstant()
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toIso8601())
    }

    override val descriptor: SerialDescriptor = String.serializer().descriptor
}