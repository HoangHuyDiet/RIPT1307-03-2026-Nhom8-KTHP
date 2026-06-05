import React, { useState, useEffect } from 'react';
import { useLocation, history } from 'umi';
import { Row, Col, Card, Space, Button, Progress, Tag, Modal, Form, Input, InputNumber, DatePicker, Select, message, Skeleton, Pagination, Dropdown } from 'antd';
import { PlusOutlined, HomeOutlined, CarOutlined, SafetyOutlined, CompassOutlined, LineChartOutlined, FolderOpenOutlined, RobotOutlined, DeleteOutlined, EditOutlined, DollarCircleOutlined, FlagOutlined, PushpinOutlined, PushpinFilled, MedicineBoxOutlined, BookOutlined, ShoppingOutlined, AppstoreOutlined, LaptopOutlined, BankOutlined, GoldOutlined, GiftOutlined, TrophyOutlined, HeartOutlined, CoffeeOutlined, RocketOutlined, MoreOutlined, MinusCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import styles from './index.less';
import api from '../../utils/api';

export interface SavingGoal {
  id: number;
  name: string;
  targetAmount: number;
  currentAmount: number;
  currency?: 'VND';
  dueDate: string;
  status: 'ON_TRACK' | 'AT_RISK' | 'COMPLETED';
  category: 'housing' | 'car' | 'emergency' | 'travel' | 'investment' | 'medical' | 'education' | 'shopping' | 'furniture' | 'electronics' | 'bank' | 'gold' | 'gift' | 'trophy' | 'heart' | 'coffee' | 'rocket' | 'other';
  customCategory?: string;
  isPinned?: boolean;
}

const CATEGORY_ICONS: Record<string, React.ReactNode> = {
  housing: <HomeOutlined />,
  car: <CarOutlined />,
  emergency: <SafetyOutlined />,
  travel: <CompassOutlined />,
  investment: <LineChartOutlined />,
  medical: <MedicineBoxOutlined />,
  education: <BookOutlined />,
  shopping: <ShoppingOutlined />,
  furniture: <AppstoreOutlined />,
  electronics: <LaptopOutlined />,
  bank: <BankOutlined />,
  gold: <GoldOutlined />,
  gift: <GiftOutlined />,
  trophy: <TrophyOutlined />,
  heart: <HeartOutlined />,
  coffee: <CoffeeOutlined />,
  rocket: <RocketOutlined />,
  other: <FolderOpenOutlined />,
};

const STATUS_CONFIG = {
  ON_TRACK: { color: '#34A853', text: 'Đúng tiến độ', className: styles.onTrack },
  AT_RISK: { color: '#EA4335', text: 'Cần lưu ý', className: styles.atRisk },
  COMPLETED: { color: '#1A73E8', text: 'Hoàn thành', className: styles.completed },
};

export const getGoalDisplayDetails = (goal: SavingGoal) => {
  const percent = Math.round((goal.currentAmount / goal.targetAmount) * 100);
  const displayPercent = Math.min(100, percent);
  const isCompleted = goal.currentAmount >= goal.targetAmount;

  let displayStatus = STATUS_CONFIG[goal.status] || STATUS_CONFIG.ON_TRACK;
  if (isCompleted) {
    displayStatus = STATUS_CONFIG.COMPLETED;
  }

  let urgencyWarning = '';
  let urgencyColor = '';

  if (goal.dueDate && !isCompleted) {
    const daysRemaining = dayjs(goal.dueDate).diff(dayjs().startOf('day'), 'day');
    if (daysRemaining < 0) {
      displayStatus = {
        color: '#EA4335',
        text: 'Quá hạn',
        className: styles.atRisk,
      };
      urgencyWarning = 'Đã quá hạn tích lũy!';
      urgencyColor = '#EA4335';
    } else if (daysRemaining <= 7) {
      displayStatus = {
        color: '#F9AB00',
        text: 'Sắp đến hạn',
        className: styles.nearDue,
      };
      urgencyWarning = `Chỉ còn ${daysRemaining} ngày!`;
      urgencyColor = '#F9AB00';
    }
  }

  return {
    percent,
    displayPercent,
    isCompleted,
    displayStatus,
    urgencyWarning,
    urgencyColor,
  };
};

export const getUrgencyScore = (goal: SavingGoal) => {
  const isCompleted = goal.currentAmount >= goal.targetAmount;
  if (isCompleted) {
    return 100000;
  }
  if (!goal.dueDate) {
    return 50000;
  }
  const daysRemaining = dayjs(goal.dueDate).diff(dayjs().startOf('day'), 'day');
  return daysRemaining;
};

const API_BASE = '';

const goalsApi = {
  getAll:    (): string => `${API_BASE}/api/saving-goals`,
  create:    (): string => `${API_BASE}/api/saving-goals`,
  update:    (id: number): string => `${API_BASE}/api/saving-goals/${id}`,
  remove:    (id: number): string => `${API_BASE}/api/saving-goals/${id}`,
  togglePin: (id: number): string => `${API_BASE}/api/saving-goals/${id}/pin`,
  deposit:   (id: number): string => `${API_BASE}/api/saving-goals/${id}/deposit`,
};

const jsonHeaders = { 'Content-Type': 'application/json' };

export default function SavingGoals() {
  const [goals, setGoals] = useState<SavingGoal[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 4;

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const goalIdParam = queryParams.get('id');

  useEffect(() => {
    if (goalIdParam && goals.length > 0) {
      const gId = Number(goalIdParam);
      const sortedGoals = [...goals].sort((a, b) => getUrgencyScore(a) - getUrgencyScore(b));
      const index = sortedGoals.findIndex(g => g.id === gId);
      if (index !== -1) {
        const page = Math.floor(index / ITEMS_PER_PAGE) + 1;
        setCurrentPage(page);
        setTimeout(() => {
          scrollToGoal(gId);
          history.replace('/saving-goals');
        }, 100);
      }
    }
  }, [goalIdParam, goals]);

  const totalPages = Math.ceil(goals.length / ITEMS_PER_PAGE) || 1;
  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [goals.length, totalPages, currentPage]);

  const syncCache = (data: SavingGoal[]) => {
    localStorage.setItem('saving_goals', JSON.stringify(data));
  };

  const [isGoalModalOpen, setIsGoalModalOpen] = useState(false);
  const [editingGoal, setEditingGoal] = useState<SavingGoal | null>(null);
  const [isDepositModalOpen, setIsDepositModalOpen] = useState(false);
  const [isWithdrawModalOpen, setIsWithdrawModalOpen] = useState(false);
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
  const [activeGoalId, setActiveGoalId] = useState<number | null>(null);
  const [isDeleteRefundModalOpen, setIsDeleteRefundModalOpen] = useState(false);
  const [deletingGoalId, setDeletingGoalId] = useState<number | null>(null);

  const [goalForm] = Form.useForm();
  const [depositForm] = Form.useForm();
  const [withdrawForm] = Form.useForm();
  const [paymentForm] = Form.useForm();
  const [deleteRefundForm] = Form.useForm();
  const [funds, setFunds] = useState<any[]>([]);

  useEffect(() => {
    api.get('/saving-goals')
      .then(res => { setGoals(res.data.data || []); syncCache(res.data.data || []); })
      .catch(() => {
        try {
          const cached = localStorage.getItem('saving_goals');
          if (cached) setGoals(JSON.parse(cached) as SavingGoal[]);
        } catch {}
      });
    api.get('/personal-funds').then(res => setFunds(res.data.data || [])).catch(() => {});
  }, []);

  const [highlightedGoalId, setHighlightedGoalId] = useState<number | null>(null);

  const pinnedGoals = goals.filter(g => g.isPinned).slice(0, 5);

  const scrollToGoal = (goalId: number) => {
    const sortedGoals = [...goals].sort((a, b) => getUrgencyScore(a) - getUrgencyScore(b));
    const index = sortedGoals.findIndex(g => g.id === goalId);
    if (index !== -1) {
      const page = Math.floor(index / ITEMS_PER_PAGE) + 1;
      setCurrentPage(page);
      setTimeout(() => {
        const element = document.getElementById(`goal-card-${goalId}`);
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });
          setHighlightedGoalId(goalId);
          setTimeout(() => setHighlightedGoalId(null), 2000);
        }
      }, 100);
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(val);

  const handleOpenAddModal = () => {
    setEditingGoal(null);
    goalForm.resetFields();
    goalForm.setFieldsValue({
      status: 'ON_TRACK',
      category: undefined,
      currentAmount: 0,
      currency: 'VND',
    });
    setIsGoalModalOpen(true);
  };

  const handleOpenEditModal = (goal: SavingGoal) => {
    setEditingGoal(goal);
    goalForm.setFieldsValue({
      ...goal,
      dueDate: goal.dueDate ? dayjs(goal.dueDate) : null,
    });
    setIsGoalModalOpen(true);
  };

  const handleSaveGoal = async (values: any) => {
    const targetAmt = Number(values.targetAmount);
    const currentAmt = editingGoal
      ? editingGoal.currentAmount
      : Number(values.currentAmount || 0);

    const isCompleted = currentAmt >= targetAmt;

    const formattedData = {
      name: values.name,
      targetAmount: targetAmt,
      currentAmount: currentAmt,
      currency: 'VND' as const,
      dueDate: values.dueDate ? values.dueDate.format('YYYY-MM-DD') : '',
      status: isCompleted ? ('COMPLETED' as const) : (editingGoal ? (editingGoal.status === 'COMPLETED' ? ('ON_TRACK' as const) : editingGoal.status) : ('ON_TRACK' as const)),
      category: values.category,
    };

    const apiPayload = editingGoal
      ? { name: formattedData.name, targetAmount: formattedData.targetAmount, currency: formattedData.currency, dueDate: formattedData.dueDate, category: formattedData.category }
      : formattedData;

    let updated: SavingGoal[] = goals;
    try {
      if (editingGoal) {
        const res = await api.put(`/saving-goals/${editingGoal.id}`, apiPayload);
        const saved: SavingGoal = res.data.data;
        updated = goals.map(g => g.id === editingGoal.id ? { ...saved, currentAmount: saved.currentAmount ?? editingGoal.currentAmount } : g);
      } else {
        const res = await api.post('/saving-goals', { ...formattedData, isPinned: false });
        const saved: SavingGoal = res.data.data;
        updated = [...goals, saved];
      }
    } catch {
      if (editingGoal) {
        updated = goals.map(g => g.id === editingGoal.id ? { ...g, ...formattedData, currentAmount: editingGoal.currentAmount } : g);
      } else {
        updated = [...goals, { ...formattedData, id: Date.now(), isPinned: false }];
      }
    }

    message.success(editingGoal ? 'Cập nhật mục tiêu thành công!' : 'Thêm mục tiêu mới thành công!');
    setGoals(updated);
    syncCache(updated);
    setIsGoalModalOpen(false);
    goalForm.resetFields();
  };

  const handleTogglePin = async (goalId: number) => {
    const target = goals.find(g => g.id === goalId);
    if (!target) return;

    if (!target.isPinned && goals.filter(g => g.isPinned).length >= 5) {
      message.warning('Bạn chỉ có thể ghim tối đa 5 mục tiêu!');
      return;
    }

    const newPinned = !target.isPinned;
    let updated: SavingGoal[] = goals;
    try {
      const res = await api.patch(`/saving-goals/${goalId}/pin`, { isPinned: newPinned });
      const saved: SavingGoal = res.data.data;
      updated = goals.map(g => g.id === goalId ? saved : g);
    } catch {
      updated = goals.map(g => g.id === goalId ? { ...g, isPinned: newPinned } : g);
    }

    setGoals(updated);
    syncCache(updated);
    message.success(target.isPinned ? 'Đã bỏ ghim mục tiêu!' : 'Đã ghim mục tiêu!');
  };

  const handleDeleteGoal = (goalId: number) => {
    const goal = goals.find(g => g.id === goalId);
    if (!goal) return;

    if (goal.currentAmount > 0) {
      setDeletingGoalId(goalId);
      deleteRefundForm.resetFields();
      setIsDeleteRefundModalOpen(true);
    } else {
      Modal.confirm({
        title: 'Xác nhận xóa mục tiêu',
        content: 'Bạn có chắc chắn muốn xóa mục tiêu tiết kiệm này không? Hành động này không thể hoàn tác.',
        okText: 'Xóa',
        okType: 'danger',
        cancelText: 'Hủy',
        onOk: async () => {
          try {
            await api.delete(`/saving-goals/${goalId}`);
          } catch {}
          const updated = goals.filter(g => g.id !== goalId);
          setGoals(updated);
          syncCache(updated);
          message.success('Xóa mục tiêu thành công!');
        }
      });
    }
  };

  const handleDeleteRefundSubmit = async (values: any) => {
    if (deletingGoalId === null) return;
    const goal = goals.find(g => g.id === deletingGoalId);
    if (!goal) return;

    const remainingAmount = goal.currentAmount;

    try {
      await api.patch(`/saving-goals/${deletingGoalId}/withdraw`, {
        amount: remainingAmount,
        personalFundId: values.fundSource !== 'external' ? values.fundSource : undefined,
        bankName: values.bankName,
        bankAccount: values.accountNumber
      });
      await api.delete(`/saving-goals/${deletingGoalId}`);
      const updated = goals.filter(g => g.id !== deletingGoalId);
      setGoals(updated);
      syncCache(updated);
      message.success('Đã rút hết số dư thừa và xóa mục tiêu thành công!');
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || 'Có lỗi xảy ra khi rút số dư hoặc xóa mục tiêu!';
      message.error(errMsg);
      return;
    }

    setIsDeleteRefundModalOpen(false);
    setDeletingGoalId(null);
  };

  const handleOpenDepositModal = (goalId: number) => {
    setActiveGoalId(goalId);
    depositForm.resetFields();
    setIsDepositModalOpen(true);
  };

  const handleDepositSubmit = async (values: any) => {
    if (activeGoalId === null) return;
    const addAmount = Number(values.amount);
    const activeGoal = goals.find(g => g.id === activeGoalId);

    if (activeGoal) {
      const remaining = activeGoal.targetAmount - activeGoal.currentAmount;
      if (addAmount > remaining) {
        message.error(`Số tiền nạp vượt quá mục tiêu! Bạn chỉ có thể nạp tối đa ${formatCurrency(remaining)} nữa.`);
        return;
      }
    }

    let updated: SavingGoal[] = goals;
    try {
      const res = await api.patch(`/saving-goals/${activeGoalId}/deposit`, {
        amount: addAmount,
        personalFundId: values.fundSource !== 'external' ? values.fundSource : undefined
      });
      const saved: SavingGoal = res.data.data;
      updated = goals.map(g => g.id === activeGoalId ? saved : g);
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || '';
      if (errMsg) {
        message.error(errMsg);
        return;
      }
      updated = goals.map(g => {
        if (g.id === activeGoalId) {
          const updatedAmount = g.currentAmount + addAmount;
          return { ...g, currentAmount: updatedAmount, status: updatedAmount >= g.targetAmount ? ('COMPLETED' as const) : g.status };
        }
        return g;
      });
    }

    setGoals(updated);
    syncCache(updated);
    message.success('Gửi tiền vào quỹ tiết kiệm thành công!');
    setIsDepositModalOpen(false);
    setActiveGoalId(null);
  };

  const handleWithdrawSubmit = async (values: any) => {
    if (activeGoalId === null) return;
    const withdrawAmount = Number(values.amount);

    let updated: SavingGoal[] = goals;
    try {
      const res = await api.patch(`/saving-goals/${activeGoalId}/withdraw`, {
        amount: withdrawAmount,
        personalFundId: values.fundSource !== 'external' ? values.fundSource : undefined,
        bankName: values.bankName,
        bankAccount: values.accountNumber
      });
      const saved: SavingGoal = res.data.data;
      updated = goals.map(g => g.id === activeGoalId ? saved : g);
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || '';
      if (errMsg) {
        message.error(errMsg);
        return;
      }
      updated = goals.map(g => {
        if (g.id === activeGoalId) {
          const updatedAmount = g.currentAmount - withdrawAmount;
          return { ...g, currentAmount: updatedAmount < 0 ? 0 : updatedAmount, status: 'IN_PROGRESS' as any };
        }
        return g;
      });
    }

    setGoals(updated);
    syncCache(updated);
    message.success('Rút tiền thành công!');
    setIsWithdrawModalOpen(false);
    setActiveGoalId(null);
  };

  const handlePaymentSubmit = async (values: any) => {
    message.success('Thanh toán thành công!');
    setIsPaymentModalOpen(false);
    setActiveGoalId(null);
  };


  return (
    <div className={styles.pageContainer}>
      <div className={styles.headerSection}>
        <div className={styles.titleInfo}>
          <h1 className={styles.pageTitle}>Mục tiêu</h1>
          <p className={styles.pageSubtitle}>Thiết lập, theo dõi tiến độ và tối ưu hóa các kế hoạch tích lũy dài hạn của bạn.</p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleOpenAddModal}
          className={styles.newGoalBtn}
        >
          Mục tiêu mới
        </Button>
      </div>

      <Row gutter={[24, 24]} className={styles.mainContent}>
        <Col xs={24} lg={18} xl={18}>
          <div className={styles.goalsListContainer}>
            {pinnedGoals.length > 0 && (
              <Row gutter={[16, 16]} style={{ marginBottom: 12 }}>
                <Col span={24}>
                  <div className={styles.pinnedBarContainer}>
                <div className={styles.pinnedBarHeader}>
                  <PushpinFilled style={{ color: '#1A73E8', marginRight: 8 }} />
                  <span className={styles.pinnedBarTitle}>Mục tiêu đã ghim ({pinnedGoals.length}/5)</span>
                </div>
                <div className={styles.pinnedList}>
                  {pinnedGoals.map(goal => {
                    const { percent, displayPercent, displayStatus } = getGoalDisplayDetails(goal);
                      return (
                        <div
                           key={goal.id}
                           className={styles.pinnedItem}
                           onClick={() => scrollToGoal(goal.id)}
                        >
                          <div className={styles.pinnedItemTop}>
                            <div className={`${styles.pinnedIconWrapper} ${styles[goal.category]}`}>
                              {CATEGORY_ICONS[goal.category] || <FolderOpenOutlined />}
                            </div>
                            <div className={styles.pinnedProgressWrapper}>
                              <div className={styles.pinnedProgressHeader}>
                                <span className={styles.pinnedPercentText}>{percent}%</span>
                              </div>
                              <Progress
                                percent={displayPercent}
                                strokeColor={displayStatus.color}
                                showInfo={false}
                                className={styles.pinnedProgress}
                              />
                            </div>
                          </div>
                          <div className={styles.pinnedItemName} title={goal.name}>
                            {goal.name}
                          </div>
                        </div>
                      );
                    })}
                </div>
              </div>
            </Col>
          </Row>
        )}
            {goals.length === 0 ? (
              <div className={styles.emptyStateContainer}>
                <FlagOutlined className={styles.emptyIcon} />
                <h3 className={styles.emptyTitle}>Chưa có mục tiêu nào</h3>
                <p className={styles.emptyDesc}>
                  Bắt đầu lập kế hoạch tài chính của bạn bằng cách tạo mục tiêu tiết kiệm đầu tiên như mua nhà, mua xe hay lập quỹ dự phòng khẩn cấp.
                </p>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleOpenAddModal}
                  className={styles.createFirstBtn}
                >
                  Tạo mục tiêu đầu tiên
                </Button>
              </div>
            ) : (
              <>
                <Row gutter={[16, 16]}>
                  {[...goals]
                    .sort((a, b) => getUrgencyScore(a) - getUrgencyScore(b))
                    .slice((currentPage - 1) * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE)
                    .map((goal) => {
                      const { percent, displayPercent, isCompleted, displayStatus, urgencyWarning, urgencyColor } = getGoalDisplayDetails(goal);

                      return (
                        <Col xs={24} sm={12} md={12} lg={6} key={goal.id}>
                          <Card
                            id={`goal-card-${goal.id}`}
                            bordered={false}
                            className={`${styles.goalCard} ${highlightedGoalId === goal.id ? styles.highlightedCard : ''}`}
                          >
                            <div className={styles.cardHeader}>
                              <div className={styles.headerLeft}>
                                <div className={`${styles.iconWrapper} ${styles[goal.category]}`}>
                                  {CATEGORY_ICONS[goal.category] || <FolderOpenOutlined />}
                                </div>
                              </div>
                              <div className={styles.headerActions}>
                                <Dropdown
                                  menu={{
                                    items: [
                                      {
                                        key: 'pin',
                                        label: goal.isPinned ? 'Bỏ ghim' : 'Ghim',
                                        icon: goal.isPinned ? <PushpinFilled /> : <PushpinOutlined />,
                                        disabled: !goal.isPinned && pinnedGoals.length >= 5,
                                        onClick: () => handleTogglePin(goal.id)
                                      },
                                      {
                                        key: 'edit',
                                        label: 'Sửa',
                                        icon: <EditOutlined />,
                                        onClick: () => handleOpenEditModal(goal)
                                      },
                                      {
                                        key: 'withdraw',
                                        label: 'Rút tiền',
                                        icon: <MinusCircleOutlined />,
                                        onClick: () => {
                                          setActiveGoalId(goal.id);
                                          withdrawForm.resetFields();
                                          setIsWithdrawModalOpen(true);
                                        }
                                      },
                                      {
                                        key: 'delete',
                                        label: 'Xóa',
                                        icon: <DeleteOutlined />,
                                        danger: true,
                                        onClick: () => handleDeleteGoal(goal.id)
                                      }
                                    ]
                                  }}
                                  trigger={['click']}
                                >
                                  <Button type="text" icon={<MoreOutlined />} style={{ fontSize: '20px' }} />
                                </Dropdown>
                              </div>
                            </div>

                            <div className={styles.progressSection}>
                              <div className={styles.progressHeader}>
                                <span className={styles.progressLabel}>Tiến độ</span>
                                <span className={styles.progressPercentage}>{percent}%</span>
                              </div>
                              <Progress
                                percent={displayPercent}
                                strokeColor={displayStatus.color}
                                showInfo={false}
                                className={styles.customProgress}
                              />
                              <div className={styles.dueDateRow}>
                                <Tag className={`${styles.statusTag} ${displayStatus.className}`} style={{ margin: 0 }} title={urgencyWarning}>
                                  {displayStatus.text}
                                </Tag>
                                {goal.dueDate ? (
                                  <span className={styles.targetDateText}>
                                    Hạn: {dayjs(goal.dueDate).format('DD/MM/YYYY')}
                                  </span>
                                ) : (
                                  <span className={styles.targetDateText} style={{ visibility: 'hidden' }}>
                                    Hạn: 00/00/0000
                                  </span>
                                )}
                              </div>
                            </div>

                            <div className={styles.infoSection}>
                              <h3 className={styles.goalName} title={goal.name}>
                                {goal.name}
                              </h3>
                              <div className={styles.amountText}>
                                {formatCurrency(goal.currentAmount)} / {formatCurrency(goal.targetAmount)}
                              </div>
                            </div>

                            <div className={styles.cardActions}>
                              {isCompleted ? (
                                <Button
                                  type="primary"
                                  icon={<CheckCircleOutlined />}
                                  className={`${styles.actionBtn} ${styles.depositBtn}`}
                                  style={{ backgroundColor: '#1A73E8', color: 'white', border: 'none' }}
                                  onClick={() => {
                                    setActiveGoalId(goal.id);
                                    paymentForm.resetFields();
                                    setIsPaymentModalOpen(true);
                                  }}
                                >
                                  Thanh toán ngay
                                </Button>
                              ) : (
                                <Button
                                  type="text"
                                  icon={<DollarCircleOutlined />}
                                  className={`${styles.actionBtn} ${styles.depositBtn}`}
                                  onClick={() => handleOpenDepositModal(goal.id)}
                                >
                                  Gửi tiền tiết kiệm
                                </Button>
                              )}
                            </div>
                          </Card>
                        </Col>
                      );
                    })}
                </Row>
                {goals.length > ITEMS_PER_PAGE && (
                  <div className={styles.paginationContainer}>
                    <Pagination
                      size="small"
                      current={currentPage}
                      pageSize={ITEMS_PER_PAGE}
                      total={goals.length}
                      onChange={(page) => setCurrentPage(page)}
                      showSizeChanger={false}
                    />
                  </div>
                )}
              </>
            )}
          </div>
        </Col>

        <Col xs={24} lg={6} xl={6} className={styles.sidebarColumn}>
          <div className={styles.sidebarContainer}>
            <Card bordered={true} className={styles.aiInsightsCard}>
              <div className={styles.cardHeader}>
                <Space>
                  <RobotOutlined style={{ color: '#1A73E8', fontSize: '18px' }} />
                  <span style={{ fontWeight: 600, fontSize: '16px', color: '#1f2937' }}>Gợi ý từ AI</span>
                </Space>
              </div>
              <div style={{ padding: '16px 0 8px 0' }}>
                <Skeleton active paragraph={{ rows: 3 }} />
              </div>
              <div className={styles.aiCardFooter}>
                <Button type="link" size="small" className={styles.aiActionLink}>
                  Xem thêm gợi ý
                </Button>
              </div>
            </Card>
          </div>
        </Col>
      </Row>

      <Modal
        title={editingGoal ? "Cập nhật mục tiêu tiết kiệm" : "Tạo mục tiêu tiết kiệm mới"}
        open={isGoalModalOpen}
        onCancel={() => setIsGoalModalOpen(false)}
        footer={null}
        destroyOnClose
      >
        <Form
          form={goalForm}
          layout="vertical"
          onFinish={handleSaveGoal}
          className={styles.modalForm}
        >
          <Form.Item
            name="name"
            label="Tên mục tiêu"
            rules={[{ required: true, message: 'Vui lòng nhập tên mục tiêu!' }]}
          >
            <Input placeholder="Ví dụ: Mua nhà, Mua xe, Đi du lịch..." />
          </Form.Item>

          <Form.Item
            name="category"
            label="Danh mục mục tiêu"
            rules={[{ required: true, message: 'Vui lòng chọn danh mục!' }]}
          >
            <Select
              placeholder="Chọn danh mục"
              options={[
                { value: 'housing', label: <Space><HomeOutlined /><span>Mua nhà</span></Space> },
                { value: 'car', label: <Space><CarOutlined /><span>Mua xe</span></Space> },
                { value: 'emergency', label: <Space><SafetyOutlined /><span>Dự phòng khẩn cấp</span></Space> },
                { value: 'travel', label: <Space><CompassOutlined /><span>Du lịch & Giải trí</span></Space> },
                { value: 'investment', label: <Space><LineChartOutlined /><span>Quỹ đầu tư</span></Space> },
                { value: 'medical', label: <Space><MedicineBoxOutlined /><span>Y tế</span></Space> },
                { value: 'education', label: <Space><BookOutlined /><span>Học tập</span></Space> },
                { value: 'shopping', label: <Space><ShoppingOutlined /><span>Mua sắm</span></Space> },
                { value: 'furniture', label: <Space><AppstoreOutlined /><span>Nội thất</span></Space> },
                { value: 'electronics', label: <Space><LaptopOutlined /><span>Đồ điện tử</span></Space> },
                { value: 'bank', label: <Space><BankOutlined /><span>Tài khoản / Ngân hàng</span></Space> },
                { value: 'gold', label: <Space><GoldOutlined /><span>Vàng & Kim loại quý</span></Space> },
                { value: 'gift', label: <Space><GiftOutlined /><span>Quà tặng & Đám cưới</span></Space> },
                { value: 'trophy', label: <Space><TrophyOutlined /><span>Mục tiêu lớn / Thành tựu</span></Space> },
                { value: 'heart', label: <Space><HeartOutlined /><span>Sức khỏe & Từ thiện</span></Space> },
                { value: 'coffee', label: <Space><CoffeeOutlined /><span>Ăn uống & Cà phê</span></Space> },
                { value: 'rocket', label: <Space><RocketOutlined /><span>Khởi nghiệp & Dự án</span></Space> },
                { value: 'other', label: <Space><FolderOpenOutlined /><span>Mục tiêu khác</span></Space> }
              ]}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="targetAmount"
                label="Số tiền mục tiêu"
                rules={[{ required: true, message: 'Nhập số tiền mục tiêu!' }]}
              >
                <InputNumber
                  min={1 as any}
                  className={styles.amountInputNumber}
                  formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={value => value!.replace(/(,*)/g, '') as any}
                  placeholder="Nhập số tiền"
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="currentAmount"
                label="Số tiền hiện có"
              >
                <InputNumber
                  min={0 as any}
                  className={styles.amountInputNumber}
                  formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={value => value!.replace(/(,*)/g, '') as any}
                  placeholder="Mặc định là 0"
                  disabled={!!editingGoal}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="dueDate"
            label="Hạn định hoàn thành"
          >
            <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" placeholder="Chọn ngày hạn định" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setIsGoalModalOpen(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit">
                {editingGoal ? "Cập nhật" : "Tạo mới"}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="Gửi tiền tiết kiệm tích lũy (đ)"
        open={isDepositModalOpen}
        onCancel={() => setIsDepositModalOpen(false)}
        footer={null}
        destroyOnClose
      >
        {(() => {
          const activeGoal = goals.find(g => g.id === activeGoalId);
          const remaining = activeGoal ? activeGoal.targetAmount - activeGoal.currentAmount : 0;
          return (
            <Form
              form={depositForm}
              layout="vertical"
              onFinish={handleDepositSubmit}
              className={styles.modalForm}
            >
              {activeGoal && (
                <div style={{ background: '#f0f7ff', border: '1px solid #91caff', borderRadius: 8, padding: '10px 14px', marginBottom: 16, fontSize: 13 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#555' }}>Hiện có:</span>
                    <span style={{ fontWeight: 600, color: '#1677ff' }}>{formatCurrency(activeGoal.currentAmount)}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
                    <span style={{ color: '#555' }}>Mục tiêu:</span>
                    <span style={{ fontWeight: 600 }}>{formatCurrency(activeGoal.targetAmount)}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4, borderTop: '1px dashed #ccc', paddingTop: 4 }}>
                    <span style={{ color: '#555' }}>Còn cần nạp tối đa:</span>
                    <span style={{ fontWeight: 700, color: '#52c41a' }}>{formatCurrency(remaining)}</span>
                  </div>
                </div>
              )}
              <Form.Item
                name="fundSource"
                label="Nguồn tiền"
                rules={[{ required: true, message: 'Vui lòng chọn nguồn tiền!' }]}
              >
                <Select
                  placeholder="Chọn nguồn tiền"
                  options={[
                    ...funds.map(f => ({ value: f.id, label: `Từ quỹ: ${f.name} (Số dư: ${formatCurrency(f.balance)})` })),
                    { value: 'external', label: 'Từ ngân hàng' }
                  ]}
                />
              </Form.Item>

              <Form.Item
                name="amount"
                label="Số tiền muốn tích lũy thêm"
                rules={[
                  { required: true, message: 'Vui lòng nhập số tiền muốn nạp!' },
                  { type: 'number', min: 1000, message: 'Số tiền nạp tối thiểu là 1,000 đ!' },
                  {
                    validator: (_: any, value: number) => {
                      if (!value) return Promise.resolve();
                      const activeGoal = goals.find(g => g.id === activeGoalId);
                      if (activeGoal) {
                        const remaining = activeGoal.targetAmount - activeGoal.currentAmount;
                        if (Number(value) > remaining) {
                          return Promise.reject(new Error(`Không thể nạp vượt mục tiêu! Tối đa: ${formatCurrency(remaining)}`));
                        }
                      }
                      return Promise.resolve();
                    }
                  }
                ]}
              >
                <InputNumber
                  className={styles.amountInputNumber}
                  formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={value => value!.replace(/(,*)/g, '') as any}
                  placeholder="Nhập số tiền gửi"
                  max={activeGoal ? activeGoal.targetAmount - activeGoal.currentAmount : undefined}
                  min={1000}
                  style={{ width: '100%' }}
                />
              </Form.Item>

              <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
                <Space>
                  <Button onClick={() => setIsDepositModalOpen(false)}>Hủy</Button>
                  <Button type="primary" htmlType="submit" icon={<DollarCircleOutlined />}>
                    Xác nhận gửi tiền
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          );
        })()}
      </Modal>

      <Modal
        title="Rút tiền khỏi mục tiêu (đ)"
        open={isWithdrawModalOpen}
        onCancel={() => setIsWithdrawModalOpen(false)}
        footer={null}
        destroyOnClose
      >
        {(() => {
          const activeGoal = goals.find(g => g.id === activeGoalId);
          return (
            <Form
              form={withdrawForm}
              layout="vertical"
              onFinish={handleWithdrawSubmit}
              className={styles.modalForm}
            >
              {activeGoal && (
                <div style={{ background: '#f0f7ff', border: '1px solid #91caff', borderRadius: 8, padding: '10px 14px', marginBottom: 16, fontSize: 13 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#555' }}>Số tiền đang có:</span>
                    <span style={{ fontWeight: 600, color: '#1677ff' }}>{formatCurrency(activeGoal.currentAmount)}</span>
                  </div>
                </div>
              )}
              
              <Form.Item
                name="fundSource"
                label="Chuyển về nguồn tiền"
                rules={[{ required: true, message: 'Vui lòng chọn nguồn nhận tiền!' }]}
              >
                <Select
                  placeholder="Chọn nguồn nhận tiền"
                  options={[
                    ...funds.map(f => ({ value: f.id, label: `Vào quỹ: ${f.name} (Số dư: ${formatCurrency(f.balance)})` })),
                    { value: 'external', label: 'Rút về ngân hàng' }
                  ]}
                />
              </Form.Item>
              <Form.Item noStyle dependencies={['fundSource']}>
                {({ getFieldValue }) => {
                  const fundSource = getFieldValue('fundSource');
                  if (fundSource === 'external') {
                    return (
                      <>
                        <Form.Item
                          name="bankName"
                          label="Ngân hàng thụ hưởng"
                          rules={[{ required: true, message: 'Vui lòng nhập tên ngân hàng!' }]}
                        >
                          <Input placeholder="Ví dụ: Vietcombank, Techcombank..." />
                        </Form.Item>
                        <Form.Item
                          name="accountNumber"
                          label="Số tài khoản thụ hưởng"
                          rules={[{ required: true, message: 'Vui lòng nhập số tài khoản!' }]}
                        >
                          <Input placeholder="Nhập số tài khoản thụ hưởng" />
                        </Form.Item>
                      </>
                    );
                  }
                  return null;
                }}
              </Form.Item>

              <Form.Item
                name="amount"
                label="Số tiền muốn rút"
                rules={[
                  { required: true, message: 'Vui lòng nhập số tiền muốn rút!' },
                  { type: 'number', min: 1000, message: 'Số tiền rút tối thiểu là 1,000 đ!' },
                  {
                    validator: (_: any, value: number) => {
                      if (!value) return Promise.resolve();
                      const activeGoal = goals.find(g => g.id === activeGoalId);
                      if (activeGoal && Number(value) > activeGoal.currentAmount) {
                        return Promise.reject(new Error(`Không thể rút vượt quá số dư hiện có: ${formatCurrency(activeGoal.currentAmount)}`));
                      }
                      return Promise.resolve();
                    }
                  }
                ]}
              >
                <InputNumber
                  className={styles.amountInputNumber}
                  formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={value => value!.replace(/(,*)/g, '') as any}
                  placeholder="Nhập số tiền rút"
                  max={activeGoal ? activeGoal.currentAmount : undefined}
                  min={1000}
                  style={{ width: '100%' }}
                />
              </Form.Item>

              <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
                <Space>
                  <Button onClick={() => setIsWithdrawModalOpen(false)}>Hủy</Button>
                  <Button type="primary" htmlType="submit" icon={<MinusCircleOutlined />} danger>
                    Xác nhận rút tiền
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          );
        })()}
      </Modal>

      <Modal
        title="Thanh toán ngay"
        open={isPaymentModalOpen}
        onCancel={() => setIsPaymentModalOpen(false)}
        footer={null}
        destroyOnClose
      >
        <Form
          form={paymentForm}
          layout="vertical"
          onFinish={handlePaymentSubmit}
          className={styles.modalForm}
        >
          <Form.Item
            name="bankName"
            label="Ngân hàng thụ hưởng"
            rules={[{ required: true, message: 'Vui lòng nhập tên ngân hàng!' }]}
          >
            <Input placeholder="Ví dụ: Vietcombank, Techcombank..." />
          </Form.Item>

          <Form.Item
            name="accountNumber"
            label="Số tài khoản"
            rules={[{ required: true, message: 'Vui lòng nhập số tài khoản!' }]}
          >
            <Input placeholder="Nhập số tài khoản thụ hưởng" />
          </Form.Item>
          
          <Form.Item
            name="transferContent"
            label="Nội dung chuyển khoản"
            rules={[{ required: true, message: 'Vui lòng nhập nội dung!' }]}
          >
            <Input.TextArea rows={3} placeholder="Ví dụ: Thanh toán tiền mua xe..." />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setIsPaymentModalOpen(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit">
                Tiến hành thanh toán
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="Rút tiền thừa & Xóa mục tiêu"
        open={isDeleteRefundModalOpen}
        onCancel={() => {
          setIsDeleteRefundModalOpen(false);
          setDeletingGoalId(null);
        }}
        footer={null}
        destroyOnClose
      >
        {(() => {
          const goal = goals.find(g => g.id === deletingGoalId);
          return (
            <Form
              form={deleteRefundForm}
              layout="vertical"
              onFinish={handleDeleteRefundSubmit}
              className={styles.modalForm}
            >
              {goal && (
                <div style={{ background: '#fff2e8', border: '1px solid #ffbb96', borderRadius: 8, padding: '10px 14px', marginBottom: 16, fontSize: 13 }}>
                  <div style={{ color: '#d4380d', fontWeight: 600, marginBottom: 4 }}>Cảnh báo số dư chưa rút hết!</div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#555' }}>Số tiền còn thừa:</span>
                    <span style={{ fontWeight: 600, color: '#d4380d' }}>{formatCurrency(goal.currentAmount)}</span>
                  </div>
                  <div style={{ marginTop: 4, color: '#666' }}>Vui lòng chọn tài khoản để rút hết số tiền thừa này trước khi tiến hành xóa mục tiêu.</div>
                </div>
              )}
              
              <Form.Item
                name="fundSource"
                label="Chuyển tiền thừa về"
                rules={[{ required: true, message: 'Vui lòng chọn nơi nhận tiền thừa!' }]}
              >
                <Select
                  placeholder="Chọn nơi nhận tiền"
                  options={[
                    ...funds.map(f => ({ value: f.id, label: `Vào quỹ: ${f.name} (Số dư: ${formatCurrency(f.balance)})` })),
                    { value: 'external', label: 'Rút về ngân hàng' }
                  ]}
                />
              </Form.Item>

              <Form.Item noStyle dependencies={['fundSource']}>
                {({ getFieldValue }) => {
                  const fundSource = getFieldValue('fundSource');
                  if (fundSource === 'external') {
                    return (
                      <>
                        <Form.Item
                          name="bankName"
                          label="Ngân hàng thụ hưởng"
                          rules={[{ required: true, message: 'Vui lòng nhập tên ngân hàng!' }]}
                        >
                          <Input placeholder="Ví dụ: Vietcombank, Techcombank..." />
                        </Form.Item>
                        <Form.Item
                          name="accountNumber"
                          label="Số tài khoản thụ hưởng"
                          rules={[{ required: true, message: 'Vui lòng nhập số tài khoản!' }]}
                        >
                          <Input placeholder="Nhập số tài khoản thụ hưởng" />
                        </Form.Item>
                      </>
                    );
                  }
                  return null;
                }}
              </Form.Item>

              <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
                <Space>
                  <Button onClick={() => {
                    setIsDeleteRefundModalOpen(false);
                    setDeletingGoalId(null);
                  }}>
                    Hủy
                  </Button>
                  <Button type="primary" htmlType="submit" danger>
                    Rút tiền & Xóa mục tiêu
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          );
        })()}
      </Modal>
    </div>
  );
}
