// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'valid_keys_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ValidKeysResponse _$ValidKeysResponseFromJson(Map<String, dynamic> json) =>
    ValidKeysResponse(
      json['tenantId'] as int,
      json['clientId'] as String,
      json['clientSecret'] as String,
    );

Map<String, dynamic> _$ValidKeysResponseToJson(ValidKeysResponse instance) =>
    <String, dynamic>{
      'tenantId': instance.tenantId,
      'clientId': instance.clientId,
      'clientSecret': instance.clientSecret,
    };
