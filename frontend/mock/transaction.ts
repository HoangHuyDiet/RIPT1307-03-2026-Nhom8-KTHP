const CATEGORIES = [
  { id: 1, name: 'Ăn uống', type: 'EXPENSE', description: 'Chi phí ăn uống, đồ uống hàng ngày' },
  { id: 2, name: 'Di chuyển', type: 'EXPENSE', description: 'Xăng, xe bus, grab, taxi...' },
  { id: 3, name: 'Mua sắm', type: 'EXPENSE', description: 'Quần áo, đồ dùng, mỹ phẩm...' },
  { id: 4, name: 'Giải trí', type: 'EXPENSE', description: 'Xem phim, du lịch, game...' },
  { id: 5, name: 'Giáo dục', type: 'EXPENSE', description: 'Học phí, sách vở, khóa học...' },
  { id: 6, name: 'Sức khỏe', type: 'EXPENSE', description: 'Khám bệnh, thuốc, gym...' },
  { id: 7, name: 'Nhà cửa', type: 'EXPENSE', description: 'Tiền thuê, điện, nước, internet...' },
  { id: 8, name: 'Hóa đơn', type: 'EXPENSE', description: 'Điện thoại, bảo hiểm, thuế...' },
  { id: 9, name: 'Quà tặng', type: 'EXPENSE', description: 'Sinh nhật, lễ tết, từ thiện...' },
  { id: 10, name: 'Chi khác', type: 'EXPENSE', description: 'Các khoản chi phí khác' },

  { id: 11, name: 'Lương', type: 'INCOME', description: 'Lương tháng, lương thưởng' },
  { id: 12, name: 'Thưởng', type: 'INCOME', description: 'Thưởng KPI, thưởng dự án' },
  { id: 13, name: 'Đầu tư', type: 'INCOME', description: 'Lãi cổ phiếu, crypto, bất động sản...' },
  { id: 14, name: 'Bán hàng', type: 'INCOME', description: 'Thu nhập từ kinh doanh, bán đồ' },
  { id: 15, name: 'Freelance', type: 'INCOME', description: 'Thu nhập làm thêm, freelance' },
  { id: 16, name: 'Thu khác', type: 'INCOME', description: 'Các khoản thu nhập khác' }
];

const MOCK_TRANSACTIONS = Array.from({ length: 25 }, (_, index) => {
  const id = index + 1;
  const isExpense = id % 3 !== 0; 
  
  const categoryList = CATEGORIES.filter(c => c.type === (isExpense ? 'EXPENSE' : 'INCOME'));
  const category = categoryList[id % categoryList.length];

  const dateObj = new Date();
  dateObj.setDate(dateObj.getDate() - (id % 15));
  const dateStr = dateObj.toISOString().split('T')[0];

  return {
    id: id,
    amount: isExpense ? (id * 15000 + 20000) : (id * 100000 + 500000),
    type: isExpense ? 'EXPENSE' : 'INCOME',
    description: isExpense 
      ? `Chi trả cho ${category.name} lần thứ ${id}`
      : `Nhận khoản ${category.name} lần thứ ${id}`,
    date: dateStr,
    category: {
      id: category.id,
      name: category.name
    }
  };
});

export default {
  'GET /api/transactions/search': (req: any, res: any) => {
    const { 
      page = '0', 
      size = '10', 
      type, 
      category_id, 
      search, 
      start_date, 
      end_date 
    } = req.query;

    const pageNum = parseInt(page, 10);
    const pageSize = parseInt(size, 10);

    let filtered = [...MOCK_TRANSACTIONS];

    if (type && type !== 'ALL') {
      filtered = filtered.filter(item => item.type === type);
    }

    if (category_id) {
      filtered = filtered.filter(item => item.category.id === parseInt(category_id, 10));
    }

    if (search) {
      const keyword = search.toLowerCase();
      filtered = filtered.filter(item => 
        item.description.toLowerCase().includes(keyword)
      );
    }

    if (start_date && end_date) {
      filtered = filtered.filter(item => 
        item.date >= start_date && item.date <= end_date
      );
    }

    filtered.sort((a, b) => b.date.localeCompare(a.date));

    const totalElements = filtered.length;
    const totalPages = Math.ceil(totalElements / pageSize);
    const startIdx = pageNum * pageSize;
    const paginatedContent = filtered.slice(startIdx, startIdx + pageSize);

    res.json({
      content: paginatedContent,
      page_number: pageNum,
      page_size: pageSize,
      total_elements: totalElements,
      total_pages: totalPages
    });
  },

  'GET /api/categories': (req: any, res: any) => {
    res.json(CATEGORIES);
  },

  'POST /api/funds/invite': (req: any, res: any) => {
    const { email } = req.body;
    
    if (!email) {
      return res.status(400).json({
        status: 'error',
        message: 'Email không được để trống!'
      });
    }

    res.json({
      status: 'success',
      message: `Đã gửi lời mời tham gia quỹ đến email: ${email}!`
    });
  },

  'POST /api/transactions': (req: any, res: any) => {
    const { amount, type, description, date, categoryId } = req.body;

    const newId = MOCK_TRANSACTIONS.length + 1;

    const category = CATEGORIES.find(c => c.id === categoryId);

    const newTransaction = {
      id: newId,
      amount: parseFloat(amount),
      type: type,
      description: description || '',
      date: date, 
      category: {
        id: categoryId,
        name: category?.name || 'Không xác định'
      }
    };

    MOCK_TRANSACTIONS.unshift(newTransaction);

    res.json({
      status: 'success',
      message: 'Thêm giao dịch thành công!',
      data: newTransaction
    });
  }
};
