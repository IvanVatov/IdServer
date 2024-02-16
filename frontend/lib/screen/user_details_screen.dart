import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:frontend/domain/api_service_user.dart';
import 'package:frontend/ext/snack_bar_state_error.dart';
import 'package:frontend/ext/snack_bar_state_message.dart';
import 'package:frontend/ext/state_mounted_state.dart';
import 'package:frontend/screen/protected_screen_state.dart';
import 'package:frontend/ui/panel_nav_rail.dart';
import 'package:frontend/util/pretty_json.dart';
import 'package:intl/intl.dart';

import '../domain/model/user.dart';
import '../ui/profile_action_button.dart';
import '../ui/text_styles.dart';

class UsersDetailsScreen extends StatefulWidget {
  final int tenantId;
  final String userId;

  const UsersDetailsScreen({super.key, required this.tenantId, required this.userId});

  @override
  State<UsersDetailsScreen> createState() => _UsersDetailsScreenState();
}

class _UsersDetailsScreenState extends ProtectedScreenState<UsersDetailsScreen> {
  _UsersDetailsScreenState();

  User? _user;

  bool _isLoading = false;
  bool _isUpdating = false;

  final String _emptyJson = '{}';

  late final int _tenantId;
  late final String _userId;

  late TextEditingController _idController;
  late TextEditingController _accountController;
  late TextEditingController _createdAtController;
  late TextEditingController _nameController;
  late TextEditingController _givenNameController;
  late TextEditingController _familyNameController;
  late TextEditingController _middleNameController;
  late TextEditingController _nicknameController;
  late TextEditingController _preferredUsernameController;
  late TextEditingController _profileController;
  late TextEditingController _pictureController;
  late TextEditingController _websiteController;
  late TextEditingController _emailController;
  late TextEditingController _emailVerifiedController;
  late TextEditingController _genderController;
  late TextEditingController _birthdateController;
  late TextEditingController _zoneInfoController;
  late TextEditingController _localeController;
  late TextEditingController _phoneNumberController;
  late TextEditingController _phoneNumberVerifiedController;
  late TextEditingController _addressController;
  late TextEditingController _updatedAtController;
  late TextEditingController _addRoleController;
  late TextEditingController _userDataController;
  late TextEditingController _serverDataController;

  final Set<String> _addedRoles = {};

  @override
  void initState() {
    _tenantId = widget.tenantId;
    _userId = widget.userId;

    _loadUser();

    _idController = TextEditingController();
    _accountController = TextEditingController();
    _createdAtController = TextEditingController();
    _nameController = TextEditingController();
    _givenNameController = TextEditingController();
    _familyNameController = TextEditingController();
    _middleNameController = TextEditingController();
    _nicknameController = TextEditingController();
    _preferredUsernameController = TextEditingController();
    _profileController = TextEditingController();
    _pictureController = TextEditingController();
    _websiteController = TextEditingController();
    _emailController = TextEditingController();
    _emailVerifiedController = TextEditingController();
    _genderController = TextEditingController();
    _birthdateController = TextEditingController();
    _zoneInfoController = TextEditingController();
    _localeController = TextEditingController();
    _phoneNumberController = TextEditingController();
    _phoneNumberVerifiedController = TextEditingController();
    _addressController = TextEditingController();
    _updatedAtController = TextEditingController();
    _addRoleController = TextEditingController();
    _userDataController = TextEditingController();
    _serverDataController = TextEditingController();

    super.initState();
  }

  void _loadUser() async {
    setMountedState(() {
      _isLoading = true;
    });
    try {
      User user = await service.getUser(_tenantId, _userId);

      setUserState(user);
    } catch (e) {
      showError('Failed to load user', e);
    } finally {
      setMountedState(() {
        _isLoading = false;
      });
    }
  }

