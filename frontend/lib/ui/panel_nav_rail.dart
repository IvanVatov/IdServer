import 'package:flutter/material.dart';
import 'package:frontend/main.dart';
import 'package:frontend/ui/text_styles.dart';
import 'package:go_router/go_router.dart';

class PanelNavRail extends StatelessWidget {
  const PanelNavRail({super.key});

  @override
  Widget build(BuildContext context) {
    String location = GoRouterState.of(context).uri.toString();

    int? selected;
    if (location == '/configuration') {
      selected = 0;
    } else if (location == '/tenants') {
      selected = 1;
    }

    return NavigationRail(
      extended: true,
      minExtendedWidth: 160,
      destinations: const [
        NavigationRailDestination(
          icon: Icon(
            Icons.home,
            color: Colors.deepPurple,
          ),
          label: Heading18(text: 'Home'),
        ),
        NavigationRailDestination(
          icon: Icon(
            Icons.apps,
            color: Colors.deepPurple,
          ),
          label: Heading18(text: 'Tenants'),
        ),
      ],
      selectedIndex: selected,
      onDestinationSelected: (value) {
        if (value == 0) {
          context.pushReplacementNamed(RouteName.configuration);
        } else if (value == 1) {
          context.pushReplacementNamed(RouteName.tenants);
        }
      },
    );
  }
}
