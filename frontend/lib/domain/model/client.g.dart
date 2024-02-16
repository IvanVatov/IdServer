// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'client.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Client _$ClientFromJson(Map<String, dynamic> json) => Client(
      json['tenantId'] as int,
      json['clientId'] as String,
      json['application'] as String,
      json['clientSecret'] as String,
      ClientSettings.fromJson(json['settings'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$ClientToJson(Client instance) => <String, dynamic>{
      'tenantId': instance.tenantId,
      'clientId': instance.clientId,
      'application': instance.application,
      'clientSecret': instance.clientSecret,
      'settings': instance.settings,
    };
