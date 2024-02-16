// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'valid_keys.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ValidKeys _$ValidKeysFromJson(Map<String, dynamic> json) => ValidKeys(
      RsaKey.fromJson(json['current'] as Map<String, dynamic>),
      (json['valid'] as List<dynamic>)
          .map((e) => RsaKey.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$ValidKeysToJson(ValidKeys instance) => <String, dynamic>{
      'current': instance.current,
      'valid': instance.valid,
    };
