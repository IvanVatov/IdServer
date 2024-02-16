import 'package:frontend/domain/model/converter/date_time_converter.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rsa_key.g.dart';

@JsonSerializable()
class RsaKey {
  RsaKey(
    this.id,
    this.createdAt,
    this.publicKey,
  );

  String id;
  @DateTimeConverter()
  DateTime createdAt;
  String publicKey;

  factory RsaKey.fromJson(Map<String, dynamic> json) => _$RsaKeyFromJson(json);

  Map<String, dynamic> toJson() => _$RsaKeyToJson(
        this,
      );

  String formattedKey() {
    int chunkSize = 64;
    List<String> chunks = [];
    chunks.add('-----BEGIN PUBLIC KEY-----');
    chunks.addAll(Iterable.generate(
      (publicKey.length / chunkSize).ceil(),
      (i) {
        var end = (i + 1) * chunkSize;
        if (end > publicKey.length) {
          end = publicKey.length;
        }
        return publicKey.substring(i * chunkSize, end);
      },
    ));
    chunks.add('-----END PUBLIC KEY-----');
    return chunks.join('\n');
  }
}
