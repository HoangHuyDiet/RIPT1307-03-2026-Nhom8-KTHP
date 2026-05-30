export const MOCK_FUNDS_SUMMARY = {
  totalAssets: 2450000000,
  growthRate: 12.5,
};

export const MOCK_FUNDS_LIST = [
  {
    id: '1',
    name: 'Tiền mặt',
    balance: 25400000,
    type: 'CASH',
    isNegative: false,
    icon: 'wallet',
    actions: [
      { key: 'deposit', label: 'Nạp tiền' },
      { key: 'withdraw', label: 'Rút tiền' }
    ]
  },
  {
    id: '2',
    name: 'Vietcombank',
    balance: 1820500000,
    type: 'BANK',
    isNegative: false,
    icon: 'bank',
    actions: [
      { key: 'deposit', label: 'Nạp tiền' },
      { key: 'withdraw', label: 'Rút tiền' }
    ]
  },
  {
    id: '3',
    name: 'Thẻ tín dụng (Dư nợ)',
    balance: -14200000,
    type: 'CREDIT_CARD',
    isNegative: true,
    icon: 'card',
    actions: [
      { key: 'repay', label: 'Trả nợ' },
      { key: 'spend', label: 'Chi tiêu' }
    ]
  }
];

export const MOCK_BALANCE_HISTORY = [
  { date: '01/09', amount: 1100, type: 'Số dư' },
  { date: '08/09', amount: 1550, type: 'Số dư' },
  { date: '15/09', amount: 1480, type: 'Số dư' },
  { date: '22/09', amount: 2200, type: 'Số dư' },
  { date: '30/09', amount: 2450, type: 'Số dư' }
];

export const MOCK_ASSET_DISTRIBUTION = [
  { type: 'Tài khoản Ngân hàng', value: 1820500000, percentage: 74, color: '#1A73E8' },
  { type: 'Đầu tư & Tiết kiệm', value: 604100000, percentage: 25, color: '#34A853' },
  { type: 'Tiền mặt & Khác', value: 25400000, percentage: 1, color: '#F1F4F7' }
];

