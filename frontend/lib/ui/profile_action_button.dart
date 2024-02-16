import 'package:flutter/material.dart';
import 'package:frontend/domain/api_service.dart';
import 'package:frontend/main.dart';
import 'package:go_router/go_router.dart';

class ProfileActionButton extends StatelessWidget {
  const ProfileActionButton({
    super.key,
    required this.service,
  });

  final ApiService service;

  @override
  Widget build(BuildContext context) {
    ImageProvider avatarImage;

    if (service.user?.picture != null) {
      avatarImage = NetworkImage(service.user!.picture!);
    } else {
      avatarImage = const AssetImage('assets/images/default_avatar.png');
    }

    return PopupMenuButton(
        child: Center(
          child: Container(
            margin: const EdgeInsets.only(left: 16, right: 16),
            child: CircleAvatar(
              backgroundColor: Colors.deepPurple,
              backgroundImage: avatarImage,
            ),
          ),
        ),
        itemBuilder: (BuildContext context) {
          return [
            PopupMenuItem(
              onTap: () {
                authenticationManager.logout();
                // context.pushReplacementNamed(RouteName.login);
              },
              child: const Row(children: [
                Icon(
                  Icons.logout,
                  color: Colors.black,
                ),
                SizedBox(width: 8),
                Text("Logout"),
              ]),
            ),
          ];
        });
  }
}
