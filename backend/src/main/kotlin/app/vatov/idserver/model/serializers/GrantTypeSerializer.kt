package app.vatov.idserver.model.serializers

import app.vatov.idserver.model.GrantType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = GrantType::class)
object GrantTypeSerializer {

    override fun deserialize(decoder: Decoder): GrantType {
        return GrantType.valueOf(decoder.decodeString().uppercase())
    }

    override fun serialize(encoder: Encoder, value: GrantType) {
        encoder.encodeString(value.name.lowercase())
    }

    override val descriptor: SerialDescriptor = String.serializer().descriptor
}