export const MOCK_RECENT_TRANSACTIONS = [
  {
    key: '1',
    date: '2026-05-29T14:20:00',
    description: 'Thanh toán GrabFood',
    category: 'Ăn uống',
    fund: 'Vietcombank',
    amount: -152000,
    type: 'EXPENSE'
  },
  {
    key: '2',
    date: '2026-05-29T09:00:00',
    description: 'Lương tháng 05',
    category: 'Thu nhập',
    fund: 'Vietcombank',
    amount: 45000000,
    type: 'INCOME'
  },
  {
    key: '3',
    date: '2026-05-28T18:45:00',
    description: 'Winmart - Hàng tiêu dùng',
    category: 'Mua sắm',
    fund: 'Tiền mặt',
    amount: -840000,
    type: 'EXPENSE'
  },
  {
    key: '4',
    date: '2026-05-28T21:00:00',
    description: 'Thanh toán cước Internet',
    category: 'Tiện ích',
    fund: 'Vietcombank',
    amount: -350000,
    type: 'EXPENSE'
  },
  {
    key: '5',
    date: '2026-05-27T15:30:00',
    description: 'Chuyển khoản lương phụ',
    category: 'Thu nhập',
    fund: 'Vietcombank',
    amount: 12000000,
    type: 'INCOME'
  },
  {
    key: '6',
    date: '2026-05-25T12:00:00',
    description: 'Ăn trưa đồng nghiệp',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -120000,
    type: 'EXPENSE'
  },
  {
    key: '7',
    date: '2026-05-24T09:15:00',
    description: 'Mua sách chuyên ngành',
    category: 'Mua sắm',
    fund: 'Tiền mặt',
    amount: -250000,
    type: 'EXPENSE'
  },
  {
    key: '8',
    date: '2026-05-22T19:40:00',
    description: 'Đóng cước Netflix',
    category: 'Tiện ích',
    fund: 'Vietcombank',
    amount: -260000,
    type: 'EXPENSE'
  },
  {
    key: '9',
    date: '2026-05-20T08:00:00',
    description: 'Bán đồ công nghệ cũ',
    category: 'Thu nhập',
    fund: 'Tiền mặt',
    amount: 1500000,
    type: 'INCOME'
  },
  {
    key: '10',
    date: '2026-05-18T16:20:00',
    description: 'Cà phê Highlands',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -850000,
    type: 'EXPENSE'
  },
  {
    key: '11',
    date: '2026-05-15T10:00:00',
    description: 'Đóng phí bảo hiểm sức khỏe',
    category: 'Tiện ích',
    fund: 'Vietcombank',
    amount: -1200000,
    type: 'EXPENSE'
  },
  {
    key: '12',
    date: '2026-05-12T14:10:00',
    description: 'Mua sắm quần áo thể thao',
    category: 'Mua sắm',
    fund: 'Vietcombank',
    amount: -950000,
    type: 'EXPENSE'
  },
  {
    key: '13',
    date: '2026-05-10T18:30:00',
    description: 'Thanh toán hoá đơn nước',
    category: 'Tiện ích',
    fund: 'Vietcombank',
    amount: -120000,
    type: 'EXPENSE'
  },
  {
    key: '14',
    date: '2026-05-08T12:30:00',
    description: 'Ăn tối Sumo BBQ',
    category: 'Ăn uống',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -1100000,
    type: 'EXPENSE'
  },
  {
    key: '15',
    date: '2026-05-05T09:00:00',
    description: 'Nhận lãi tiết kiệm',
    category: 'Thu nhập',
    fund: 'Vietcombank',
    amount: 3200000,
    type: 'INCOME'
  },
  {
    key: '16',
    date: '2026-04-28T15:00:00',
    description: 'Mua sắm Tiki',
    category: 'Mua sắm',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -450000,
    type: 'EXPENSE'
  },
  {
    key: '17',
    date: '2026-04-25T08:30:00',
    description: 'Ăn sáng phở Lý Quốc Sư',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -65000,
    type: 'EXPENSE'
  },
  {
    key: '18',
    date: '2026-04-12T10:00:00',
    description: 'Rút tiền mặt chi tiêu',
    category: 'Chuyển khoản',
    fund: 'Vietcombank',
    amount: -2000000,
    type: 'TRANSFER'
  },
  {
    key: '19',
    date: '2026-05-02T16:00:00',
    description: 'Cà phê Highlands',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -95000,
    type: 'EXPENSE'
  },
  {
    key: '20',
    date: '2026-04-15T11:00:00',
    description: 'Mua sách chuyên ngành',
    category: 'Mua sắm',
    fund: 'Tiền mặt',
    amount: -280000,
    type: 'EXPENSE'
  },
  {
    key: '21',
    date: '2026-03-20T10:00:00',
    description: 'Mua sắm Shopee',
    category: 'Mua sắm',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -1250000,
    type: 'EXPENSE'
  },
  {
    key: '22',
    date: '2026-03-15T14:00:00',
    description: 'Cơm trưa văn phòng',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -75000,
    type: 'EXPENSE'
  },
  {
    key: '24',
    date: '2026-03-01T09:00:00',
    description: 'Nhận chuyển khoản ngoài',
    category: 'Thu nhập',
    fund: 'Vietcombank',
    amount: 5000000,
    type: 'INCOME'
  },
  {
    key: '25',
    date: '2026-05-29T16:00:00',
    description: 'Mua sách lập trình',
    category: 'Mua sắm',
    fund: 'Tiền mặt',
    amount: -350000,
    type: 'EXPENSE'
  },
  {
    key: '26',
    date: '2026-05-28T10:15:00',
    description: 'Tiền điện tháng 5',
    category: 'Tiện ích',
    fund: 'Vietcombank',
    amount: -1250000,
    type: 'EXPENSE'
  },
  {
    key: '27',
    date: '2026-05-28T12:30:00',
    description: 'Ăn trưa bún chả',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -60000,
    type: 'EXPENSE'
  },
  {
    key: '28',
    date: '2026-05-27T08:00:00',
    description: 'Cà phê sáng',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -45000,
    type: 'EXPENSE'
  },
  {
    key: '29',
    date: '2026-05-26T19:00:00',
    description: 'Siêu thị Aeon',
    category: 'Mua sắm',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -620000,
    type: 'EXPENSE'
  },
  {
    key: '30',
    date: '2026-05-26T15:30:00',
    description: 'Nhận tiền hoàn trả',
    category: 'Thu nhập',
    fund: 'Vietcombank',
    amount: 150000,
    type: 'INCOME'
  },
  {
    key: '31',
    date: '2026-05-25T14:20:00',
    description: 'Trà sữa cùng phòng',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -85000,
    type: 'EXPENSE'
  },
  {
    key: '32',
    date: '2026-05-24T18:00:00',
    description: 'Đổ xăng xe máy',
    category: 'Di chuyển',
    fund: 'Tiền mặt',
    amount: -80000,
    type: 'EXPENSE'
  },
  {
    key: '33',
    date: '2026-05-23T20:30:00',
    description: 'Rạp chiếu phim CGV',
    category: 'Giải trí',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -240000,
    type: 'EXPENSE'
  },
  {
    key: '34',
    date: '2026-05-22T09:00:00',
    description: 'Cà phê Highland',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -55000,
    type: 'EXPENSE'
  },
  {
    key: '35',
    date: '2026-05-21T11:45:00',
    description: 'Cơm niêu trưa',
    category: 'Ăn uống',
    fund: 'Vietcombank',
    amount: -110000,
    type: 'EXPENSE'
  },
  {
    key: '36',
    date: '2026-05-20T17:30:00',
    description: 'Mua gói Canva Pro',
    category: 'Tiện ích',
    fund: 'Vietcombank',
    amount: -299000,
    type: 'EXPENSE'
  },
  {
    key: '37',
    date: '2026-05-19T08:30:00',
    description: 'Ăn sáng bánh mì',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -25000,
    type: 'EXPENSE'
  },
  {
    key: '38',
    date: '2026-05-18T21:00:00',
    description: 'Trả tiền nước tháng',
    category: 'Tiện ích',
    fund: 'Vietcombank',
    amount: -85000,
    type: 'EXPENSE'
  },
  {
    key: '39',
    date: '2026-05-17T15:00:00',
    description: 'Mua quần áo Zara',
    category: 'Mua sắm',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -1500000,
    type: 'EXPENSE'
  },
  {
    key: '40',
    date: '2026-05-16T13:00:00',
    description: 'Thưởng dự án',
    category: 'Thu nhập',
    fund: 'Vietcombank',
    amount: 5000000,
    type: 'INCOME'
  },
  {
    key: '41',
    date: '2026-05-15T09:15:00',
    description: 'Đi chợ mua thực phẩm',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -320000,
    type: 'EXPENSE'
  },
  {
    key: '42',
    date: '2026-05-14T19:30:00',
    description: 'Tiệc tối gia đình',
    category: 'Ăn uống',
    fund: 'Vietcombank',
    amount: -1850000,
    type: 'EXPENSE'
  },
  {
    key: '43',
    date: '2026-05-13T10:00:00',
    description: 'Mua chuột máy tính',
    category: 'Mua sắm',
    fund: 'Vietcombank',
    amount: -450000,
    type: 'EXPENSE'
  },
  {
    key: '44',
    date: '2026-05-12T07:45:00',
    description: 'Cà phê Starbucks',
    category: 'Ăn uống',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -95000,
    type: 'EXPENSE'
  },
  {
    key: '45',
    date: '2026-05-11T16:20:00',
    description: 'Cắt tóc nam',
    category: 'Tiêu dùng',
    fund: 'Tiền mặt',
    amount: -100000,
    type: 'EXPENSE'
  },
  {
    key: '46',
    date: '2026-05-10T14:00:00',
    description: 'Lãi gửi tiết kiệm',
    category: 'Thu nhập',
    fund: 'Vietcombank',
    amount: 850000,
    type: 'INCOME'
  },
  {
    key: '47',
    date: '2026-05-09T18:15:00',
    description: 'Buffet lẩu nướng',
    category: 'Ăn uống',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -880000,
    type: 'EXPENSE'
  },
  {
    key: '48',
    date: '2026-05-08T08:30:00',
    description: 'Mua vé xe buýt tháng',
    category: 'Di chuyển',
    fund: 'Tiền mặt',
    amount: -100000,
    type: 'EXPENSE'
  },
  {
    key: '49',
    date: '2026-05-07T11:30:00',
    description: 'Cơm gà xối mỡ',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -50000,
    type: 'EXPENSE'
  },
  {
    key: '50',
    date: '2026-05-06T15:00:00',
    description: 'Phí duy trì tài khoản',
    category: 'Tiện ích',
    fund: 'Vietcombank',
    amount: -11000,
    type: 'EXPENSE'
  },
  {
    key: '51',
    date: '2026-05-05T20:00:00',
    description: 'Mua sách kỹ năng',
    category: 'Mua sắm',
    fund: 'Tiền mặt',
    amount: -120000,
    type: 'EXPENSE'
  },
  {
    key: '52',
    date: '2026-05-04T10:00:00',
    description: 'Cà phê giao lưu',
    category: 'Ăn uống',
    fund: 'Tiền mặt',
    amount: -65000,
    type: 'EXPENSE'
  },
  {
    key: '53',
    date: '2026-05-03T18:30:00',
    description: 'Pizza Hut cuối tuần',
    category: 'Ăn uống',
    fund: 'Thẻ tín dụng (Dư nợ)',
    amount: -480000,
    type: 'EXPENSE'
  },
  {
    key: '54',
    date: '2026-05-02T13:00:00',
    description: 'Mua sắm phụ kiện điện thoại',
    category: 'Mua sắm',
    fund: 'Tiền mặt',
    amount: -180000,
    type: 'EXPENSE'
  }
];

