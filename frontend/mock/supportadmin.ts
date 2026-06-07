type ChatRequest = { id: number; name: string; email: string; lastMessage: string; time: string; status: 'PENDING' | 'RESOLVED'; messages: any[]; priority: 'HIGH' | 'MEDIUM' | 'LOW' };
type UserAccount = { id: number; email: string; name: string; status: 'ACTIVE' | 'BANNED'; role: string; balance: number; createdAt: string };
type AuditLog = { key: string; time: string; action: string; targetUser: string; ip: string; status: string };
type Fund = { id: string; name: string; owner: string; balance: number; status: 'ACTIVE' | 'FROZEN' };
type Transaction = { id: string; description: string; amount: number; sender: string; receiver: string; date: string; status: 'SUCCESS' | 'REVERTED' };
type Broadcast = { key: string; title: string; content: string; target: string; urgency: 'INFO' | 'WARNING' | 'CRITICAL'; time: string };

interface LockRequest {
  id: number;
  email: string;
  name: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  time: string;
}

let lockRequests: LockRequest[] = [
  {
    id: 1,
    email: 'tranb@gmail.com',
    name: 'Trần Thị B',
    reason: 'Spam giao dịch quỹ nhóm liên tục, vi phạm điều khoản sử dụng.',
    status: 'PENDING',
    time: '2026-06-06 14:30'
  }
];

let chatRequests: ChatRequest[] = [
  {
    id: 1, name: 'Nguyễn Văn A', email: 'dana@gmail.com',
    lastMessage: 'Tôi chuyển 500k vào quỹ nhưng số dư không cập nhật.',
    time: '10:30', status: 'PENDING', priority: 'HIGH',
    messages: [
      { id: 1, sender: 'user', content: 'Xin chào, tôi cần hỗ trợ về giao dịch quỹ nhóm.', time: '10:25' },
      { id: 2, sender: 'admin', content: 'Xin chào quý khách VIP, bộ phận hỗ trợ kỹ thuật có thể giúp gì cho quý khách ạ?', time: '10:27' },
      { id: 3, sender: 'user', content: 'Tôi chuyển 500k vào quỹ nhưng số dư không cập nhật.', time: '10:28' },
      { id: 4, sender: 'admin', content: 'Quý khách vui lòng cung cấp mã giao dịch và ảnh chụp biên lai chuyển tiền để chúng tôi đối soát.', time: '10:29' },
      { id: 5, sender: 'user', content: 'Mã giao dịch TX-9982, tôi đã chuyển lúc 09:45 sáng.', time: '10:30' }
    ]
  },
  {
    id: 2, name: 'Trần Thị B', email: 'tranb@gmail.com',
    lastMessage: 'Cảm ơn hỗ trợ nhiệt tình',
    time: 'Hôm qua', status: 'RESOLVED', priority: 'MEDIUM',
    messages: [
      { id: 1, sender: 'user', content: 'Làm sao để tạo quỹ tiết kiệm nhóm?', time: '14:20' },
      { id: 2, sender: 'admin', content: 'Chào chị B, chị vào phần Quỹ nhóm -> Tạo quỹ mới nhé.', time: '14:22' },
      { id: 3, sender: 'user', content: 'Cảm ơn hỗ trợ nhiệt tình', time: '14:25' }
    ]
  },
  {
    id: 3, name: 'Lê Văn C', email: 'levanc@gmail.com',
    lastMessage: 'Tài khoản của tôi bị khóa vô cớ',
    time: '2 giờ trước', status: 'PENDING', priority: 'LOW',
    messages: [
      { id: 1, sender: 'user', content: 'Tài khoản của tôi bị khóa vô cớ.', time: '08:15' },
      { id: 2, sender: 'admin', content: 'Yêu cầu của quý khách đã được ghi nhận. Hệ thống đang kiểm tra và sẽ phản hồi trong vòng 5 phút.', time: '08:20' }
    ]
  },
  {
    id: 4, name: 'Phạm Minh D', email: 'phamminhdung@gmail.com',
    lastMessage: 'Quên mật khẩu không nhận được email OTP',
    time: '30 phút trước', status: 'PENDING', priority: 'HIGH',
    messages: [
      { id: 1, sender: 'user', content: 'Tôi nhấn quên mật khẩu nhưng không nhận được email OTP.', time: '22:10' },
      { id: 2, sender: 'user', content: 'Đã thử lại nhiều lần vẫn không được.', time: '22:12' }
    ]
  },
  {
    id: 5, name: 'Hoàng Thị E', email: 'hoangthi.e@gmail.com',
    lastMessage: 'Cảm ơn, vấn đề đã được giải quyết',
    time: '2 ngày trước', status: 'RESOLVED', priority: 'LOW',
    messages: [
      { id: 1, sender: 'user', content: 'Tôi không thể nạp tiền vào quỹ qua ví MoMo.', time: '09:00' },
      { id: 2, sender: 'admin', content: 'Chào chị E, hiện tại hệ thống đang bảo trì kết nối MoMo, dự kiến khôi phục sau 2 tiếng.', time: '09:05' },
      { id: 3, sender: 'user', content: 'Cảm ơn, vấn đề đã được giải quyết', time: '11:30' }
    ]
  },
  {
    id: 6, name: 'Vũ Đức F', email: 'vuduc.f@gmail.com',
    lastMessage: 'Giao dịch rút tiền bị treo hơn 1 tiếng',
    time: '15 phút trước', status: 'PENDING', priority: 'HIGH',
    messages: [
      { id: 1, sender: 'user', content: 'Tôi yêu cầu rút 2 triệu từ quỹ nhóm nhưng giao dịch đang PENDING hơn 1 tiếng.', time: '22:20' },
      { id: 2, sender: 'admin', content: 'Anh F cho em mã giao dịch để kiểm tra nhé.', time: '22:25' },
      { id: 3, sender: 'user', content: 'TX-5521. Tiền vẫn chưa về tài khoản ngân hàng.', time: '22:26' }
    ]
  }
];

