type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED';
type CategoryType = 'INCOME' | 'EXPENSE';

type User = {
  id: number;
  email: string;
  displayName: string;
  roles: string[];
  status: UserStatus;
  createdAt: string;
};

type Category = {
  id: number;
  name: string;
  type: CategoryType;
  description?: string;
  system?: boolean;
  createdAt?: string;
  updatedAt?: string;
};

let users: User[] = [
  { id: 1, email: 'admin@smartfinance.vn', displayName: 'Quản trị viên hệ thống', roles: ['ADMIN'], status: 'ACTIVE', createdAt: '2026-01-01' },
  { id: 2, email: 'support1@smartfinance.vn', displayName: 'Support Nguyễn', roles: ['SUPPORT_ADMIN'], status: 'ACTIVE', createdAt: '2026-01-05' },
  { id: 3, email: 'support2@smartfinance.vn', displayName: 'Support Trần', roles: ['SUPPORT_ADMIN'], status: 'ACTIVE', createdAt: '2026-01-10' },
  { id: 4, email: 'dana@gmail.com', displayName: 'Nguyễn Văn A', roles: ['USER'], status: 'ACTIVE', createdAt: '2026-01-15' },
  { id: 5, email: 'tranb@gmail.com', displayName: 'Trần Thị B', roles: ['USER'], status: 'ACTIVE', createdAt: '2026-02-20' },
  { id: 6, email: 'levanc@gmail.com', displayName: 'Lê Văn C', roles: ['USER'], status: 'BANNED', createdAt: '2026-03-05' },
  { id: 7, email: 'phamminhdung@gmail.com', displayName: 'Phạm Minh D', roles: ['USER'], status: 'ACTIVE', createdAt: '2026-03-10' },
  { id: 8, email: 'hoangthi.e@gmail.com', displayName: 'Hoàng Thị E', roles: ['USER'], status: 'ACTIVE', createdAt: '2026-01-28' },
  { id: 9, email: 'vuduc.f@gmail.com', displayName: 'Vũ Đức F', roles: ['USER'], status: 'BANNED', createdAt: '2026-04-01' },
  { id: 10, email: 'nguyeng@gmail.com', displayName: 'Nguyễn Thị G', roles: ['USER'], status: 'INACTIVE', createdAt: '2026-04-15' },
  { id: 11, email: 'buih@gmail.com', displayName: 'Bùi Văn H', roles: ['USER'], status: 'ACTIVE', createdAt: '2026-05-01' },
  { id: 12, email: 'lethii@gmail.com', displayName: 'Lê Thị I', roles: ['USER'], status: 'ACTIVE', createdAt: '2026-05-20' }
];

let categories: Category[] = [
  // EXPENSE
  { id: 1, name: 'Ăn uống', type: 'EXPENSE', description: 'Chi tiêu cho ăn uống hàng ngày', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 2, name: 'Di chuyển', type: 'EXPENSE', description: 'Xăng xe, taxi, xe bus, giao thông', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 3, name: 'Hóa đơn & Tiện ích', type: 'EXPENSE', description: 'Điện, nước, internet, điện thoại', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 4, name: 'Mua sắm', type: 'EXPENSE', description: 'Quần áo, đồ dùng, điện tử', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 5, name: 'Giải trí', type: 'EXPENSE', description: 'Phim, du lịch, game, thể thao', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 6, name: 'Sức khỏe & Y tế', type: 'EXPENSE', description: 'Thuốc, khám bệnh, bảo hiểm y tế', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 7, name: 'Giáo dục', type: 'EXPENSE', description: 'Học phí, sách vở, khóa học', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 8, name: 'Nhà ở', type: 'EXPENSE', description: 'Thuê nhà, sửa chữa, nội thất', system: false, createdAt: '2026-02-01', updatedAt: '2026-02-01' },
  { id: 9, name: 'Đầu tư', type: 'EXPENSE', description: 'Cổ phiếu, quỹ đầu tư, tiết kiệm', system: false, createdAt: '2026-03-01', updatedAt: '2026-03-01' },
  // INCOME
  { id: 10, name: 'Lương', type: 'INCOME', description: 'Thu nhập từ lương hàng tháng', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 11, name: 'Thưởng', type: 'INCOME', description: 'Thưởng hiệu suất, thưởng dự án', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 12, name: 'Kinh doanh', type: 'INCOME', description: 'Thu nhập từ kinh doanh tự do', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 13, name: 'Đầu tư & Cổ tức', type: 'INCOME', description: 'Lợi nhuận từ cổ phiếu, trái phiếu', system: true, createdAt: '2026-01-01', updatedAt: '2026-01-01' },
  { id: 14, name: 'Cho thuê', type: 'INCOME', description: 'Thu nhập từ cho thuê nhà, xe', system: false, createdAt: '2026-02-01', updatedAt: '2026-02-01' },
  { id: 15, name: 'Quà tặng & Hỗ trợ', type: 'INCOME', description: 'Tiền nhận quà, hỗ trợ từ gia đình', system: false, createdAt: '2026-04-01', updatedAt: '2026-04-01' }
];

