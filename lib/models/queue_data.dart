class QueueData {
  final int currentNumber;
  final int currentCounter;
  final int totalWaiting;
  final int servedToday;
  final int avgWaitTime;
  final int activeCounters;
  final Ticket? userTicket;

  QueueData({
    required this.currentNumber,
    required this.currentCounter,
    required this.totalWaiting,
    required this.servedToday,
    required this.avgWaitTime,
    required this.activeCounters,
    this.userTicket,
  });

  QueueData copyWith({
    int? currentNumber,
    int? currentCounter,
    int? totalWaiting,
    int? servedToday,
    int? avgWaitTime,
    int? activeCounters,
    Ticket? userTicket,
  }) {
    return QueueData(
      currentNumber: currentNumber ?? this.currentNumber,
      currentCounter: currentCounter ?? this.currentCounter,
      totalWaiting: totalWaiting ?? this.totalWaiting,
      servedToday: servedToday ?? this.servedToday,
      avgWaitTime: avgWaitTime ?? this.avgWaitTime,
      activeCounters: activeCounters ?? this.activeCounters,
      userTicket: userTicket ?? this.userTicket,
    );
  }

  factory QueueData.fromJson(Map<String, dynamic> json) {
    return QueueData(
      currentNumber: json['currentNumber'] ?? 0,
      currentCounter: json['currentCounter'] ?? 1,
      totalWaiting: json['totalWaiting'] ?? 0,
      servedToday: json['servedToday'] ?? 0,
      avgWaitTime: json['avgWaitTime'] ?? 0,
      activeCounters: json['activeCounters'] ?? 1,
      userTicket:
          json['userTicket'] != null
              ? Ticket.fromJson(json['userTicket'])
              : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'currentNumber': currentNumber,
      'currentCounter': currentCounter,
      'totalWaiting': totalWaiting,
      'servedToday': servedToday,
      'avgWaitTime': avgWaitTime,
      'activeCounters': activeCounters,
      'userTicket': userTicket?.toJson(),
    };
  }
}

class Ticket {
  final int number;
  final String status;
  final DateTime issuedAt;
  final DateTime? calledAt;
  final DateTime? servedAt;
  final int? counterNumber;

  Ticket({
    required this.number,
    required this.status,
    required this.issuedAt,
    this.calledAt,
    this.servedAt,
    this.counterNumber,
  });

  factory Ticket.fromJson(Map<String, dynamic> json) {
    return Ticket(
      number: json['number'],
      status: json['status'],
      issuedAt: DateTime.parse(json['issuedAt']),
      calledAt:
          json['calledAt'] != null ? DateTime.parse(json['calledAt']) : null,
      servedAt:
          json['servedAt'] != null ? DateTime.parse(json['servedAt']) : null,
      counterNumber: json['counterNumber'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'number': number,
      'status': status,
      'issuedAt': issuedAt.toIso8601String(),
      'calledAt': calledAt?.toIso8601String(),
      'servedAt': servedAt?.toIso8601String(),
      'counterNumber': counterNumber,
    };
  }
}

class HistoryItem {
  final int ticketNumber;
  final DateTime date;
  final String status;
  final int counterNumber;
  final int waitTimeMinutes;
  final String serviceTime;
  final String notes;

  HistoryItem({
    required this.ticketNumber,
    required this.date,
    required this.status,
    required this.counterNumber,
    required this.waitTimeMinutes,
    required this.serviceTime,
    this.notes = '',
  });

  factory HistoryItem.fromJson(Map<String, dynamic> json) {
    return HistoryItem(
      ticketNumber: json['ticketNumber'],
      date: DateTime.parse(json['date']),
      status: json['status'],
      counterNumber: json['counterNumber'],
      waitTimeMinutes: json['waitTimeMinutes'],
      serviceTime: json['serviceTime'],
      notes: json['notes'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'ticketNumber': ticketNumber,
      'date': date.toIso8601String(),
      'status': status,
      'counterNumber': counterNumber,
      'waitTimeMinutes': waitTimeMinutes,
      'serviceTime': serviceTime,
      'notes': notes,
    };
  }
}

class NotificationMessage {
  final String id;
  final String title;
  final String body;
  final DateTime timestamp;
  final String type;
  final Map<String, dynamic>? data;
  final bool isRead;

  NotificationMessage({
    required this.id,
    required this.title,
    required this.body,
    required this.timestamp,
    required this.type,
    this.data,
    this.isRead = false,
  });

  factory NotificationMessage.fromJson(Map<String, dynamic> json) {
    return NotificationMessage(
      id: json['id'],
      title: json['title'],
      body: json['body'],
      timestamp: DateTime.parse(json['timestamp']),
      type: json['type'],
      data: json['data'],
      isRead: json['isRead'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'body': body,
      'timestamp': timestamp.toIso8601String(),
      'type': type,
      'data': data,
      'isRead': isRead,
    };
  }

  NotificationMessage copyWith({
    String? id,
    String? title,
    String? body,
    DateTime? timestamp,
    String? type,
    Map<String, dynamic>? data,
    bool? isRead,
  }) {
    return NotificationMessage(
      id: id ?? this.id,
      title: title ?? this.title,
      body: body ?? this.body,
      timestamp: timestamp ?? this.timestamp,
      type: type ?? this.type,
      data: data ?? this.data,
      isRead: isRead ?? this.isRead,
    );
  }
}