let usersMock: UserAccount[] = [
  { id: 1, email: 'dana@gmail.com', name: 'Nguyễn Văn A', status: 'ACTIVE', role: 'USER', balance: 15200000, createdAt: '2026-01-15' },
  { id: 2, email: 'tranb@gmail.com', name: 'Trần Thị B', status: 'ACTIVE', role: 'USER', balance: 8500000, createdAt: '2026-02-20' },
  { id: 3, email: 'levanc@gmail.com', name: 'Lê Văn C', status: 'BANNED', role: 'USER', balance: 0, createdAt: '2026-03-05' },
  { id: 4, email: 'admin@gmail.com', name: 'Hệ thống Admin', status: 'ACTIVE', role: 'ADMIN', balance: 999999999, createdAt: '2026-01-01' },
  { id: 5, email: 'phamminhdung@gmail.com', name: 'Phạm Minh D', status: 'ACTIVE', role: 'USER', balance: 3200000, createdAt: '2026-03-10' },
  { id: 6, email: 'hoangthi.e@gmail.com', name: 'Hoàng Thị E', status: 'ACTIVE', role: 'USER', balance: 22000000, createdAt: '2026-01-28' },
  { id: 7, email: 'vuduc.f@gmail.com', name: 'Vũ Đức F', status: 'BANNED', role: 'USER', balance: 500000, createdAt: '2026-04-01' },
  { id: 8, email: 'support1@smartfinance.vn', name: 'Support Nguyễn', status: 'ACTIVE', role: 'SUPPORT', balance: 0, createdAt: '2026-01-01' }
];

