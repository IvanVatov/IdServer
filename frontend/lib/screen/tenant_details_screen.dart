import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:frontend/domain/api_service_client.dart';
import 'package:frontend/domain/api_service_tenant.dart';
import 'package:frontend/domain/model/client.dart';
import 'package:frontend/domain/model/rsa_key.dart';
import 'package:frontend/domain/model/valid_keys.dart';
import 'package:frontend/ext/build_context_confirmation_dialog.dart';
import 'package:frontend/ext/snack_bar_state_error.dart';
import 'package:frontend/ext/state_mounted_state.dart';
import 'package:frontend/main.dart';
import 'package:frontend/screen/protected_screen_state.dart';
import 'package:frontend/ui/panel_nav_rail.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../ui/profile_action_button.dart';
import '../ui/text_styles.dart';

class TenantDetailsScreen extends StatefulWidget {
  final int id;

  const TenantDetailsScreen({super.key, required this.id});

  @override
  State<TenantDetailsScreen> createState() => _TenantDetailsScreenState();
}

class _TenantDetailsScreenState extends ProtectedScreenState<TenantDetailsScreen> {
  late int id;

  List<Client>? _clients;

  ValidKeys? _validKeys;

  bool _isLoadingClients = false;
  bool _isLoadingKeys = false;
  bool _isCreatingClient = false;

  late TextEditingController _applicationNameController;
  late TextEditingController _clientIdController;

  @override
  void initState() {
    super.initState();

    id = widget.id;
    _loadClients();
    _loadKeys();

    _applicationNameController = TextEditingController();
    _clientIdController = TextEditingController();
  }

  void _loadClients() async {
    setMountedState(() {
      _isLoadingClients = true;
    });

    try {
      List<Client>? apiResponse = await service.getClients(id);
      setMountedState(() {
        _clients = apiResponse;
      });
    } catch (e) {
      showError('Failed to load clients', e);
    } finally {
      setMountedState(() {
        _isLoadingClients = false;
      });
    }
  }

  void _loadKeys() async {
    setMountedState(() {
      _isLoadingKeys = true;
    });

    try {
      ValidKeys? apiResponse = await service.getKeys(id);

      setMountedState(() {
        _validKeys = apiResponse;
      });
    } catch (e) {
      showError('Failed to load keys', e);
    } finally {
      setMountedState(() {
        _isLoadingKeys = false;
      });
    }
  }

  void _rotateKey() async {
    try {
      var newValidKeys = _validKeys;
      var newKey = await service.rotateKey(id);

      if (newValidKeys != null) {
        List<RsaKey> valid = [];
        valid.add(newValidKeys.current);
        valid.addAll(newValidKeys.valid);

        newValidKeys.valid = valid;
        newValidKeys.current = newKey;

        setMountedState(() {
          _validKeys = newValidKeys;
        });
      }
    } catch (e) {
      showError('Failed to rotate key', e);
    }
  }

  void _deleteKey(String keyId) async {
    try {
      var result = await service.deleteKey(id, keyId);
      if (result.success) {
        _validKeys?.valid.removeWhere((element) => element.id == keyId);
        setMountedState(() {
          _validKeys = _validKeys;
        });
      }
    } catch (e) {
      showError('Failed to delete key', e);
    }
  }

  void _createClient() async {
    setMountedState(() {
      _isCreatingClient = true;
    });
    try {
      var client =
          await service.createClient(id, _clientIdController.value.text, _applicationNameController.value.text);

      List<Client> newList = [];
      var oldClients = _clients;
      if (oldClients != null) {
        newList.addAll(oldClients);
      }
      newList.add(client);

      setMountedState(() {
        _clients = newList;
      });
    } catch (e) {
      showError('Failed to create client', e);
    } finally {
      setMountedState(() {
        _isCreatingClient = false;
      });
    }
  }

  void _deleteClient(String clientId) async {
    try {
      var result = await service.deleteClient(id, clientId);
      if (result.success) {
        List<Client> mutable = [];
        var clients = _clients;
        if (clients != null) {
          mutable.addAll(clients);
          clients.removeWhere((element) => element.clientId == clientId);
        }
        setMountedState(() {
          _clients = clients;
        });
      }
    } catch (e) {
      showError('Failed to delete client', e);
    }
  }

  void _deleteKeyDialog(String keyId) {
    context.showConfirmationDialog(
      'Are you sure you want to delete key with id: "$keyId" ?\nThis action will make all issued tokens with this key invalid!',
      () {
        _deleteKey(keyId);
      },
    );
  }

  void _deleteClientDialog(String clientId) {
    context.showConfirmationDialog(
      'Are you sure you want to delete clientId: "$clientId"?',
      () {
        _deleteClient(clientId);
      },
    );
  }

  void _rotateRsaKeyDialog() {
    context.showConfirmationDialog(
      'Are you sure you want to rotate current key?',
      () {
        _rotateKey();
      },
    );
  }

