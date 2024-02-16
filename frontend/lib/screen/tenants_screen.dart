import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:frontend/domain/api_service_tenant.dart';
import 'package:frontend/domain/model/tenant.dart';
import 'package:frontend/ext/build_context_confirmation_dialog.dart';
import 'package:frontend/ext/snack_bar_state_error.dart';
import 'package:frontend/ext/state_mounted_state.dart';
import 'package:frontend/main.dart';
import 'package:frontend/screen/protected_screen_state.dart';
import 'package:frontend/ui/panel_nav_rail.dart';
import 'package:go_router/go_router.dart';

import '../ui/profile_action_button.dart';
import '../ui/text_styles.dart';

class TenantsScreen extends StatefulWidget {
  const TenantsScreen({super.key});

  @override
  State<TenantsScreen> createState() => _TenantsScreenState();
}

class _TenantsScreenState extends ProtectedScreenState<TenantsScreen> {
  _TenantsScreenState();

  late TextEditingController _nameController;
  late TextEditingController _hostController;

  List<Tenant>? _tenants;

  bool _isLoading = false;

  bool _isCreating = false;

  @override
  void initState() {
    _loadTenants();
    _nameController = TextEditingController();
    _hostController = TextEditingController();
    super.initState();
  }

  void _loadTenants() async {
    setMountedState(() {
      _isLoading = true;
    });
    try {
      List<Tenant>? apiResponse = await service.getTenants();

      setMountedState(() {
        _tenants = apiResponse;
      });
    } catch (e) {
      showError('Failed to load tenants', e);
    } finally {
      setMountedState(() {
        _isLoading = false;
      });
    }
  }

  void _deleteTenant(int tenantId) async {
    try {
      var result = await service.deleteTenant(tenantId);
      if (result.success) {
        List<Tenant> mutable = [];
        var tenants = _tenants;
        if (tenants != null) {
          mutable.addAll(tenants);
        }
        mutable.removeWhere((element) => element.id == tenantId);
        setMountedState(() {
          _tenants = mutable;
        });
      }
    } catch (e) {
      showError('Failed to delete tenant', e);
    }
  }

  void _createTenant() async {
    setMountedState(() {
      _isCreating = true;
    });
    try {
      var tenant = await service.createTenant(_nameController.value.text, _hostController.value.text);

      _nameController.text = '';
      _hostController.text = '';

      setMountedState(() {
        List<Tenant> newTenants = [];
        var oldTenants = _tenants;
        if (oldTenants != null) {
          newTenants.addAll(oldTenants);
        }
        newTenants.add(tenant);
        _tenants = newTenants;
      });
    } catch (e) {
      showError('Failed to create tenant', e);
    } finally {
      setMountedState(() {
        _isCreating = false;
      });
    }
  }

  void _deleteTenantDialog(int tenantId) {
    var tenant = _tenants?.firstWhere((element) => element.id == tenantId);

    if (tenant == null) {
      return;
    }

    context.showConfirmationDialog(
      'Are you sure you want to delete id: "${tenant.id}" name: "${tenant.name}" host: "${tenant.host}"?',
      () {
        _deleteTenant(tenantId);
      },
    );
  }

  List<DataRow> _buildTenantsDataCells(List<Tenant>? tenants) {
    List<DataRow> widgets = [];

    if (tenants == null) {
      return widgets;
    }

    for (var t in tenants) {
      widgets.add(DataRow(cells: [
        DataCell(
          Text(
            t.id.toString(),
            style: const TextStyle(color: Colors.deepPurple),
          ),
        ),
        DataCell(
          Text(
            t.name,
            style: const TextStyle(color: Colors.deepPurple),
          ),
        ),
        DataCell(
          Text(
            t.host,
            style: const TextStyle(color: Colors.deepPurple),
          ),
        ),
        DataCell(
          IconButton(
            onPressed: () {
              context.pushNamed(RouteName.users, pathParameters: {'tenantId': t.id.toString()});
            },
            icon: const Icon(
              Icons.person,
              color: Colors.deepPurple,
            ),
          ),
        ),
        DataCell(
          IconButton(
            onPressed: () {
              context.pushNamed(RouteName.tenant, pathParameters: {'id': t.id.toString()});
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
              _deleteTenantDialog(t.id);
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

    if (!_isLoading && _tenants != null) {
      widgets.add(
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizedBox(
              width: 300,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: TextField(
                  controller: _nameController,
                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Name'),
                ),
              ),
            ),
            SizedBox(
              width: 300,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: TextField(
                  controller: _hostController,
                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Host'),
                ),
              ),
            ),
            ElevatedButton(
                onPressed: _isCreating
                    ? null
                    : () {
                        _createTenant();
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
                DataColumn(label: Text('Id')),
                DataColumn(label: Text('Name')),
                DataColumn(label: Text('Host')),
                DataColumn(label: SizedBox(width: 24)),
                DataColumn(label: SizedBox(width: 24)),
                DataColumn(label: SizedBox(width: 24)),
              ],
              rows: _buildTenantsDataCells(_tenants),
            ),
          ),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Heading24(text: 'Tenants'),
        centerTitle: true,
        automaticallyImplyLeading: false,
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