let auditLogs: Record<string, AuditLog[]> = {
  'dana@gmail.com': [
    { key: '1', time: '2026-06-06 10:15:32', action: 'Đăng nhập hệ thống', targetUser: 'Nguyễn Văn A', ip: '192.168.1.12', status: 'SUCCESS' },
    { key: '2', time: '2026-06-06 09:30:10', action: 'Cập nhật thông tin cá nhân', targetUser: 'Nguyễn Văn A', ip: '192.168.1.12', status: 'SUCCESS' },
    { key: '3', time: '2026-06-05 15:45:22', action: 'Rút tiền từ Quỹ nhóm', targetUser: 'Nguyễn Văn A', ip: '192.168.1.12', status: 'SUCCESS' },
    { key: '4', time: '2026-06-05 08:10:00', action: 'Nạp tiền vào Quỹ nhóm', targetUser: 'Nguyễn Văn A', ip: '192.168.1.12', status: 'SUCCESS' },
    { key: '5', time: '2026-06-04 20:00:11', action: 'Thay đổi mật khẩu', targetUser: 'Nguyễn Văn A', ip: '192.168.1.12', status: 'SUCCESS' },
    { key: '6', time: '2026-06-03 07:55:40', action: 'Đăng nhập thất bại (sai mật khẩu)', targetUser: 'Nguyễn Văn A', ip: '10.0.0.5', status: 'FAILED' }
  ],
  'tranb@gmail.com': [
    { key: '1', time: '2026-06-06 11:20:05', action: 'Đăng nhập hệ thống', targetUser: 'Trần Thị B', ip: '192.168.1.50', status: 'SUCCESS' },
    { key: '2', time: '2026-06-05 10:00:15', action: 'Nạp tiền vào Quỹ tiết kiệm', targetUser: 'Trần Thị B', ip: '192.168.1.50', status: 'SUCCESS' },
    { key: '3', time: '2026-06-04 14:30:00', action: 'Tạo quỹ nhóm mới', targetUser: 'Trần Thị B', ip: '192.168.1.50', status: 'SUCCESS' },
    { key: '4', time: '2026-06-03 09:15:22', action: 'Đăng nhập hệ thống', targetUser: 'Trần Thị B', ip: '192.168.1.50', status: 'SUCCESS' }
  ],
  'levanc@gmail.com': [
    { key: '1', time: '2026-06-06 08:00:00', action: 'Tài khoản bị khóa tự động', targetUser: 'Lê Văn C', ip: 'System', status: 'WARNING' },
    { key: '2', time: '2026-06-05 18:30:40', action: 'Cố gắng truy cập trái phép', targetUser: 'Lê Văn C', ip: '172.16.0.4', status: 'FAILED' },
    { key: '3', time: '2026-06-05 18:28:10', action: 'Đăng nhập thất bại (sai mật khẩu)', targetUser: 'Lê Văn C', ip: '172.16.0.4', status: 'FAILED' },
    { key: '4', time: '2026-06-05 18:25:00', action: 'Đăng nhập thất bại (sai mật khẩu)', targetUser: 'Lê Văn C', ip: '172.16.0.4', status: 'FAILED' },
    { key: '5', time: '2026-06-04 10:00:00', action: 'Đăng nhập hệ thống', targetUser: 'Lê Văn C', ip: '192.168.1.99', status: 'SUCCESS' }
  ],
  'phamminhdung@gmail.com': [
    { key: '1', time: '2026-06-06 22:10:00', action: 'Yêu cầu OTP quên mật khẩu', targetUser: 'Phạm Minh D', ip: '10.10.0.3', status: 'WARNING' },
    { key: '2', time: '2026-06-06 10:05:00', action: 'Đăng nhập hệ thống', targetUser: 'Phạm Minh D', ip: '10.10.0.3', status: 'SUCCESS' }
  ],
  'hoangthi.e@gmail.com': [
    { key: '1', time: '2026-06-06 11:30:00', action: 'Nạp tiền MoMo thành công', targetUser: 'Hoàng Thị E', ip: '192.168.5.20', status: 'SUCCESS' },
    { key: '2', time: '2026-06-06 09:00:00', action: 'Nạp tiền MoMo thất bại', targetUser: 'Hoàng Thị E', ip: '192.168.5.20', status: 'FAILED' },
    { key: '3', time: '2026-06-05 08:30:00', action: 'Đăng nhập hệ thống', targetUser: 'Hoàng Thị E', ip: '192.168.5.20', status: 'SUCCESS' }
  ],
  'vuduc.f@gmail.com': [
    { key: '1', time: '2026-06-06 22:20:00', action: 'Yêu cầu rút tiền bị treo', targetUser: 'Vũ Đức F', ip: '1.2.3.4', status: 'WARNING' },
    { key: '2', time: '2026-06-06 21:00:00', action: 'Tài khoản bị khóa thủ công', targetUser: 'Vũ Đức F', ip: 'Support Admin', status: 'WARNING' },
    { key: '3', time: '2026-06-05 12:00:00', action: 'Đăng nhập hệ thống', targetUser: 'Vũ Đức F', ip: '1.2.3.4', status: 'SUCCESS' }
  ],
  'admin@gmail.com': [
    { key: '1', time: '2026-06-06 12:00:00', action: 'Truy cập hệ thống quản trị', targetUser: 'System Admin', ip: '127.0.0.1', status: 'SUCCESS' },
    { key: '2', time: '2026-06-06 08:00:00', action: 'Phê duyệt yêu cầu khóa tài khoản', targetUser: 'System Admin', ip: '127.0.0.1', status: 'SUCCESS' }
  ]
};

