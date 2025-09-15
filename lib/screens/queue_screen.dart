import 'package:flutter/material.dart';
import 'dart:async';
import '../models/queue_data.dart';
import '../services/queue_service.dart';

class QueueScreen extends StatefulWidget {
  const QueueScreen({super.key});

  @override
  State<QueueScreen> createState() => _QueueScreenState();
}

class _QueueScreenState extends State<QueueScreen>
    with SingleTickerProviderStateMixin {
  final QueueService _queueService = QueueService();
  QueueData? _queueData;
  Timer? _refreshTimer;
  late AnimationController _animationController;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: const Duration(seconds: 2),
      vsync: this,
    );
    _pulseAnimation = Tween<double>(begin: 0.8, end: 1.2).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeInOut),
    );
    _animationController.repeat(reverse: true);

    _loadQueueData();
    _startAutoRefresh();
  }

  @override
  void dispose() {
    _refreshTimer?.cancel();
    _animationController.dispose();
    super.dispose();
  }

  void _startAutoRefresh() {
    _refreshTimer = Timer.periodic(const Duration(seconds: 5), (timer) {
      _loadQueueData();
    });
  }

  Future<void> _loadQueueData() async {
    try {
      final data = await _queueService.getQueueStatus();
      setState(() => _queueData = data);
    } catch (e) {
      // Handle error silently for auto-refresh
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Queue Status'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadQueueData,
          ),
        ],
      ),
      body:
          _queueData == null
              ? const Center(child: CircularProgressIndicator())
              : RefreshIndicator(
                onRefresh: _loadQueueData,
                child: SingleChildScrollView(
                  physics: const AlwaysScrollableScrollPhysics(),
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      // Current Number Display
                      _buildCurrentNumberCard(),
                      const SizedBox(height: 20),

                      // Your Position (if has ticket)
                      if (_queueData?.userTicket != null) ...[
                        _buildYourPositionCard(),
                        const SizedBox(height: 20),
                      ],

                      // Queue Progress
                      _buildQueueProgressCard(),
                      const SizedBox(height: 20),

                      // Counter Status
                      _buildCounterStatusCard(),
                      const SizedBox(height: 20),

                      // Recent Activity
                      _buildRecentActivityCard(),
                    ],
                  ),
                ),
              ),
    );
  }

  Widget _buildCurrentNumberCard() {
    return Card(
      elevation: 8,
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(32),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          gradient: const LinearGradient(
            colors: [Color(0xFF2196F3), Color(0xFF1976D2)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: Column(
          children: [
            const Text(
              'NOW SERVING',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w500,
                color: Colors.white70,
                letterSpacing: 2,
              ),
            ),
            const SizedBox(height: 16),
            AnimatedBuilder(
              animation: _pulseAnimation,
              builder: (context, child) {
                return Transform.scale(
                  scale: _pulseAnimation.value,
                  child: Text(
                    '${_queueData?.currentNumber ?? '--'}',
                    style: const TextStyle(
                      fontSize: 80,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                );
              },
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.2),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.location_on, color: Colors.white, size: 16),
                  const SizedBox(width: 4),
                  Text(
                    'Counter ${_queueData?.currentCounter ?? 'N/A'}',
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildYourPositionCard() {
    final ticket = _queueData!.userTicket!;
    final position = ticket.number - (_queueData?.currentNumber ?? 0);
    final isNext = position <= 3;

    return Card(
      color: isNext ? const Color(0xFFE8F5E8) : null,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: isNext ? Colors.green : const Color(0xFF9C27B0),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(
                    isNext ? Icons.near_me : Icons.confirmation_number,
                    color: Colors.white,
                    size: 24,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        isNext ? 'You\'re Next!' : 'Your Position',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                          color: isNext ? Colors.green : Colors.black87,
                        ),
                      ),
                      Text(
                        'Ticket #${ticket.number}',
                        style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                      ),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '$position',
                      style: TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.bold,
                        color: isNext ? Colors.green : const Color(0xFF9C27B0),
                      ),
                    ),
                    Text(
                      position == 1 ? 'person ahead' : 'people ahead',
                      style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                    ),
                  ],
                ),
              ],
            ),
            if (isNext) ...[
              const SizedBox(height: 16),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.green.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.green.withOpacity(0.3)),
                ),
                child: const Row(
                  children: [
                    Icon(
                      Icons.notifications_active,
                      color: Colors.green,
                      size: 20,
                    ),
                    SizedBox(width: 8),
                    Text(
                      'Get ready! You\'ll be called soon.',
                      style: TextStyle(
                        color: Colors.green,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildQueueProgressCard() {
    final totalServed = _queueData?.servedToday ?? 0;
    final totalWaiting = _queueData?.totalWaiting ?? 0;
    final currentNumber = _queueData?.currentNumber ?? 0;
    final progress =
        totalServed > 0 ? totalServed / (totalServed + totalWaiting) : 0.0;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Queue Progress',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Current: $currentNumber',
                        style: const TextStyle(fontWeight: FontWeight.w500),
                      ),
                      const SizedBox(height: 8),
                      LinearProgressIndicator(
                        value: progress,
                        backgroundColor: Colors.grey[300],
                        valueColor: const AlwaysStoppedAnimation<Color>(
                          Color(0xFF2196F3),
                        ),
                      ),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            'Served: $totalServed',
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey[600],
                            ),
                          ),
                          Text(
                            'Waiting: $totalWaiting',
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey[600],
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCounterStatusCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Counter Status',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                childAspectRatio: 1.2,
                crossAxisSpacing: 12,
                mainAxisSpacing: 12,
              ),
              itemCount: 6, // Example: 6 counters
              itemBuilder: (context, index) {
                final counterNumber = index + 1;
                final isActive =
                    counterNumber <= (_queueData?.activeCounters ?? 0);
                final isCurrent =
                    counterNumber == (_queueData?.currentCounter ?? 0);

                return Container(
                  decoration: BoxDecoration(
                    color:
                        isCurrent
                            ? const Color(0xFF2196F3)
                            : isActive
                            ? Colors.green.withOpacity(0.1)
                            : Colors.grey.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(
                      color:
                          isCurrent
                              ? const Color(0xFF2196F3)
                              : isActive
                              ? Colors.green
                              : Colors.grey,
                    ),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        isActive
                            ? Icons.desktop_windows
                            : Icons.desktop_access_disabled,
                        color:
                            isCurrent
                                ? Colors.white
                                : isActive
                                ? Colors.green
                                : Colors.grey,
                        size: 24,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Counter $counterNumber',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                          color:
                              isCurrent
                                  ? Colors.white
                                  : isActive
                                  ? Colors.green
                                  : Colors.grey,
                        ),
                      ),
                      Text(
                        isCurrent
                            ? 'Active'
                            : isActive
                            ? 'Available'
                            : 'Offline',
                        style: TextStyle(
                          fontSize: 10,
                          color:
                              isCurrent
                                  ? Colors.white70
                                  : isActive
                                  ? Colors.green.withOpacity(0.7)
                                  : Colors.grey.withOpacity(0.7),
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRecentActivityCard() {
    final recentNumbers =
        List.generate(
          5,
          (index) => (_queueData?.currentNumber ?? 0) - index - 1,
        ).where((number) => number > 0).toList();

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Recent Activity',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            if (recentNumbers.isEmpty)
              const Center(
                child: Text(
                  'No recent activity',
                  style: TextStyle(color: Colors.grey),
                ),
              )
            else
              Column(
                children:
                    recentNumbers.map((number) {
                      final isRecent = recentNumbers.indexOf(number) < 3;
                      return Container(
                        margin: const EdgeInsets.only(bottom: 8),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color:
                              isRecent
                                  ? Colors.green.withOpacity(0.1)
                                  : Colors.grey.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Row(
                          children: [
                            Icon(
                              Icons.check_circle,
                              color: isRecent ? Colors.green : Colors.grey,
                              size: 20,
                            ),
                            const SizedBox(width: 12),
                            Text(
                              'Ticket #$number served',
                              style: TextStyle(
                                fontWeight: FontWeight.w500,
                                color:
                                    isRecent ? Colors.green : Colors.grey[600],
                              ),
                            ),
                            const Spacer(),
                            Text(
                              '${(recentNumbers.indexOf(number) + 1) * 2}m ago',
                              style: TextStyle(
                                fontSize: 12,
                                color: Colors.grey[600],
                              ),
                            ),
                          ],
                        ),
                      );
                    }).toList(),
              ),
          ],
        ),
      ),
    );
  }
}
