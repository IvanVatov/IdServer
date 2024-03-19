import 'package:json_annotation/json_annotation.dart';

import 'converter/date_time_converter.dart';

part 'user.g.dart';

@JsonSerializable()
class User {
  String id;
  String account;
  @JsonKey(name: "created_at")
  @DateTimeConverter()
  DateTime createdAt;
  String? name;
  @JsonKey(name: "given_name")
  String? givenName;
  @JsonKey(name: "family_name")
  String? familyName;
  @JsonKey(name: "middle_name")
  String? middleName;
  String? nickname;
  @JsonKey(name: "preferred_username")
  String? preferredUsername;
  String? profile;
  String? picture;
  String? website;
  String? email;
  @JsonKey(name: "email_verified")
  bool? emailVerified;
  String? gender;
  @DateTimeConverter()
  DateTime? birthdate;
  @JsonKey(name: "zoneinfo")
  String? zoneInfo;
  String? locale;
  @JsonKey(name: "phone_number")
  String? phoneNumber;
  @JsonKey(name: "phone_number_verified")
  bool? phoneNumberVerified;
  String? address;
  @JsonKey(name: "updated_at")
  @DateTimeConverter()
  DateTime updatedAt;
  List<String>? roles;
  @JsonKey(name: "user_data")
  Map<String, dynamic>? userData;
  @JsonKey(name: "server_data")
  Map<String, dynamic>? serverData;

  User(
      this.id,
      this.account,
      this.createdAt,
      this.name,
      this.givenName,
      this.familyName,
      this.middleName,
      this.nickname,
      this.preferredUsername,
      this.profile,
      this.picture,
      this.website,
      this.email,
      this.emailVerified,
      this.gender,
      this.birthdate,
      this.zoneInfo,
      this.locale,
      this.phoneNumber,
      this.phoneNumberVerified,
      this.address,
      this.updatedAt,
      this.roles,
      this.userData,
      this.serverData);

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);

  Map<String, dynamic> toJson() => _$UserToJson(this);
}