let fundsMock: Fund[] = [
  { id: 'FUND-001', name: 'Quỹ Gia Đình Nguyễn', owner: 'dana@gmail.com', balance: 50000000, status: 'ACTIVE' },
  { id: 'FUND-002', name: 'Quỹ Du Lịch Nhóm 8', owner: 'tranb@gmail.com', balance: 12500000, status: 'ACTIVE' },
  { id: 'FUND-003', name: 'Quỹ Đầu Tư Chung', owner: 'levanc@gmail.com', balance: 200000000, status: 'FROZEN' },
  { id: 'FUND-004', name: 'Quỹ Mua Xe 2026', owner: 'phamminhdung@gmail.com', balance: 75000000, status: 'ACTIVE' },
  { id: 'FUND-005', name: 'Quỹ Học Phí Con Cái', owner: 'hoangthi.e@gmail.com', balance: 30000000, status: 'FROZEN' }
];

let transactionsMock: Record<string, Transaction[]> = {
  'FUND-001': [
    { id: 'TX-101', description: 'Đóng góp hàng tháng tháng 6', amount: 5000000, sender: 'Nguyễn Văn A', receiver: 'Quỹ Gia Đình', date: '2026-06-06 09:00', status: 'SUCCESS' },
    { id: 'TX-102', description: 'Thanh toán tiền điện tháng 5', amount: -2000000, sender: 'Quỹ Gia Đình', receiver: 'Công ty Điện lực', date: '2026-06-05 14:00', status: 'SUCCESS' },
    { id: 'TX-103', description: 'Mua sắm đồ gia dụng', amount: -15000000, sender: 'Quỹ Gia Đình', receiver: 'Siêu thị Điện máy', date: '2026-06-04 10:30', status: 'SUCCESS' },
    { id: 'TX-104', description: 'Đóng góp hàng tháng tháng 5', amount: 5000000, sender: 'Nguyễn Văn A', receiver: 'Quỹ Gia Đình', date: '2026-05-06 09:00', status: 'SUCCESS' },
    { id: 'TX-105', description: 'Thanh toán tiền nước tháng 5', amount: -500000, sender: 'Quỹ Gia Đình', receiver: 'Công ty Cấp nước', date: '2026-05-05 10:00', status: 'SUCCESS' },
    { id: 'TX-106', description: 'Mua thực phẩm cuối tuần', amount: -3000000, sender: 'Quỹ Gia Đình', receiver: 'Siêu thị Co.opmart', date: '2026-05-04 16:00', status: 'SUCCESS' }
  ],
  'FUND-002': [
    { id: 'TX-201', description: 'Đặt vé máy bay khứ hồi Hà Nội - TP.HCM', amount: -8000000, sender: 'Quỹ Du Lịch', receiver: 'Vietnam Airlines', date: '2026-06-05 16:00', status: 'SUCCESS' },
    { id: 'TX-202', description: 'Nạp quỹ du lịch - Trần Thị B', amount: 4000000, sender: 'Trần Thị B', receiver: 'Quỹ Du Lịch', date: '2026-06-04 09:00', status: 'SUCCESS' },
    { id: 'TX-203', description: 'Đặt khách sạn 3 đêm Đà Nẵng', amount: -6000000, sender: 'Quỹ Du Lịch', receiver: 'Booking.com', date: '2026-06-03 11:00', status: 'SUCCESS' },
    { id: 'TX-204', description: 'Nạp quỹ du lịch - Nguyễn Văn A', amount: 3000000, sender: 'Nguyễn Văn A', receiver: 'Quỹ Du Lịch', date: '2026-06-02 08:00', status: 'SUCCESS' },
    { id: 'TX-205', description: 'Thuê xe máy tham quan', amount: -1500000, sender: 'Quỹ Du Lịch', receiver: 'Thuê xe Minh Tuấn', date: '2026-06-01 07:30', status: 'SUCCESS' }
  ],
  'FUND-003': [
    { id: 'TX-301', description: 'Nạp quỹ đầu tư ban đầu', amount: 200000000, sender: 'Lê Văn C', receiver: 'Quỹ Đầu Tư Chung', date: '2026-06-03 11:00', status: 'SUCCESS' },
    { id: 'TX-302', description: 'Mua cổ phiếu VNM', amount: -50000000, sender: 'Quỹ Đầu Tư Chung', receiver: 'Công ty CK TCBS', date: '2026-06-04 09:30', status: 'SUCCESS' },
    { id: 'TX-303', description: 'Cổ tức quý 1/2026', amount: 8000000, sender: 'Vinamilk', receiver: 'Quỹ Đầu Tư Chung', date: '2026-06-05 10:00', status: 'SUCCESS' }
  ],
  'FUND-004': [
    { id: 'TX-401', description: 'Nạp tiết kiệm mua xe tháng 6', amount: 10000000, sender: 'Phạm Minh D', receiver: 'Quỹ Mua Xe', date: '2026-06-06 08:00', status: 'SUCCESS' },
    { id: 'TX-402', description: 'Nạp tiết kiệm mua xe tháng 5', amount: 10000000, sender: 'Phạm Minh D', receiver: 'Quỹ Mua Xe', date: '2026-05-06 08:00', status: 'SUCCESS' },
    { id: 'TX-403', description: 'Rút khẩn cấp sửa chữa nhà', amount: -5000000, sender: 'Quỹ Mua Xe', receiver: 'Phạm Minh D', date: '2026-05-20 14:00', status: 'SUCCESS' }
  ],
  'FUND-005': [
    { id: 'TX-501', description: 'Học phí kỳ 1/2026', amount: -15000000, sender: 'Quỹ Học Phí', receiver: 'Trường ĐH Bách Khoa', date: '2026-06-01 09:00', status: 'SUCCESS' },
    { id: 'TX-502', description: 'Nạp học phí tháng 6', amount: 8000000, sender: 'Hoàng Thị E', receiver: 'Quỹ Học Phí', date: '2026-06-01 07:00', status: 'SUCCESS' },
    { id: 'TX-503', description: 'Mua sách giáo khoa', amount: -2500000, sender: 'Quỹ Học Phí', receiver: 'Nhà sách Fahasa', date: '2026-05-28 11:00', status: 'SUCCESS' },
    { id: 'TX-504', description: 'Giao dịch đáng ngờ - đang xem xét', amount: -20000000, sender: 'Quỹ Học Phí', receiver: 'Tài khoản lạ', date: '2026-05-25 03:00', status: 'SUCCESS' }
  ]
};

