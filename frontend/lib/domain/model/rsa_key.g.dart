// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rsa_key.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RsaKey _$RsaKeyFromJson(Map<String, dynamic> json) => RsaKey(
      json['id'] as String,
      const DateTimeConverter().fromJson(json['createdAt'] as String),
      json['publicKey'] as String,
    );

Map<String, dynamic> _$RsaKeyToJson(RsaKey instance) => <String, dynamic>{
      'id': instance.id,
      'createdAt': const DateTimeConverter().toJson(instance.createdAt),
      'publicKey': instance.publicKey,
    };
