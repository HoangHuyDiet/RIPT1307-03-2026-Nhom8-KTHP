const baseNotifications = [
  {
    id: 'dash_1',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '💡 Gợi ý từ AI: Bạn đang chi tiêu vượt mức 15% cho mục Ăn uống trong tuần này. Hãy cân nhắc điều chỉnh.',
    requesterName: 'Hệ thống AI',
    date: 'Hôm nay, 09:30',
    read: false,
    targetRole: 'MEMBER'
  },
  {
    id: 'dash_2',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '📊 Báo cáo tài chính tuần qua của bạn đã sẵn sàng. Tổng tài sản tăng trưởng +4.2%!',
    requesterName: 'Hệ thống',
    date: 'Hôm qua, 18:00',
    read: true,
    targetRole: 'MEMBER'
  },
  {
    id: 'goal_1',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '🎯 Đạt tiến độ: Mục tiêu "Mua xe Honda Vision" của bạn đã tích lũy được 80% (24.000.000 đ / 30.000.000 đ)!',
    requesterName: 'Mục tiêu tiết kiệm',
    date: 'Hôm nay, 08:15',
    read: false,
    targetRole: 'MEMBER'
  },
  {
    id: 'goal_2',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '⏰ Sắp đến hạn: Mục tiêu "Du lịch SaPa" còn 3 ngày nữa là đến hạn hoàn thành. Số dư hiện tại là 90%.',
    requesterName: 'Mục tiêu tiết kiệm',
    date: '2 ngày trước',
    read: true,
    targetRole: 'MEMBER'
  },
  {
    id: 'pf_1',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '⚠️ Cảnh báo số dư: Quỹ "Ví chi tiêu" của bạn hiện dưới mức tối thiểu thiết lập (450.000 đ / 500.000 đ).',
    requesterName: 'Quỹ cá nhân',
    date: 'Hôm nay, 11:00',
    read: false,
    targetRole: 'MEMBER'
  },
  {
    id: 'pf_2',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 350000,
    description: '💳 Nhắc nhở: Lịch thanh toán hóa đơn điện tháng này trị giá 350.000 đ của quỹ "Thanh toán" sắp đến hạn (05/06/2026).',
    requesterName: 'Nhắc lịch thanh toán',
    date: 'Hôm qua, 10:00',
    read: false,
    targetRole: 'MEMBER'
  },
  {
    id: 'tx_1',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 1000000,
    description: '💸 Chuyển tiền tự động: Hệ thống đã tự động thực hiện giao dịch chuyển 1.000.000 đ từ "Lương" sang quỹ "Tiết kiệm".',
    requesterName: 'Giao dịch định kỳ',
    date: '3 ngày trước',
    read: true,
    targetRole: 'MEMBER'
  },
  {
    id: 'fund_req_1',
    type: 'DEPOSIT_REQUEST',
    fundId: 1,
    fundName: 'Du Lịch Gia Đình',
    amount: 500000,
    description: 'Nạp tiền đóng góp quỹ tháng 8',
    requesterName: 'Alice Nguyễn',
    date: 'Hôm nay, 14:20',
    read: false,
    targetRole: 'OWNER'
  },
  {
    id: 'fund_req_2',
    type: 'WITHDRAW_REQUEST',
    fundId: 1,
    fundName: 'Du Lịch Gia Đình',
    amount: 1200000,
    description: 'Rút tiền mua đồ ăn tối nhóm',
    requesterName: 'Bùi Minh',
    bankAccount: '190345678910',
    bankName: 'Techcombank',
    date: 'Hôm nay, 13:05',
    read: false,
    targetRole: 'OWNER'
  },
  {
    id: 'fund_approved_1',
    type: 'DEPOSIT_APPROVED',
    fundId: 1,
    fundName: 'Du Lịch Gia Đình',
    amount: 500000,
    description: 'Nội dung: Đóng góp vào quỹ',
    requesterName: 'Trưởng nhóm',
    date: 'Hôm qua, 15:30',
    read: true,
    targetRole: 'MEMBER'
  },
  {
    id: 'fund_rejected_1',
    type: 'WITHDRAW_REJECTED',
    fundId: 2,
    fundName: 'Chi Phí Bạn Cùng Phòng',
    amount: 300000,
    description: 'Lý do: Hóa đơn không hợp lệ',
    requesterName: 'Trưởng nhóm',
    date: '4 ngày trước',
    read: true,
    targetRole: 'MEMBER'
  }
];

