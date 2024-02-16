import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:frontend/domain/model/server_configuration.dart';
import 'package:frontend/ext/snack_bar_state_error.dart';
import 'package:frontend/ext/state_mounted_state.dart';
import 'package:frontend/screen/protected_screen_state.dart';
import 'package:frontend/ui/panel_nav_rail.dart';
import 'package:frontend/ui/profile_action_button.dart';
import 'package:frontend/ui/text_styles.dart';

class PanelScreen extends StatefulWidget {
  const PanelScreen({super.key});

  @override
  State<PanelScreen> createState() => _PanelScreenState();
}

class _PanelScreenState extends ProtectedScreenState<PanelScreen> {
  ServerConfiguration? _configuration;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadConfiguration();
  }

  void _loadConfiguration() async {
    setState(() {
      _isLoading = true;
    });
    try {
      await service.getConfiguration().then((value) {
        setMountedState(() {
          _configuration = value;
        });
      });
    } catch (e) {
      showError('Failed to load configuration', e);
    } finally {
      setMountedState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    List<Widget> widgets = [];

    if (_isLoading) {
      widgets.add(
        const SpinKitThreeBounce(
          color: Colors.deepPurpleAccent,
          size: 24,
        ),
      );
    }

    if (!_isLoading && _configuration != null) {
      widgets.add(_buildRowWidgets("JWT Signing Algorithm", _configuration?.jwtSigningAlgorithm));
      widgets.add(_buildRowWidgets("JWT Key Size", _configuration?.jwtSigningKeySize.toString()));
    }

    return Scaffold(
      appBar: AppBar(
        title: const Heading24(text: 'Configuration'),
        centerTitle: true,
        automaticallyImplyLeading: false,
        actions: [ProfileActionButton(service: service)],
      ),
      body: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          const PanelNavRail(),
          Expanded(
            // padding: const EdgeInsets.all(24),
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: widgets,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRowWidgets(String name, String? value) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(0, 0, 0, 4),
      child: ListTile(
        tileColor: Colors.deepPurple.shade50,
        shape: const RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(8))),
        title: Text(
          name,
          style: const TextStyle(color: Colors.deepPurple),
        ),
        // subtitle: Text(
        //     'Created on: ${DateFormat.yMMMd().format(i.second.startedAt)}'),
        trailing: Text(value!),
      ),
    );
  }
}