  void setUserState(User user) {
    setMountedState(() {
      _user = user;

      // Update controllers
      _idController.text = user.id;
      _accountController.text = user.account;
      _createdAtController.text = DateFormat.yMMMd().add_Hms().format(user.createdAt);
      _nameController.text = user.name ?? '';
      _givenNameController.text = user.givenName ?? '';
      _familyNameController.text = user.familyName ?? '';
      _middleNameController.text = user.middleName ?? '';
      _nicknameController.text = user.nickname ?? '';
      _preferredUsernameController.text = user.preferredUsername ?? '';
      _profileController.text = user.profile ?? '';
      _pictureController.text = user.picture ?? '';
      _websiteController.text = user.website ?? '';
      _emailController.text = user.email ?? '';
      // _emailVerifiedController.text = user.id;
      _genderController.text = user.gender ?? '';

      if (user.birthdate != null) {
        DateFormat.yMMMd().add_Hms().format(user.birthdate!);
      } else {
        _birthdateController.text = '';
      }
      _zoneInfoController.text = user.zoneInfo ?? '';
      _localeController.text = user.locale ?? '';
      _phoneNumberController.text = user.phoneNumber ?? '';
      // _phoneNumberVerifiedController.text = user.id;
      _addressController.text = user.address ?? '';
      _updatedAtController.text = DateFormat.yMMMd().add_Hms().format(user.updatedAt);

      // _roleController.text = user.id;

      _userDataController.text = _emptyJson;
      if (user.userData != null) {
        _userDataController.text = prettyJson.convert(user.userData);
      }

      _serverDataController.text = _emptyJson;
      if (user.serverData != null) {
        _serverDataController.text = prettyJson.convert(user.serverData);
      }

      if (user.role != null) {
        _addedRoles.addAll(user.role!);
      }
    });
  }

