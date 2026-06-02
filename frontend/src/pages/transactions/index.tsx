import React, { useEffect, useMemo, useState } from 'react';
import {
  Avatar,
  Button,
  Card,
  Col,
  DatePicker,
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
  DollarCircleOutlined,
  FilterOutlined,
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

  const categoryOptions = useMemo(
    () =>
      categories
        .filter((category) => type === 'ALL' || category.type === type)
        .map((category) => ({
          value: category.id,
          label: `${category.name}${category.system ? '' : ' (ca nhan)'}`,
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
      message.error('Khong the tai danh muc hoac nguon tien');
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
      message.error(error.response?.data?.message || 'Khong the tai danh sach giao dich');
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
      message.success(res.data?.message || 'Da them giao dich');
      setIsAddTxOpen(false);
      addTxForm.resetFields();
      setPage(0);
      fetchTransactions();
      fetchFilterData();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Them giao dich that bai');
    } finally {
      setAddTxLoading(false);
    }
  };

  const columns = [
    {
      title: 'Chi tiet giao dich',
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
            <Text strong className={styles.txTitle}>{record.description || 'Giao dich'}</Text>
            <div className={styles.txSub}>
              {record.personalFundName ? (
                <Space size={4}>
                  <WalletOutlined />
                  <span>{record.personalFundName}</span>
                </Space>
              ) : (
                'Chua gan nguon tien'
              )}
            </div>
          </div>
        </Space>
      ),
    },
    {
      title: 'Danh muc',
      key: 'category',
      width: '20%',
      render: (_: any, record: Transaction) => (
        <Tag color={record.type === 'INCOME' ? 'success' : 'default'} className={styles.txTag}>
          {record.categoryName || 'Chua phan loai'}
        </Tag>
      ),
    },
    {
      title: 'Ngay giao dich',
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
      title: 'So tien',
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

  return (
    <div className={styles.premiumContainer}>
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={17}>
          <div className={styles.leftSection}>
            <div className={styles.sectionHeader}>
              <Title level={3} className={styles.mainTitle}>Giao dich</Title>
              <Space size="middle" className={styles.actionButtons}>
                <Button icon={<PlusOutlined />} type="primary" className={styles.addBtn} onClick={handleOpenAdd}>
                  Them moi
                </Button>
              </Space>
            </div>

            <div className={styles.filterBarRow}>
              <Space size="middle" className={styles.capsuleFilters}>
                <Input
                  prefix={<SearchOutlined style={{ color: '#8c8c8c' }} />}
                  placeholder="Tim noi dung..."
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
                    { value: 'ALL', label: 'Tat ca loai' },
                    { value: 'INCOME', label: 'Khoan thu' },
                    { value: 'EXPENSE', label: 'Khoan chi' },
                  ]}
                />

                <Select
                  value={categoryId}
                  placeholder="Tat ca danh muc"
                  onChange={(val) => { setCategoryId(val); setPage(0); }}
                  bordered={false}
                  allowClear
                  className={styles.capsuleSelect}
                  suffixIcon={<TagsOutlined />}
                  options={categoryOptions}
                />

                <Select
                  value={personalFundId}
                  placeholder="Tat ca nguon tien"
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
                  placeholder={['Tu ngay', 'Den ngay']}
                />

                <Button type="primary" icon={<FilterOutlined />} className={styles.btnFilter} onClick={handleSearch} title="Tim kiem" />
                <Button icon={<ReloadOutlined />} className={styles.btnReload} onClick={handleResetFilters} title="Dat lai bo loc" />
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
                  <Text strong style={{ fontSize: '15px' }}>Phan tich Ngan sach AI</Text>
                </Space>
              </div>
              <Text className={styles.aiContent}>
                Danh muc va nguon tien da duoc noi API, nen phan tich chi tieu co the tinh theo nhom giao dich that.
              </Text>
            </Card>

            <Card bordered={false} className={styles.patternCard}>
              <div className={styles.cardHeader}>
                <Text strong style={{ fontSize: '15px', color: '#202124' }}>Mau chi tieu</Text>
              </div>
              <div className={styles.patternBody}>
                <div className={styles.patternItem}>
                  <div className={styles.patternMeta}>
                    <Text className={styles.patternName}>Nguon tien da loc</Text>
                    <Text className={styles.patternPercent} style={{ color: '#1A73E8' }}>{personalFundId ? '1' : 'Tat ca'}</Text>
                  </div>
                  <Progress percent={personalFundId ? 100 : 65} showInfo={false} strokeWidth={6} strokeColor="#1A73E8" />
                </div>
              </div>
            </Card>
          </Space>
        </Col>
      </Row>

      <Modal
        title={<span style={{ fontSize: '18px', fontWeight: 700 }}>Them giao dich moi</span>}
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
            label={<span style={{ fontWeight: 600 }}>Mo ta giao dich</span>}
            name="description"
            rules={[{ required: true, message: 'Vui long nhap mo ta giao dich!' }]}
          >
            <Input placeholder="Vi du: Mua sam sieu thi, Nhan luong..." style={{ borderRadius: 12 }} />
          </Form.Item>

          <Form.Item label={<span style={{ fontWeight: 600 }}>Loai giao dich</span>} name="type" rules={[{ required: true }]}>
            <Segmented
              options={[
                { label: 'Chi phi', value: 'EXPENSE' },
                { label: 'Thu nhap', value: 'INCOME' },
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
                label={<span style={{ fontWeight: 600 }}>Danh muc</span>}
                name="categoryId"
                rules={[{ required: true, message: 'Vui long chon danh muc!' }]}
              >
                <Select
                  placeholder="Chon danh muc..."
                  style={{ borderRadius: 12 }}
                  options={categories
                    .filter((category) => category.type === getFieldValue('type'))
                    .map((category) => ({ value: category.id, label: category.name }))}
                />
              </Form.Item>
            )}
          </Form.Item>

          <Form.Item
            label={<span style={{ fontWeight: 600 }}>Nguon tien</span>}
            name="personalFundId"
            rules={[{ required: true, message: 'Vui long chon nguon tien!' }]}
          >
            <Select
              placeholder="Chon vi hoac tai khoan..."
              style={{ borderRadius: 12 }}
              options={fundOptions}
              suffixIcon={<BankOutlined />}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label={<span style={{ fontWeight: 600 }}>So tien</span>}
                name="amount"
                rules={[
                  { required: true, message: 'Vui long nhap so tien!' },
                  { type: 'number', min: 1, message: 'So tien phai lon hon 0!' },
                ]}
              >
                <InputNumber
                  min={1}
                  style={{ width: '100%', borderRadius: 12 }}
                  formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={(value) => value!.replace(/(,*)/g, '') as any}
                  placeholder="Nhap so tien"
                />
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item
                label={<span style={{ fontWeight: 600 }}>Ngay giao dich</span>}
                name="date"
                rules={[{ required: true, message: 'Vui long chon ngay!' }]}
              >
                <DatePicker style={{ width: '100%', borderRadius: 12 }} format="DD/MM/YYYY" placeholder="Chon ngay" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item style={{ marginBottom: 0, marginTop: 24, textAlign: 'right' }}>
            <Space size="middle">
              <Button onClick={() => setIsAddTxOpen(false)} style={{ borderRadius: 16, minWidth: 80 }}>Huy</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={addTxLoading}
                icon={<DollarCircleOutlined />}
                className={styles.primaryBtn}
                style={{ borderRadius: 16, minWidth: 140 }}
              >
                Luu giao dich
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
