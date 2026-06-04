import React, { useState, useEffect } from 'react';
import { Tabs, Button, Card, Space, Tag, List, message, Modal, Input } from 'antd';
import {
  BellOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  RobotOutlined,
  FlagOutlined,
  WalletOutlined,
  DollarCircleOutlined,
  TeamOutlined,
  BankOutlined,
  DeleteOutlined,
  CheckOutlined,
} from '@ant-design/icons';
import styles from './index.less';
import request from '@/utils/request';
import { useAuthStore } from '@/store/useAuthStore';
import { useNotificationStore, FundNotification } from '@/store/useNotificationStore';
import { history } from 'umi';

const EXCLUDED_NAV_TYPES = ['AI_MESSAGE', 'FUND_INVITATION', 'FUND_DISSOLUTION', 'FUND_KICKED', 'FUND_DISBAND_PROPOSAL', 'FUND_MEMBER_REMOVED'];

export default function NotificationCenter() {
  const user = useAuthStore((s) => s.user);
  const markAsReadLocal = useNotificationStore((s) => s.markAsRead);
  const removeNotificationLocal = useNotificationStore((s) => s.removeNotification);

  const [activeTab, setActiveTab] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const [notifications, setNotifications] = useState<FundNotification[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setCurrentPage(1);
  }, [activeTab]);

  const [rejectModalVisible, setRejectModalVisible] = useState(false);
  const [rejectingItem, setRejectingItem] = useState<FundNotification | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  const fetchNotifications = async () => {
    if (!user?.email) return;
    setLoading(true);
    try {
      const notifRes = await request.get('/funds/my-notifications');
      const serverNotifs = notifRes.data || [];

      const fundsRes = await request.get('/funds/list');
      const funds = fundsRes.data || [];
      const ownedFunds = funds.filter((fund: any) =>
        (fund.members || []).some((member: any) =>
          member.email === user.email && member.role === 'OWNER'
        )
      );

      const txLists = await Promise.all(
        ownedFunds.map(async (fund: any) => {
          try {
            const txRes = await request.get('/funds/transactions', { params: { fundId: fund.id } });
            return (txRes.data || [])
              .filter((tx: any) => tx.is_approved === false && tx.status !== 'REJECTED')
              .map((tx: any) => ({
                id: String(tx.id),
                type: tx.type === 'INCOME' ? 'DEPOSIT_REQUEST' : 'WITHDRAW_REQUEST',
                fundId: fund.id,
                fundName: fund.name,
                amount: Number(tx.amount),
                description: tx.description || '',
                requesterName: tx.user_display_name || 'Người dùng',
                bankAccount: tx.bank_account,
                bankName: tx.bank_name,
                date: tx.date,
                read: false,
                targetRole: 'OWNER'
              } as FundNotification));
          } catch (e) {
            console.warn(e);
            return [];
          }
        })
      );

      const pendingRequests = txLists.flat();
      setNotifications([...pendingRequests, ...serverNotifs]);
    } catch (e) {
      console.error(e);
      message.error('Lỗi khi tải danh sách thông báo!');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, [user?.email]);

  const handleMarkAsRead = async (id: string) => {
    try {
      await request.post('/funds/my-notifications/read', { id });
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      markAsReadLocal(id);
      message.success('Đã đánh dấu đã đọc');
    } catch {
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      markAsReadLocal(id);
    }
  };

  const handleNotificationClick = async (item: FundNotification) => {
    // 1. Luôn luôn gọi hàm markAsReadLocal(item.id) từ store để cập nhật trạng thái đã đọc
    markAsReadLocal(item.id);

    // Đồng thời gọi API đánh dấu đã đọc trên server để đồng bộ
    if (!item.read) {
      try {
        await request.post('/funds/my-notifications/read', { id: item.id });
        setNotifications((prev) =>
          prev.map((n) => (n.id === item.id ? { ...n, read: true } : n))
        );
      } catch (e) {
        console.error('Không thể đồng bộ trạng thái đọc lên server:', e);
      }
    }

    // 2. Nếu loại thông báo thuộc mảng ngoại lệ thì dừng lại, không điều hướng
    if (EXCLUDED_NAV_TYPES.includes(item.type)) {
      return;
    }

    // 3. Nếu có link_action hoặc actionUrl thì điều hướng
    const targetUrl = item.link_action || item.actionUrl;
    if (targetUrl) {
      history.push(targetUrl);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await request.post('/funds/my-notifications/read-all');
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      message.success('Đã đánh dấu tất cả thông báo là đã đọc');
    } catch {
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    }
  };

  const handleDeleteNotification = async (id: string) => {
    try {
      await request.post('/funds/my-notifications/delete', { id });
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      removeNotificationLocal(id);
      message.success('Đã xóa thông báo');
    } catch {
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      removeNotificationLocal(id);
    }
  };

  const handleDeleteAllNotifications = async () => {
    Modal.confirm({
      title: 'Xóa toàn bộ thông báo',
      content: 'Bạn có chắc chắn muốn xóa toàn bộ thông báo không? Hành động này không thể phục hồi.',
      okText: 'Xác nhận xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await request.post('/funds/my-notifications/delete-all');
          setNotifications([]);
          message.success('Đã xóa toàn bộ thông báo');
        } catch {
          setNotifications([]);
        }
      }
    });
  };

  const handleApproveRequest = async (req: FundNotification, action: 'approved' | 'rejected', reason?: string) => {
    try {
      const res = await request.post('/funds/approve-transaction', {
        requestId: req.id,
        action,
        rejectReason: reason
      });
      if (res.success) {
        message.success(action === 'approved' ? 'Đã duyệt yêu cầu thành công!' : 'Đã từ chối yêu cầu.');
        setNotifications((prev) => prev.filter((n) => n.id !== req.id));
        removeNotificationLocal(req.id);
        window.dispatchEvent(new Event('transaction-approved'));
      }
    } catch {
      message.error('Lỗi xử lý duyệt yêu cầu!');
    }
  };

  const getIcon = (item: FundNotification) => {
    const id = item.id.toLowerCase();
    if (id.startsWith('dash')) {
      return <div className={`${styles.iconWrapper} ${styles.dashboard}`}><RobotOutlined /></div>;
    }
    if (id.startsWith('goal')) {
      return <div className={`${styles.iconWrapper} ${styles.goals}`}><FlagOutlined /></div>;
    }
    if (id.startsWith('pf')) {
      return <div className={`${styles.iconWrapper} ${styles.personalFunds}`}><WalletOutlined /></div>;
    }
    if (id.startsWith('tx')) {
      return <div className={`${styles.iconWrapper} ${styles.transactions}`}><DollarCircleOutlined /></div>;
    }
    return <div className={`${styles.iconWrapper} ${styles.funds}`}><TeamOutlined /></div>;
  };

  const getSourcePageTag = (item: FundNotification) => {
    const id = item.id.toLowerCase();
    if (id.startsWith('dash')) {
      return <Tag color="blue">Tổng quan</Tag>;
    }
    if (id.startsWith('goal')) {
      return <Tag color="orange">Mục tiêu tiết kiệm</Tag>;
    }
    if (id.startsWith('pf')) {
      return <Tag color="red">Quỹ cá nhân</Tag>;
    }
    if (id.startsWith('tx')) {
      return <Tag color="purple">Giao dịch</Tag>;
    }
    if (item.type === 'FUND_INVITATION') {
      return <Tag color="purple">Lời mời tham gia quỹ</Tag>;
    }
    if (item.type === 'FUND_DISBAND_PROPOSAL') {
      return <Tag color="warning">Đề xuất giải tán quỹ</Tag>;
    }
    if (item.type === 'FUND_MEMBER_REMOVED') {
      return <Tag color="error">Bị xóa khỏi quỹ</Tag>;
    }
    return <Tag color="green">Quỹ nhóm</Tag>;
  };

  const filteredNotifications = notifications.filter((n) => {
    if (activeTab === 'unread') return !n.read;
    if (activeTab === 'system') return n.id.startsWith('dash') || n.id.startsWith('goal') || n.id.startsWith('tx');
    if (activeTab === 'personal') return n.id.startsWith('pf');
    if (activeTab === 'funds') return n.id.startsWith('fund') || n.type.includes('REQUEST') || n.type.includes('APPROVED') || n.type.includes('REJECTED') || n.type === 'FUND_INVITATION' || n.type === 'FUND_DISBAND_PROPOSAL' || n.type === 'FUND_MEMBER_REMOVED';
    return true;
  });

  const pageSize = 10;
  const totalPages = Math.ceil(filteredNotifications.length / pageSize);
  const startPage = Math.floor((currentPage - 1) / 6) * 6 + 1;
  const endPage = Math.min(startPage + 5, totalPages);
  const paginatedNotifications = filteredNotifications.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <div className={styles.titleSection}>
          <h1 className={styles.pageTitle}>Trung tâm thông báo</h1>
          <p className={styles.pageSubtitle}>
            Theo dõi gợi ý từ AI, tiến độ mục tiêu, biến động quỹ cá nhân và phê duyệt các giao dịch quỹ nhóm.
          </p>
        </div>
      </div>

      <div className={styles.notificationsCard}>
        <div className={styles.tabBarContainer}>
          <Tabs
            activeKey={activeTab}
            onChange={(key) => setActiveTab(key)}
            items={[
              { key: 'all', label: `Tất cả (${notifications.length})` },
              { key: 'unread', label: `Chưa đọc (${unreadCount})` },
              { key: 'system', label: 'Hệ thống & AI' },
              { key: 'personal', label: 'Quỹ cá nhân' },
              { key: 'funds', label: 'Quỹ nhóm' },
            ]}
          />
          <div className={styles.actionButtons}>
            <Button
              type="text"
              icon={<CheckOutlined />}
              disabled={unreadCount === 0}
              onClick={handleMarkAllAsRead}
            >
              Đánh dấu đọc tất cả
            </Button>
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              disabled={notifications.length === 0}
              onClick={handleDeleteAllNotifications}
            >
              Xóa tất cả
            </Button>
          </div>
        </div>

        <List
          loading={loading}
          dataSource={paginatedNotifications}
          locale={{
            emptyText: (
              <div className={styles.emptyState}>
                <BellOutlined className={styles.emptyIcon} />
                <h3 className={styles.emptyTitle}>Không có thông báo</h3>
                <p className={styles.emptyDesc}>Các thông báo và cập nhật mới nhất của bạn sẽ xuất hiện tại đây.</p>
              </div>
            )
          }}
          renderItem={(item) => {
            const isRequest = item.type === 'DEPOSIT_REQUEST' || item.type === 'WITHDRAW_REQUEST';
            return (
              <List.Item
                className={`${styles.notifItem} ${item.read ? '' : styles.unreadItem} ${EXCLUDED_NAV_TYPES.includes(item.type) ? styles.excludedNavItem : ''}`}
                onClick={() => handleNotificationClick(item)}
              >
                {getIcon(item)}
                <div className={styles.notifContent}>
                  <div className={styles.metaRow}>
                    <Space size="small" align="center">
                      <span className={styles.notifTitle}>
                        {item.type === 'DEPOSIT_REQUEST' && '💰 Yêu cầu nạp tiền'}
                        {item.type === 'WITHDRAW_REQUEST' && '💸 Yêu cầu rút tiền'}
                        {item.type === 'DEPOSIT_APPROVED' && '✅ Nạp tiền được duyệt'}
                        {item.type === 'DEPOSIT_REJECTED' && '❌ Nạp tiền bị từ chối'}
                        {item.type === 'WITHDRAW_APPROVED' && '✅ Rút tiền được duyệt'}
                        {item.type === 'WITHDRAW_REJECTED' && '❌ Rút tiền bị từ chối'}
                        {item.type === 'SYSTEM_INFO' && 'ℹ️ Hệ thống'}
                        {item.type === 'FUND_INVITATION' && '📩 Lời mời tham gia quỹ'}
                        {item.type === 'FUND_DISBAND_PROPOSAL' && '⚠️ Đề xuất giải tán quỹ'}
                        {item.type === 'FUND_MEMBER_REMOVED' && '🚫 Bị xóa khỏi quỹ'}
                      </span>
                      {getSourcePageTag(item)}
                    </Space>
                    <Space>
                      <span className={styles.notifDate}>{item.date}</span>
                      <Button
                        type="text"
                        size="small"
                        danger
                        icon={<DeleteOutlined />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteNotification(item.id);
                        }}
                        title="Xóa thông báo"
                      />
                    </Space>
                  </div>

                  <div className={styles.notifMessage}>{item.description}</div>

                  {item.type !== 'SYSTEM_INFO' && item.type !== 'FUND_INVITATION' && item.type !== 'FUND_DISBAND_PROPOSAL' && item.type !== 'FUND_MEMBER_REMOVED' && item.amount > 0 && (
                    <div className={styles.amountBox}>
                      Số tiền: <span className={styles.amountVal}>
                        {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.amount)}
                      </span>
                    </div>
                  )}

                  {item.bankAccount && (
                    <div className={styles.notifBank}>
                      Tài khoản chuyển: {item.bankAccount} - {item.bankName}
                    </div>
                  )}

                  <div className={styles.notifDetails}>
                    Người gửi: <strong>{item.requesterName}</strong> {item.fundName ? `• Quỹ nhóm: "${item.fundName}"` : ''}
                  </div>

                  {!item.read && isRequest && (
                    <div className={styles.notifActions} onClick={(e) => e.stopPropagation()}>
                      <Button
                        type="primary"
                        size="small"
                        icon={<CheckCircleOutlined />}
                        onClick={() => handleApproveRequest(item, 'approved')}
                      >
                        Phê duyệt
                      </Button>
                      <Button
                        danger
                        size="small"
                        icon={<CloseCircleOutlined />}
                        onClick={() => {
                          setRejectingItem(item);
                          setRejectModalVisible(true);
                        }}
                      >
                        Từ chối
                      </Button>
                    </div>
                  )}
                </div>
              </List.Item>
            );
          }}
        />

        {totalPages > 1 && (
          <div className={styles.customPagination}>
            {startPage > 1 && (
              <Button
                type="text"
                onClick={() => setCurrentPage(Math.max(currentPage - 6, 1))}
                className={styles.pageBtn}
              >
                &lt;&lt;
              </Button>
            )}
            {Array.from({ length: endPage - startPage + 1 }, (_, index) => {
              const pageNum = startPage + index;
              return (
                <Button
                  type="text"
                  key={pageNum}
                  onClick={() => setCurrentPage(pageNum)}
                  className={`${styles.pageBtn} ${currentPage === pageNum ? styles.activePage : ''}`}
                >
                  {pageNum}
                </Button>
              );
            })}
            {endPage < totalPages && (
              <Button
                type="text"
                onClick={() => setCurrentPage(Math.min(currentPage + 6, totalPages))}
                className={styles.pageBtn}
              >
                &gt;&gt;
              </Button>
            )}
          </div>
        )}
      </div>

      <Modal
        title="Lý do từ chối giao dịch"
        open={rejectModalVisible}
        onOk={() => {
          if (rejectingItem) {
            handleApproveRequest(rejectingItem, 'rejected', rejectReason);
          }
          setRejectModalVisible(false);
          setRejectingItem(null);
          setRejectReason('');
        }}
        onCancel={() => {
          setRejectModalVisible(false);
          setRejectingItem(null);
          setRejectReason('');
        }}
        okText="Xác nhận từ chối"
        cancelText="Hủy"
        okButtonProps={{ danger: true }}
      >
        <Input.TextArea
          rows={4}
          placeholder="Nhập lý do từ chối để phản hồi lại thành viên..."
          value={rejectReason}
          onChange={(e) => setRejectReason(e.target.value)}
        />
      </Modal>
    </div>
  );
}
