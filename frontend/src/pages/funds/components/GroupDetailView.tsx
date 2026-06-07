import React, { useState, useEffect, useRef } from 'react';
import { Row, Col, Card, Typography, Space, Button, Progress, Avatar, Input, Tag, Modal, message, Upload, Table } from 'antd';
import { 
  ArrowLeftOutlined, EditOutlined, TeamOutlined, LogoutOutlined, UserAddOutlined, 
  WalletOutlined, MessageOutlined, MoreOutlined, PaperClipOutlined, SendOutlined, 
  DeleteOutlined, UploadOutlined, SmileOutlined, WarningOutlined 
} from '@ant-design/icons';
import styles from '../index.less';
import { GroupFund, FundMember } from '../types';
import { formatVND } from '@/utils/format';
import { useAuthStore } from '@/store/useAuthStore';
import { useNotificationStore } from '@/store/useNotificationStore';
import request from '@/utils/request';
import { useWebSocket } from '@/hooks/useWebSocket';
import { Column } from '@ant-design/charts';

import InviteMemberModal from './modals/InviteMemberModal';
import DepositModal from './modals/DepositModal';
import WithdrawModal from './modals/WithdrawModal';
import RenameFundModal from './modals/RenameFundModal';
import MembersModal from './modals/MembersModal';
import FloatingChat from './FloatingChat';

const { Title, Text } = Typography;

interface GroupDetailViewProps {
  group: GroupFund;
  onBack: () => void;
  onLeaveGroup: (groupId: number) => void;
  onRenameGroup: (groupId: number, newName: string) => void;
  onDeleteGroup: (groupId: number) => void;
}

