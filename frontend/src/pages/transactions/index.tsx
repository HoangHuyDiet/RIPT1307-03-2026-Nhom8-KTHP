import React, { useState, useEffect } from 'react';
import { 
  Table, Row, Col, Card, Typography, Space, Button, Input, Select, DatePicker, Modal, Form, Tag, message, Progress, Avatar, Radio, Segmented
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  WarningOutlined,
  LaptopOutlined,
  CheckCircleOutlined,
  GlobalOutlined,
  CoffeeOutlined,
  RobotOutlined,
  ArrowRightOutlined,
  CalendarOutlined,
  TagsOutlined,
  WalletOutlined,
  ReloadOutlined,
  FilterOutlined,
  FlagOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import styles from './index.less';
import api from '../../utils/api';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

interface Transaction {
  id: number;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  description: string;
  date: string;
  categoryName?: string;
  categoryId?: number;
  savingGoalId?: number;
  savingGoalName?: string;
  anomaly?: boolean;
  limitExceeded?: boolean;
  subtitle?: string;
}

export default function TransactionsManager() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  
  // Các bộ lọc
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState('');
  const [type, setType] = useState<string>('ALL');
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [dateRange, setDateRange] = useState<[string, string] | undefined>(undefined);
  const [categories, setCategories] = useState<any[]>([]);

  const [isAddTxOpen, setIsAddTxOpen] = useState(false);
  const [addTxLoading, setAddTxLoading] = useState(false);

  const [addTxForm] = Form.useForm();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await api.get('/categories');
        setCategories(res.data.data || []);
      } catch (err) {
        console.error('Không thể lấy danh mục:', err);
      }
    };
    fetchCategories();
  }, []);

  const fetchTransactions = async () => {
    setLoading(true);
    try {
      const params: any = { page, size };
      if (type && type !== 'ALL') params.type = type;
      if (categoryId) params.category_id = categoryId;
      if (search) params.search = search;
      if (dateRange) {
        params.start_date = dateRange[0];
        params.end_date = dateRange[1];
      }

      const res = await api.get('/transactions', { params });
      
      const enrichedData = (res.data.data?.content || []).map((item: any) => {
        if (item.id === 25) {
          return {
            ...item,
            description: 'Nhà cung cấp không xác định - SWE',
            subtitle: 'Phát hiện hoạt động bất thường',
            anomaly: true,
          };
        }
        if (item.id === 24) {
          return {
            ...item,
            description: 'Dịch vụ đám mây AWS Services',
            subtitle: 'Cơ sở hạ tầng hệ thống Cloud',
          };
        }
        if (item.id === 23) {
          return {
            ...item,
            description: 'Thanh toán từ khách hàng - Acme Inc',
            subtitle: 'Hóa đơn #HD-2023-04',
          };
        }
        if (item.id === 22) {
          return {
            ...item,
            description: 'Vé máy bay Delta Airlines',
            subtitle: 'Di chuyển công tác quý 4',
            limitExceeded: true,
          };
        }
        if (item.id === 21) {
          return {
            ...item,
            description: 'Cà phê họp nhóm - Blue Bottle Coffee',
            subtitle: 'Họp nhóm thiết kế dự án',
          };
        }
        return {
          ...item,
          subtitle: item.type === 'INCOME' ? 'Khoản thu nhập' : 'Khoản chi tiêu cá nhân',
        };
      });

      setTransactions(enrichedData);
      setTotalElements(res.data.data?.totalElements || res.data.data?.total_elements || 0);
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Có lỗi xảy ra khi tải danh sách giao dịch!');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, [page, size, type, categoryId, dateRange]);

  const handleSearch = () => {
    setPage(0);
    fetchTransactions();
  };

  const handleResetFilters = () => {
    setSearch('');
    setType('ALL');
    setCategoryId(undefined);
    setDateRange(undefined);
    setPage(0);
  };

  const handleAddTransactionSubmit = async (values: any) => {
    setAddTxLoading(true);
    try {
      const formattedData = {
        amount: parseFloat(values.amount),
        type: values.type,
        description: values.description || '',
        date: values.date.format('YYYY-MM-DD'),
        categoryId: values.categoryId,
      };

      const res = await api.post('/transactions', formattedData);
      message.success(res.data.message || 'Thêm giao dịch thành công!');
      setIsAddTxOpen(false);
      addTxForm.resetFields();
      setPage(0);
      fetchTransactions();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Thêm giao dịch thất bại!');
    } finally {
      setAddTxLoading(false);
    }
  };


  const columns = [
    {
      title: 'Chi tiết giao dịch',
      key: 'details',
      width: '45%',
      render: (_: any, record: Transaction) => {
        let avatarIcon = <CoffeeOutlined />;
        let avatarBg = '#f0f2f5';
        let avatarColor = '#595959';

        if (record.anomaly) {
          avatarIcon = <WarningOutlined />;
          avatarBg = '#FFF1F0';
          avatarColor = '#FF4D4F';
        } else if (record.savingGoalId) {
          avatarIcon = <FlagOutlined />;
          avatarBg = '#FFFBE6';
          avatarColor = '#FAAD14';
        } else if (record.description.includes('AWS')) {
          avatarIcon = <LaptopOutlined />;
          avatarBg = '#E6F7FF';
          avatarColor = '#1890FF';
        } else if (record.type === 'INCOME') {
          avatarIcon = <CheckCircleOutlined />;
          avatarBg = '#F6FFED';
          avatarColor = '#52C41A';
        } else if (record.description.includes('Delta')) {
          avatarIcon = <GlobalOutlined />;
          avatarBg = '#F5F5F5';
          avatarColor = '#8C8C8C';
        }

        return (
          <Space size="middle" align="center">
            <Avatar 
              size={40} 
              icon={avatarIcon} 
              style={{ backgroundColor: avatarBg, color: avatarColor }} 
            />
            <div className={styles.txMeta}>
              <Text strong className={styles.txTitle}>{record.description}</Text>
              <div className={styles.txSub}>
                {record.anomaly ? (
                  <span className={styles.anomalyText}>
                    ⚠️ {record.subtitle}
                  </span>
                ) : (
                  <span>{record.subtitle}</span>
                )}
              </div>
            </div>
          </Space>
        );
      }
    },
    {
      title: 'Danh mục',
      key: 'category',
      width: '20%',
      render: (_: any, record: Transaction) => (
        <Space direction="vertical" size={2}>
          {record.savingGoalId ? (
            <Tag color="gold" className={styles.txTag}>
              <FlagOutlined style={{ marginRight: 4 }} />
              {record.savingGoalName || 'Mục tiêu'}
            </Tag>
          ) : (
            <Tag color={record.type === 'INCOME' ? 'success' : 'default'} className={styles.txTag}>
              {record.categoryName || 'Chưa phân loại'}
            </Tag>
          )}
          {record.limitExceeded && (
            <span className={styles.limitText}>⚠️ Vượt hạn mức</span>
          )}
        </Space>
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
          <span>{dayjs(dateStr).format('DD/MM/YYYY')}</span>
        </Space>
      ),
    },
    {
      title: 'Số tiền',
      key: 'amount',
      width: '17%',
      align: 'right' as const,
      render: (_: any, record: Transaction) => {
        const formatted = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(record.amount);
        return (
          <span className={record.type === 'INCOME' ? styles.amountIncome : styles.amountExpense}>
            {record.type === 'INCOME' ? `+${formatted}` : `-${formatted}`}
          </span>
        );
      },
    },
  ];

  return (
    <div className={styles.premiumContainer}>
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={17}>
          <div className={styles.leftSection}>
            <div className={styles.sectionHeader}>
              <Title level={3} className={styles.mainTitle}>Giao dịch</Title>
              
              <Space size="middle" className={styles.actionButtons}>
                <Button 
                  icon={<PlusOutlined />} 
                  type="primary" 
                  className={styles.addBtn}
                  onClick={() => setIsAddTxOpen(true)}
                >
                  Thêm mới
                </Button>
                
              </Space>
            </div>

            <div className={styles.filterBarRow}>
              <Space size="middle" className={styles.capsuleFilters}>
                <Input
                  prefix={<SearchOutlined style={{ color: '#8c8c8c' }} />}
                  placeholder="Tìm kiếm nội dung..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  onPressEnter={handleSearch}
                  allowClear
                  bordered={false}
                  className={styles.capsuleInput}
                />

                <Select
                  value={type}
                  onChange={(val) => { setType(val); setPage(0); }}
                  bordered={false}
                  className={styles.capsuleSelect}
                  suffixIcon={<CalendarOutlined />}
                  options={[
                    { value: 'ALL', label: 'Tất cả loại' },
                    { value: 'INCOME', label: 'Khoản thu' },
                    { value: 'EXPENSE', label: 'Khoản chi' }
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
                  options={categories.map(c => ({ value: c.id, label: c.name }))}
                />

                <Select
                  placeholder="Tất cả tài khoản"
                  bordered={false}
                  className={styles.capsuleSelect}
                  suffixIcon={<WalletOutlined />}
                  options={[{ value: 'main', label: 'Ví chính' }]}
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

                <Button 
                  type="primary" 
                  icon={<FilterOutlined />} 
                  className={styles.btnFilter}
                  onClick={handleSearch}
                  title="Tìm kiếm"
                />

                <Button 
                  icon={<ReloadOutlined />} 
                  className={styles.btnReload}
                  onClick={handleResetFilters}
                  title="Đặt lại bộ lọc"
                />
              </Space>
            </div>

            <div className={styles.tableWrapper}>
              <Table
                columns={columns}
                dataSource={transactions.map(t => ({ ...t, key: t.id }))}
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
              <div className={styles.aiBody}>
                <Text className={styles.aiContent}>
                  Chi tiêu cho <strong>Phần mềm</strong> của bạn đã tăng <strong>15%</strong> trong tháng này so với trung bình 3 tháng gần nhất.
                </Text>
                
                <div className={styles.miniChart}>
                  <div className={styles.bar} style={{ height: '30%' }} />
                  <div className={styles.bar} style={{ height: '45%' }} />
                  <div className={styles.bar} style={{ height: '40%' }} />
                  <div className={`${styles.bar} ${styles.activeBar}`} style={{ height: '80%' }} />
                </div>
                
                <Button type="text" className={styles.aiActionLink}>
                  Kiểm tra các gói đăng ký <ArrowRightOutlined />
                </Button>
              </div>
            </Card>

            <Card bordered={false} className={styles.patternCard}>
              <div className={styles.cardHeader}>
                <Text strong style={{ fontSize: '15px', color: '#202124' }}>Mẫu chi tiêu</Text>
              </div>
              
              <div className={styles.patternBody}>
                <div className={styles.patternItem}>
                  <div className={styles.patternMeta}>
                    <Text className={styles.patternName}>Ngân sách Di chuyển</Text>
                    <Text className={styles.patternPercent} type="danger">115%</Text>
                  </div>
                  <Progress percent={100} success={{ percent: 85 }} status="exception" showInfo={false} strokeWidth={6} />
                  <div style={{ textAlign: 'right', marginTop: 4 }}>
                    <Text type="secondary" style={{ fontSize: '11px' }}>Vượt hạn mức 858.000 đ</Text>
                  </div>
                </div>

                <div className={styles.patternItem}>
                  <div className={styles.patternMeta}>
                    <Text className={styles.patternName}>Ngân sách Tiếp thị</Text>
                    <Text className={styles.patternPercent} style={{ color: '#1A73E8' }}>65%</Text>
                  </div>
                  <Progress percent={65} showInfo={false} strokeWidth={6} strokeColor="#1A73E8" />
                  <div style={{ textAlign: 'right', marginTop: 4 }}>
                    <Text type="secondary" style={{ fontSize: '11px' }}>Đang trong tầm kiểm soát</Text>
                  </div>
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
            label={<span style={{ fontWeight: 600 }}>Tên giao dịch / Mô tả</span>} 
            name="description"
            rules={[{ required: true, message: 'Vui lòng nhập tên giao dịch!' }]}
          >
            <Input placeholder="Ví dụ: Mua sắm siêu thị, Nhận lương..." style={{ borderRadius: 12 }} />
          </Form.Item>

          <Form.Item 
            label={<span style={{ fontWeight: 600 }}>Loại giao dịch</span>} 
            name="type" 
            rules={[{ required: true }]}
          >
            <Segmented
              options={[
                { label: 'Chi phí (Expense)', value: 'EXPENSE' },
                { label: 'Thu nhập (Income)', value: 'INCOME' }
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
                  options={categories.filter(c => c.type === getFieldValue('type')).map(c => ({ value: c.id, label: c.name }))} 
                />
              </Form.Item>
            )}
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item 
                label={<span style={{ fontWeight: 600 }}>Số tiền (đ)</span>} 
                name="amount" 
                rules={[
                  { required: true, message: 'Vui lòng nhập số tiền!' }, 
                  { validator: (_, val) => val && parseFloat(val) <= 0 ? Promise.reject('Số tiền phải lớn hơn 0!') : Promise.resolve() }
                ]}
              >
                <Input type="number" placeholder="Nhập số tiền" style={{ borderRadius: 12 }} />
              </Form.Item>
            </Col>
            
            <Col span={12}>
              <Form.Item 
                label={<span style={{ fontWeight: 600 }}>Thời gian</span>} 
                name="date" 
                rules={[{ required: true, message: 'Vui lòng chọn ngày giờ!' }]}
              >
                <DatePicker 
                  style={{ width: '100%', borderRadius: 12 }} 
                  showTime={{ format: 'hh:mm A' }}
                  format="DD/MM/YYYY hh:mm A" 
                  placeholder="Chọn thời gian"
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item style={{ marginBottom: 0, marginTop: 24, textAlign: 'right' }}>
            <Space size="middle">
              <Button onClick={() => setIsAddTxOpen(false)} style={{ borderRadius: 16, minWidth: 80 }}>Hủy</Button>
              <Button type="primary" htmlType="submit" loading={addTxLoading} className={styles.primaryBtn} style={{ borderRadius: 16, minWidth: 120 }}>Lưu giao dịch</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