  void _showPublicKey(RsaKey rsaKey) {
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
                      width: 600,
                      child: TextFormField(
                        minLines: 2,
                        maxLines: 20,
                        readOnly: true,
                        initialValue: rsaKey.formattedKey(),
                        style: const TextStyle(fontFamily: 'Consolas'),
                      ),
                    ),
                    TextButton(
                      onPressed: () {
                        Navigator.pop(context);
                      },
                      child: const Text('Close'),
                    ),
                  ],
                ),
              ),
            ));
  }

  List<DataRow> _buildClientsDataCells(List<Client>? clients) {
    List<DataRow> widgets = [];

    if (clients != null) {
      for (var c in clients) {
        widgets.add(DataRow(cells: [
          DataCell(
            SelectableText(
              c.application,
              style: const TextStyle(color: Colors.deepPurple),
            ),
          ),
          DataCell(
            SelectableText(
              c.clientId,
              style: const TextStyle(color: Colors.deepPurple),
            ),
          ),
          DataCell(
            SelectableText(
              c.clientSecret,
              style: const TextStyle(color: Colors.deepPurple),
            ),
          ),
          DataCell(
            IconButton(
              onPressed: () {
                context.pushNamed(RouteName.client,
                    pathParameters: {'tenantId': c.tenantId.toString(), 'clientId': c.clientId});
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
                _deleteClientDialog(c.clientId);
              },
              icon: const Icon(
                Icons.delete,
                color: Colors.red,
              ),
            ),
          ),
        ]));
      }
    }

    return widgets;
  }

  List<DataRow> _buildValidKeys(List<RsaKey>? keys) {
    List<DataRow> widgets = [];

    if (keys != null) {
      for (var key in keys) {
        widgets.add(
          DataRow(
            cells: [
              DataCell(
                SelectableText(
                  key.id,
                  style: const TextStyle(color: Colors.deepPurple),
                ),
              ),
              DataCell(
                SelectableText(
                  DateFormat('dd MMM yyyy HH:mm:ss').format(key.createdAt.toLocal()),
                  style: const TextStyle(color: Colors.deepPurple),
                ),
              ),
              DataCell(
                IconButton(
                  onPressed: () {
                    _showPublicKey(key);
                  },
                  icon: const Icon(
                    Icons.visibility,
                    color: Colors.deepPurple,
                  ),
                ),
              ),
              DataCell(
                IconButton(
                  onPressed: () {
                    _deleteKeyDialog(key.id);
                  },
                  icon: const Icon(
                    Icons.delete,
                    color: Colors.red,
                  ),
                ),
              ),
            ],
          ),
        );
      }
    }

    return widgets;
  }

  @override
  Widget build(BuildContext context) {
    var widgets = <Widget>[];

    if (_isLoadingKeys || _isLoadingClients) {
      widgets.add(
        const SpinKitThreeBounce(
          color: Colors.deepPurpleAccent,
          size: 24,
        ),
      );
    }

    if (!_isLoadingKeys && !_isLoadingClients && _validKeys != null && _clients != null) {
      var validKeys = _validKeys;
      if (validKeys != null) {
        widgets.add(
          ListTile(
            tileColor: Colors.deepPurple.shade50,
            shape: const RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(8))),
            title: SelectableText(
              validKeys.current.id,
              style: const TextStyle(color: Colors.deepPurple),
            ),
            subtitle: Text(DateFormat('dd MMM yyyy HH:mm:ss').format(validKeys.current.createdAt.toLocal())),
            trailing: Wrap(spacing: 16, children: [
              IconButton(
                  icon: const Icon(Icons.visibility),
                  color: Colors.deepPurple,
                  onPressed: () {
                    _showPublicKey(validKeys.current);
                  }),
              IconButton(
                  icon: const Icon(
                    Icons.autorenew,
                    color: Colors.deepPurple,
                  ),
                  onPressed: () {
                    _rotateRsaKeyDialog();
                  })
            ]),
          ),
        );

        widgets.add(
          DataTable(
            columns: const [
              DataColumn(label: Text('KeyId')),
              DataColumn(label: Text('Created at')),
              DataColumn(label: SizedBox(width: 24)),
              DataColumn(label: SizedBox(width: 24))
            ],
            rows: _buildValidKeys(validKeys.valid),
          ),
        );
      }

      widgets.add(
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizedBox(
              width: 300,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: TextField(
                  controller: _applicationNameController,
                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Application'),
                ),
              ),
            ),
            SizedBox(
              width: 300,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: TextField(
                  controller: _clientIdController,
                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'ClientId'),
                ),
              ),
            ),
            ElevatedButton(
                onPressed: _isCreatingClient
                    ? null
                    : () {
                        _createClient();
                      },
                child: const Text('Create'))
          ],
        ),
      );

      widgets.add(
        Expanded(
          child: SingleChildScrollView(
            child: DataTable(
              columns: const [
                DataColumn(label: Text('Application')),
                DataColumn(label: Text('ClientId')),
                DataColumn(label: Text('ClientSecret')),
                DataColumn(label: SizedBox(width: 24)),
                DataColumn(label: SizedBox(width: 24))
              ],
              rows: _buildClientsDataCells(_clients),
            ),
          ),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Heading24(text: 'Tenant details'),
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
