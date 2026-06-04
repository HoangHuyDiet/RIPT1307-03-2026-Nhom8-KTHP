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
    targetRole: 'MEMBER',
    link_action: '/dashboard'
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
    targetRole: 'MEMBER',
    link_action: '/dashboard'
  },
  {
    id: 'goal_1',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '🎯 Đạt tiến độ: Mục tiêu "Mua Nhà (Hà Nội)" của bạn đã tích lũy được 45% (900.000.000 đ / 2.000.000.000 đ)!',
    requesterName: 'Mục tiêu tiết kiệm',
    date: 'Hôm nay, 08:15',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/saving-goals?id=1'
  },
  {
    id: 'goal_2',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '⏰ Sắp đến hạn: Mục tiêu "Du lịch Châu Âu" còn 3 ngày nữa là đến hạn hoàn thành. Tiến độ hiện tại là 82%.',
    requesterName: 'Mục tiêu tiết kiệm',
    date: '2 ngày trước',
    read: true,
    targetRole: 'MEMBER',
    link_action: '/saving-goals?id=2'
  },
  {
    id: 'goal_3',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '🎯 Tiến độ tích lũy: Mục tiêu "Mua xe Honda SH" đã đạt 20.000.000 đ (20% kế hoạch).',
    requesterName: 'Mục tiêu tiết kiệm',
    date: 'Hôm nay, 10:00',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/saving-goals?id=3'
  },
  {
    id: 'goal_4',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '⏰ Nhắc nhở: Hãy gửi thêm 10.000.000 đ để hoàn thành mục tiêu "Quỹ dự phòng y tế".',
    requesterName: 'Mục tiêu tiết kiệm',
    date: 'Hôm qua, 14:00',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/saving-goals?id=4'
  },
  {
    id: 'goal_5',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '🎯 Cập nhật: Mục tiêu "Học phí Cao học" đã bắt đầu tích lũy từ tuần này.',
    requesterName: 'Mục tiêu tiết kiệm',
    date: '3 ngày trước',
    read: true,
    targetRole: 'MEMBER',
    link_action: '/saving-goals?id=5'
  },
  {
    id: 'goal_6',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '⏰ Sắp đến hạn: Mục tiêu "Mua Macbook Pro M4" đã tích lũy được 55.000.000 đ / 60.000.000 đ (91%).',
    requesterName: 'Mục tiêu tiết kiệm',
    date: 'Hôm nay, 12:00',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/saving-goals?id=6'
  },
  {
    id: 'goal_7',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '💡 Khởi tạo: Mục tiêu "Khởi nghiệp quán cà phê" đã được tạo thành công với số tiền 200.000.000 đ.',
    requesterName: 'Mục tiêu tiết kiệm',
    date: 'Hôm qua, 08:00',
    read: true,
    targetRole: 'MEMBER',
    link_action: '/saving-goals?id=7'
  },
  {
    id: 'pf_1',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '⚠️ Cảnh báo số dư: Quỹ "Tiền mặt" của bạn hiện dưới mức tối thiểu thiết lập (450.000 đ / 500.000 đ).',
    requesterName: 'Quỹ cá nhân',
    date: 'Hôm nay, 11:00',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/personal-funds?id=1'
  },
  {
    id: 'pf_2',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 350000,
    description: '💳 Nhắc nhở: Lịch thanh toán hóa đơn điện tháng này trị giá 350.000 đ của quỹ "Vietcombank" sắp đến hạn (05/06/2026).',
    requesterName: 'Nhắc lịch thanh toán',
    date: 'Hôm qua, 10:00',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/personal-funds?action=pay-bill&fundId=2'
  },
  {
    id: 'pf_3',
    type: 'SYSTEM_INFO',
    fundId: 0,
    fundName: '',
    amount: 0,
    description: '⚠️ Cảnh báo số dư: Quỹ "Thẻ tín dụng (Dư nợ)" của bạn đã vượt quá giới hạn an toàn chi tiêu tháng này.',
    requesterName: 'Quỹ cá nhân',
    date: '2 ngày trước',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/personal-funds?id=3'
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
    targetRole: 'MEMBER',
    link_action: '/transactions'
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
    targetRole: 'OWNER',
    link_action: '/funds?id=1'
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
    targetRole: 'OWNER',
    link_action: '/funds?id=1'
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
    targetRole: 'MEMBER',
    link_action: '/funds?id=1'
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
    targetRole: 'MEMBER',
    link_action: '/funds?id=2'
  },
  {
    id: 'fund_invitation_mock_1',
    type: 'FUND_INVITATION',
    fundId: 1,
    fundName: 'Du Lịch Gia Đình',
    amount: 0,
    description: 'Alice Nguyễn đã mời bạn tham gia quỹ nhóm "Du Lịch Gia Đình". Vui lòng kiểm tra email của bạn để xác nhận.',
    requesterName: 'Alice Nguyễn',
    date: 'Hôm nay, 15:45',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/funds?id=1'
  },
  {
    id: 'fund_disband_mock_1',
    type: 'FUND_DISBAND_PROPOSAL',
    fundId: 2,
    fundName: 'Chi Phí Bạn Cùng Phòng',
    amount: 0,
    description: 'Chủ quỹ Bùi Minh đề xuất giải tán quỹ nhóm "Chi Phí Bạn Cùng Phòng". Vui lòng kiểm tra email của bạn để xác nhận.',
    requesterName: 'Bùi Minh',
    date: 'Hôm nay, 16:10',
    read: false,
    targetRole: 'MEMBER',
    link_action: '/funds?id=2'
  },
  {
    id: 'fund_removed_mock_1',
    type: 'FUND_MEMBER_REMOVED',
    fundId: 3,
    fundName: 'Quỹ Ăn Nhậu',
    amount: 0,
    description: 'Bạn đã bị xóa khỏi quỹ nhóm "Quỹ Ăn Nhậu" bởi chủ quỹ.',
    requesterName: 'Trưởng nhóm',
    date: 'Hôm qua, 09:00',
    read: true,
    targetRole: 'MEMBER',
    link_action: '/funds?id=3'
  }
];

