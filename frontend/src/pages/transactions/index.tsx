import React, { useEffect, useMemo, useState } from 'react';
import {
  Avatar,
  Button,
  Card,
  Col,
  DatePicker,
  Divider,
  Empty,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Progress,
  Row,
  Segmented,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import {
  BankOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  CoffeeOutlined,
  CreditCardOutlined,
  DollarCircleOutlined,
  FilterOutlined,
  MobileOutlined,
  PlusOutlined,
  ReloadOutlined,
  RobotOutlined,
  SearchOutlined,
  TagsOutlined,
  WalletOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import api from '../../utils/api';
import { Category, categoryApi, CategoryType } from '../../services/categories';
import styles from './index.less';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const FUND_TYPE_OPTIONS = [
  { value: 'CASH', label: 'Tiền mặt', icon: <WalletOutlined /> },
  { value: 'BANK_ACCOUNT', label: 'Tài khoản ngân hàng', icon: <BankOutlined /> },
  { value: 'E_WALLET', label: 'Ví điện tử', icon: <MobileOutlined /> },
  { value: 'CREDIT_CARD', label: 'Thẻ tín dụng', icon: <CreditCardOutlined /> },
  { value: 'INVESTMENT', label: 'Đầu tư', icon: <DollarCircleOutlined /> },
];

const getFundTypeMeta = (fundType?: string) =>
  FUND_TYPE_OPTIONS.find((item) => item.value === fundType) || FUND_TYPE_OPTIONS[0];

interface Transaction {
  id: number;
  amount: number;
  type: CategoryType;
  description: string;
  date: string;
  categoryName?: string;
  personalFundName?: string;
}

interface PersonalFund {
  id: number;
  name: string;
  balance: number;
  fundType?: string;
}

const formatCurrency = (value: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0);

export default function TransactionsManager() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [personalFunds, setPersonalFunds] = useState<PersonalFund[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState('');
  const [type, setType] = useState<CategoryType | 'ALL'>('ALL');
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [personalFundId, setPersonalFundId] = useState<number | undefined>();
  const [dateRange, setDateRange] = useState<[string, string] | undefined>();

  const [isAddTxOpen, setIsAddTxOpen] = useState(false);
  const [addTxLoading, setAddTxLoading] = useState(false);
  const [addTxForm] = Form.useForm();
  const [quickFundOpen, setQuickFundOpen] = useState(false);
  const [quickFundLoading, setQuickFundLoading] = useState(false);
  const [quickFundForm] = Form.useForm();
  const watchedFundId = Form.useWatch('personalFundId', addTxForm);
  const watchedTxType = Form.useWatch('type', addTxForm);
  const watchedAmount = Form.useWatch('amount', addTxForm);

  const selectedFund = useMemo(
    () => personalFunds.find((fund) => fund.id === watchedFundId),
    [personalFunds, watchedFundId],
  );

  const balanceAfterTransaction = useMemo(() => {
    if (!selectedFund || !watchedAmount) return undefined;
    const currentBalance = Number(selectedFund.balance || 0);
    const amount = Number(watchedAmount || 0);
    return watchedTxType === 'INCOME' ? currentBalance + amount : currentBalance - amount;
  }, [selectedFund, watchedAmount, watchedTxType]);

  const categoryOptions = useMemo(
    () =>
      categories
        .filter((category) => type === 'ALL' || category.type === type)
        .map((category) => ({
          value: category.id,
          label: `${category.name}${category.system ? '' : ' (cá nhân)'}`,
        })),
    [categories, type],
  );

  const fundOptions = useMemo(
    () =>
      personalFunds.map((fund) => ({
        value: fund.id,
        label: `${fund.name} - ${formatCurrency(Number(fund.balance))}`,
      })),
    [personalFunds],
  );

  const richFundOptions = useMemo(
    () =>
      personalFunds.map((fund) => {
        const fundType = getFundTypeMeta(fund.fundType);
        return {
          value: fund.id,
          label: (
            <div className={styles.fundOption}>
              <div className={styles.fundOptionIcon}>{fundType.icon}</div>
              <div className={styles.fundOptionMeta}>
                <Text strong className={styles.fundOptionName}>{fund.name}</Text>
                <Text type="secondary" className={styles.fundOptionSub}>
                  {fundType.label} · {formatCurrency(Number(fund.balance))}
                </Text>
              </div>
            </div>
          ),
        };
      }),
    [personalFunds],
  );

  const fetchFilterData = async () => {
    try {
      const [categoryData, fundsRes] = await Promise.all([
        categoryApi.list(),
        api.get('/personal-funds/list'),
      ]);
      const fundsData = fundsRes.data?.data ?? fundsRes.data ?? [];
      setCategories(Array.isArray(categoryData) ? categoryData : []);
      setPersonalFunds(Array.isArray(fundsData) ? fundsData : []);
    } catch (error) {
      console.error(error);
      message.error('Không thể tải danh mục hoặc nguồn tiền');
    }
  };

  const fetchTransactions = async () => {
    setLoading(true);
    try {
      const params: any = { page, size };
      if (type !== 'ALL') params.type = type;
      if (categoryId) params.categoryId = categoryId;
      if (personalFundId) params.personalFundId = personalFundId;
      if (search.trim()) params.search = search.trim();
      if (dateRange) {
        params.startDate = dateRange[0];
        params.endDate = dateRange[1];
      }

      const res = await api.get('/transactions', { params });
      const data = res.data?.data ?? {};
      setTransactions(data.content || []);
      setTotalElements(data.totalElements || data.total_elements || 0);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Không thể tải danh sách giao dịch');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFilterData();
  }, []);

  useEffect(() => {
    fetchTransactions();
  }, [page, size, type, categoryId, personalFundId, dateRange]);

  const handleSearch = () => {
    setPage(0);
    fetchTransactions();
  };

  const handleResetFilters = () => {
    setSearch('');
    setType('ALL');
    setCategoryId(undefined);
    setPersonalFundId(undefined);
    setDateRange(undefined);
    setPage(0);
  };

  const handleOpenAdd = () => {
    addTxForm.resetFields();
    addTxForm.setFieldsValue({ type: 'EXPENSE', date: dayjs() });
    setIsAddTxOpen(true);
  };

  const openQuickFundModal = () => {
    quickFundForm.resetFields();
    quickFundForm.setFieldsValue({
      fundType: 'CASH',
      initialBalance: 0,
      currency: 'VND',
    });
    setQuickFundOpen(true);
  };

  const handleCreateQuickFund = async () => {
    try {
      const values = await quickFundForm.validateFields();
      setQuickFundLoading(true);
      const payload = {
        name: values.name,
        fundType: values.fundType,
        initialBalance: Number(values.initialBalance || 0),
        currency: 'VND',
        description: values.description || '',
      };

      const res = await api.post('/personal-funds/create', payload);
      const createdFund = res.data?.data;
      if (createdFund?.id) {
        setPersonalFunds((prev) => [...prev.filter((fund) => fund.id !== createdFund.id), createdFund]);
        addTxForm.setFieldValue('personalFundId', createdFund.id);
      } else {
        await fetchFilterData();
      }
      message.success(res.data?.message || 'Đã tạo nguồn tiền');
      setQuickFundOpen(false);
      quickFundForm.resetFields();
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.response?.data?.message || 'Không thể tạo nguồn tiền');
    } finally {
      setQuickFundLoading(false);
    }
  };

  const handleAddTransactionSubmit = async (values: any) => {
    setAddTxLoading(true);
    try {
      const payload = {
        amount: Number(values.amount),
        type: values.type,
        description: values.description || '',
        date: values.date.format('YYYY-MM-DD'),
        categoryId: values.categoryId,
        personalFundId: values.personalFundId,
      };

      const res = await api.post('/transactions', payload);
      message.success(res.data?.message || 'Đã thêm giao dịch');
      setIsAddTxOpen(false);
      addTxForm.resetFields();
      setPage(0);
      fetchTransactions();
      fetchFilterData();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Thêm giao dịch thất bại');
    } finally {
      setAddTxLoading(false);
    }
  };

  const columns = [
    {
      title: 'Chi tiết giao dịch',
      key: 'details',
      width: '42%',
      render: (_: any, record: Transaction) => (
        <Space size="middle" align="center">
          <Avatar
            size={40}
            icon={record.type === 'INCOME' ? <CheckCircleOutlined /> : <CoffeeOutlined />}
            style={{
              backgroundColor: record.type === 'INCOME' ? '#F6FFED' : '#F0F7FF',
              color: record.type === 'INCOME' ? '#52C41A' : '#1A73E8',
            }}
          />
          <div className={styles.txMeta}>
            <Text strong className={styles.txTitle}>{record.description || 'Giao dịch'}</Text>
            <div className={styles.txSub}>
              {record.personalFundName ? (
                <Space size={4}>
                  <WalletOutlined />
                  <span>{record.personalFundName}</span>
                </Space>
              ) : (
                'Chưa gắn nguồn tiền'
              )}
            </div>
          </div>
        </Space>
      ),
    },
    {
      title: 'Danh mục',
      key: 'category',
      width: '20%',
      render: (_: any, record: Transaction) => (
        <Tag color={record.type === 'INCOME' ? 'success' : 'default'} className={styles.txTag}>
          {record.categoryName || 'Chưa phân loại'}
        </Tag>
      ),
    },
    {
      title: 'Ngày giao dịch',
      dataIndex: 'date',
      key: 'date',
      width: '18%',
      render: (dateStr: string) => (
        <Space size="small">
          <CalendarOutlined style={{ color: '#8c8c8c' }} />
          <span>{dateStr ? dayjs(dateStr).format('DD/MM/YYYY') : '-'}</span>
        </Space>
      ),
    },
    {
      title: 'Số tiền',
      key: 'amount',
      width: '20%',
      align: 'right' as const,
      render: (_: any, record: Transaction) => (
        <span className={record.type === 'INCOME' ? styles.amountIncome : styles.amountExpense}>
          {record.type === 'INCOME' ? '+' : '-'}{formatCurrency(Number(record.amount))}
        </span>
      ),
    },
  ];

  const transactionAiTips = useMemo(() => {
    if (transactions.length === 0) {
      return [
        'Chưa có giao dịch trong bộ lọc hiện tại. Hãy thêm giao dịch và gắn đúng danh mục để nhận phân tích chính xác hơn.',
        'Nên chọn nguồn tiền cho mỗi giao dịch để hệ thống theo dõi dòng tiền theo ví/tài khoản.',
        'Bạn có thể lọc theo tháng hoặc danh mục để xem nhóm chi tiêu nào cần kiểm soát trước.',
      ];
    }

    const income = transactions
      .filter((item) => item.type === 'INCOME')
      .reduce((sum, item) => sum + Number(item.amount || 0), 0);
    const expense = transactions
      .filter((item) => item.type === 'EXPENSE')
      .reduce((sum, item) => sum + Number(item.amount || 0), 0);
    const uncategorized = transactions.filter((item) => !item.categoryName).length;
    const withoutFund = transactions.filter((item) => !item.personalFundName).length;
    const topExpense = transactions
      .filter((item) => item.type === 'EXPENSE')
      .sort((left, right) => Number(right.amount || 0) - Number(left.amount || 0))[0];

    const tips: string[] = [];
    tips.push(`Bộ lọc hiện tại ghi nhận thu ${formatCurrency(income)} và chi ${formatCurrency(expense)}.`);
    if (topExpense) {
      tips.push(`Khoản chi lớn nhất là "${topExpense.description || 'Giao dịch'}" với ${formatCurrency(Number(topExpense.amount))}.`);
    }
    if (uncategorized > 0 || withoutFund > 0) {
      tips.push(`Có ${uncategorized} giao dịch chưa phân loại và ${withoutFund} giao dịch chưa gắn nguồn tiền, nên bổ sung để phân tích chuẩn hơn.`);
    } else {
      tips.push('Danh mục và nguồn tiền đã khá đầy đủ, có thể dùng bộ lọc để soi sâu từng nhóm chi tiêu.');
    }
    return tips.slice(0, 3);
  }, [transactions]);

  return (
    <div className={styles.premiumContainer}>
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={17}>
          <div className={styles.leftSection}>
            <div className={styles.sectionHeader}>
              <Title level={3} className={styles.mainTitle}>Giao dịch</Title>
              <Space size="middle" className={styles.actionButtons}>
                <Button icon={<PlusOutlined />} type="primary" className={styles.addBtn} onClick={handleOpenAdd}>
                  Thêm mới
                </Button>
              </Space>
            </div>

            <div className={styles.filterBarRow}>
              <Space size="middle" className={styles.capsuleFilters}>
                <Input
                  prefix={<SearchOutlined style={{ color: '#8c8c8c' }} />}
                  placeholder="Tìm nội dung..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  onPressEnter={handleSearch}
                  allowClear
                  bordered={false}
                  className={styles.capsuleInput}
                />

                <Select
                  value={type}
                  onChange={(val) => {
                    setType(val);
                    setCategoryId(undefined);
                    setPage(0);
                  }}
                  bordered={false}
                  className={styles.capsuleSelect}
                  suffixIcon={<CalendarOutlined />}
                  options={[
                    { value: 'ALL', label: 'Tất cả loại' },
                    { value: 'INCOME', label: 'Khoản thu' },
                    { value: 'EXPENSE', label: 'Khoản chi' },
                  ]}
                />

                <Select
                  value={categoryId}
                  placeholder="Tất cả danh mục"
                  onChange={(val) => { setCategoryId(val); setPage(0); }}
                  bordered={false}
                  allowClear
                  className={styles.capsuleSelect}
                  suffixIcon={<TagsOutlined />}
                  options={categoryOptions}
                />

                <Select
                  value={personalFundId}
                  placeholder="Tất cả nguồn tiền"
                  onChange={(val) => { setPersonalFundId(val); setPage(0); }}
                  bordered={false}
                  allowClear
                  className={styles.capsuleSelect}
                  suffixIcon={<WalletOutlined />}
                  options={fundOptions}
                />

                <RangePicker
                  bordered={false}
                  className={styles.capsuleRangePicker}
                  onChange={(dates) => {
                    if (dates && dates[0] && dates[1]) {
                      setDateRange([dates[0].format('YYYY-MM-DD'), dates[1].format('YYYY-MM-DD')]);
                    } else {
                      setDateRange(undefined);
                    }
                    setPage(0);
                  }}
                  placeholder={['Từ ngày', 'Đến ngày']}
                />

                <Button type="primary" icon={<FilterOutlined />} className={styles.btnFilter} onClick={handleSearch} title="Tìm kiếm" />
                <Button icon={<ReloadOutlined />} className={styles.btnReload} onClick={handleResetFilters} title="Đặt lại bộ lọc" />
              </Space>
            </div>

            <div className={styles.tableWrapper}>
              <Table
                columns={columns}
                dataSource={transactions.map((t) => ({ ...t, key: t.id }))}
                loading={loading}
                pagination={{
                  current: page + 1,
                  pageSize: size,
                  total: totalElements,
                  showSizeChanger: true,
                  pageSizeOptions: ['5', '10', '20'],
                  onChange: (p, s) => {
                    setPage(p - 1);
                    setSize(s);
                  },
                }}
                className={styles.cleanTable}
              />
            </div>
          </div>
        </Col>

        <Col xs={24} lg={7}>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card bordered={false} className={styles.aiCard}>
              <div className={styles.aiHeader}>
                <Space>
                  <RobotOutlined className={styles.aiRobotIcon} />
                  <Text strong style={{ fontSize: '15px' }}>Phân tích Ngân sách AI</Text>
                </Space>
              </div>
              <Text className={styles.aiContent}>
                Danh mục và nguồn tiền đã được nối API, nên phân tích chi tiêu có thể tính theo nhóm giao dịch thật.
              </Text>
              <Text className={styles.aiTipIntro}>Gợi ý miễn phí dựa trên giao dịch đang hiển thị:</Text>
              <div className={styles.aiTipList}>
                {transactionAiTips.map((tip) => (
                  <div key={tip} className={styles.aiTipItem}>
                    <span>✓</span>
                    <p>{tip}</p>
                  </div>
                ))}
              </div>
            </Card>

            <Card bordered={false} className={styles.patternCard}>
              <div className={styles.cardHeader}>
                <Text strong style={{ fontSize: '15px', color: '#202124' }}>Mẫu chi tiêu</Text>
              </div>
              <div className={styles.patternBody}>
                <div className={styles.patternItem}>
                  <div className={styles.patternMeta}>
                    <Text className={styles.patternName}>Nguồn tiền đã lọc</Text>
                    <Text className={styles.patternPercent} style={{ color: '#1A73E8' }}>{personalFundId ? '1' : 'Tất cả'}</Text>
                  </div>
                  <Progress percent={personalFundId ? 100 : 65} showInfo={false} strokeWidth={6} strokeColor="#1A73E8" />
                </div>
              </div>
            </Card>
          </Space>
        </Col>
      </Row>

      <Modal
        title={<span style={{ fontSize: '18px', fontWeight: 700 }}>Thêm giao dịch mới</span>}
        open={isAddTxOpen}
        onCancel={() => { setIsAddTxOpen(false); addTxForm.resetFields(); }}
        footer={null}
        destroyOnClose
        centered
        width={560}
      >
        <Form
          form={addTxForm}
          layout="vertical"
          onFinish={handleAddTransactionSubmit}
          size="large"
          initialValues={{ type: 'EXPENSE', date: dayjs() }}
          style={{ marginTop: 8 }}
        >
          <Form.Item
            label={<span style={{ fontWeight: 600 }}>Mô tả giao dịch</span>}
            name="description"
            rules={[{ required: true, message: 'Vui lòng nhập mô tả giao dịch!' }]}
          >
            <Input placeholder="Ví dụ: Mua sắm siêu thị, Nhận lương..." style={{ borderRadius: 12 }} />
          </Form.Item>

          <Form.Item label={<span style={{ fontWeight: 600 }}>Loại giao dịch</span>} name="type" rules={[{ required: true }]}>
            <Segmented
              options={[
                { label: 'Chi phí', value: 'EXPENSE' },
                { label: 'Thu nhập', value: 'INCOME' },
              ]}
              onChange={(val) => {
                addTxForm.setFieldValue('type', val);
                addTxForm.setFieldValue('categoryId', undefined);
              }}
              style={{ borderRadius: 16, padding: 3, backgroundColor: '#f1f3f8' }}
            />
          </Form.Item>

          <Form.Item noStyle shouldUpdate={(prev, curr) => prev.type !== curr.type}>
            {({ getFieldValue }) => (
              <Form.Item
                label={<span style={{ fontWeight: 600 }}>Danh mục</span>}
                name="categoryId"
                rules={[{ required: true, message: 'Vui lòng chọn danh mục!' }]}
              >
                <Select
                  placeholder="Chọn danh mục..."
                  style={{ borderRadius: 12 }}
                  options={categories
                    .filter((category) => category.type === getFieldValue('type'))
                    .map((category) => ({ value: category.id, label: category.name }))}
                />
              </Form.Item>
            )}
          </Form.Item>

          <Form.Item
            label={<span style={{ fontWeight: 600 }}>Nguồn tiền</span>}
            name="personalFundId"
            rules={[{ required: true, message: 'Vui lòng chọn nguồn tiền!' }]}
          >
            <Select
              placeholder="Chọn ví hoặc tài khoản..."
              style={{ borderRadius: 12 }}
              options={richFundOptions}
              suffixIcon={<BankOutlined />}
              optionLabelProp="label"
              dropdownRender={(menu) => (
                <>
                  {personalFunds.length > 0 ? menu : (
                    <div className={styles.fundEmptyState}>
                      <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có nguồn tiền" />
                    </div>
                  )}
                  <Divider style={{ margin: '8px 0' }} />
                  <Button type="link" icon={<PlusOutlined />} onClick={openQuickFundModal} block>
                    Tạo nguồn tiền mới
                  </Button>
                </>
              )}
            />
          </Form.Item>

          {selectedFund && (
            <div className={styles.balancePreview}>
              <div>
                <Text type="secondary">Số dư hiện tại</Text>
                <Text strong>{formatCurrency(Number(selectedFund.balance))}</Text>
              </div>
              <div>
                <Text type="secondary">Sau giao dịch</Text>
                <Text strong className={balanceAfterTransaction !== undefined && balanceAfterTransaction < 0 ? styles.negativeBalance : ''}>
                  {balanceAfterTransaction !== undefined ? formatCurrency(balanceAfterTransaction) : '-'}
                </Text>
              </div>
            </div>
          )}

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label={<span style={{ fontWeight: 600 }}>Số tiền</span>}
                name="amount"
                rules={[
                  { required: true, message: 'Vui lòng nhập số tiền!' },
                  { type: 'number', min: 1, message: 'Số tiền phải lớn hơn 0!' },
                ]}
              >
                <InputNumber
                  min={1}
                  style={{ width: '100%', borderRadius: 12 }}
                  formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={(value) => value!.replace(/(,*)/g, '') as any}
                  placeholder="Nhập số tiền"
                />
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item
                label={<span style={{ fontWeight: 600 }}>Ngày giao dịch</span>}
                name="date"
                rules={[{ required: true, message: 'Vui lòng chọn ngày!' }]}
              >
                <DatePicker style={{ width: '100%', borderRadius: 12 }} format="DD/MM/YYYY" placeholder="Chọn ngày" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item style={{ marginBottom: 0, marginTop: 24, textAlign: 'right' }}>
            <Space size="middle">
              <Button onClick={() => setIsAddTxOpen(false)} style={{ borderRadius: 16, minWidth: 80 }}>Hủy</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={addTxLoading}
                icon={<DollarCircleOutlined />}
                className={styles.primaryBtn}
                style={{ borderRadius: 16, minWidth: 140 }}
              >
                Lưu giao dịch
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={<span style={{ fontSize: '18px', fontWeight: 700 }}>Tạo nguồn tiền mới</span>}
        open={quickFundOpen}
        onOk={handleCreateQuickFund}
        onCancel={() => setQuickFundOpen(false)}
        confirmLoading={quickFundLoading}
        okText="Tạo nguồn tiền"
        cancelText="Hủy"
        centered
        width={480}
      >
        <Form
          form={quickFundForm}
          layout="vertical"
          size="large"
          initialValues={{ fundType: 'CASH', initialBalance: 0, currency: 'VND' }}
          style={{ marginTop: 12 }}
        >
          <Form.Item
            name="name"
            label={<span style={{ fontWeight: 600 }}>Tên nguồn tiền</span>}
            rules={[{ required: true, message: 'Vui lòng nhập tên nguồn tiền!' }]}
          >
            <Input placeholder="Ví dụ: Ví tiền mặt, MB Bank, MoMo..." />
          </Form.Item>

          <Form.Item
            name="fundType"
            label={<span style={{ fontWeight: 600 }}>Loại nguồn tiền</span>}
            rules={[{ required: true, message: 'Vui lòng chọn loại nguồn tiền!' }]}
          >
            <Select
              options={FUND_TYPE_OPTIONS.map((item) => ({
                value: item.value,
                label: (
                  <Space>
                    {item.icon}
                    <span>{item.label}</span>
                  </Space>
                ),
              }))}
            />
          </Form.Item>

          <Form.Item
            name="initialBalance"
            label={<span style={{ fontWeight: 600 }}>Số dư ban đầu</span>}
            rules={[
              { required: true, message: 'Vui lòng nhập số dư ban đầu!' },
              { type: 'number', min: 0, message: 'Số dư ban đầu không được âm!' },
            ]}
          >
            <InputNumber
              min={0}
              style={{ width: '100%' }}
              formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => value!.replace(/(,*)/g, '') as any}
              placeholder="Nhập số dư ban đầu"
            />
          </Form.Item>

          <Form.Item name="description" label={<span style={{ fontWeight: 600 }}>Mô tả</span>}>
            <Input.TextArea rows={3} placeholder="Ghi chú ngắn về nguồn tiền này" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