let nextUserId = users.length + 1;
let nextCategoryId = categories.length + 1;

export default {
  // ─── USERS ──────────────────────────────────────────────────────────────────

  'GET /api/admin/users': (req: any, res: any) => {
    res.json({ success: true, data: users });
  },

  'POST /api/admin/users': (req: any, res: any) => {
    const { email, displayName, roles, status, password } = req.body;
    if (users.find(u => u.email === email)) {
      return res.status(409).json({ success: false, message: 'Email đã tồn tại trong hệ thống' });
    }
    const newUser: User = {
      id: nextUserId++,
      email,
      displayName,
      roles: roles || ['USER'],
      status: status || 'ACTIVE',
      createdAt: new Date().toISOString().split('T')[0]
    };
    users.push(newUser);
    res.json({ success: true, data: newUser });
  },

  'PUT /api/admin/users/:id': (req: any, res: any) => {
    const id = Number(req.params.id);
    const index = users.findIndex(u => u.id === id);
    if (index === -1) return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    users[index] = { ...users[index], ...req.body };
    res.json({ success: true, data: users[index] });
  },

  'PUT /api/admin/users/:id/status': (req: any, res: any) => {
    const id = Number(req.params.id);
    const { status } = req.body;
    const user = users.find(u => u.id === id);
    if (!user) return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    user.status = status;
    res.json({ success: true, data: user });
  },

  'DELETE /api/admin/users/:id': (req: any, res: any) => {
    const id = Number(req.params.id);
    const index = users.findIndex(u => u.id === id);
    if (index === -1) return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    users.splice(index, 1);
    res.json({ success: true });
  },

  // ─── CATEGORIES ─────────────────────────────────────────────────────────────

  'GET /api/admin/categories': (req: any, res: any) => {
    const { type } = req.query;
    const result = type ? categories.filter(c => c.type === type) : categories;
    res.json({ success: true, data: result });
  },

  'POST /api/admin/categories': (req: any, res: any) => {
    const { name, type, description } = req.body;
    if (categories.find(c => c.name === name && c.type === type)) {
      return res.status(409).json({ success: false, message: 'Danh mục đã tồn tại' });
    }
    const now = new Date().toISOString();
    const newCategory: Category = {
      id: nextCategoryId++,
      name,
      type,
      description,
      system: false,
      createdAt: now,
      updatedAt: now
    };
    categories.push(newCategory);
    res.json({ success: true, data: newCategory });
  },

  'PUT /api/admin/categories/:id': (req: any, res: any) => {
    const id = Number(req.params.id);
    const index = categories.findIndex(c => c.id === id);
    if (index === -1) return res.status(404).json({ success: false, message: 'Không tìm thấy danh mục' });
    categories[index] = { ...categories[index], ...req.body, updatedAt: new Date().toISOString() };
    res.json({ success: true, data: categories[index] });
  },

  'DELETE /api/admin/categories/:id': (req: any, res: any) => {
    const id = Number(req.params.id);
    const index = categories.findIndex(c => c.id === id);
    if (index === -1) return res.status(404).json({ success: false, message: 'Không tìm thấy danh mục' });
    categories.splice(index, 1);
    res.json({ success: true });
  }
};