export const MOCK_SAVINGS_GOALS = [
  {
    id: '1',
    name: 'Mua Nhà (Hà Nội)',
    percent: 45,
    color: '#1A73E8'
  },
  {
    id: '2',
    name: 'Du lịch Châu Âu',
    percent: 82,
    color: '#34A853'
  }
];
export const MOCK_AI_TIPS = {
  createFund: {
    show: true,
    message: 'SmartFinance sẽ tự động phân tích và tối ưu hóa hiệu suất cho quỹ mới của bạn.',
  },
  transfer: {
    show: true,
    message: 'Hệ thống AI gợi ý: Bạn thường chuyển từ Quỹ Tiết kiệm sang Quỹ Đầu tư vào ngày này hằng tháng để gia tăng tích lũy.',
    description: 'Chuyển tiền định kỳ từ Quỹ Tiết kiệm sang Quỹ Đầu tư tăng trưởng',
  },
  reminder: {
    show: true,
    message: 'AI nhận thấy hóa đơn Tiền điện của bạn thường phát sinh giao dịch vào ngày 15 hằng tháng.',
    defaultTitle: 'Hóa đơn Tiền điện',
  },
  mainCard: {
    show: true,
    title: 'GỢI Ý THÔNG MINH',
    content: 'Bạn có các khoản thanh toán định kỳ vào tuần tới trị giá 4.500.000 đ. Chúng tôi đã tự động dự phòng số dư này.',
    buttonText: 'Lên lịch thanh toán',
    actionTitle: 'Thanh toán tiền điện tháng 10',
    actionDate: null,
  },
};

