import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:frontend/di.dart';
import 'package:frontend/domain/api_service.dart';
import 'package:frontend/main.dart';
import 'package:go_router/go_router.dart';

class LoadingScreen extends StatefulWidget {
  const LoadingScreen({super.key});

  @override
  State<LoadingScreen> createState() => _LoadingScreenState();
}

class _LoadingScreenState extends State<LoadingScreen> {
  final ApiService apiService = getIt<ApiService>();

  @override
  void initState() {
    super.initState();
    //
    Future.delayed(const Duration(seconds: 2), () {
      try {
        authenticationManager.tryRefresh();
      } catch (e) {
        e;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: true,
      onPopInvoked: (didPop) {},
      child: Scaffold(
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              Hero(
                tag: 'logo',
                child: SvgPicture.asset(
                  'assets/images/logo.svg',
                  width: 662,
                  height: 160,
                ),
              ),
              const SpinKitFoldingCube(
                color: Colors.deepPurple,
                size: 50.0,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