export default function GroupDetailView({ group, onBack, onLeaveGroup, onRenameGroup, onDeleteGroup }: GroupDetailViewProps) {
  const user = useAuthStore(state => state.user);
  const [discussions, setDiscussions] = useState<any[]>([]);
  
  const { sendMessage } = useWebSocket(group.id, (newMsg) => {
    setDiscussions((prev) => {
      if (prev.some((d) => d.id === newMsg.id)) return prev;
      return [...prev, newMsg];
    });
  });
  const [chartData, setChartData] = useState<any[]>([]);
  const [fundTransactions, setFundTransactions] = useState<any[]>([]);
  const [loadingTx, setLoadingTx] = useState(false);
  const [qrImage, setQrImage] = useState<string | null>(null);
  
  const [txModalVisible, setTxModalVisible] = useState(false);
  const [txModalType, setTxModalType] = useState<'INCOME' | 'EXPENSE'>('INCOME');

  const addNotification = useNotificationStore(state => state.addNotification);

  const [isInviteOpen, setIsInviteOpen] = useState(false);
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const [isRenameOpen, setIsRenameOpen] = useState(false);
  const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);

  useEffect(() => {
    const savedQr = localStorage.getItem(`qr_fund_${group.id}`);
    setQrImage(savedQr);
  }, [group.id]);

  useEffect(() => {
    fetchFundTransactions(group.id);
    
    const handleTransactionApproved = () => {
      fetchFundTransactions(group.id);
    };
    
    window.addEventListener('transaction-approved', handleTransactionApproved);
    return () => {
      window.removeEventListener('transaction-approved', handleTransactionApproved);
    };
  }, [group.id]);

  const displayChartData = React.useMemo(() => {
    if (!chartData || chartData.length === 0) return [];
    const newData = [...chartData];
    const lastIndex = newData.length - 1;
    newData[lastIndex] = {
      ...newData[lastIndex],
      amount: group.balance
    };
    
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1;

    return newData.map(d => {
      let tooltipTitle = '';
      if (d.month === 'Tháng này') {
        tooltipTitle = `Tháng ${currentMonth}/${currentYear}`;
      } else if (d.month.startsWith('T')) {
        const m = parseInt(d.month.replace('T', ''), 10);
        const y = m > currentMonth ? currentYear - 1 : currentYear;
        tooltipTitle = `Tháng ${m}/${y}`;
      } else {
        tooltipTitle = d.month;
      }

      return {
        ...d,
        tooltipTitle,
        yValue: d.amount / 1000000,
        formattedValue: `${Number(d.amount).toLocaleString('vi-VN')}đ`
      };
    });
  }, [chartData, group.balance]);

  const trendData = React.useMemo(() => {
    if (!fundTransactions || fundTransactions.length === 0) return { percent: 0, isPositive: true };
    
    const now = new Date().getTime();
    const sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;
    
    let netChangeLast7Days = 0;
    
    fundTransactions.forEach(tx => {
      const txTime = new Date(tx.date).getTime();
      if (txTime >= sevenDaysAgo) {
        if (tx.type === 'INCOME') {
          netChangeLast7Days += tx.amount;
        } else if (tx.type === 'EXPENSE') {
          netChangeLast7Days -= tx.amount;
        }
      }
    });
    
    const balance7DaysAgo = group.balance - netChangeLast7Days;
    
    let percent = 0;
    if (balance7DaysAgo > 0) {
      percent = (netChangeLast7Days / balance7DaysAgo) * 100;
    } else if (balance7DaysAgo === 0 && netChangeLast7Days > 0) {
      percent = 100;
    } else if (balance7DaysAgo === 0 && netChangeLast7Days === 0) {
      percent = 0;
    } else if (balance7DaysAgo === 0 && netChangeLast7Days < 0) {
      percent = -100;
    }
    
    return {
      percent: Math.abs(percent).toFixed(1),
      isPositive: netChangeLast7Days >= 0
    };
  }, [fundTransactions, group.balance]);



  const topContributorsData = React.useMemo(() => {
    if (!fundTransactions || fundTransactions.length === 0) return [];
    
    const userTotals: Record<string, number> = {};
    
    fundTransactions.forEach(tx => {
      if (tx.type === 'INCOME' && tx.is_approved !== false) {
        const name = tx.user_display_name || 'Khách';
        if (!userTotals[name]) userTotals[name] = 0;
        userTotals[name] += tx.amount;
      }
    });

    const colors = ['#595959', '#1A73E8', '#8C8C8C', '#FA8C16', '#52C41A'];
    let colorIndex = 0;
    
    return Object.entries(userTotals)
      .map(([name, amount]) => {
        const initials = name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
        return {
          id: name,
          name,
          avatarInitials: initials,
          avatarColor: colors[(colorIndex++) % colors.length],
          amount
        };
      })
      .sort((a, b) => b.amount - a.amount)
      .slice(0, 3);
  }, [fundTransactions]);

  const topContributorsDataV2 = React.useMemo(() => {
    const contributors = new Map<string, {
      id: string;
      name: string;
      email?: string;
      amount: number;
    }>();

    group.members.forEach((member) => {
      const key = member.email || member.name;
      contributors.set(key, {
        id: key,
        name: member.name || member.email || 'Thanh vien',
        email: member.email,
        amount: 0
      });
    });

    fundTransactions.forEach((tx) => {
      const isApproved = tx.status === 'APPROVED' || tx.is_approved === true;
      if (tx.type !== 'INCOME' || !isApproved) return;

      const key = String(tx.user_email || tx.user_display_name);
      if (contributors.has(key)) {
        contributors.get(key)!.amount += Number(tx.amount || 0);
      } else {
        const found = Array.from(contributors.values()).find(c => c.email === tx.user_email || c.name === tx.user_display_name);
        if (found) {
          found.amount += Number(tx.amount || 0);
        }
      }
    });

    const colors = ['#595959', '#1A73E8', '#8C8C8C', '#FA8C16', '#52C41A'];

    return Array.from(contributors.values())
      .sort((a, b) => b.amount - a.amount)
      .slice(0, Math.min(3, group.members.length))
      .map((item, index) => {
        const initials = item.name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
        return {
          id: item.id,
          name: item.name,
          avatarInitials: initials,
          avatarColor: colors[index % colors.length],
          amount: item.amount
        };
      });
  }, [fundTransactions, group.members]);

  const fetchFundTransactions = async (fundId: number) => {
    setLoadingTx(true);
    try {
      const [txData, discData, chartDataRes] = await Promise.all([
        request.get(`/funds/transactions`, { params: { fundId } }),
        request.get(`/funds/${fundId}/discussions`),
        request.get(`/funds/${fundId}/budget-chart`)
      ]);

      setFundTransactions(txData.data || []);
      setDiscussions(discData.data || []);
      setChartData(chartDataRes.data || []);
    } catch (e) {
      console.error('Lỗi tải chi tiết quỹ:', e);
    } finally {
      setLoadingTx(false);
    }
  };

  const handleUploadQr = (file: File) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const base64 = e.target?.result as string;
      setQrImage(base64);
      localStorage.setItem(`qr_fund_${group.id}`, base64);
      message.success('Đã lưu ảnh QR thành công!');
    };
    reader.readAsDataURL(file);
    return false;
  };

  const handleRemoveQr = () => {
    setQrImage(null);
    localStorage.removeItem(`qr_fund_${group.id}`);
  };



  const confirmLeaveGroup = () => {
    Modal.confirm({
      title: 'Xác nhận rời nhóm',
      content: `Bạn có chắc chắn muốn rời khỏi quỹ nhóm "${group.name}" không?`,
      okText: 'Xác nhận rời',
      cancelText: 'Hủy',
      okType: 'danger',
      onOk: async () => {
        if (!user) return;
        try {
          const data = await request.post(`/funds/${group.id}/leave`, { email: user.email });
          if (data.success) {
            const newSystemMsg = {
              id: Date.now(),
              groupId: group.id,
              type: 'system',
              text: `${user.display_name || user.email.split('@')[0]} đã rời khỏi nhóm`,
              time: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
            };
            setDiscussions(prev => [...prev, newSystemMsg]);
            message.success(`Đã rời khỏi quỹ nhóm ${group.name}`);
            addNotification({
              id: `sys_${Date.now()}`,
              type: 'SYSTEM_INFO',
              fundId: group.id,
              fundName: group.name,
              amount: 0,
              description: `Bạn đã rời khỏi nhóm ${group.name}`,
              requesterName: 'Hệ thống',
              date: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
              read: false,
              targetRole: 'MEMBER'
            });
            onLeaveGroup(group.id);
          } else {
            message.error(data.message || 'Lỗi rời nhóm');
          }
        } catch (error) {
          message.error('Không thể kết nối đến máy chủ!');
        }
      }
    });
  };

  const percent = group.target > 0 ? Math.round((group.balance / group.target) * 100) : 0;

  const currentUserMember = group.members.find(m => m.email === user?.email);
  const isOwner = currentUserMember?.role === 'OWNER';

  const confirmDeleteGroup = () => {
    if (isOwner) {
      Modal.confirm({
        title: 'Xóa Quỹ Nhóm',
        icon: <WarningOutlined style={{ color: '#faad14' }} />,
        content: 'Bạn có chắc chắn muốn xóa quỹ này? Hành động này không thể hoàn tác.',
        okText: 'Tiếp tục',
        cancelText: 'Hủy',
        okButtonProps: { danger: true },
        onOk: async () => {
          if (!user) return;
          try {
            const data = await request.post(`/funds/${group.id}/request-delete-fund`, { email: user.email });
            if (data.success) {
              message.success(data.message || 'Đã gửi yêu cầu xóa quỹ. Vui lòng kiểm tra email để xác nhận.');
            } else {
              message.error(data.message || 'Lỗi yêu cầu xóa quỹ');
            }
          } catch (error) {
            message.error('Không thể kết nối đến máy chủ!');
          }
        }
      });
    }
  };

  const stats = React.useMemo(() => {
    let totalIncome = 0;
    let totalExpense = 0;
    let incomeThisWeek = 0;
    let incomeLastWeek = 0;
    let expenseThisWeek = 0;
    let expenseLastWeek = 0;

    if (!fundTransactions || fundTransactions.length === 0) {
      return { 
        totalIncome, 
        totalExpense, 
        incomeTrend: { percent: '0.0', isPositive: true }, 
        expenseTrend: { percent: '0.0', isPositive: true } 
      };
    }

    const now = new Date().getTime();
    const sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;
    const fourteenDaysAgo = now - 14 * 24 * 60 * 60 * 1000;

    const currentUserName = user?.display_name || (currentUserMember?.name ? currentUserMember.name.split(' (')[0] : '');

    fundTransactions.forEach(tx => {
      if (tx.is_approved === false) return;

      const isMyTx = isOwner || tx.user_display_name === currentUserName;
      if (!isMyTx) return;

      const txTime = new Date(tx.date).getTime();

      if (tx.type === 'INCOME') {
        totalIncome += tx.amount;
        if (txTime >= sevenDaysAgo) incomeThisWeek += tx.amount;
        else if (txTime >= fourteenDaysAgo) incomeLastWeek += tx.amount;
      } else if (tx.type === 'EXPENSE') {
        totalExpense += tx.amount;
        if (txTime >= sevenDaysAgo) expenseThisWeek += tx.amount;
        else if (txTime >= fourteenDaysAgo) expenseLastWeek += tx.amount;
      }
    });

    const calcTrend = (thisW: number, lastW: number) => {
      const change = thisW - lastW;
      let percent = 0;
      if (lastW > 0) percent = (change / lastW) * 100;
      else if (lastW === 0 && thisW > 0) percent = 100;

      return {
        percent: Math.abs(percent).toFixed(1),
        isPositive: change >= 0
      };
    };

    return {
      totalIncome,
      totalExpense,
      incomeTrend: calcTrend(incomeThisWeek, incomeLastWeek),
      expenseTrend: calcTrend(expenseThisWeek, expenseLastWeek)
    };
  }, [fundTransactions, isOwner, user, currentUserMember]);

  return (
    <div className={styles.container}>
      <div className={styles.detailHeaderRow}>
        <Space direction="vertical" size={2}>
          <Space align="center" size="middle">
            <Button 
              type="text" 
              icon={<ArrowLeftOutlined style={{ fontSize: '18px' }} />} 
              onClick={onBack}
              className={styles.backBtn}
            />
            <Title level={3} className={styles.detailTitle}>Quỹ {group.name}</Title>
            <Button
              type="text"
              icon={<EditOutlined style={{ fontSize: '15px', color: '#8c98a5' }} />}
              onClick={() => setIsRenameOpen(true)}
              className={styles.renameBtn}
              title="Đổi tên quỹ"
            />
          </Space>
          <div className={styles.detailMetaSub} style={{ cursor: 'default' }}>
            👥 {group.membersCount} thành viên
          </div>
        </Space>
        
        <Space size="middle" className={styles.detailActions}>
          <Button 
            icon={<TeamOutlined />} 
            onClick={() => setIsMembersModalOpen(true)}
          >
            Xem thành viên
          </Button>
          {!isOwner && (
            <Button 
              icon={<LogoutOutlined />} 
              onClick={confirmLeaveGroup}
              className={styles.leaveBtn}
            >
              Rời nhóm
            </Button>
          )}
          {isOwner && (
            <Button
              danger
              icon={<DeleteOutlined />}
              onClick={confirmDeleteGroup}
              className={styles.leaveBtn}
            >
              Xóa quỹ
            </Button>
          )}
          <Button 
            type="primary" 
            icon={<UserAddOutlined />} 
            onClick={() => setIsInviteOpen(true)}
            className={styles.inviteBtn}
          >
            Mời thành viên
          </Button>
        </Space>
      </div>

      <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={8}>
          <Card bordered={false} className={styles.detailMetricCard}>
            <Text className={styles.detailMetricLabel}>TỔNG SỐ DƯ HIỆN TẠI</Text>
            <div style={{ marginTop: 'auto' }}>
              <div className={styles.metricValueLarge}>{formatVND(group.balance)}</div>
              <div className={styles.trendTextDetail} style={{ color: trendData.isPositive ? '#34A853' : '#EA4335' }}>
                {trendData.isPositive ? '↗' : '↘'} {trendData.isPositive ? '+' : '-'}{trendData.percent}% so với tuần trước
              </div>
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={8}>
          <Card bordered={false} className={styles.detailMetricCard}>
            <Text className={styles.detailMetricLabel}>
              {isOwner ? 'TỔNG QUYÊN GÓP NHÓM' : 'TỔNG QUYÊN GÓP CỦA BẠN'}
            </Text>
            <div style={{ marginTop: 'auto' }}>
              <div className={styles.metricValueLarge} style={{ color: '#34A853' }}>{formatVND(stats.totalIncome)}</div>
              <div className={styles.trendTextDetail} style={{ color: stats.incomeTrend.isPositive ? '#34A853' : '#EA4335' }}>
                {stats.incomeTrend.isPositive ? '↗' : '↘'} {stats.incomeTrend.isPositive ? '+' : '-'}{stats.incomeTrend.percent}% so với tuần trước
              </div>
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={8}>
          <Card bordered={false} className={styles.detailMetricCard}>
            <Text className={styles.detailMetricLabel}>
              {isOwner ? 'TỔNG CHI TIÊU NHÓM' : 'TỔNG CHI TIÊU CỦA BẠN'}
            </Text>
            <div style={{ marginTop: 'auto' }}>
              <div className={styles.metricValueLarge} style={{ color: '#EA4335' }}>{formatVND(stats.totalExpense)}</div>
              <div className={styles.trendTextDetail} style={{ color: stats.expenseTrend.isPositive ? '#34A853' : '#EA4335' }}>
                {stats.expenseTrend.isPositive ? '↗' : '↘'} {stats.expenseTrend.isPositive ? '+' : '-'}{stats.expenseTrend.percent}% so với tuần trước
              </div>
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={12}>
          <Card bordered={false} className={styles.detailMetricCard}>
            <Text className={styles.detailMetricLabel}>MỤC TIÊU NHÓM</Text>
            <div style={{ marginTop: 'auto' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginTop: 12, marginBottom: 8 }}>
                <span className={styles.percentLarge}>{percent}%</span>
                <span className={styles.targetLimit}>{formatVND(group.target)}</span>
              </div>
              <Progress 
                percent={percent > 100 ? 100 : percent} 
                format={() => ''}
                showInfo={false} 
                strokeWidth={8}
                strokeColor={group.themeColor}
              />
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={24} lg={12}>
          <Card bordered={false} className={styles.detailMetricCard}>
            <Text className={styles.detailMetricLabel}>BIẾN ĐỘNG NGÂN SÁCH</Text>
            <div style={{ height: 180, marginTop: 12 }}>
              <Column 
                data={displayChartData}
                xField="month"
                yField="yValue"
                color={(datum: any) => datum.month === 'Tháng này' ? '#4285F4' : '#a8c7fa'}
                columnStyle={{ radius: [4, 4, 0, 0] }}
                maxColumnWidth={32}
                appendPadding={[10, 0, 0, 0]}
                xAxis={{
                  label: { style: { fill: '#8c98a5', fontSize: 11 } },
                  line: null,
                  tickLine: null,
                }}
                yAxis={{
                  title: {
                    text: '(Triệu)',
                    position: 'end',
                    autoRotate: false,
                    style: { fill: '#8c98a5', fontSize: 11 }
                  },
                  label: { 
                    style: { fill: '#8c98a5', fontSize: 11 } 
                  },
                  grid: { line: { style: { stroke: '#f1f3f4' } } }
                }}
                tooltip={{
                  title: 'tooltipTitle',
                  items: [
                    (datum: any) => ({
                      name: 'Số dư',
                      value: datum.formattedValue
                    })
                  ]
                }}
              />
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={16}>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card bordered={false} className={styles.historyCard}>
              <div className={styles.cardHeader}>
                <Text strong style={{ fontSize: '16px', color: '#202124' }}>Lịch sử đóng góp</Text>
                <Button type="text" className={styles.seeAllBtn} onClick={() => { setTxModalType('INCOME'); setTxModalVisible(true); }}>Xem tất cả</Button>
              </div>
              
              <div className={styles.historyList}>
                {loadingTx ? (
                  <div style={{ textAlign: 'center', padding: 24, color: '#8c98a5' }}>Đang tải...</div>
                ) : fundTransactions.filter(tx => tx.type === 'INCOME').length === 0 ? (
                  <div style={{ textAlign: 'center', padding: 24, color: '#8c98a5' }}>Chưa có lịch sử đóng góp</div>
                ) : (
                  fundTransactions.filter(tx => tx.type === 'INCOME').slice(0, 5).map(tx => (
                    <div className={styles.historyItem} key={tx.id}>
                      <Space size="middle">
                        <Avatar style={{ backgroundColor: '#1A73E8', fontWeight: 600 }}>
                          {tx.user_display_name.split(' ').map((n: string) => n[0]).join('').slice(-2).toUpperCase()}
                        </Avatar>
                        <div>
                          <div className={styles.itemName}>{tx.user_display_name}</div>
                          <div className={styles.itemMeta}>{new Date(tx.date).toLocaleDateString('vi-VN')}</div>
                        </div>
                      </Space>
                      <div style={{ textAlign: 'right' }}>
                        <div className={styles.amountGreen}>+{tx.amount.toLocaleString('vi-VN')} đ</div>
                        <Tag color="success" className={styles.methodTag}>{tx.category_name}</Tag>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </Card>

            <Card bordered={false} className={styles.historyCard}>
              <div className={styles.cardHeader}>
                <Text strong style={{ fontSize: '16px', color: '#202124' }}>Lịch sử chi tiêu</Text>
                <Button type="text" className={styles.seeAllBtn} onClick={() => { setTxModalType('EXPENSE'); setTxModalVisible(true); }}>Xem tất cả</Button>
              </div>

              <div className={styles.historyList}>
                {loadingTx ? (
                  <div style={{ textAlign: 'center', padding: 24, color: '#8c98a5' }}>Đang tải...</div>
                ) : fundTransactions.filter(tx => tx.type === 'EXPENSE').length === 0 ? (
                  <div style={{ textAlign: 'center', padding: 24, color: '#8c98a5' }}>Chưa có lịch sử chi tiêu</div>
                ) : (
                  fundTransactions.filter(tx => tx.type === 'EXPENSE').slice(0, 5).map(tx => (
                    <div className={styles.historyItem} key={tx.id}>
                      <Space size="middle">
                        <div className={styles.expenseIconRed}>
                          <WalletOutlined />
                        </div>
                        <div>
                          <div className={styles.itemName}>{tx.description}</div>
                          <div className={styles.itemMeta}>Bởi: {tx.user_display_name} • {new Date(tx.date).toLocaleDateString('vi-VN')}</div>
                        </div>
                      </Space>
                      <div className={styles.amountExpense}>-{tx.amount.toLocaleString('vi-VN')} đ</div>
                    </div>
                  ))
                )}
              </div>
            </Card>


          </Space>
        </Col>

        <Col xs={24} lg={8}>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card bordered={false} className={styles.quickDepositCard}>
              <Text strong style={{ fontSize: '15px', color: '#1a1d20', display: 'block', marginBottom: 16 }}>
                Nạp tiền nhanh
              </Text>
              
              <div className={styles.qrContainer}>
                {qrImage ? (
                  <div style={{ position: 'relative', width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <img src={qrImage} alt="QR Code" style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain', borderRadius: 8 }} />
                    <Button 
                      danger 
                      size="small" 
                      icon={<DeleteOutlined />} 
                      style={{ position: 'absolute', top: -5, right: -5, borderRadius: '50%' }}
                      onClick={handleRemoveQr}
                      title="Xóa QR"
                    />
                  </div>
                ) : (
                  <Upload
                    name="qr"
                    listType="picture-card"
                    className={styles.qrUploader}
                    showUploadList={false}
                    beforeUpload={handleUploadQr}
                  >
                    <div>
                      <UploadOutlined style={{ fontSize: 24, color: '#1A73E8' }} />
                      <div style={{ marginTop: 8, color: '#1A73E8', fontWeight: 500 }}>Tải ảnh QR lên</div>
                    </div>
                  </Upload>
                )}
              </div>
              
              <div className={styles.qrText}>Quét mã để nạp trực tiếp vào quỹ</div>
              
              <Row gutter={12} style={{ marginTop: 20 }}>
                <Col span={12}>
                  <Button 
                    type="primary" 
                    block 
                    onClick={() => setIsDepositOpen(true)}
                    className={styles.depositBtn}
                  >
                    Nạp tiền
                  </Button>
                </Col>
                <Col span={12}>
                  <Button 
                    block 
                    onClick={() => setIsWithdrawOpen(true)}
                    className={styles.withdrawBtn}
                  >
                    Rút quỹ
                  </Button>
                </Col>
              </Row>
            </Card>

            <Card bordered={false} className={styles.topContributorsCard}>
              <div className={styles.logHeader} style={{ borderBottom: 'none', paddingBottom: 0 }}>
                <Space>
                  <SmileOutlined style={{ color: '#1A73E8' }} />
                  <Text strong style={{ fontSize: '15px', color: '#1a1d20' }}>Top Đóng góp</Text>
                </Space>
              </div>
              
              <div className={styles.contributorsList} style={{ marginTop: 16 }}>
                {topContributorsDataV2.map((c, idx) => (
                  <div className={styles.contributorItem} key={c.id}>
                    <Space size="middle">
                      <span className={styles.rankText}>{idx + 1}</span>
                      <Avatar style={{ backgroundColor: c.avatarColor }}>{c.avatarInitials}</Avatar>
                      <span className={styles.contributorName}>{c.name}</span>
                    </Space>
                    <span className={styles.contributorAmount}>{formatVND(c.amount)}</span>
                  </div>
                ))}
              </div>
            </Card>
          </Space>
        </Col>
      </Row>

      <Modal
        title={txModalType === 'INCOME' ? 'Tất cả lịch sử đóng góp' : 'Tất cả lịch sử chi tiêu'}
        open={txModalVisible}
        onCancel={() => setTxModalVisible(false)}
        footer={null}
        width={700}
      >
        <Table 
          dataSource={fundTransactions.filter(tx => tx.type === txModalType)}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          columns={[
            {
              title: 'Thành viên',
              dataIndex: 'user_display_name',
              key: 'user_display_name',
              render: (text) => <Text strong>{text}</Text>
            },
            {
              title: 'Số tiền',
              dataIndex: 'amount',
              key: 'amount',
              render: (amount) => (
                <Text strong style={{ color: txModalType === 'INCOME' ? '#34A853' : '#EA4335' }}>
                  {txModalType === 'INCOME' ? '+' : '-'}{amount.toLocaleString('vi-VN')} đ
                </Text>
              )
            },
            {
              title: 'Mô tả',
              dataIndex: 'description',
              key: 'description',
            },
            {
              title: 'Ngày',
              dataIndex: 'date',
              key: 'date',
              render: (date) => new Date(date).toLocaleDateString('vi-VN')
            }
          ]}
        />
      </Modal>

      <InviteMemberModal 
        isOpen={isInviteOpen} 
        onClose={() => setIsInviteOpen(false)} 
        selectedGroup={group} 
      />
      <DepositModal 
        isOpen={isDepositOpen} 
        onClose={() => setIsDepositOpen(false)} 
        selectedGroup={group} 
      />
      <WithdrawModal 
        isOpen={isWithdrawOpen} 
        onClose={() => setIsWithdrawOpen(false)} 
        selectedGroup={group} 
      />
      <RenameFundModal 
        isOpen={isRenameOpen} 
        onClose={() => setIsRenameOpen(false)} 
        selectedGroup={group} 
        onSuccess={(newName) => onRenameGroup(group.id, newName)}
      />
      <MembersModal 
        isOpen={isMembersModalOpen} 
        onClose={() => setIsMembersModalOpen(false)} 
        selectedGroup={group}
        onRequestRemove={async (member, idx) => {
          setIsMembersModalOpen(false);
          try {
            const data = await request.post('/funds/remove-request', { fundId: group.id, memberEmail: member.email });
            if (data.success) {
              message.success(`Đã gửi yêu cầu xóa thành viên. Vui lòng kiểm tra email để xác nhận.`);
            } else {
              message.error(data.message || 'Lỗi gửi yêu cầu');
            }
          } catch (error) {
            message.error('Không thể kết nối đến máy chủ!');
          }
        }}
      />
      <FloatingChat 
        group={group} 
        discussions={discussions} 
        sendMessage={sendMessage} 
      />
    </div>
  );
}
