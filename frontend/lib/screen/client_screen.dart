import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:frontend/domain/api_service_client.dart';
import 'package:frontend/domain/model/client.dart';
import 'package:frontend/ext/snack_bar_state_error.dart';
import 'package:frontend/ext/snack_bar_state_message.dart';
import 'package:frontend/ext/state_mounted_state.dart';
import 'package:frontend/screen/protected_screen_state.dart';
import 'package:frontend/ui/error_view.dart';
import 'package:frontend/ui/panel_nav_rail.dart';
import 'package:frontend/ui/text_styles.dart';

import '../ui/profile_action_button.dart';

class ClientScreen extends StatefulWidget {
  final int tenantId;
  final String clientId;

  const ClientScreen({super.key, required this.tenantId, required this.clientId});

  @override
  State<ClientScreen> createState() => _ClientScreenState();
}

class _ClientScreenState extends ProtectedScreenState<ClientScreen> {
  String? redirectError;

  late final int _tenantId;
  late final String _clientId;

  late TextEditingController _clientIdController;
  late TextEditingController _clientSecretController;
  late TextEditingController _applicationNameController;

  late TextEditingController _tokenExpirationController;
  late TextEditingController _refreshTokenExpirationController;
  late TextEditingController _refreshTokenAbsoluteExpirationController;

  late TextEditingController _addScopeController;
  late TextEditingController _addAudienceController;

  late TextEditingController _addRedirectUrlController;

  Client? _client;

  final Set<String> _selectedGrantTypes = {};

  final Set<String> _addedScopes = {};

  final Set<String> _addedRedirectUris = {};

  final Set<String> _addedAudiences = {};

  bool _isLoading = false;
  bool _isSaving = false;

  @override
  void initState() {
    _tenantId = widget.tenantId;
    _clientId = widget.clientId;

    _clientIdController = TextEditingController();
    _clientSecretController = TextEditingController();
    _applicationNameController = TextEditingController();

    _tokenExpirationController = TextEditingController();
    _refreshTokenExpirationController = TextEditingController();
    _refreshTokenAbsoluteExpirationController = TextEditingController();

    _addScopeController = TextEditingController();

    _addAudienceController = TextEditingController();

    _addRedirectUrlController = TextEditingController();

    _loadClientDetails();

    super.initState();
  }

  void _loadClientDetails() async {
    setMountedState(() {
      _isLoading = true;
    });

    try {
      Client client = await service.getClient(_tenantId, _clientId);

      setMountedState(() {
        _client = client;
        _selectedGrantTypes.addAll(client.settings.grantTypes);
        _addedScopes.addAll(client.settings.scope);
        _addedRedirectUris.addAll(client.settings.redirectUris);

        _applicationNameController.text = client.application;
        _clientIdController.text = client.clientId;
        _clientSecretController.text = client.clientSecret;

        _tokenExpirationController.text = client.settings.tokenExpiration.toString();
        _refreshTokenExpirationController.text = client.settings.refreshTokenExpiration.toString();
        _refreshTokenAbsoluteExpirationController.text = client.settings.refreshTokenAbsoluteExpiration.toString();
      });
    } catch (e) {
      showError('Cannot load client', e);
    } finally {
      setMountedState(() {
        _isLoading = false;
      });
    }
  }

  void updateClient() async {
    setMountedState(() {
      _isSaving = true;
    });
    final client = _client;
    if (client != null) {
      try {
        var newSettings = client.settings
          ..grantTypes = _selectedGrantTypes.toList(growable: false)
          ..scope = _addedScopes.toList(growable: false)
          ..redirectUris = _addedRedirectUris.toList(growable: false)
          ..audience = _addedAudiences.toList(growable: false)
          ..tokenExpiration = int.parse(_tokenExpirationController.value.text)
          ..refreshTokenExpiration = int.parse(_refreshTokenExpirationController.value.text)
          ..refreshTokenAbsoluteExpiration = int.parse(_refreshTokenAbsoluteExpirationController.value.text);

        var clientToUpdate = client
          ..tenantId = client.tenantId
          ..clientId = _clientIdController.value.text
          ..application = _applicationNameController.value.text
          ..clientSecret = _clientSecretController.value.text
          ..settings = newSettings;

        service.updateClient(clientToUpdate);

        showSuccessMessage("Updated successfully");
      } catch (e) {
        log('Failed to update client', error: e);

        showError('Failed to update client', e);
      } finally {
        setMountedState(() {
          _isSaving = false;
        });
      }
    }
  }