  List<Widget> _buildRoles(Set<String> scopes) {
    List<Widget> widgets = [];
    for (var scope in scopes) {
      widgets.add(
        Chip(
          labelPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
          shape: const RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(16.0)),
          ),
          deleteIcon: const Icon(Icons.close),
          onDeleted: () {
            _addedRoles.remove(scope);
            setState(() {});
          },
          backgroundColor: Colors.deepPurple.shade50,
          deleteIconColor: Colors.redAccent,
          label: Text(scope),
        ),
      );
    }

    widgets.add(
      SizedBox(
        width: 150,
        child: TextField(
          controller: _addRoleController,
          decoration: InputDecoration(
            contentPadding: const EdgeInsets.all(8),
            border: const OutlineInputBorder(
              borderRadius: BorderRadius.all(Radius.circular(16.0)),
              gapPadding: 0,
            ),
            suffixIcon: IconButton(
              icon: const Icon(Icons.add),
              onPressed: () {
                var toAdd = _addRoleController.text.replaceAll(' ', '');
                if (toAdd.isNotEmpty) {
                  _addedRoles.add(toAdd);
                }
                _addRoleController.text = '';
                setState(() {});
              },
            ),
          ),
        ),
      ),
    );

    return widgets;
  }

  Future<void> updateUser() async {
    var user = _user;
    if (user != null) {
      final Map<String, dynamic?> userMap = <String, dynamic?>{};

      if (user.name != null && _nameController.text.isEmpty) {
        userMap["name"] = null;
      } else if (_nameController.text.isNotEmpty && _nameController.text != user.name) {
        userMap['name'] = _nameController.text;
      }

      if (user.givenName != null && _givenNameController.text.isEmpty) {
        userMap["given_name"] = null;
      } else if (_givenNameController.text.isNotEmpty && _givenNameController.text != user.givenName) {
        userMap['given_name'] = _givenNameController.text;
      }

      if (user.familyName != null && _familyNameController.text.isEmpty) {
        userMap["family_name"] = null;
      } else if (_familyNameController.text.isNotEmpty && _familyNameController.text != user.familyName) {
        userMap['family_name'] = _familyNameController.text;
      }

      if (user.middleName != null && _middleNameController.text.isEmpty) {
        userMap["middle_name"] = null;
      } else if (_middleNameController.text.isNotEmpty && _middleNameController.text != user.middleName) {
        userMap['middle_name'] = _middleNameController.text;
      }

      if (user.nickname != null && _nicknameController.text.isEmpty) {
        userMap["nickname"] = null;
      } else if (_nicknameController.text.isNotEmpty && _nicknameController.text != user.nickname) {
        userMap['nickname'] = _nicknameController.text;
      }

      if (user.preferredUsername != null && _preferredUsernameController.text.isEmpty) {
        userMap["preferred_username"] = null;
      } else if (_preferredUsernameController.text.isNotEmpty &&
          _preferredUsernameController.text != user.preferredUsername) {
        userMap['preferred_username'] = _preferredUsernameController.text;
      }

      if (user.profile != null && _profileController.text.isEmpty) {
        userMap["profile"] = null;
      } else if (_profileController.text.isNotEmpty && _profileController.text != user.profile) {
        userMap['profile'] = _profileController.text;
      }

      if (user.picture != null && _pictureController.text.isEmpty) {
        userMap["picture"] = null;
      } else if (_pictureController.text.isNotEmpty && _pictureController.text != user.picture) {
        userMap['picture'] = _pictureController.text;
      }

      if (user.website != null && _websiteController.text.isEmpty) {
        userMap["website"] = null;
      } else if (_websiteController.text.isNotEmpty && _websiteController.text != user.website) {
        userMap['website'] = _websiteController.text;
      }

      if (user.email != null && _emailController.text.isEmpty) {
        userMap["email"] = null;
      } else if (_emailController.text.isNotEmpty && _emailController.text != user.email) {
        userMap['email'] = _emailController.text;
      }

      if (user.gender != null && _genderController.text.isEmpty) {
        userMap["gender"] = null;
      } else if (_genderController.text.isNotEmpty && _genderController.text != user.gender) {
        userMap['gender'] = _genderController.text;
      }

      if (user.zoneInfo != null && _zoneInfoController.text.isEmpty) {
        userMap["zoneinfo"] = null;
      } else if (_zoneInfoController.text.isNotEmpty && _zoneInfoController.text != user.zoneInfo) {
        userMap['zoneinfo'] = _zoneInfoController.text;
      }

      if (user.locale != null && _localeController.text.isEmpty) {
        userMap["locale"] = null;
      } else if (_localeController.text.isNotEmpty && _localeController.text != user.locale) {
        userMap['locale'] = _localeController.text;
      }

      if (user.phoneNumber != null && _phoneNumberController.text.isEmpty) {
        userMap["phone_number"] = null;
      } else if (_phoneNumberController.text.isNotEmpty && _phoneNumberController.text != user.phoneNumber) {
        userMap['phone_number'] = _phoneNumberController.text;
      }

      if (user.address != null && _addressController.text.isEmpty) {
        userMap["address"] = null;
      } else if (_addressController.text.isNotEmpty && _addressController.text != user.address) {
        userMap['address'] = _addressController.text;
      }

      if (user.role != null && _addedRoles.isEmpty) {
        userMap["role"] = null;
      } else if (_addedRoles.isNotEmpty) {
        var isTheSame = true;
        if (user.role?.length != _addedRoles.length) {
          isTheSame = false;
        } else {
          for (final element in _addedRoles) {
            if (user.role?.contains(element) != true) {
              isTheSame = false;
              break;
            }
          }
        }
        if (!isTheSame) {
          userMap['role'] = _addedRoles.toList(growable: false);
        }
      }

      if (_userDataController.text == _emptyJson) {
        if (user.userData != null) {
          userMap["user_data"] = null;
        }
      } else if (_userDataController.text.isNotEmpty && _userDataController.text != prettyJson.convert(user.userData)) {
        try {
          var jsonData = json.decode(_userDataController.text) as Map<String, dynamic>;
          if (jsonData.isEmpty) {
            userMap["user_data"] = null;
          } else {
            userMap['user_data'] = jsonData;
          }
        } catch (e) {
          showError('Invalid user_data model', e);
        }
      }

      if (_serverDataController.text == _emptyJson) {
        if (user.serverData != null) {
          userMap["server_data"] = null;
        }
      } else if (_serverDataController.text.isNotEmpty &&
          _serverDataController.text.trim() != prettyJson.convert(user.serverData)) {
        try {
          var jsonData = json.decode(_serverDataController.text) as Map<String, dynamic>;
          if (jsonData.isEmpty) {
            userMap["server_data"] = null;
          } else {
            userMap['server_data'] = jsonData;
          }
        } catch (e) {
          showError('Invalid server_data model', e);
        }
      }

      if (userMap.isEmpty) {
        showSuccessMessage('Nothing has changed');
        return;
      }
      setMountedState(() {
        _isUpdating = true;
      });
      try {
        User user = await service.updateUser(_tenantId, _userId, userMap);
        setUserState(user);
        showSuccessMessage('Updated successfully');
      } catch (e) {
        showError('Failed to update user', e);
      } finally {
        setMountedState(() {
          _isUpdating = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Heading24(text: 'User details'),
        centerTitle: true,
        actions: [ProfileActionButton(service: service)],
      ),
      body: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          const PanelNavRail(),
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: _isLoading
                  ? const SpinKitThreeBounce(
                      color: Colors.deepPurpleAccent,
                      size: 24,
                    )
                  : Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Wrap(
                          spacing: 12,
                          runSpacing: 12,
                          children: [
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _idController,
                                  readOnly: true,
                                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Id'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _accountController,
                                  readOnly: true,
                                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Account'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _nameController,
                                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Name'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _givenNameController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Given Name'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _familyNameController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Family Name'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _middleNameController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Middle Name'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _nicknameController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Nickname'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _preferredUsernameController,
                                  decoration: const InputDecoration(
                                      border: OutlineInputBorder(), labelText: 'Preferred Username'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 612,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _profileController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Profile Url'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 612,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _pictureController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Picture Url'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 612,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _websiteController,
                                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Website'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _emailController,
                                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Email'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _genderController,
                                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Gender'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _birthdateController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Birthday'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _zoneInfoController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Zone Info'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _localeController,
                                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Locale'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _phoneNumberController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Phone Number'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _createdAtController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Created At'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _updatedAtController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Updated At'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: double.infinity,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const Heading18(text: "Roles:"),
                                    Wrap(
                                      crossAxisAlignment: WrapCrossAlignment.center,
                                      spacing: 16,
                                      runSpacing: 16,
                                      children: _buildRoles(_addedRoles),
                                    )
                                  ],
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 930,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _userDataController,
                                  keyboardType: TextInputType.multiline,
                                  minLines: 3,
                                  maxLines: 10,
                                  // expands: true,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'User Data JSON'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 930,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _serverDataController,
                                  keyboardType: TextInputType.multiline,
                                  minLines: 3,
                                  maxLines: 10,
                                  // expands: true,
                                  decoration: const InputDecoration(
                                      border: OutlineInputBorder(), labelText: 'Server Data JSON'),
                                ),
                              ),
                            ),
                          ],
                        ),
                        // token settings

                        Align(
                          alignment: Alignment.topRight,
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: ElevatedButton(
                              onPressed: _isUpdating
                                  ? null
                                  : () async {
                                      setState(() {
                                        updateUser();
                                      });
                                    },
                              child: const Text('Save'),
                            ),
                          ),
                        ),
                      ],
                    ),
            ),
          )
        ],
      ),
    );
  }

  @override
  void dispose() {
    _idController.dispose();
    _accountController.dispose();
    _createdAtController.dispose();
    _nameController.dispose();
    _givenNameController.dispose();
    _familyNameController.dispose();
    _middleNameController.dispose();
    _nicknameController.dispose();
    _preferredUsernameController.dispose();
    _profileController.dispose();
    _pictureController.dispose();
    _websiteController.dispose();
    _emailController.dispose();
    _emailVerifiedController.dispose();
    _genderController.dispose();
    _birthdateController.dispose();
    _zoneInfoController.dispose();
    _localeController.dispose();
    _phoneNumberController.dispose();
    _phoneNumberVerifiedController.dispose();
    _addressController.dispose();
    _updatedAtController.dispose();
    _addRoleController.dispose();
    _userDataController.dispose();
    _serverDataController.dispose();

    super.dispose();
  }
}
