import React, { useState, useMemo, useEffect } from 'react';
import {
  Row,
  Col,
  Card,
  Typography,
  Space,
  Button,
  Table,
  Tag,
  Progress,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  DatePicker,
  Switch,
  message,
  Dropdown,
} from 'antd';
import {
  PlusOutlined,
  SwapOutlined,
  ArrowUpOutlined,
  WalletOutlined,
  BankOutlined,
  CreditCardOutlined,
  EditOutlined,
  MoreOutlined,
  RiseOutlined,
  RobotOutlined,
  FileTextOutlined,
  CalendarOutlined,
  DollarCircleOutlined,
  CheckCircleOutlined,
  InfoCircleOutlined,
  MobileOutlined,
  BookOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import { Column, Pie } from '@ant-design/charts';
import { history } from 'umi';
import dayjs from 'dayjs';
import styles from './index.less';
import api from '../../utils/api';

const WalletIcon = () => (
  <svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect x="4" y="10" width="40" height="30" rx="6" fill="url(#walletGradient)" stroke="#1A73E8" strokeWidth="3" />
    <path d="M4 18H44" stroke="#1A73E8" strokeWidth="3" strokeLinecap="round" />
    <path d="M28 18H44V30H28C24.6863 30 22 27.3137 22 24C22 20.6863 24.6863 18 28 18Z" fill="#FFFFFF" stroke="#1A73E8" strokeWidth="3" />
    <circle cx="33" cy="24" r="3" fill="#1A73E8" />
    <defs>
      <linearGradient id="walletGradient" x1="4" y1="10" x2="44" y2="40" gradientUnits="userSpaceOnUse">
        <stop stopColor="#E8F0FE" />
        <stop offset="1" stopColor="#D2E3FC" />
      </linearGradient>
    </defs>
  </svg>
);

const ICON_OPTIONS = [
  { value: 'wallet', label: <span><WalletOutlined style={{ marginRight: 8 }} /> Ví tiền</span> },
  { value: 'bank', label: <span><BankOutlined style={{ marginRight: 8 }} /> Ngân hàng</span> },
  { value: 'card', label: <span><CreditCardOutlined style={{ marginRight: 8 }} /> Thẻ tín dụng</span> },
  { value: 'mobile', label: <span><MobileOutlined style={{ marginRight: 8 }} /> Điện thoại / Ví điện tử</span> },
  { value: 'dollar', label: <span><DollarOutlined style={{ marginRight: 8 }} /> Tiền tệ</span> },
  { value: 'book', label: <span><BookOutlined style={{ marginRight: 8 }} /> Học tập / Sách</span> },
];

const { Title, Text } = Typography;

const formatCurrency = (value: number) => {
  const isNeg = value < 0;
  const absValue = Math.abs(value);
  const formatted = new Intl.NumberFormat('vi-VN').format(absValue);
  return `${isNeg ? '-' : ''}${formatted} đ`;
};

const formatTransactionDate = (dateStr: string) => {
  const date = dayjs(dateStr);
  const now = dayjs();
  if (date.isSame(now, 'day')) {
    return `Hôm nay, ${date.format('HH:mm')}`;
  }
  if (date.isSame(now.subtract(1, 'day'), 'day')) {
    return `Hôm qua, ${date.format('HH:mm')}`;
  }
  return date.format('DD/MM, HH:mm');
};

const columns = [
  {
    title: 'Ngày',
    dataIndex: 'date',
    key: 'date',
    width: '18%',
    render: (text: string) => formatTransactionDate(text),
  },
  {
    title: 'Mô tả',
    dataIndex: 'description',
    key: 'description',
    width: '35%',
    render: (text: string) => <span className={styles.descCell}>{text}</span>,
  },
  {
    title: 'Danh mục',
    dataIndex: 'category',
    key: 'category',
    width: '18%',
    render: (category: string) => {
      switch (category) {
        case 'Ăn uống':
          return <Tag bordered={false} style={{ color: '#EA4335', backgroundColor: '#FCE8E6', borderRadius: 4, fontWeight: 500 }}>Ăn uống</Tag>;
        case 'Thu nhập':
          return <Tag bordered={false} style={{ color: '#34A853', backgroundColor: '#E6F4EA', borderRadius: 4, fontWeight: 500 }}>Thu nhập</Tag>;
        case 'Mua sắm':
          return <Tag bordered={false} style={{ color: '#1A73E8', backgroundColor: '#E8F0FE', borderRadius: 4, fontWeight: 500 }}>Mua sắm</Tag>;
        case 'Tiện ích':
          return <Tag bordered={false} style={{ color: '#9C27B0', backgroundColor: '#F3E8FD', borderRadius: 4, fontWeight: 500 }}>Tiện ích</Tag>;
        default:
          return <Tag bordered={false} style={{ color: '#5F6368', backgroundColor: '#F1F4F7', borderRadius: 4, fontWeight: 500 }}>{category}</Tag>;
      }
    },
  },
  {
    title: 'Quỹ',
    dataIndex: 'fund',
    key: 'fund',
    width: '15%',
  },
  {
    title: 'Số tiền',
    dataIndex: 'amount',
    key: 'amount',
    align: 'right' as const,
    width: '14%',
    render: (amount: number, record: any) => {
      const isIncome = record.type === 'INCOME';
      const formatted = formatCurrency(amount);
      return (
        <span className={`${styles.amountCell} ${isIncome ? styles.income : styles.expense}`}>
          {isIncome ? `+${formatted}` : formatted}
        </span>
      );
    },
  },
];

export default function PersonalFundManagement() {
  const [summary, setSummary] = useState({ totalAssets: 0, growthRate: 0 });
  const [funds, setFunds] = useState<any[]>([]);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [balanceHistory, setBalanceHistory] = useState<any[]>([]);
  const [assetDistribution, setAssetDistribution] = useState<any[]>([]);
  const [aiTips, setAiTips] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState<any[]>([]);

  // States for refactored Modals
  const [isDepositModalOpen, setIsDepositModalOpen] = useState(false);
  const [depositForm] = Form.useForm();
  const [depositGoals, setDepositGoals] = useState<any[]>([]);
  const [depositLoading, setDepositLoading] = useState(false);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingFund, setEditingFund] = useState<any | null>(null);
  const [editForm] = Form.useForm();

  const [isStatementModalOpen, setIsStatementModalOpen] = useState(false);
  const [selectedFundForStatement, setSelectedFundForStatement] = useState<any | null>(null);

  const fetchFundsData = async () => {
    setLoading(true);

    const safeGet = async (url: string, fallback: any) => {
      try {
        const res = await api.get(url);
        return res.data && res.data.data !== undefined ? res.data.data : res.data;
      } catch (err) {
        return fallback;
      }
    };

    try {
      const [
        summaryData,
        fundsData,
        balanceData,
        distributionData,
        transactionsData,
        aiData
      ] = await Promise.all([
        safeGet('/personal-funds/summary', { totalAssets: 0, growthRate: 0 }),
        safeGet('/personal-funds/list', []),
        safeGet('/personal-funds/balance-history', []),
        safeGet('/personal-funds/distribution', []),
        safeGet('/personal-funds/recent-transactions', []),
        safeGet('/personal-funds/ai-tips', null),
      ]);

      setSummary(summaryData);
      setFunds(fundsData);
      setBalanceHistory(balanceData);
      setAssetDistribution(distributionData);
      setTransactions(transactionsData);
      setAiTips(aiData);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFundsData();

    const fetchCategories = async () => {
      try {
        const res = await api.get('/categories');
        if (res.data) {
          const data = res.data.data !== undefined ? res.data.data : res.data;
          setCategories(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        console.warn(err);
      }
    };
    fetchCategories();
  }, []);

  const fetchDepositGoals = async () => {
    try {
      const res = await api.get('/saving-goals');
      const data = res.data && res.data.data !== undefined ? res.data.data : res.data;
      setDepositGoals(Array.isArray(data) ? data : []);
    } catch (err) {
      console.warn(err);
    }
  };

  useEffect(() => {
    if (isDepositModalOpen) {
      fetchDepositGoals();
    }
  }, [isDepositModalOpen]);

  const handleDepositToGoal = async (values: any) => {
    const { goalId, amount, sourceId } = values;
    setDepositLoading(true);
    try {
      await api.patch(`/saving-goals/${goalId}/deposit`, { amount: Number(amount), sourceId });
      message.success('Nạp tiền vào mục tiêu tiết kiệm thành công!');
      fetchFundsData();
      setIsDepositModalOpen(false);
      depositForm.resetFields();
    } catch (err: any) {
      console.error(err);
      message.error(err.response?.data?.message || 'Nạp tiền vào mục tiêu thất bại!');
    } finally {
      setDepositLoading(false);
    }
  };

  const openEditModal = (fund: any) => {
    setEditingFund(fund);
    editForm.setFieldsValue({ name: fund.name });
    setIsEditModalOpen(true);
  };

  const handleEditFund = async (values: any) => {
    if (!editingFund) return;
    try {
      await api.put(`/personal-funds/${editingFund.id}`, { name: values.name });
      message.success(`Đã cập nhật thông tin quỹ thành công!`);
      fetchFundsData();
      setIsEditModalOpen(false);
    } catch (err: any) {
      console.error(err);
      message.error(err.response?.data?.message || 'Cập nhật thông tin quỹ thất bại!');
    }
  };

  const fundTransactions = useMemo(() => {
    if (!selectedFundForStatement) return [];
    return transactions.filter(tx => tx.fund === selectedFundForStatement.name);
  }, [selectedFundForStatement, transactions]);

  const filteredTransactions = useMemo(() => {
    const twoMonthsAgo = dayjs().subtract(2, 'month').startOf('day');
    return transactions.filter(tx => {
      const txDate = dayjs(tx.date);
      return txDate.isAfter(twoMonthsAgo) || txDate.isSame(twoMonthsAgo);
    });
  }, [transactions]);

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
  const [isPayBillModalOpen, setIsPayBillModalOpen] = useState(false);
  const [isReminderModalOpen, setIsReminderModalOpen] = useState(false);

  const [createForm] = Form.useForm();
  const [transferForm] = Form.useForm();
  const [payBillForm] = Form.useForm();
  const [reminderForm] = Form.useForm();

  const [selectedFundId, setSelectedFundId] = useState<string | null>(null);

  const handleCreateFund = async (values: any) => {
    try {
      const requestData = {
        name: values.name,
        icon: values.icon || 'wallet',
        status: 'PERSONAL',
        balance: 0
      };
      await api.post('/personal-funds/create', requestData);
      message.success(`Đã khởi tạo quỹ "${values.name}" thành công!`);
      fetchFundsData();
    } catch (err: any) {
      console.error(err);
      message.error(err.response?.data?.message || 'Khởi tạo quỹ thất bại!');
    } finally {
      setIsCreateModalOpen(false);
      createForm.resetFields();
    }
  };

  const handleTransfer = async (values: any) => {
    const { sourceId, targetId, amount, description } = values;
    const transferAmount = Number(amount);

    if (sourceId === targetId) {
      message.error('Quỹ nguồn và quỹ đích không được giống nhau!');
      return;
    }

    try {
      const requestData = {
        sourceId,
        targetId,
        amount: transferAmount,
        description,
        date: dayjs().format('YYYY-MM-DD')
      };
      await api.post('/personal-funds/transfer', requestData);
      message.success(`Đã chuyển tiền nội bộ thành công!`);
      fetchFundsData();
    } catch (err: any) {
      console.error(err);
      message.error(err.response?.data?.message || 'Chuyển tiền thất bại!');
    } finally {
      setIsTransferModalOpen(false);
      transferForm.resetFields();
    }
  };

  const handlePayBill = async (values: any) => {
    const { content, category, amount, fundId } = values;
    const expenseAmount = Number(amount);

    try {
      const isCategoryId = typeof category === 'number';
      const requestData = {
        content,
        amount: expenseAmount,
        fundId,
        ...(isCategoryId ? { categoryId: category } : { category }),
        date: dayjs().format('YYYY-MM-DD'),
        type: 'EXPENSE'
      };
      await api.post('/personal-funds/pay-bill', requestData);
      message.success(`Thanh toán hóa đơn thành công!`);
      fetchFundsData();
    } catch (err: any) {
      console.error(err);
      message.error(err.response?.data?.message || 'Thanh toán hóa đơn thất bại!');
    } finally {
      setIsPayBillModalOpen(false);
      payBillForm.resetFields();
    }
  };

  const handleReminder = async (values: any) => {
    const { title, paymentDate } = values;
    const formattedDate = paymentDate ? paymentDate.format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD');
    try {
      const targetCategory = categories.find(
        (c) => c.type === 'EXPENSE' && (c.name.includes('Tiện ích') || c.name.includes('Hóa đơn') || c.name.includes('Nhà cửa'))
      );
      const categoryId = targetCategory ? targetCategory.id : null;

      const requestData = {
        title,
        paymentDate: formattedDate,
        repeat: !!values.repeat,
        amount: 350000,
        type: 'EXPENSE',
        frequency: values.repeat ? 'MONTHLY' : 'DAILY',
        startDate: formattedDate,
        categoryId,
        description: `Lịch nhắc thanh toán: ${title}`
      };
      await api.post('/personal-funds/reminder', requestData);
      message.success(`Đã lên lịch nhắc thanh toán: "${title}" vào ngày ${paymentDate ? paymentDate.format('DD/MM/YYYY') : dayjs().format('DD/MM/YYYY')}!`);
      fetchFundsData();
    } catch (err: any) {
      console.error(err);
      message.error(err.response?.data?.message || 'Lên lịch nhắc thanh toán thất bại!');
    } finally {
      setIsReminderModalOpen(false);
      reminderForm.resetFields();
    }
  };

  const openPayBillWithFund = (fundId: string) => {
    setSelectedFundId(fundId);
    payBillForm.resetFields();
    payBillForm.setFieldsValue({ fundId });
    setIsPayBillModalOpen(true);
  };

  const openTransferWithSource = (fundId: string) => {
    transferForm.resetFields();
    transferForm.setFieldsValue({ sourceId: fundId });
    setIsTransferModalOpen(true);
  };

  const renderHeaderAndOverview = () => (
    <>
      <div className={styles.headerSection}>
        <div className={styles.titleInfo}>
          <h1 className={styles.pageTitle}>Quản lý Quỹ</h1>
          <p className={styles.pageSubtitle}>Theo dõi tổng tài sản, quản lý dòng tiền và phân bổ các quỹ tài chính hiệu quả.</p>
        </div>
        <div className={styles.headerActions}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            className={styles.actionBtn}
            onClick={() => {
              createForm.resetFields();
              setIsCreateModalOpen(true);
            }}
          >
            Tạo quỹ mới
          </Button>
          <Button
            icon={<SwapOutlined />}
            className={styles.actionBtn}
            onClick={() => {
              transferForm.resetFields();
              setIsTransferModalOpen(true);
            }}
          >
            Chuyển tiền nội bộ
          </Button>
          <Button
            icon={<ArrowUpOutlined />}
            className={styles.actionBtn}
            onClick={() => {
              setIsDepositModalOpen(true);
            }}
          >
            Nạp vào mục tiêu
          </Button>
        </div>
      </div>

      <Card className={styles.overviewCard}>
        <div className={styles.overviewContent}>
          <div className={styles.overviewInfo}>
            <span className={styles.overviewLabel}>TỔNG TÀI SẢN MẠNH MẼ</span>
            <h1 className={styles.overviewValue}>
              {new Intl.NumberFormat('vi-VN').format(summary.totalAssets)}
              <span className={styles.currencySymbol}>đ</span>
            </h1>
            <div className={styles.overviewGrowth}>
              <RiseOutlined className={styles.growthIcon} />
              <span>+{summary.growthRate}%</span>
              <span className={styles.growthText}>so với tháng trước</span>
            </div>
          </div>
          <div className={styles.walletIconWrapper}>
            <WalletIcon />
          </div>
        </div>
      </Card>
    </>
  );



  const renderFundCards = () => (
    <div className={styles.fundListGrid}>
      <div className={styles.sectionHeader}>
        <h2 className={styles.sectionTitle}>Danh sách Quỹ</h2>
        <span className={styles.viewAllLink} onClick={() => message.info('Danh sách đầy đủ đang tải')}>Xem tất cả</span>
      </div>

      <Row gutter={[20, 20]}>
        {funds.map((fund) => {
          let iconComponent = <WalletOutlined />;
          if (fund.icon === 'bank') {
            iconComponent = <BankOutlined />;
          } else if (fund.icon === 'card') {
            iconComponent = <CreditCardOutlined />;
          } else if (fund.icon === 'mobile') {
            iconComponent = <MobileOutlined />;
          } else if (fund.icon === 'dollar') {
            iconComponent = <DollarOutlined />;
          } else if (fund.icon === 'book') {
            iconComponent = <BookOutlined />;
          }

          let wrapperClass = `${styles.fundIconWrapper} ${styles.cashType}`;
          if (fund.type === 'BANK') {
            wrapperClass = `${styles.fundIconWrapper} ${styles.bankType}`;
          } else if (fund.type === 'CREDIT_CARD') {
            wrapperClass = `${styles.fundIconWrapper} ${styles.creditType}`;
          }

          return (
            <Col xs={24} sm={12} md={8} key={fund.id}>
              <Card className={styles.fundCard}>
                <div className={styles.fundCardTop}>
                  <div className={wrapperClass}>
                    {iconComponent}
                  </div>
                  <div className={styles.cardActionsMenu}>
                    <EditOutlined
                      className={styles.iconBtn}
                      onClick={() => openEditModal(fund)}
                      title="Chỉnh sửa"
                    />
                    <Dropdown
                      menu={{
                        items: [
                          { key: 'statement', label: 'Xuất sao kê' },
                          { key: 'delete', label: 'Xóa quỹ', danger: true },
                        ],
                        onClick: ({ key }) => {
                          if (key === 'statement') {
                            setSelectedFundForStatement(fund);
                            setIsStatementModalOpen(true);
                          } else if (key === 'delete') {
                            Modal.confirm({
                              title: `Xóa quỹ "${fund.name}"`,
                              content: 'Bạn có chắc chắn muốn xóa quỹ này không? Hành động này không thể hoàn tác.',
                              okText: 'Xóa quỹ',
                              okType: 'danger',
                              cancelText: 'Hủy',
                              onOk: async () => {
                                try {
                                  await api.delete(`/personal-funds/${fund.id}`);
                                  message.success(`Đã xóa quỹ "${fund.name}" thành công!`);
                                  fetchFundsData();
                                } catch (err: any) {
                                  console.error(err);
                                  message.error(err.response?.data?.message || 'Xóa quỹ thất bại!');
                                }
                              }
                            });
                          }
                        }
                      }}
                      placement="bottomRight"
                      arrow
                    >
                      <MoreOutlined className={styles.iconBtn} title="Thêm hành động" />
                    </Dropdown>
                  </div>
                </div>

                <div className={styles.fundCardMiddle}>
                  <span className={styles.fundTypeName}>{fund.name}</span>
                  <h3 className={`${styles.fundBalance} ${fund.isNegative ? styles.negativeBalance : ''}`}>
                    {formatCurrency(fund.balance)}
                  </h3>
                </div>

                <div className={styles.fundCardBottom}>
                  {(fund.balance < 0
                    ? [
                        { key: 'repay', label: 'Trả nợ' },
                        { key: 'spend', label: 'Chi tiêu' }
                      ]
                    : [
                        { key: 'deposit', label: 'Nạp tiền' },
                        { key: 'withdraw', label: 'Rút tiền' }
                      ]
                  ).map((act: any) => {
                    const buttonHandler = () => {
                      if (act.key === 'deposit') {
                        Modal.confirm({
                          title: `Nạp tiền vào quỹ ${fund.name}`,
                          content: `Bạn muốn nạp thêm tiền mặt hay nhận chuyển khoản vào quỹ này?`,
                          okText: 'Nhận chuyển khoản',
                          cancelText: 'Hủy',
                          onOk: () => openTransferWithSource(fund.id)
                        });
                      } else if (act.key === 'withdraw' || act.key === 'spend') {
                        openPayBillWithFund(fund.id);
                      } else if (act.key === 'repay') {
                        transferForm.resetFields();
                        transferForm.setFieldsValue({ targetId: fund.id });
                        setIsTransferModalOpen(true);
                      }
                    };

                    return (
                      <Button
                        key={act.key}
                        className={styles.fundActionBtn}
                        onClick={buttonHandler}
                      >
                        {act.label}
                      </Button>
                    );
                  })}
                </div>
              </Card>
            </Col>
          );
        })}
      </Row>
    </div>
  );

  const columnConfig = useMemo(() => ({
    data: balanceHistory,
    xField: 'date',
    yField: 'amount',
    colorField: 'date',
    scale: {
      color: {
        range: ['#D2E3FC', '#D2E3FC', '#D2E3FC', '#D2E3FC', '#1A73E8'],
      },
    },
    legend: false,
    style: {
      radius: 6,
    },
    tooltip: {
      items: [
        {
          channel: 'y',
          valueFormatter: (value: any) => `${(value * 1000000).toLocaleString('vi-VN')} đ`,
        },
      ],
    },
  }), [balanceHistory]);

  const largestAsset = useMemo(() => {
    if (!assetDistribution || assetDistribution.length === 0) {
      return { name: 'Chưa có', percentage: '0%' };
    }
    const sorted = [...assetDistribution].sort((a, b) => b.value - a.value);
    const largest = sorted[0];
    const total = assetDistribution.reduce((acc: number, curr: any) => acc + curr.value, 0) || 1;
    const percentage = `${Math.round((largest.value / total) * 100)}%`;
    const name = (largest.type || '').replace('Tài khoản ', '');
    return { name, percentage };
  }, [assetDistribution]);

  const donutConfig = useMemo(() => ({
    data: assetDistribution,
    angleField: 'value',
    colorField: 'type',
    radius: 1,
    innerRadius: 0.65,
    legend: false,
    label: false,
    height: 240,
    autoFit: true,
    scale: {
      color: {
        range: assetDistribution.map((item, idx) => item.color || ['#1A73E8', '#34A853', '#BCC1C6'][idx % 3])
      }
    },
    tooltip: {
      items: [
        {
          channel: 'y',
          valueFormatter: (value: any) => `${value.toLocaleString('vi-VN')} đ`,
        },
      ],
    },
  }), [assetDistribution]);

  const renderModals = () => (
    <>
      <Modal
        open={isCreateModalOpen}
        onCancel={() => setIsCreateModalOpen(false)}
        footer={null}
        destroyOnClose
        width={450}
      >
        <div style={{ marginBottom: 20 }}>
          <h2 style={{ fontSize: '22px', fontWeight: 700, margin: '0 0 6px 0', color: '#202124' }}>
            Tạo quỹ mới
          </h2>
          <p style={{ fontSize: '13px', color: '#5F6368', margin: 0, lineHeight: 1.4 }}>
            Bắt đầu quản lý tài chính thông minh với một quỹ đầu tư riêng biệt.
          </p>
        </div>

        <Form
          form={createForm}
          layout="vertical"
          onFinish={handleCreateFund}
          className={styles.modalForm}
        >
          <Form.Item
            name="name"
            label={<span style={{ fontWeight: 600, color: '#202124', fontSize: '13px' }}>Tên quỹ</span>}
            rules={[{ required: true, message: 'Vui lòng nhập tên quỹ!' }]}
            style={{ marginBottom: 20 }}
          >
            <Input
              placeholder="Nhập tên quỹ của bạn..."
              style={{
                borderRadius: 8,
                backgroundColor: '#F1F4F7',
                border: 'none',
                padding: '10px 14px',
                fontSize: '13px'
              }}
            />
          </Form.Item>

          <Form.Item
            name="icon"
            label={<span style={{ fontWeight: 600, color: '#202124', fontSize: '13px' }}>Chọn Icon</span>}
            rules={[{ required: true, message: 'Vui lòng chọn icon cho quỹ!' }]}
            initialValue="wallet"
            style={{ marginBottom: 20 }}
          >
            <Select
              placeholder="Chọn icon phù hợp..."
              options={ICON_OPTIONS}
              style={{ height: 40 }}
            />
          </Form.Item>

          {aiTips?.createFund?.show && (
            <div className={styles.aiSuggestBanner} style={{ display: 'flex', gap: '8px', padding: '12px 14px', borderRadius: '8px', marginBottom: 24, borderLeft: '3px solid #1A73E8' }}>
              <RobotOutlined style={{ fontSize: '16px', color: '#1A73E8', marginTop: '2px' }} />
              <div style={{ fontSize: '12px', color: '#1967D2', fontWeight: 500, lineHeight: 1.4 }}>
                {aiTips.createFund.message}
              </div>
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', alignItems: 'center' }}>
            <Button
              type="text"
              onClick={() => setIsCreateModalOpen(false)}
              style={{ color: '#5F6368', fontWeight: 600, fontSize: '13px', height: 38 }}
            >
              Hủy
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              style={{
                backgroundColor: '#1A73E8',
                borderRadius: 8,
                height: 38,
                padding: '0 24px',
                fontWeight: 600,
                fontSize: '13px',
                border: 'none'
              }}
            >
              Tạo quỹ
            </Button>
          </div>
        </Form>
      </Modal>

      <Modal
        title={
          <Space>
            <SwapOutlined style={{ color: '#1A73E8' }} />
            <span>Chuyển tiền nội bộ</span>
          </Space>
        }
        open={isTransferModalOpen}
        onCancel={() => setIsTransferModalOpen(false)}
        okText="Xác nhận chuyển"
        cancelText="Hủy"
        onOk={() => transferForm.submit()}
        width={580}
        destroyOnClose
      >
        <Form
          form={transferForm}
          layout="vertical"
          onFinish={handleTransfer}
          className={styles.modalForm}
        >
          {aiTips?.transfer?.show && (
            <div className={styles.aiSuggestBanner}>
              <RobotOutlined style={{ fontSize: '16px', marginTop: '2px' }} />
              <div>
                {aiTips.transfer.message}
              </div>
            </div>
          )}

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="sourceId"
                label="Quỹ nguồn"
                rules={[{ required: true, message: 'Vui lòng chọn quỹ trích tiền!' }]}
              >
                <Select
                  placeholder="Chọn tài khoản nguồn"
                  options={funds.map(f => ({ value: f.id, label: `${f.name} (${formatCurrency(f.balance)})` }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="targetId"
                label="Quỹ thụ hưởng"
                rules={[{ required: true, message: 'Vui lòng chọn quỹ đích!' }]}
              >
                <Select
                  placeholder="Chọn tài khoản nhận"
                  options={funds.map(f => ({ value: f.id, label: `${f.name} (${formatCurrency(f.balance)})` }))}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="transferDate"
                label="Ngày chuyển"
                initialValue={dayjs()}
              >
                <DatePicker style={{ width: '100%' }} format="MM/DD/YYYY" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="category"
                label="Danh mục"
                initialValue="Chuyển khoản nội bộ"
              >
                <Input disabled prefix={<DollarCircleOutlined />} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="amount"
            label="Số tiền"
            rules={[
              { required: true, message: 'Vui lòng nhập số tiền chuyển!' },
              { type: 'number', min: 1000, message: 'Số tiền tối thiểu chuyển là 1,000 đ!' }
            ]}
          >
            <InputNumber
              className={styles.amountInputNumber}
              formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={value => value!.replace(/\$\s?|(,*)/g, '') as any}
              placeholder="Nhập số tiền chuyển (VND)"
              suffix="VND"
            />
          </Form.Item>

          <Form.Item
            name="description"
            label="Mô tả"
            initialValue={aiTips?.transfer?.description || ''}
          >
            <Input.TextArea rows={3} placeholder="Ghi chú nội dung chuyển khoản..." maxLength={200} />
          </Form.Item>

          <div className={styles.infoBanner} style={{ backgroundColor: '#F1F4F7', color: '#5F6368', borderLeftColor: '#70757A' }}>
            <InfoCircleOutlined style={{ fontSize: '16px', marginTop: '2px' }} />
            <div>
              <strong>Bạn có chắc chắn muốn thực hiện chuyển tiền không?</strong><br />
              Giao dịch chuyển khoản nội bộ này sẽ được thực hiện ngay lập tức và không thể hoàn tác.
            </div>
          </div>
        </Form>
      </Modal>

      <Modal
        title={
          <Space>
            <DollarCircleOutlined style={{ color: '#EA4335' }} />
            <span>Thanh toán hóa đơn</span>
          </Space>
        }
        open={isPayBillModalOpen}
        onCancel={() => setIsPayBillModalOpen(false)}
        okText="Thực hiện thanh toán"
        cancelText="Hủy"
        onOk={() => payBillForm.submit()}
        destroyOnClose
      >
        <Form
          form={payBillForm}
          layout="vertical"
          onFinish={handlePayBill}
          initialValues={{ content: 'Tiền điện tháng 10', category: 'Tiện ích' }}
          className={styles.modalForm}
        >
          <Form.Item
            name="content"
            label="Nội dung thanh toán"
            rules={[{ required: true, message: 'Vui lòng điền nội dung hóa đơn!' }]}
          >
            <Input placeholder="Ví dụ: Hóa đơn điện nước, GrabFood, NetFlix..." />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="category"
                label="Danh mục"
                rules={[{ required: true, message: 'Vui lòng chọn danh mục chi tiêu!' }]}
              >
                <Select
                  options={categories.length > 0
                    ? categories.filter(c => c.type === 'EXPENSE').map(c => ({ value: c.id, label: c.name }))
                    : [
                      { value: 'Ăn uống', label: 'Ăn uống' },
                      { value: 'Mua sắm', label: 'Mua sắm' },
                      { value: 'Tiện ích', label: 'Tiện ích & Dịch vụ' },
                      { value: 'Giải trí', label: 'Giải trí' },
                      { value: 'Y tế', label: 'Sức khỏe & Y tế' },
                      { value: 'Chi khác', label: 'Chi phí khác' }
                    ]
                  }
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="amount"
                label="Số tiền"
                rules={[
                  { required: true, message: 'Nhập số tiền hóa đơn!' },
                  { type: 'number', min: 1000, message: 'Số tiền tối thiểu là 1,000 đ!' }
                ]}
              >
                <InputNumber
                  className={styles.amountInputNumber}
                  formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={value => value!.replace(/\$\s?|(,*)/g, '') as any}
                  placeholder="Nhập số tiền"
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="fundId"
            label="Chọn quỹ thanh toán"
            rules={[{ required: true, message: 'Chọn tài khoản trích nợ!' }]}
          >
            <Select
              placeholder="Chọn quỹ trích tiền"
              options={funds.map(f => ({ value: f.id, label: `${f.name} (${formatCurrency(f.balance)})` }))}
            />
          </Form.Item>

          <div className={styles.infoBanner}>
            <CheckCircleOutlined style={{ fontSize: '16px', marginTop: '2px' }} />
            <div>
              AI nhận thấy việc thanh toán từ Vietcombank sẽ giúp bạn tích thêm <strong>1.250 điểm</strong> thưởng trong tháng này.
            </div>
          </div>
        </Form>
      </Modal>

      <Modal
        title={
          <Space>
            <CalendarOutlined style={{ color: '#1A73E8' }} />
            <span>Lên lịch nhắc thanh toán</span>
          </Space>
        }
        open={isReminderModalOpen}
        onCancel={() => setIsReminderModalOpen(false)}
        okText="Lưu lịch hẹn"
        cancelText="Hủy"
        onOk={() => reminderForm.submit()}
        destroyOnClose
      >
        <Form
          form={reminderForm}
          layout="vertical"
          onFinish={handleReminder}
          initialValues={{ title: aiTips?.reminder?.defaultTitle || '', repeat: true }}
          className={styles.modalForm}
        >
          {aiTips?.reminder?.show && (
            <div className={styles.aiSuggestBanner} style={{ backgroundColor: '#E8F0FE', color: '#1967D2', borderLeftColor: '#1A73E8' }}>
              <RobotOutlined style={{ fontSize: '16px', marginTop: '2px' }} />
              <div>
                {aiTips.reminder.message}
              </div>
            </div>
          )}

          <Form.Item
            name="title"
            label="Tên hóa đơn"
            rules={[{ required: true, message: 'Vui lòng nhập tên hóa đơn/khoản chi!' }]}
          >
            <Input placeholder="Ví dụ: Tiền điện nước, Tiền học phí, Internet..." />
          </Form.Item>

          <Form.Item
            name="paymentDate"
            label="Ngày thanh toán tiếp theo"
            rules={[{ required: true, message: 'Chọn ngày hẹn nhắc nhở!' }]}
          >
            <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" placeholder="Chọn ngày nhắc" />
          </Form.Item>

          <Form.Item
            name="repeat"
            label="Nhắc lại hàng tháng"
            valuePropName="checked"
          >
            <Switch checkedChildren="Bật" unCheckedChildren="Tắt" />
          </Form.Item>
        </Form>
      </Modal>

      {/* Modal Nạp tiền vào Mục tiêu */}
      <Modal
        title={
          <Space>
            <ArrowUpOutlined style={{ color: '#1A73E8' }} />
            <span>Nạp tiền vào Mục tiêu</span>
          </Space>
        }
        open={isDepositModalOpen}
        onCancel={() => {
          setIsDepositModalOpen(false);
          depositForm.resetFields();
        }}
        okText="Xác nhận nạp"
        cancelText="Hủy"
        onOk={() => depositForm.submit()}
        confirmLoading={depositLoading}
        width={580}
        destroyOnClose
      >
        <Form
          form={depositForm}
          layout="vertical"
          onFinish={handleDepositToGoal}
          className={styles.modalForm}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="sourceId"
                label="Quỹ nguồn"
                rules={[{ required: true, message: 'Vui lòng chọn quỹ trích tiền!' }]}
              >
                <Select
                  placeholder="Chọn tài khoản nguồn"
                  options={funds.map(f => ({ value: f.id, label: `${f.name} (${formatCurrency(f.balance)})` }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="goalId"
                label="Mục tiêu"
                rules={[{ required: true, message: 'Vui lòng chọn mục tiêu!' }]}
              >
                <Select
                  placeholder="Chọn mục tiêu tiết kiệm"
                  options={depositGoals.map((g) => ({
                    value: g.id,
                    label: `${g.name} (Đã có: ${formatCurrency(g.currentAmount)} / Mục tiêu: ${formatCurrency(g.targetAmount)})`,
                  }))}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="transferDate"
                label="Ngày chuyển"
                initialValue={dayjs()}
              >
                <DatePicker style={{ width: '100%' }} format="MM/DD/YYYY" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="category"
                label="Danh mục"
                initialValue="Nạp vào mục tiêu"
              >
                <Input disabled prefix={<DollarCircleOutlined />} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="amount"
            label="Số tiền"
            rules={[
              { required: true, message: 'Vui lòng nhập số tiền nạp!' },
              { type: 'number', min: 1000, message: 'Số tiền tối thiểu nạp là 1,000 đ!' }
            ]}
          >
            <InputNumber
              className={styles.amountInputNumber}
              formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={value => value!.replace(/\$\s?|(,*)/g, '') as any}
              placeholder="Nhập số tiền nạp (VND)"
              suffix="VND"
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* Modal Sửa thông tin quỹ */}
      <Modal
        title="Sửa thông tin quỹ"
        open={isEditModalOpen}
        onCancel={() => setIsEditModalOpen(false)}
        footer={null}
        destroyOnClose
        width={450}
      >
        <div style={{ marginBottom: 20 }}>
          <h2 style={{ fontSize: '20px', fontWeight: 700, margin: '0 0 6px 0', color: '#202124' }}>
            Sửa thông tin quỹ
          </h2>
          <p style={{ fontSize: '13px', color: '#5F6368', margin: 0, lineHeight: 1.4 }}>
            Thay đổi tên hiển thị của quỹ tài chính cá nhân.
          </p>
        </div>
        <Form
          form={editForm}
          layout="vertical"
          onFinish={handleEditFund}
          className={styles.modalForm}
        >
          <Form.Item
            name="name"
            label={<span style={{ fontWeight: 600, color: '#202124', fontSize: '13px' }}>Tên quỹ mới</span>}
            rules={[{ required: true, message: 'Vui lòng nhập tên quỹ!' }]}
            style={{ marginBottom: 20 }}
          >
            <Input
              placeholder="Nhập tên quỹ mới..."
              style={{
                borderRadius: 8,
                backgroundColor: '#F1F4F7',
                border: 'none',
                padding: '10px 14px',
                fontSize: '13px'
              }}
            />
          </Form.Item>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', alignItems: 'center' }}>
            <Button
              type="text"
              onClick={() => setIsEditModalOpen(false)}
              style={{ color: '#5F6368', fontWeight: 600, fontSize: '13px', height: 38 }}
            >
              Hủy
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              style={{
                backgroundColor: '#1A73E8',
                borderRadius: 8,
                height: 38,
                padding: '0 24px',
                fontWeight: 600,
                fontSize: '13px',
                border: 'none'
              }}
            >
              Lưu thay đổi
            </Button>
          </div>
        </Form>
      </Modal>

      {/* Modal Xuất sao kê giao dịch */}
      <Modal
        title={`Sao kê giao dịch: ${selectedFundForStatement?.name || ''}`}
        open={isStatementModalOpen}
        onCancel={() => setIsStatementModalOpen(false)}
        width={800}
        footer={[
          <Button key="pdf" type="primary" danger onClick={() => message.success('Xuất sao kê PDF thành công!')}>
            Xuất PDF
          </Button>,
          <Button key="excel" type="primary" style={{ backgroundColor: '#34A853', borderColor: '#34A853' }} onClick={() => message.success('Xuất sao kê Excel thành công!')}>
            Xuất Excel
          </Button>,
          <Button key="close" onClick={() => setIsStatementModalOpen(false)}>
            Đóng
          </Button>
        ]}
        destroyOnClose
      >
        <Table
          dataSource={fundTransactions}
          columns={[
            {
              title: 'Ngày',
              dataIndex: 'date',
              key: 'date',
              render: (text) => formatTransactionDate(text),
              width: '25%',
            },
            {
              title: 'Mô tả',
              dataIndex: 'description',
              key: 'description',
              width: '40%',
            },
            {
              title: 'Loại',
              dataIndex: 'type',
              key: 'type',
              render: (type) => type === 'INCOME' ? <Tag color="success">THU</Tag> : <Tag color="error">CHI</Tag>,
              width: '15%',
            },
            {
              title: 'Số tiền',
              dataIndex: 'amount',
              key: 'amount',
              align: 'right' as const,
              render: (amount, record) => {
                const isIncome = record.type === 'INCOME';
                return (
                  <span style={{ fontWeight: 700, color: isIncome ? '#34A853' : '#EA4335' }}>
                    {isIncome ? '+' : ''}{formatCurrency(amount)}
                  </span>
                );
              },
              width: '20%',
            }
          ]}
          pagination={{ pageSize: 5 }}
        />
      </Modal>
    </>
  );

  return (
    <div className={styles.fundsWrapper}>
      {renderHeaderAndOverview()}
      {renderFundCards()}

      <Row gutter={[20, 20]} style={{ display: 'flex', alignItems: 'stretch' }}>
        <Col xs={24} lg={16} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <Card
            title="Biến động số dư (30 ngày)"
            className={styles.chartCard}
            bordered={false}
            style={{ height: 350, display: 'flex', flexDirection: 'column', marginBottom: 0 }}
            styles={{ body: { flex: 1, display: 'flex', flexDirection: 'column', padding: '20px' } }}
          >
            <div className={styles.chartHeader}>
              <span className={styles.chartUnit}>[TRIỆU VND]</span>
              <div className={styles.chartLegend}>
                <div className={styles.legendDot}></div>
                <span>Số dư</span>
              </div>
            </div>
            <div style={{ height: 200, width: '100%', overflow: 'hidden' }}>
              <Column {...columnConfig} height={200} />
            </div>
          </Card>

          <Card
            title="Giao dịch gần đây"
            className={styles.tableCard}
            bordered={false}
            style={{ flex: 1, display: 'flex', flexDirection: 'column', marginBottom: 0 }}
            styles={{ body: { flex: 1, display: 'flex', flexDirection: 'column', padding: '0px' } }}
            extra={
              <Dropdown
                menu={{
                  items: [
                    { key: 'excel', label: 'Xuất file Excel', style: { color: '#34A853', fontWeight: 500 } },
                    { key: 'pdf', label: 'Xuất file PDF', style: { color: '#EA4335', fontWeight: 500 } },
                  ],
                  onClick: ({ key }) => {
                    if (key === 'excel') {
                      message.success('Xuất file Excel thành công!');
                    } else if (key === 'pdf') {
                      message.success('Xuất file PDF thành công!');
                    }
                  }
                }}
                trigger={['hover']}
                placement="bottomRight"
              >
                <span className={styles.exportLink}>
                  <FileTextOutlined style={{ marginRight: 6 }} />
                  Xuất báo cáo
                </span>
              </Dropdown>
            }
          >
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between', height: '100%' }}>
              <Table
                dataSource={filteredTransactions}
                columns={columns}
                pagination={{ pageSize: 10, showSizeChanger: false, position: ['bottomCenter'] }}
                className={styles.transactionTable}
              />
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={8} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <Card
            title="Phân bổ tài sản"
            className={styles.chartCard}
            bordered={false}
            style={{ display: 'flex', flexDirection: 'column', marginBottom: 0 }}
            styles={{ body: { flex: 1, display: 'flex', flexDirection: 'column', padding: '20px' } }}
          >
            <div className={styles.donutChartWrapper}>
              <Pie {...donutConfig} />
              <div style={{ 
                position: 'absolute', 
                top: '50%', 
                left: '50%', 
                transform: 'translate(-50%, -50%)', 
                textAlign: 'center', 
                pointerEvents: 'none'
              }}>
                <div style={{ fontSize: '14px', color: '#5F6368', fontWeight: 500, marginBottom: '4px' }}>
                  {largestAsset.name}
                </div>
                <div style={{ fontSize: '34px', color: '#202124', fontWeight: 900, lineHeight: 1 }}>
                  {largestAsset.percentage}
                </div>
              </div>
            </div>

            <div className={styles.donutLegendList}>
              {assetDistribution.map((item, idx) => {
                const defaultColors = ['#1A73E8', '#34A853', '#BCC1C6'];
                const itemColor = item.color || defaultColors[idx % defaultColors.length] || '#E8EAED';
                const total = assetDistribution.reduce((acc: number, curr: any) => acc + curr.value, 0) || 1;
                const percentage = `${Math.round((item.value / total) * 100)}%`;
                return (
                  <div className={styles.donutLegendItem} key={item.type}>
                    <div className={styles.legendLeft}>
                      <div
                        className={styles.legendIndicator}
                        style={{ backgroundColor: itemColor }}
                      />
                      <span>{item.type}</span>
                    </div>
                    <Space size="small">
                      <span style={{ color: '#5F6368', fontSize: '12px', marginRight: '8px' }}>{percentage}</span>
                      <span className={styles.legendValue}>{formatCurrency(item.value)}</span>
                    </Space>
                  </div>
                );
              })}
            </div>
          </Card>

          {aiTips?.mainCard?.show && (
            <Card className={styles.aiSuggestionCard} bordered={false} style={{ marginBottom: 0 }}>
              <div className={styles.aiHeader}>
                <RobotOutlined style={{ fontSize: '18px' }} />
                <span>{aiTips.mainCard.title}</span>
              </div>
              <div className={styles.aiBody}>
                {aiTips.mainCard.content}
              </div>
              <Button
                block
                className={styles.aiButton}
                onClick={() => {
                  reminderForm.resetFields();
                  reminderForm.setFieldsValue({
                    title: aiTips.mainCard.actionTitle || '',
                    paymentDate: aiTips.mainCard.actionDate ? dayjs(aiTips.mainCard.actionDate) : dayjs().set('date', 15),
                    repeat: true,
                  });
                  setIsReminderModalOpen(true);
                }}
              >
                {aiTips.mainCard.buttonText}
              </Button>
            </Card>
          )}
        </Col>
      </Row>

      {renderModals()}
    </div>
  );
}