  List<Widget> _buildGrantTypes() {
    List<Widget> widgets = [];
    for (var i in [
      "authorization_code",
      // "implicit",
      "password",
      "client_credentials",
      "refresh_token"
    ]) {
      var isSelected = _selectedGrantTypes.contains(i);

      widgets.add(
        FilterChip(
          labelPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
          shape: const RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(16.0)),
          ),
          label: Text(i),
          onSelected: (bool value) {
            setState(() {
              if (value) {
                _selectedGrantTypes.add(i);
              } else {
                _selectedGrantTypes.remove(i);
              }
            });
          },
          selectedColor: Colors.deepPurple.shade50,
          selected: isSelected,
        ),
      );
    }

    return widgets;
  }

  List<Widget> _buildRedirectUrl(Set<String> redirectUrls) {
    List<Widget> widgets = [];

    if (redirectError != null) {
      widgets.add(ErrorView(
        message: redirectError!,
        closeCallback: () {
          setState(() {
            redirectError = null;
          });
        },
      ));
    }

    for (var redirectUrl in redirectUrls) {
      widgets.add(
        Chip(
          labelPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
          shape: const RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(16.0)),
          ),
          deleteIcon: const Icon(Icons.close),
          onDeleted: () {
            _addedRedirectUris.remove(redirectUrl);
            setState(() {});
          },
          backgroundColor: Colors.deepPurple.shade50,
          deleteIconColor: Colors.redAccent,
          label: Text(redirectUrl),
        ),
      );
    }

    widgets.add(
      SizedBox(
        width: 250,
        child: TextField(
          controller: _addRedirectUrlController,
          decoration: InputDecoration(
            contentPadding: const EdgeInsets.all(8),
            border: const OutlineInputBorder(
              borderRadius: BorderRadius.all(Radius.circular(16.0)),
              gapPadding: 0,
            ),
            suffixIcon: IconButton(
              icon: const Icon(Icons.add),
              onPressed: () {
                var toAdd = _addRedirectUrlController.text.replaceAll(' ', '');

                var uri = Uri.parse(toAdd);
                try {
                  if (uri.isAbsolute) {
                    _addedRedirectUris.add(toAdd);
                    _addRedirectUrlController.text = '';
                    redirectError = null;
                  } else {
                    throw const FormatException("Invalid url");
                  }
                } catch (e) {
                  redirectError = e.toString();
                }
                setState(() {});
              },
            ),
          ),
        ),
      ),
    );

    return widgets;
  }

  List<Widget> _buildScope(Set<String> scopes) {
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
            _addedScopes.remove(scope);
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
          controller: _addScopeController,
          decoration: InputDecoration(
            contentPadding: const EdgeInsets.all(8),
            border: const OutlineInputBorder(
              borderRadius: BorderRadius.all(Radius.circular(16.0)),
              gapPadding: 0,
            ),
            suffixIcon: IconButton(
              icon: const Icon(Icons.add),
              onPressed: () {
                var toAdd = _addScopeController.text.replaceAll(' ', '');
                if (toAdd.isNotEmpty) {
                  _addedScopes.add(toAdd);
                }
                _addScopeController.text = '';
                setState(() {});
              },
            ),
          ),
        ),
      ),
    );

    return widgets;
  }

  List<Widget> _buildAudience(Set<String> audiences) {
    List<Widget> widgets = [];
    for (var audience in audiences) {
      widgets.add(
        Chip(
          labelPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
          shape: const RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(16.0)),
          ),
          deleteIcon: const Icon(Icons.close),
          onDeleted: () {
            _addedAudiences.remove(audience);
            setState(() {});
          },
          backgroundColor: Colors.deepPurple.shade50,
          deleteIconColor: Colors.redAccent,
          label: Text(audience),
        ),
      );
    }

    widgets.add(
      SizedBox(
        width: 250,
        child: TextField(
          controller: _addAudienceController,
          decoration: InputDecoration(
            contentPadding: const EdgeInsets.all(8),
            border: const OutlineInputBorder(
              borderRadius: BorderRadius.all(Radius.circular(16.0)),
              gapPadding: 0,
            ),
            suffixIcon: IconButton(
              icon: const Icon(Icons.add),
              onPressed: () {
                var toAdd = _addAudienceController.text.replaceAll(' ', '');
                if (toAdd.isNotEmpty) {
                  _addedAudiences.add(toAdd);
                }
                _addAudienceController.text = '';
                setState(() {});
              },
            ),
          ),
        ),
      ),
    );

    return widgets;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Heading24(text: 'Client details'),
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
                                  controller: _applicationNameController,
                                  decoration: const InputDecoration(
                                      border: OutlineInputBorder(), labelText: 'Application Name'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _clientIdController,
                                  readOnly: true,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'ClientId'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _clientSecretController,
                                  decoration:
                                      const InputDecoration(border: OutlineInputBorder(), labelText: 'Client Secret'),
                                ),
                              ),
                            ),
                          ],
                        ),
                        // token settings
                        const Padding(
                          padding: EdgeInsets.fromLTRB(16, 32, 16, 8),
                          child: Heading18(text: "Token settings"),
                        ),
                        Wrap(
                          spacing: 12,
                          runSpacing: 12,
                          children: [
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _tokenExpirationController,
                                  keyboardType: TextInputType.number,
                                  inputFormatters: <TextInputFormatter>[
                                    FilteringTextInputFormatter.digitsOnly,
                                  ],
                                  decoration: const InputDecoration(
                                      border: OutlineInputBorder(), labelText: 'Token expiration'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _refreshTokenExpirationController,
                                  keyboardType: TextInputType.number,
                                  inputFormatters: <TextInputFormatter>[
                                    FilteringTextInputFormatter.digitsOnly,
                                  ],
                                  decoration: const InputDecoration(
                                      border: OutlineInputBorder(), labelText: 'Refresh expiration'),
                                ),
                              ),
                            ),
                            SizedBox(
                              width: 300,
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: TextField(
                                  controller: _refreshTokenAbsoluteExpirationController,
                                  keyboardType: TextInputType.number,
                                  inputFormatters: <TextInputFormatter>[
                                    FilteringTextInputFormatter.digitsOnly,
                                  ],
                                  decoration: const InputDecoration(
                                      border: OutlineInputBorder(), labelText: 'Absolute expiration'),
                                ),
                              ),
                            ),
                          ],
                        ),
                        // end token settings
                        const Padding(
                          padding: EdgeInsets.fromLTRB(16, 32, 16, 8),
                          child: Heading18(text: "Flows settings"),
                        ),
                        Wrap(
                          spacing: 12,
                          runSpacing: 12,
                          children: _buildGrantTypes(),
                        ),
                        const Padding(
                          padding: EdgeInsets.fromLTRB(16, 32, 16, 8),
                          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                            Heading18(text: "Allowed redirect urls"),
                            ErrorText14(
                              text: "affects only authorization code flow",
                              color: Colors.blueGrey,
                            )
                          ]),
                        ),
                        Wrap(
                          crossAxisAlignment: WrapCrossAlignment.center,
                          spacing: 12,
                          runSpacing: 12,
                          children: _buildRedirectUrl(_addedRedirectUris),
                        ),
                        const Padding(
                          padding: EdgeInsets.fromLTRB(16, 32, 16, 8),
                          child: Heading18(text: "Scope settings"),
                        ),
                        Wrap(
                          crossAxisAlignment: WrapCrossAlignment.center,
                          spacing: 12,
                          runSpacing: 12,
                          children: _buildScope(_addedScopes),
                        ),
                        const Padding(
                          padding: EdgeInsets.fromLTRB(16, 32, 16, 8),
                          child: Heading18(text: "Audience settings"),
                        ),
                        Wrap(
                          crossAxisAlignment: WrapCrossAlignment.center,
                          spacing: 12,
                          runSpacing: 12,
                          children: _buildAudience(_addedAudiences),
                        ),
                        Align(
                          alignment: Alignment.topRight,
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: ElevatedButton(
                              onPressed: _isSaving
                                  ? null
                                  : () {
                                      updateClient();
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
}