let broadcasts: Broadcast[] = [
  { key: '1', title: 'Bảo trì hệ thống định kỳ', content: 'Hệ thống Smart Finance sẽ tiến hành bảo trì từ 2h đến 4h sáng ngày 08/06/2026 để nâng cấp tính năng thanh toán.', target: 'ALL', urgency: 'WARNING', time: '2026-06-06 01:00:00' },
  { key: '2', title: 'Chương trình ưu đãi VIP tháng 6', content: 'Nhân đôi tích lũy điểm thưởng khi nạp quỹ nhóm trên 10 triệu từ ngày 10/06 đến hết tháng 6/2026.', target: 'VIP', urgency: 'INFO', time: '2026-06-05 14:00:00' },
  { key: '3', title: 'Cảnh báo bảo mật khẩn cấp', content: 'Phát hiện một số tài khoản bị tấn công brute-force. Người dùng vui lòng đổi mật khẩu ngay và bật xác thực 2 lớp.', target: 'ALL', urgency: 'CRITICAL', time: '2026-06-04 18:00:00' },
  { key: '4', title: 'Tính năng mới: Liên kết ngân hàng tự động', content: 'Smart Finance vừa ra mắt tính năng liên kết ngân hàng tự động. Nạp tiền nhanh hơn, không cần xác nhận thủ công.', target: 'SUBSCRIBED', urgency: 'INFO', time: '2026-06-03 10:00:00' }
];

