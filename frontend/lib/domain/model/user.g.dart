// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

User _$UserFromJson(Map<String, dynamic> json) => User(
      json['id'] as String,
      json['account'] as String,
      const DateTimeConverter().fromJson(json['created_at'] as String),
      json['name'] as String?,
      json['given_name'] as String?,
      json['family_name'] as String?,
      json['middle_name'] as String?,
      json['nickname'] as String?,
      json['preferred_username'] as String?,
      json['profile'] as String?,
      json['picture'] as String?,
      json['website'] as String?,
      json['email'] as String?,
      json['email_verified'] as bool?,
      json['gender'] as String?,
      _$JsonConverterFromJson<String, DateTime>(
          json['birthdate'], const DateTimeConverter().fromJson),
      json['zoneinfo'] as String?,
      json['locale'] as String?,
      json['phone_number'] as String?,
      json['phone_number_verified'] as bool?,
      json['address'] as String?,
      const DateTimeConverter().fromJson(json['updated_at'] as String),
      (json['role'] as List<dynamic>?)?.map((e) => e as String).toList(),
      json['user_data'] as Map<String, dynamic>?,
      json['server_data'] as Map<String, dynamic>?,
    );

Map<String, dynamic> _$UserToJson(User instance) => <String, dynamic>{
      'id': instance.id,
      'account': instance.account,
      'created_at': const DateTimeConverter().toJson(instance.createdAt),
      'name': instance.name,
      'given_name': instance.givenName,
      'family_name': instance.familyName,
      'middle_name': instance.middleName,
      'nickname': instance.nickname,
      'preferred_username': instance.preferredUsername,
      'profile': instance.profile,
      'picture': instance.picture,
      'website': instance.website,
      'email': instance.email,
      'email_verified': instance.emailVerified,
      'gender': instance.gender,
      'birthdate': _$JsonConverterToJson<String, DateTime>(
          instance.birthdate, const DateTimeConverter().toJson),
      'zoneinfo': instance.zoneInfo,
      'locale': instance.locale,
      'phone_number': instance.phoneNumber,
      'phone_number_verified': instance.phoneNumberVerified,
      'address': instance.address,
      'updated_at': const DateTimeConverter().toJson(instance.updatedAt),
      'role': instance.role,
      'user_data': instance.userData,
      'server_data': instance.serverData,
    };

Value? _$JsonConverterFromJson<Json, Value>(
  Object? json,
  Value? Function(Json json) fromJson,
) =>
    json == null ? null : fromJson(json as Json);

Json? _$JsonConverterToJson<Json, Value>(
  Value? value,
  Json? Function(Value value) toJson,
) =>
    value == null ? null : toJson(value);
