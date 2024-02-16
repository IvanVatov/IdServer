import 'package:json_annotation/json_annotation.dart';

part 'tenant.g.dart';

@JsonSerializable()
class Tenant {
  Tenant(
      this.id,
      this.name,
      this.host,
      );

  int id;
  String name;
  String host;

  factory Tenant.fromJson(Map<String, dynamic> json) => _$TenantFromJson(json);

  Map<String, dynamic> toJson() => _$TenantToJson(
    this,
  );
}