export const MOCK_SAVINGS_GOALS_API = [
  {
    id: 1,
    name: 'Mua Nhà (Hà Nội)',
    targetAmount: 2000000000,
    currentAmount: 900000000,
    dueDate: '2030-12-31',
    status: 'ON_TRACK',
    category: 'housing',
    isPinned: true
  },
  {
    id: 2,
    name: 'Du lịch Châu Âu',
    targetAmount: 100000000,
    currentAmount: 82000000,
    dueDate: '2026-08-31',
    status: 'ON_TRACK',
    category: 'travel',
    isPinned: false
  }
];

export default {
  'GET /api/personal-funds/summary': (req: any, res: any) => {
    const fundsSum = MOCK_FUNDS_LIST.reduce((acc, f) => acc + f.balance, 0);
    const savingsSum = MOCK_SAVINGS_GOALS_API.reduce((acc, g) => acc + g.currentAmount, 0);
    res.json({
      totalAssets: fundsSum + savingsSum,
      growthRate: 12.5
    });
  },
  'GET /api/personal-funds/list': (req: any, res: any) => {
    res.json(MOCK_FUNDS_LIST);
  },
  'GET /api/personal-funds/balance-history': (req: any, res: any) => {
    res.json(MOCK_BALANCE_HISTORY);
  },
  'GET /api/personal-funds/distribution': (req: any, res: any) => {
    const bankSum = MOCK_FUNDS_LIST.filter(f => f.type === 'BANK' && f.balance > 0).reduce((acc, f) => acc + f.balance, 0);
    const cashSum = MOCK_FUNDS_LIST.filter(f => f.type === 'CASH' && f.balance > 0).reduce((acc, f) => acc + f.balance, 0);
    const savingsSum = MOCK_SAVINGS_GOALS_API.reduce((acc, g) => acc + g.currentAmount, 0);
    const total = bankSum + cashSum + savingsSum || 1;
    res.json([
      { type: 'Tài khoản Ngân hàng', value: bankSum, percentage: Math.round((bankSum / total) * 100), color: '#1A73E8' },
      { type: 'Đầu tư & Tiết kiệm', value: savingsSum, percentage: Math.round((savingsSum / total) * 100), color: '#34A853' },
      { type: 'Tiền mặt & Khác', value: cashSum, percentage: Math.round((cashSum / total) * 100), color: '#BCC1C6' }
    ]);
  },
  'GET /api/personal-funds/recent-transactions': (req: any, res: any) => {
    res.json(MOCK_RECENT_TRANSACTIONS);
  },
  'GET /api/personal-funds/savings-goals': (req: any, res: any) => {
    res.json(MOCK_SAVINGS_GOALS);
  },
  'GET /api/personal-funds/ai-tips': (req: any, res: any) => {
    res.json(MOCK_AI_TIPS);
  },
  'POST /api/personal-funds/create': (req: any, res: any) => {
    const { name, icon } = req.body;
    const newFund = {
      id: Date.now().toString(),
      name: name || 'Quỹ mới',
      balance: 0,
      type: icon === 'bank' ? 'BANK' : icon === 'card' ? 'CREDIT_CARD' : 'CASH',
      isNegative: false,
      icon: icon || 'wallet',
      actions: [
        { key: 'deposit', label: 'Nạp tiền' },
        { key: 'withdraw', label: 'Rút tiền' }
      ]
    };
    MOCK_FUNDS_LIST.push(newFund);
    res.json({ success: true, message: 'Tạo quỹ thành công', data: newFund });
  },
  'POST /api/personal-funds/transfer': (req: any, res: any) => {
    const { sourceId, targetId, amount } = req.body;
    const transferAmount = Number(amount);
    const source = MOCK_FUNDS_LIST.find(f => f.id === sourceId);
    const target = MOCK_FUNDS_LIST.find(f => f.id === targetId);
    if (source && target) {
      source.balance -= transferAmount;
      target.balance += transferAmount;
      res.json({ success: true, message: 'Chuyển tiền thành công' });
    } else {
      res.status(404).json({ success: false, message: 'Không tìm thấy tài khoản!' });
    }
  },
  'POST /api/personal-funds/pay-bill': (req: any, res: any) => {
    const { fundId, amount } = req.body;
    const expenseAmount = Number(amount);
    const fund = MOCK_FUNDS_LIST.find(f => f.id === fundId);
    if (fund) {
      fund.balance -= expenseAmount;
      res.json({ success: true, message: 'Thanh toán hóa đơn thành công' });
    } else {
      res.status(404).json({ success: false, message: 'Không tìm thấy quỹ thanh toán!' });
    }
  },
  'POST /api/personal-funds/reminder': (req: any, res: any) => {
    res.json({ success: true, message: 'Lên lịch nhắc thanh toán thành công' });
  },
  'GET /api/saving-goals': (req: any, res: any) => {
    res.json({ success: true, data: MOCK_SAVINGS_GOALS_API });
  },
  'PATCH /api/saving-goals/:id/deposit': (req: any, res: any) => {
    const { id } = req.params;
    const { amount, sourceId } = req.body;
    const goal = MOCK_SAVINGS_GOALS_API.find(g => g.id === parseInt(id));
    if (goal) {
      goal.currentAmount += amount;
      if (sourceId) {
        const fund = MOCK_FUNDS_LIST.find(f => f.id === sourceId);
        if (fund) {
          fund.balance -= amount;
        }
      }
      res.json({ success: true, message: `Đã nạp ${amount.toLocaleString('vi-VN')} đ vào mục tiêu "${goal.name}" thành công!` });
    } else {
      res.status(404).json({ success: false, message: 'Không tìm thấy mục tiêu!' });
    }
  },
  'PUT /api/personal-funds/:id': (req: any, res: any) => {
    const { id } = req.params;
    const { name } = req.body;
    const fund = MOCK_FUNDS_LIST.find(f => f.id === id);
    if (fund) {
      fund.name = name;
      res.json({ success: true, message: 'Cập nhật tên quỹ thành công!', data: fund });
    } else {
      res.status(404).json({ success: false, message: 'Không tìm thấy quỹ!' });
    }
  },
  'DELETE /api/personal-funds/:id': (req: any, res: any) => {
    const { id } = req.params;
    const fundIdx = MOCK_FUNDS_LIST.findIndex(f => f.id === id);
    if (fundIdx !== -1) {
      const deletedFund = MOCK_FUNDS_LIST.splice(fundIdx, 1);
      res.json({ success: true, message: `Đã xóa quỹ "${deletedFund[0].name}" thành công!` });
    } else {
      res.status(404).json({ success: false, message: 'Không tìm thấy quỹ!' });
    }
  },
};