const generatedNotifications = [...baseNotifications];
for (let i = 1; i <= 80; i++) {
  const typeIndex = i % 4;
  if (typeIndex === 0) {
    generatedNotifications.push({
      id: `dash_extra_${i}`,
      type: 'SYSTEM_INFO',
      fundId: 0,
      fundName: '',
      amount: 0,
      description: `💡 Gợi ý tự động từ AI số ${i}: Phân tích chi tiêu hàng tuần của bạn.`,
      requesterName: 'Hệ thống AI',
      date: `${i} ngày trước`,
      read: true,
      targetRole: 'MEMBER'
    });
  } else if (typeIndex === 1) {
    generatedNotifications.push({
      id: `goal_extra_${i}`,
      type: 'SYSTEM_INFO',
      fundId: 0,
      fundName: '',
      amount: 0,
      description: `🎯 Tiến độ tích lũy mục tiêu phụ số ${i}: Đang hoạt động ổn định.`,
      requesterName: 'Mục tiêu tiết kiệm',
      date: `${i} ngày trước`,
      read: true,
      targetRole: 'MEMBER'
    });
  } else if (typeIndex === 2) {
    generatedNotifications.push({
      id: `pf_extra_${i}`,
      type: 'SYSTEM_INFO',
      fundId: 0,
      fundName: '',
      amount: 150000,
      description: `💳 Nhắc lịch quỹ cá nhân số ${i}: Thanh toán dịch vụ tháng định kỳ.`,
      requesterName: 'Quỹ cá nhân',
      date: `${i} ngày trước`,
      read: true,
      targetRole: 'MEMBER'
    });
  } else {
    generatedNotifications.push({
      id: `fund_extra_${i}`,
      type: 'DEPOSIT_APPROVED',
      fundId: 1,
      fundName: 'Quỹ nhóm test',
      amount: 100000,
      description: `Nạp tiền đóng góp nhóm lần ${i}`,
      requesterName: 'Thành viên nhóm',
      date: `${i} ngày trước`,
      read: true,
      targetRole: 'MEMBER'
    });
  }
}

let mockNotifications = generatedNotifications;

export default {
  'GET /api/funds/my-notifications': (req: any, res: any) => {
    res.send({
      success: true,
      data: mockNotifications
    });
  },

  'POST /api/funds/my-notifications/read': (req: any, res: any) => {
    const { id } = req.body;
    mockNotifications = mockNotifications.map(n => n.id === id ? { ...n, read: true } : n);
    res.send({
      success: true,
      message: 'Đã đánh dấu đã đọc'
    });
  },

  'POST /api/funds/my-notifications/read-all': (req: any, res: any) => {
    mockNotifications = mockNotifications.map(n => ({ ...n, read: true }));
    res.send({
      success: true,
      message: 'Đã đánh dấu tất cả đã đọc'
    });
  },

  'POST /api/funds/my-notifications/delete': (req: any, res: any) => {
    const { id } = req.body;
    mockNotifications = mockNotifications.filter(n => n.id !== id);
    res.send({
      success: true,
      message: 'Đã xóa thông báo thành công'
    });
  },

  'POST /api/funds/my-notifications/delete-all': (req: any, res: any) => {
    mockNotifications = [];
    res.send({
      success: true,
      message: 'Đã xóa tất cả thông báo'
    });
  }
};
