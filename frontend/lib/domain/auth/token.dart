import 'package:frontend/domain/auth/auth_constants.dart';
import 'package:json_annotation/json_annotation.dart';

part 'token.g.dart';

@JsonSerializable()
class Token {
  Token(this.accessToken, this.tokenType, this.expiresIn, this.refreshToken,
      this.scope);

  @JsonKey(name: AuthConstants.keyAccessToken)
  String accessToken;
  @JsonKey(name: AuthConstants.keyTokenType)
  String tokenType;
  @JsonKey(name: AuthConstants.keyExpiresIn)
  int expiresIn;
  @JsonKey(name: AuthConstants.keyRefreshToken)
  String? refreshToken;
  @JsonKey(name: AuthConstants.keyScope)
  String? scope;


  @JsonKey(includeFromJson: false, includeToJson: false)
  final int _time = DateTime.now().millisecondsSinceEpoch;

  @JsonKey(includeFromJson: false, includeToJson: false)
  bool get isExpired => DateTime.now().millisecondsSinceEpoch > _time + (expiresIn * 1000) + 10000;

  factory Token.fromJson(Map<String, dynamic> json) => _$TokenFromJson(json);

  Map<String, dynamic> toJson() => _$TokenToJson(
        this,
      );
}
