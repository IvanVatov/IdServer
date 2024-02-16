import 'dart:developer';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:frontend/domain/auth/authentication_manager.dart';
import 'package:frontend/screen/client_screen.dart';
import 'package:frontend/screen/loading_screen.dart';
import 'package:frontend/screen/login_screen.dart';
import 'package:frontend/screen/panel_screen.dart';
import 'package:frontend/screen/tenant_details_screen.dart';
import 'package:frontend/screen/tenants_screen.dart';
import 'package:frontend/screen/user_details_screen.dart';
import 'package:frontend/screen/users_list_screen.dart';
import 'package:go_router/go_router.dart';

import 'di.dart';

class MyHttpOverrides extends HttpOverrides {
  @override
  HttpClient createHttpClient(SecurityContext? context) {
    return super.createHttpClient(context)
      ..badCertificateCallback = (X509Certificate cert, String host, int port) => true;
  }
}

void main() {
  if (!kIsWeb && kDebugMode) {
    HttpOverrides.global = MyHttpOverrides();
  }

  configureDependencies();

  runApp(const IDServer());
}

class RouteName {
  static const login = 'login';
  static const configuration = 'configuration';
  static const tenants = 'tenants';
  static const tenant = 'tenant';
  static const client = 'client';
  static const users = 'users';
  static const user = 'user';
}

// final AuthChangeNotifier authChangeNotifier = AuthChangeNotifier();

final GoRouter router = GoRouter(
  routes: <RouteBase>[
    GoRoute(
      path: '/',
      builder: (BuildContext context, GoRouterState state) {
        return const LoadingScreen();
      },
      routes: <RouteBase>[
        GoRoute(
          name: RouteName.login,
          path: 'login',
          pageBuilder: (context, state) => CustomTransitionPage<void>(
            key: state.pageKey,
            transitionDuration: const Duration(milliseconds: 800),
            reverseTransitionDuration: const Duration(milliseconds: 800),
            child: const LoginScreen(),
            transitionsBuilder: (context, animation, secondaryAnimation, child) =>
                FadeTransition(opacity: animation, child: child),
          ),
        ),
        GoRoute(
          name: RouteName.configuration,
          path: 'configuration',
          pageBuilder: (context, state) => NoTransitionPage<void>(
            key: state.pageKey,
            child: const PanelScreen(),
          ),
        ),
        GoRoute(
          name: RouteName.tenants,
          path: 'tenants',
          pageBuilder: (context, state) => NoTransitionPage<void>(
            key: state.pageKey,
            child: const TenantsScreen(),
          ),
        ),
        GoRoute(
          name: RouteName.tenant,
          path: 'tenant/:id',
          pageBuilder: (context, state) => NoTransitionPage<void>(
            key: state.pageKey,
            child: TenantDetailsScreen(id: int.parse(state.pathParameters['id']!)),
          ),
        ),
        GoRoute(
          name: RouteName.client,
          path: 'client/:tenantId/:clientId',
          pageBuilder: (context, state) => NoTransitionPage<void>(
            key: state.pageKey,
            child: ClientScreen(
              tenantId: int.parse(state.pathParameters['tenantId']!),
              clientId: state.pathParameters['clientId']!,
            ),
          ),
        ),
        GoRoute(
          name: RouteName.users,
          path: 'users/:tenantId',
          pageBuilder: (context, state) => NoTransitionPage<void>(
            key: state.pageKey,
            child: UsersListScreen(
              tenantId: int.parse(state.pathParameters['tenantId']!),
            ),
          ),
        ),
        GoRoute(
          name: RouteName.user,
          path: 'user/:tenantId/:userId',
          pageBuilder: (context, state) => NoTransitionPage<void>(
            key: state.pageKey,
            child: UsersDetailsScreen(
              tenantId: int.parse(state.pathParameters['tenantId']!),
              userId: state.pathParameters['userId']!,
            ),
          ),
        ),
      ],
    ),
  ],
);

bool? isAuthenticated;

String baseUrl = kIsWeb ? '${Uri.base.scheme}://${Uri.base.host}' : 'http://127.0.0.1';

final authenticationManager = AuthenticationManager('$baseUrl/admin/token', "adminClient", "adminSecret", (value) {
  log("Is Authenticated $value");

  if (value != isAuthenticated) {
    if (value) {
      router.pushReplacementNamed(RouteName.configuration);
    } else {
      router.pushReplacementNamed(RouteName.login);
    }
  }
  isAuthenticated = value;
});

class IDServer extends StatelessWidget {
  const IDServer({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      routerConfig: router,
      title: 'Id Server',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.deepPurple,
      ),
    );
  }
}
