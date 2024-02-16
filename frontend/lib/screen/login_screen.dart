import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:frontend/ext/exception_parse_error.dart';
import 'package:frontend/ext/state_mounted_state.dart';
import 'package:frontend/main.dart';
import 'package:frontend/util/pair.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  late TextEditingController _accountController;
  late TextEditingController _passwordController;

  String? _error;

  bool _isLoading = false;

  @override
  void initState() {
    _accountController = TextEditingController(text: "admin");
    _passwordController = TextEditingController(text: "123456");

    super.initState();
  }

  void _tryLogin() async {
    try {
      setMountedState(() {
        _isLoading = true;
      });
      await authenticationManager.authenticate(Pair(_accountController.value.text, _passwordController.value.text));
    } catch (e) {
      setMountedState(() {
        _error = e.parseMessage();
      });
    } finally {
      setMountedState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () {
        return Future(() => false);
      },
      child: Scaffold(
        body: Container(
          padding: const EdgeInsets.all(16),
          alignment: Alignment.center,
          child: SizedBox(
            width: 400,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                Hero(
                    tag: 'logo',
                    child: SvgPicture.asset(
                      'assets/images/logo.svg',
                      width: 331,
                      height: 80,
                    )),
                const SizedBox(height: 24),
                TextField(
                  onChanged: (_) {
                    setState(() {
                      _error = null;
                    });
                  },
                  enabled: !_isLoading,
                  controller: _accountController,
                  decoration:
                      InputDecoration(errorText: _error, border: const OutlineInputBorder(), labelText: 'Account'),
                ),
                const SizedBox(height: 16),
                TextField(
                  onChanged: (_) {
                    setState(() {
                      _error = null;
                    });
                  },
                  enabled: !_isLoading,
                  controller: _passwordController,
                  obscureText: true,
                  decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Password'),
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () {
                                _tryLogin();
                              },
                        child: const Text('Login'))
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