const GOAL_NAMES = [
  'Mua Nhà (Hà Nội)',
  'Du lịch Châu Âu',
  'Mua xe Honda SH',
  'Quỹ dự phòng y tế',
  'Học phí Cao học',
  'Mua Macbook Pro M4',
  'Khởi nghiệp quán cà phê'
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
      targetRole: 'MEMBER',
      link_action: '/dashboard'
    });
  } else if (typeIndex === 1) {
    const goalIdx = i % 7;
    const targetId = goalIdx + 1;
    const goalName = GOAL_NAMES[goalIdx];
    generatedNotifications.push({
      id: `goal_extra_${i}`,
      type: 'SYSTEM_INFO',
      fundId: 0,
      fundName: '',
      amount: 0,
      description: `🎯 Tiến độ tích lũy mục tiêu "${goalName}": Đang hoạt động ổn định.`,
      requesterName: 'Mục tiêu tiết kiệm',
      date: `${i} ngày trước`,
      read: true,
      targetRole: 'MEMBER',
      link_action: `/saving-goals?id=${targetId}`
    });
  } else if (typeIndex === 2) {
    const actionType = i % 3;
    let targetLink = '/personal-funds';
    let targetDesc = `💳 Nhắc lịch quỹ cá nhân số ${i}: Thanh toán dịch vụ tháng định kỳ.`;
    if (actionType === 0) {
      targetLink = '/personal-funds?id=1';
      targetDesc = `⚠️ Cảnh báo số dư: Ví "Tiền mặt" của bạn hiện dưới mức tối thiểu (mẫu số ${i}).`;
    } else if (actionType === 1) {
      targetLink = '/personal-funds?id=3';
      targetDesc = `⚠️ Cảnh báo số dư: Quỹ "Thẻ tín dụng (Dư nợ)" cần được thanh toán định kỳ (mẫu số ${i}).`;
    } else {
      targetLink = '/personal-funds?action=pay-bill&fundId=2';
      targetDesc = `💳 Nhắc nhở: Lịch đóng tiền mạng tháng này qua quỹ "Vietcombank" (mẫu số ${i}).`;
    }
    generatedNotifications.push({
      id: `pf_extra_${i}`,
      type: 'SYSTEM_INFO',
      fundId: 0,
      fundName: '',
      amount: 150000,
      description: targetDesc,
      requesterName: 'Quỹ cá nhân',
      date: `${i} ngày trước`,
      read: true,
      targetRole: 'MEMBER',
      link_action: targetLink
    });
  } else {
    generatedNotifications.push({
      id: `fund_extra_${i}`,
      type: 'DEPOSIT_APPROVED',
      fundId: 1,
      fundName: 'Du Lịch Gia Đình',
      amount: 100000,
      description: `Nạp tiền đóng góp nhóm lần ${i}`,
      requesterName: 'Thành viên nhóm',
      date: `${i} ngày trước`,
      read: true,
      targetRole: 'MEMBER',
      link_action: '/funds?id=1'
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