export default {
  'GET /api/support/chat-requests': (req: any, res: any) => {
    res.json({ success: true, data: chatRequests });
  },

  'POST /api/support/chat-requests/create': (req: any, res: any) => {
    const { title, description, priority, email, name } = req.body;
    
    const newChat: ChatRequest = {
      id: chatRequests.length + 1,
      name: name || 'Người dùng',
      email: email || 'user@example.com',
      lastMessage: description || 'Yêu cầu hỗ trợ mới',
      time: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
      status: 'PENDING',
      priority: priority || 'MEDIUM',
      messages: [
        { 
          id: 1, 
          sender: 'user', 
          content: title ? `[${title}] ${description}` : description, 
          time: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) 
        }
      ]
    };
    
    chatRequests.unshift(newChat);
    res.json({ success: true, data: newChat });
  },

  'POST /api/support/chat-requests/send': (req: any, res: any) => {
    const { chatId, message: newMsg } = req.body;
    const chat = chatRequests.find(c => c.id === Number(chatId));
    if (!chat) return res.status(404).json({ success: false, message: 'Chat request not found' });
    chat.messages.push(newMsg);
    chat.lastMessage = newMsg.content;
    chat.time = newMsg.time;
    res.json({ success: true, data: chat });
  },

  'POST /api/support/chat-requests/resolve': (req: any, res: any) => {
    const { chatId } = req.body;
    const chat = chatRequests.find(c => c.id === Number(chatId));
    if (!chat) return res.status(404).json({ success: false, message: 'Chat request not found' });
    chat.status = 'RESOLVED';
    res.json({ success: true, data: chat });
  },

  'GET /api/support/users': (req: any, res: any) => {
    res.json({ success: true, data: usersMock });
  },

  'POST /api/support/users/toggle-status': (req: any, res: any) => {
    const { email, checked } = req.body;
    const user = usersMock.find(u => u.email === email);
    if (!user) return res.status(404).json({ success: false, message: 'User not found' });

    const newStatus = checked ? 'ACTIVE' : 'BANNED';
    user.status = newStatus;

    const newLog: AuditLog = {
      key: Date.now().toString(),
      time: new Date().toISOString().replace('T', ' ').substring(0, 19),
      action: newStatus === 'ACTIVE' ? 'Kích hoạt tài khoản' : 'Khóa tài khoản (Banned)',
      targetUser: user.name,
      ip: 'Support Admin',
      status: newStatus === 'ACTIVE' ? 'SUCCESS' : 'WARNING'
    };

    auditLogs[email] = [newLog, ...(auditLogs[email] || [])];
    res.json({ success: true, user, newLog });
  },

  'POST /api/support/users/lock-request': (req: any, res: any) => {
    const { email, reason } = req.body;
    const user = usersMock.find(u => u.email === email);
    if (!user) return res.status(404).json({ success: false, message: 'User not found' });

    const newRequest: LockRequest = {
      id: lockRequests.length + 1,
      email: user.email,
      name: user.name,
      reason,
      status: 'PENDING',
      time: new Date().toISOString().replace('T', ' ').substring(0, 16)
    };
    lockRequests.unshift(newRequest);

    const newLog: AuditLog = {
      key: Date.now().toString(),
      time: new Date().toISOString().replace('T', ' ').substring(0, 19),
      action: `Gửi yêu cầu khóa tài khoản (Lý do: ${reason})`,
      targetUser: user.name,
      ip: 'Support Admin',
      status: 'WARNING'
    };

    auditLogs[email] = [newLog, ...(auditLogs[email] || [])];
    res.json({ success: true, data: newRequest, newLog });
  },

  'GET /api/support/lock-requests': (req: any, res: any) => {
    res.json({ success: true, data: lockRequests });
  },

  'POST /api/support/lock-requests/:id/approve': (req: any, res: any) => {
    const { id } = req.params;
    const request = lockRequests.find(r => r.id === Number(id));
    if (!request) return res.status(404).json({ success: false, message: 'Request not found' });

    request.status = 'APPROVED';

    const user = usersMock.find(u => u.email === request.email);
    if (user) {
      user.status = 'BANNED';
    }

    const newLog: AuditLog = {
      key: Date.now().toString(),
      time: new Date().toISOString().replace('T', ' ').substring(0, 19),
      action: 'Admin phê duyệt khóa tài khoản',
      targetUser: request.name,
      ip: 'Admin Panel',
      status: 'WARNING'
    };
    auditLogs[request.email] = [newLog, ...(auditLogs[request.email] || [])];

    res.json({ success: true, data: request });
  },

  'POST /api/support/lock-requests/:id/reject': (req: any, res: any) => {
    const { id } = req.params;
    const request = lockRequests.find(r => r.id === Number(id));
    if (!request) return res.status(404).json({ success: false, message: 'Request not found' });

    request.status = 'REJECTED';

    const newLog: AuditLog = {
      key: Date.now().toString(),
      time: new Date().toISOString().replace('T', ' ').substring(0, 19),
      action: 'Admin từ chối khóa tài khoản',
      targetUser: request.name,
      ip: 'Admin Panel',
      status: 'SUCCESS'
    };
    auditLogs[request.email] = [newLog, ...(auditLogs[request.email] || [])];

    res.json({ success: true, data: request });
  },

  'DELETE /api/support/lock-requests/:id': (req: any, res: any) => {
    const { id } = req.params;
    const index = lockRequests.findIndex(r => r.id === Number(id));
    if (index === -1) {
      return res.status(404).json({ success: false, message: 'Yêu cầu không tồn tại' });
    }
    const request = lockRequests[index];
    if (request.status === 'PENDING') {
      return res.status(400).json({ success: false, message: 'Không thể xóa yêu cầu chưa xử lý' });
    }
    lockRequests.splice(index, 1);
    res.json({ success: true, message: 'Xóa yêu cầu thành công' });
  },

  'GET /api/support/audit-logs': (req: any, res: any) => {
    res.json({ success: true, data: auditLogs });
  },

  'GET /api/support/funds': (req: any, res: any) => {
    res.json({ success: true, data: fundsMock });
  },

  'POST /api/support/funds/toggle-freeze': (req: any, res: any) => {
    const { fundId } = req.body;
    const fund = fundsMock.find(f => f.id === fundId);
    if (!fund) return res.status(404).json({ success: false, message: 'Fund not found' });
    fund.status = fund.status === 'ACTIVE' ? 'FROZEN' : 'ACTIVE';
    res.json({ success: true, data: fund });
  },

  'GET /api/support/transactions': (req: any, res: any) => {
    res.json({ success: true, data: transactionsMock });
  },

  'POST /api/support/transactions/revert': (req: any, res: any) => {
    const { fundId, txId } = req.body;
    const fund = fundsMock.find(f => f.id === fundId);
    const tx = (transactionsMock[fundId] || []).find(t => t.id === txId);

    if (!fund || !tx || tx.status === 'REVERTED') {
      return res.status(400).json({ success: false, message: 'Invalid transaction or already reverted' });
    }

    tx.status = 'REVERTED';
    const rollbackAmt = tx.amount < 0 ? Math.abs(tx.amount) : -tx.amount;
    fund.balance += rollbackAmt;
    res.json({ success: true, fund, tx });
  },

  'GET /api/support/broadcasts': (req: any, res: any) => {
    res.json({ success: true, data: broadcasts });
  },

  'POST /api/support/broadcasts/create': (req: any, res: any) => {
    const { title, content, target, urgency } = req.body;
    const newBroadcast: Broadcast = {
      key: Date.now().toString(),
      title, content, target, urgency,
      time: new Date().toISOString().replace('T', ' ').substring(0, 19)
    };
    broadcasts = [newBroadcast, ...broadcasts];
    res.json({ success: true, data: newBroadcast });
  }
};
