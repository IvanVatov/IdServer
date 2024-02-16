import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:frontend/domain/api_service_user.dart';
import 'package:frontend/ext/snack_bar_state_error.dart';
import 'package:frontend/ext/state_mounted_state.dart';
import 'package:frontend/screen/protected_screen_state.dart';
import 'package:frontend/ui/panel_nav_rail.dart';
import 'package:go_router/go_router.dart';

import '../domain/model/user.dart';
import '../main.dart';
import '../ui/profile_action_button.dart';
import '../ui/text_styles.dart';

class UsersListScreen extends StatefulWidget {
  final int tenantId;

  const UsersListScreen({super.key, required this.tenantId});

  @override
  State<UsersListScreen> createState() => _UsersListScreenState();
}

class _UsersListScreenState extends ProtectedScreenState<UsersListScreen> {
  late final int _tenantId;

  late TextEditingController _registerAccountController;
  late TextEditingController _registerPasswordController;

  bool _isLoading = false;
  bool _isCreating = false;

  @override
  void initState() {
    _tenantId = widget.tenantId;

    _loadUsers();

    _registerAccountController = TextEditingController();
    _registerPasswordController = TextEditingController();

    super.initState();
  }

  List<User>? _users;

  _UsersListScreenState();

  void _loadUsers() async {
    setMountedState(() {
      _isLoading = true;
    });
    try {
      List<User>? apiResponse = await service.getUsers(_tenantId);

      setMountedState(() {
        _users = apiResponse;
      });
    } catch (e) {
      showError('Failed to load users', e);
    } finally {
      setMountedState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _createUser() async {
    setMountedState(() {
      _isCreating = true;
    });
    try {
      String account = _registerAccountController.text;
      String password = _registerPasswordController.text;

      var newUser = await service.createUser(_tenantId, account, password);

      List<User> newUsers = [];
      newUsers.add(newUser);

      if (_users != null && _users!.isNotEmpty) {
        newUsers.addAll(_users!);
      }
      setMountedState(() {
        _users = newUsers;
      });
    } catch (e) {
      showError('Failed to create user', e);
    } finally {
      setMountedState(() {
        _isCreating = false;
      });
    }
  }

  List<DataRow> _buildUserDataCells(List<User>? users) {
    List<DataRow> widgets = [];

    if (users == null) {
      return widgets;
    }

    for (var user in users) {
      widgets.add(DataRow(cells: [
        DataCell(
          Text(
            user.id.toString(),
            style: const TextStyle(color: Colors.deepPurple),
          ),
        ),
        DataCell(
          Text(
            user.account,
            style: const TextStyle(color: Colors.deepPurple),
          ),
        ),
        DataCell(
          Text(
            user.name ?? '',
            style: const TextStyle(color: Colors.deepPurple),
          ),
        ),
        DataCell(
          IconButton(
            onPressed: () {
              context.pushNamed(RouteName.user, pathParameters: {'tenantId': _tenantId.toString(), 'userId': user.id});
            },
            icon: const Icon(
              Icons.edit,
              color: Colors.deepPurple,
            ),
          ),
        ),
        DataCell(
          IconButton(
            onPressed: () {
              // _deleteTenantDialog(t.id);
            },
            icon: const Icon(
              Icons.delete,
              color: Colors.red,
            ),
          ),
        ),
      ]));
    }

    return widgets;
  }

  void _showUserRegistrationDialog() {
    showDialog(
        context: context,
        builder: (BuildContext context) => Dialog(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    SizedBox(
                      width: 300,
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: TextField(
                          controller: _registerAccountController,
                          decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Account'),
                        ),
                      ),
                    ),
                    SizedBox(
                      width: 300,
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: TextField(
                          controller: _registerPasswordController,
                          obscureText: true,
                          decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Password'),
                        ),
                      ),
                    ),
                    SizedBox(
                      width: 300,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          TextButton(
                              onPressed: () {
                                Navigator.pop(context);
                              },
                              child: const Text('Cancel')),
                          TextButton(
                            onPressed: () async {
                              _createUser();
                              Navigator.pop(context);
                            },
                            child: const Text('Create'),
                          ),
                        ],
                      ),
                    )
                  ],
                ),
              ),
            ));
  }

  @override
  Widget build(BuildContext context) {
    var widgets = <Widget>[];

    if (_isLoading) {
      widgets.add(
        const SpinKitThreeBounce(
          color: Colors.deepPurpleAccent,
          size: 24,
        ),
      );
    }

    if (!_isLoading) {
      widgets.add(
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
                onPressed: _isCreating
                    ? null
                    : () {
                        _showUserRegistrationDialog();
                      },
                child: const Text('New User'))
          ],
        ),
      );

      widgets.add(
        Expanded(
          child: SingleChildScrollView(
            child: DataTable(
              columns: const [
                DataColumn(label: Text('Id')),
                DataColumn(label: Text('Account')),
                DataColumn(label: Text('Name')),
                DataColumn(label: SizedBox(width: 24)),
                DataColumn(label: SizedBox(width: 24)),
              ],
              rows: _buildUserDataCells(_users),
            ),
          ),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Heading24(text: 'Users list'),
        centerTitle: true,
        actions: [ProfileActionButton(service: service)],
      ),
      body: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          const PanelNavRail(),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: widgets,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
