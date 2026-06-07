const requests: Record<string, {
  id: string;
  fundId: number;
  memberEmail: string;
  status: 'pending' | 'approved' | 'rejected';
}> = {};
const pendingTransactions: Record<string, {
  id: string;
  fundId: number;
  type: string;
  amount: number;
  description: string;
  bankAccount: string;
  bankName: string;
  requesterName: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  date: string;
}> = {};

let mockFunds = [
  {
    id: 1,
    name: 'Du Lịch Gia Đình',
    target: 22000000,
    balance: 14250000,
    membersCount: 5,
    status: 'active',
    themeColor: '#1A73E8',
    members: [
      { name: 'Nguyễn Văn An (Trưởng quỹ)', email: 'admin@gmail.com', avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Admin', role: 'OWNER' },
      { name: 'Alice Nguyễn', email: 'alice@gmail.com', avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Alice', role: 'MEMBER' },
      { name: 'Bùi Minh', email: 'bob@gmail.com', avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Bob', role: 'MEMBER' },
      { name: 'Cao Linh', email: 'charlie@gmail.com', avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Charlie', role: 'MEMBER' },
      { name: 'Dương Anh', email: 'dave@gmail.com', avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Dave', role: 'MEMBER' }
    ],
  },
  {
    id: 2,
    name: 'Chi Phí Bạn Cùng Phòng',
    target: 5000000,
    balance: 5000000,
    membersCount: 2,
    status: 'settled',
    themeColor: '#52C41A',
    members: [
      { name: 'Emily Phạm (Trưởng quỹ)', email: 'emily@gmail.com', avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Emily', role: 'OWNER' },
      { name: 'Fred Lê', email: 'fred@gmail.com', avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Fred', role: 'MEMBER' },
    ],
  }
];


const deleteRequests: Record<string, {
  id: string;
  fundId: number;
  status: 'pending' | 'approved' | 'rejected';
  email?: string;
}> = {};

const mockTransactions: Record<string, any[]> = {
  '1': [
    { id: 1, type: 'INCOME', amount: 500000, description: 'Đóng góp tháng 8', date: '2023-08-14', user_display_name: 'Nguyễn Văn An', category_name: 'Thu khác', is_approved: true },
    { id: 2, type: 'INCOME', amount: 250000, description: 'Đóng góp tháng 8', date: '2023-08-12', user_display_name: 'Bùi Minh', category_name: 'Thu khác', is_approved: true },
    { id: 3, type: 'INCOME', amount: 1000000, description: 'Đóng góp tháng 8', date: '2023-08-10', user_display_name: 'Cao Linh', category_name: 'Thu khác', is_approved: true },
    { id: 4, type: 'EXPENSE', amount: 1200000, description: 'Đặt vé máy bay', date: '2023-08-05', user_display_name: 'Nguyễn Văn An', category_name: 'Giải trí', is_approved: true },
    { id: 5, type: 'EXPENSE', amount: 850000, description: 'Cọc khách sạn 3 đêm', date: '2023-08-01', user_display_name: 'Nguyễn Văn An', category_name: 'Nhà cửa', is_approved: true },
  ],
  '2': [
    { id: 6, type: 'INCOME', amount: 2500000, description: 'Đóng góp tiền phòng', date: '2023-10-01', user_display_name: 'Emily Phạm', category_name: 'Thu khác', is_approved: true },
    { id: 7, type: 'INCOME', amount: 2500000, description: 'Đóng góp tiền phòng', date: '2023-10-01', user_display_name: 'Fred Lê', category_name: 'Thu khác', is_approved: true },
    { id: 8, type: 'EXPENSE', amount: 1800000, description: 'Tiền điện + nước tháng 10', date: '2023-10-05', user_display_name: 'Emily Phạm', category_name: 'Hóa đơn', is_approved: true },
  ],
};


let mockActivities = [
  {
    id: 1,
    email: 'admin@gmail.com',
    type: 'join',
    text: "Bạn đã tham gia nhóm 'Du Lịch Gia Đình'",
    time: 'Hôm nay, 10:45 SA',
    color: '#34A853'
  },
  {
    id: 2,
    email: 'admin@gmail.com',
    type: 'create',
    text: "Bạn đã tạo quỹ 'Chi Phí Bạn Cùng Phòng'",
    time: '12 Tháng 10, 2023',
    color: '#1A73E8'
  },
  {
    id: 3,
    email: 'admin@gmail.com',
    type: 'delete',
    text: "Bạn đã xóa quỹ nhóm 'Chuyến Đi Cuối Tuần'",
    time: '05 Tháng 10, 2023',
    color: '#EA4335'
  }
];

export default {
  'POST /api/funds/remove-request': (req: any, res: any) => {
    const { fundId, memberEmail } = req.body;

    if (!fundId || !memberEmail) {
      return res.status(400).send({
        status: 'error',
        message: 'Thiếu thông tin fundId hoặc memberEmail'
      });
    }

    const requestId = `req_${Date.now()}`;
    requests[requestId] = {
      id: requestId,
      fundId: parseInt(fundId),
      memberEmail,
      status: 'pending'
    };

    console.log(`[Mock BE Server] Đã gửi email xác nhận đến ${memberEmail} (ID yêu cầu: ${requestId})`);

    res.send({
      success: true,
      requestId,
      message: 'Đã gửi yêu cầu xác nhận rời quỹ đến email thành viên!'
    });
  },

  'GET /api/funds/remove-status': (req: any, res: any) => {
    const { requestId } = req.query;

    if (!requestId) {
      return res.status(400).send({
        status: 'error',
        message: 'Thiếu tham số requestId'
      });
    }

    const request = requests[requestId];
    if (!request) {
      return res.status(404).send({
        status: 'error',
        message: 'Không tìm thấy yêu cầu xóa này'
      });
    }

    res.send({
      status: request.status
    });
  },

  'POST /api/funds/simulate-email': (req: any, res: any) => {
    const { requestId, action } = req.body; 

    if (!requestId || !action) {
      return res.status(400).send({
        status: 'error',
        message: 'Thiếu thông tin requestId hoặc action phản hồi'
      });
    }

    if (requests[requestId]) {
      requests[requestId].status = action;
      console.log(`[Mock BE Server] Thành viên đã click ${action.toUpperCase()} trên giao diện Email.`);
      res.send({
        status: 'success',
        message: `Mô phỏng phản hồi ${action} thành công!`
      });
    } else {
      res.status(404).send({
        status: 'error',
        message: 'Không tìm thấy yêu cầu'
      });
    }
  },

  'PUT /api/funds/rename': (req: any, res: any) => {
    const { fundId, newName } = req.body;

    if (!fundId || !newName || !newName.trim()) {
      return res.status(400).send({
        success: false,
        message: 'Thiếu fundId hoặc tên quỹ mới không được để trống'
      });
    }

    if (newName.trim().length > 100) {
      return res.status(400).send({
        success: false,
        message: 'Tên quỹ không được vượt quá 100 ký tự (giới hạn cột share_funds.name)'
      });
    }

    console.log(`[Mock BE Server] Đổi tên quỹ ID=${fundId}: "${newName.trim()}"`);
    console.log(`[Mock BE Server] SQL: UPDATE share_funds SET name='${newName.trim()}', updated_at=NOW() WHERE id=${fundId}`);

    res.send({
      success: true,
      fundId,
      newName: newName.trim(),
      message: 'Đã cập nhật tên quỹ thành công!'
    });
  },
  'GET /api/funds/transactions': (req: any, res: any) => {
    const { fundId } = req.query;
    const transactions = mockTransactions[String(fundId)] || [];
    res.send({ success: true, data: transactions });
  },
  'POST /api/funds/transaction-request': (req: any, res: any) => {
    const { fundId, type, amount, description, bankAccount, bankName, requesterName } = req.body;
    if (!fundId || !type || !amount) {
      return res.status(400).send({ success: false, message: 'Thiếu thông tin bắt buộc' });
    }
    const requestId = `txreq_${Date.now()}`;

    pendingTransactions[requestId] = {
      id: requestId,
      fundId: parseInt(fundId),
      type,
      amount: parseFloat(amount),
      description: description || '',
      bankAccount: bankAccount || '',
      bankName: bankName || '',
      requesterName,
      status: 'PENDING',
      date: new Date().toISOString().slice(0, 10)
    };

    console.log(`[Mock BE] Yêu cầu ${type}: ${requesterName} - ${amount}đ - Quỹ ${fundId}`);
    console.log(`[Mock BE] SQL: INSERT INTO transactions (fund_id, type, amount, description, is_approved) VALUES (${fundId}, '${type}', ${amount}, '${description}', false)`);
    console.log(`[Mock BE] → Gửi thông báo đến OWNER của quỹ ${fundId}`);

    res.send({
      success: true,
      requestId,
      message: `Đã gửi yêu cầu đến chủ quỹ để duyệt!`
    });
  },

  'GET /api/funds/pending-requests': (req: any, res: any) => {
    const pending = Object.values(pendingTransactions).filter(t => t.status === 'PENDING');
    console.log(`[Mock BE] Trả về ${pending.length} yêu cầu chờ duyệt`);
    res.send({ success: true, data: pending });
  },

  'POST /api/funds/approve-transaction': (req: any, res: any) => {
    const { requestId, action, rejectReason } = req.body; 

    if (!requestId || !action) {
      return res.status(400).send({ success: false, message: 'Thiếu requestId hoặc action' });
    }

    const txReq = pendingTransactions[requestId];
    if (!txReq) {
      return res.status(404).send({ success: false, message: 'Không tìm thấy yêu cầu' });
    }

    txReq.status = action === 'approved' ? 'APPROVED' : 'REJECTED';

    if (action === 'approved') {
      const fund = mockFunds.find(f => f.id === txReq.fundId);
      if (fund) {
        if (txReq.type === 'INCOME') {
          fund.balance += txReq.amount;
        } else {
          fund.balance -= txReq.amount;
        }
      }
      if (!mockTransactions[String(txReq.fundId)]) {
        mockTransactions[String(txReq.fundId)] = [];
      }
      mockTransactions[String(txReq.fundId)].unshift({
        id: Date.now(),
        type: txReq.type,
        amount: txReq.amount,
        description: txReq.description,
        date: txReq.date,
        user_display_name: txReq.requesterName,
        category_name: txReq.type === 'INCOME' ? 'Thu khác' : 'Chi tiêu khác',
        is_approved: true
      });
      const op = txReq.type === 'INCOME' ? '+' : '-';
      console.log(`[Mock BE] DUYỆT: UPDATE transactions SET is_approved=true WHERE id='${requestId}'`);
      console.log(`[Mock BE] SQL: UPDATE share_funds SET balance = balance ${op} ${txReq.amount} WHERE id=${txReq.fundId}`);
    } else {
      console.log(`[Mock BE] TỪ CHỐI yêu cầu ${requestId} của ${txReq.requesterName}. Lý do: ${rejectReason || 'Không có'}`);
    }

    console.log(`[Mock BE] → Gửi thông báo kết quả "${action}" cho ${txReq.requesterName}`);

    res.send({
      success: true,
      action,
      transaction: txReq,
      message: action === 'approved' ? 'Đã duyệt giao dịch!' : 'Đã từ chối yêu cầu!'
    });
  },

  'POST /api/funds/:id/respond': (req: any, res: any) => {
    const { invitationId, action } = req.body;
    const { id } = req.params;
    
    console.log(`[Mock BE] User đã phản hồi lời mời ${invitationId} quỹ ${id}: ${action}`);
    
    res.send({
      success: true,
      message: action === 'accepted' ? 'Đã tham gia nhóm thành công!' : 'Đã từ chối lời mời'
    });
  },

  'POST /api/funds/:id/leave': (req: any, res: any) => {
    const { email } = req.body;
    const { id } = req.params;
    const fundId = parseInt(id);
    const fund = mockFunds.find(f => f.id === fundId);
    const fundName = fund?.name || 'Quỹ';
    
    if (fund) {
      fund.members = fund.members.filter((m: any) => m.email !== email);
      fund.membersCount = fund.members.length;
    }
    
    console.log(`[Mock BE] Thành viên ${email} đã rời khỏi quỹ ${id}`);
    
    const newActivity = {
      id: Date.now(),
      email: email || '',
      type: 'leave',
      text: `Bạn đã rời khỏi nhóm '${fundName}'`,
      time: new Date().toLocaleDateString('vi-VN'),
      color: '#FA8C16'
    };
    mockActivities.unshift(newActivity);
    
    res.send({
      success: true,
      message: 'Đã rời nhóm thành công',
      newActivity
    });
  },

  'GET /api/funds/list': (req: any, res: any) => {
    const { email } = req.query;
    let result = [...mockFunds];
    
    if (email) {
      result = result.filter(f => f.members.some((m: any) => m.email === email));
    }
    
    res.send({
      success: true,
      data: result
    });
  },

  'POST /api/funds/:id/request-delete-fund': (req: any, res: any) => {
    const { id } = req.params;
    const { email } = req.body;
    
    const requestId = `del_req_${Date.now()}`;
    deleteRequests[requestId] = {
      id: requestId,
      fundId: parseInt(id),
      status: 'pending',
      email: email
    };

    console.log(`[Mock BE Server] Đã gửi yêu cầu xác nhận xóa quỹ ${id} đến tất cả thành viên (ID yêu cầu: ${requestId})`);

    res.send({
      success: true,
      requestId,
      message: 'Đã gửi yêu cầu xác nhận xóa quỹ đến tất cả thành viên!'
    });
  },

  'GET /api/funds/delete-status': (req: any, res: any) => {
    const { requestId } = req.query;

    if (!requestId || !deleteRequests[requestId]) {
      return res.status(404).send({ status: 'error', message: 'Không tìm thấy yêu cầu xóa' });
    }

    res.send({ status: deleteRequests[requestId].status });
  },

  'POST /api/funds/simulate-delete-email': (req: any, res: any) => {
    const { requestId, action } = req.body; 

    if (!requestId || !action || !deleteRequests[requestId]) {
      return res.status(400).send({ status: 'error', message: 'Thiếu thông tin hoặc không tìm thấy' });
    }

    deleteRequests[requestId].status = action;
    console.log(`[Mock BE Server] Thành viên đã phản hồi xóa quỹ: ${action.toUpperCase()}`);
    
    if (action === 'approved') {
       const fundId = deleteRequests[requestId].fundId;
       const fundName = mockFunds.find(f => f.id === fundId)?.name || 'Quỹ';
       mockFunds = mockFunds.filter(f => f.id !== fundId);
       console.log(`[Mock BE Server] Đã xóa hoàn toàn quỹ ${fundId} khỏi database`);
       
       const newActivity = {
         id: Date.now(),
         email: deleteRequests[requestId].email || '',
         type: 'delete',
         text: `Bạn đã xóa quỹ nhóm '${fundName}'`,
         time: new Date().toLocaleDateString('vi-VN'),
         color: '#EA4335'
       };
       mockActivities.unshift(newActivity);
       
       return res.send({ status: 'success', message: `Mô phỏng phản hồi ${action} thành công!`, newActivity });
    }

    res.send({ status: 'success', message: `Mô phỏng phản hồi ${action} thành công!` });
  },

  'DELETE /api/funds/:id': (req: any, res: any) => {
    const { id } = req.params;
    const { email } = req.query;
    const fundName = mockFunds.find(f => f.id === parseInt(id))?.name || 'Quỹ';
    mockFunds = mockFunds.filter(f => f.id !== parseInt(id));
    console.log(`[Mock BE Server] Đã xóa hoàn toàn quỹ ${id} khỏi database (Trưởng nhóm xóa)`);
    
    const newActivity = {
      id: Date.now(),
      email: email || '',
      type: 'delete',
      text: `Bạn đã xóa quỹ nhóm '${fundName}'`,
      time: new Date().toLocaleDateString('vi-VN'),
      color: '#EA4335'
    };
    mockActivities.unshift(newActivity);
    
    res.send({ success: true, message: 'Đã xóa quỹ thành công', newActivity });
  },



  'POST /api/funds': (req: any, res: any) => {
    const { name, target, initialContribution, createdBy } = req.body;
    
    const balance = initialContribution ? parseFloat(initialContribution) : 0;
    const targetAmount = parseFloat(target);
    
    console.log(`[Mock BE] Tạo quỹ mới: ${name} bởi ${createdBy}`);
    
    const newFund = {
      id: Date.now(), 
      name,
      target: targetAmount,
      balance: balance,
      membersCount: 1,
      status: 'active',
      themeColor: '#1A73E8',
      members: [{
        name: (createdBy.split('@')[0] || 'User') + ' (Trưởng quỹ)',
        email: createdBy,
        avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Admin',
        role: 'OWNER'
      }]
    };

    mockFunds.push(newFund);
    
    const newActivity = {
      id: Date.now(),
      email: createdBy,
      type: 'create',
      text: `Bạn đã tạo quỹ '${name}'`,
      time: new Date().toLocaleDateString('vi-VN'),
      color: '#1A73E8'
    };
    mockActivities.unshift(newActivity);
    
    res.send({
      success: true,
      message: 'Tạo quỹ thành công',
      data: newFund
    });
  },

  'POST /api/funds/:id/messages': (req: any, res: any) => {
    const { text, senderName, senderAvatar } = req.body;
    const { id } = req.params;
    
    console.log(`[Mock BE] Quỹ ${id} nhận tin nhắn từ ${senderName}: ${text}`);
    
    res.send({
      success: true,
      message: 'Gửi tin nhắn thành công',
      data: {
        id: Date.now(),
        groupId: parseInt(id),
        type: 'message',
        senderName,
        senderAvatar,
        text,
        time: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
        isMe: true 
      }
    });
  },



  'GET /api/funds/activities': (req: any, res: any) => {
    const { email } = req.query;
    const activities = email ? mockActivities.filter(a => a.email === email) : mockActivities;
    res.send({
      success: true,
      data: activities
    });
  },

  'GET /api/funds/:id/discussions': (req: any, res: any) => {
    res.send({
      success: true,
      data: [
        {
          id: 1,
          groupId: parseInt(req.params.id),
          type: 'message',
          senderName: 'Bùi Minh',
          senderAvatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Bob',
          text: 'Tháng sau mình định đặt vé tàu đi nội ô nhé, mọi người duyệt ngân sách khoảng 5.000.000 đ cho khoản này nha?',
          time: '10:42 SA',
          isMe: false,
        },
        {
          id: 2,
          groupId: parseInt(req.params.id),
          type: 'system',
          text: 'Cao Linh đã tham gia nhóm',
          time: '10:43 SA'
        },
        {
          id: 3,
          groupId: parseInt(req.params.id),
          type: 'message',
          senderName: 'Nguyễn Văn An',
          senderAvatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=Admin',
          text: 'Đồng ý, rẻ hơn bay nhiều. Lát mình tạo khoản dự chi luôn.',
          time: '10:45 SA',
          isMe: true,
        }
      ]
    });
  },

  'GET /api/funds/:id/top-contributors': (req: any, res: any) => {
    res.send({
      success: true,
      data: [
        { id: 1, name: 'Cao Linh', avatarInitials: 'CL', avatarColor: '#595959', amount: 1500000 },
        { id: 2, name: 'Nguyễn An', avatarInitials: 'AN', avatarColor: '#1A73E8', amount: 800000 },
        { id: 3, name: 'Bùi Minh', avatarInitials: 'BM', avatarColor: '#8C8C8C', amount: 250000 },
      ]
    });
  },

  'GET /api/funds/:id/budget-chart': (req: any, res: any) => {
    res.send({
      success: true,
      data: [
        { month: 'T12', amount: 200000 },
        { month: 'T1', amount: 150000 },
        { month: 'T2', amount: 50000 },
        { month: 'T3', amount: 100000 },
        { month: 'T4', amount: 50000 },
        { month: 'Tháng này', amount: 2595000 }
      ]
    });
  }
};
