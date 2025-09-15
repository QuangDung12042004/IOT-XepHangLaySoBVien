import 'package:flutter/material.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _notificationsEnabled = true;
  bool _soundEnabled = true;
  bool _vibrationEnabled = true;
  bool _darkModeEnabled = false;
  String _selectedLanguage = 'English';
  int _notificationMinutes = 5;
  String _preferredCounter = 'Any';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Profile Section
          _buildProfileSection(),
          const SizedBox(height: 20),

          // Notification Settings
          _buildNotificationSection(),
          const SizedBox(height: 20),

          // Queue Preferences
          _buildQueuePreferencesSection(),
          const SizedBox(height: 20),

          // App Settings
          _buildAppSettingsSection(),
          const SizedBox(height: 20),

          // Support & About
          _buildSupportSection(),
        ],
      ),
    );
  }

  Widget _buildProfileSection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Profile',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                CircleAvatar(
                  radius: 30,
                  backgroundColor: const Color(0xFF2196F3),
                  child: const Text(
                    'U',
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Guest User',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      Text(
                        'Tap to sign in for more features',
                        style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  onPressed: () {
                    // Navigate to profile/login
                  },
                  icon: const Icon(Icons.edit),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNotificationSection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Notifications',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            _buildSwitchTile(
              'Push Notifications',
              'Get notified when it\'s your turn',
              _notificationsEnabled,
              (value) => setState(() => _notificationsEnabled = value),
              Icons.notifications,
            ),
            _buildSwitchTile(
              'Sound',
              'Play notification sound',
              _soundEnabled,
              (value) => setState(() => _soundEnabled = value),
              Icons.volume_up,
            ),
            _buildSwitchTile(
              'Vibration',
              'Vibrate on notifications',
              _vibrationEnabled,
              (value) => setState(() => _vibrationEnabled = value),
              Icons.vibration,
            ),
            const SizedBox(height: 16),
            _buildNotificationTimingTile(),
          ],
        ),
      ),
    );
  }

  Widget _buildQueuePreferencesSection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Queue Preferences',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            _buildDropdownTile(
              'Preferred Counter',
              'Choose your preferred service counter',
              _preferredCounter,
              ['Any', 'Counter 1', 'Counter 2', 'Counter 3', 'Counter 4'],
              (value) => setState(() => _preferredCounter = value!),
              Icons.desktop_windows,
            ),
            _buildTile(
              'Auto-refresh',
              'Automatically update queue status',
              Icons.refresh,
              trailing: Switch(
                value: true,
                onChanged: (value) {
                  // Handle auto-refresh toggle
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAppSettingsSection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'App Settings',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            _buildDropdownTile(
              'Language',
              'Choose app language',
              _selectedLanguage,
              ['English', 'Vietnamese', 'Spanish', 'French'],
              (value) => setState(() => _selectedLanguage = value!),
              Icons.language,
            ),
            _buildSwitchTile(
              'Dark Mode',
              'Use dark theme',
              _darkModeEnabled,
              (value) => setState(() => _darkModeEnabled = value),
              Icons.dark_mode,
            ),
            _buildTile(
              'Clear Cache',
              'Free up storage space',
              Icons.delete_sweep,
              onTap: _showClearCacheDialog,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSupportSection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Support & About',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            _buildTile(
              'Help & FAQ',
              'Get help and find answers',
              Icons.help_outline,
              onTap: () {
                // Navigate to help
              },
            ),
            _buildTile(
              'Contact Support',
              'Get in touch with our team',
              Icons.support_agent,
              onTap: () {
                // Open contact support
              },
            ),
            _buildTile(
              'Rate App',
              'Rate us on the app store',
              Icons.star_outline,
              onTap: () {
                // Open app store rating
              },
            ),
            _buildTile(
              'Privacy Policy',
              'Read our privacy policy',
              Icons.privacy_tip,
              onTap: () {
                // Open privacy policy
              },
            ),
            _buildTile(
              'About App',
              'Version 1.0.0',
              Icons.info_outline,
              onTap: _showAboutDialog,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSwitchTile(
    String title,
    String subtitle,
    bool value,
    ValueChanged<bool> onChanged,
    IconData icon,
  ) {
    return _buildTile(
      title,
      subtitle,
      icon,
      trailing: Switch(value: value, onChanged: onChanged),
    );
  }

  Widget _buildDropdownTile(
    String title,
    String subtitle,
    String value,
    List<String> options,
    ValueChanged<String?> onChanged,
    IconData icon,
  ) {
    return _buildTile(
      title,
      subtitle,
      icon,
      trailing: DropdownButton<String>(
        value: value,
        onChanged: onChanged,
        underline: const SizedBox(),
        items:
            options.map((option) {
              return DropdownMenuItem<String>(
                value: option,
                child: Text(option),
              );
            }).toList(),
      ),
    );
  }

  Widget _buildNotificationTimingTile() {
    return _buildTile(
      'Notify me before',
      'Get notified $_notificationMinutes minutes before your turn',
      Icons.schedule,
      trailing: DropdownButton<int>(
        value: _notificationMinutes,
        onChanged: (value) {
          setState(() => _notificationMinutes = value!);
        },
        underline: const SizedBox(),
        items:
            [1, 3, 5, 10, 15].map((minutes) {
              return DropdownMenuItem<int>(
                value: minutes,
                child: Text('${minutes}m'),
              );
            }).toList(),
      ),
    );
  }

  Widget _buildTile(
    String title,
    String subtitle,
    IconData icon, {
    Widget? trailing,
    VoidCallback? onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: const Color(0xFF2196F3).withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, color: const Color(0xFF2196F3), size: 20),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  Text(
                    subtitle,
                    style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
            if (trailing != null) trailing,
            if (onTap != null && trailing == null)
              Icon(Icons.arrow_forward_ios, size: 16, color: Colors.grey[400]),
          ],
        ),
      ),
    );
  }

  void _showClearCacheDialog() {
    showDialog(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text('Clear Cache'),
            content: const Text(
              'This will clear all cached data and may require you to log in again. Continue?',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Cancel'),
              ),
              ElevatedButton(
                onPressed: () {
                  Navigator.pop(context);
                  _clearCache();
                },
                child: const Text('Clear'),
              ),
            ],
          ),
    );
  }

  void _clearCache() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Cache cleared successfully'),
        backgroundColor: Colors.green,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  void _showAboutDialog() {
    showAboutDialog(
      context: context,
      applicationName: 'Queue Management System',
      applicationVersion: '1.0.0',
      applicationIcon: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: const Color(0xFF2196F3),
          borderRadius: BorderRadius.circular(8),
        ),
        child: const Icon(Icons.queue, color: Colors.white, size: 32),
      ),
      children: [
        const Text(
          'A modern queue management system that helps you track your position and get notified when it\'s your turn.',
        ),
        const SizedBox(height: 16),
        const Text('Features:', style: TextStyle(fontWeight: FontWeight.bold)),
        const Text('• Real-time queue updates'),
        const Text('• Push notifications'),
        const Text('• Queue history'),
        const Text('• Multi-counter support'),
      ],
    );
  }
}
