import 'package:frontend/domain/model/converter/date_time_converter.dart';
import 'package:frontend/domain/model/rsa_key.dart';
import 'package:json_annotation/json_annotation.dart';

part 'valid_keys.g.dart';

@JsonSerializable()
class ValidKeys {
  ValidKeys(
      this.current,
      this.valid,
      );

  RsaKey current;
  List<RsaKey> valid;


  factory ValidKeys.fromJson(Map<String, dynamic> json) => _$ValidKeysFromJson(json);

  Map<String, dynamic> toJson() => _$ValidKeysToJson(
    this,
  );
}
