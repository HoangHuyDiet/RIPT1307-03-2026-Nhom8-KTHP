import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'umi';
import { Popover, Badge, Dropdown, Button, List, Tag, message, Modal, Input } from 'antd';
import {
  BellFilled,
  MoreOutlined,
  CheckOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  RobotOutlined,
  FlagOutlined,
  WalletOutlined,
  DollarCircleOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import styles from './index.less';
import { useNotificationStore, FundNotification } from '@/store/useNotificationStore';
import { useAuthStore } from '@/store/useAuthStore';
import request from '@/utils/request';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function NotificationsPopover() {
  const location = useLocation();
  const user = useAuthStore((s) => s.user);
  const notifications = useNotificationStore((s) => s.notifications);
  const markAsRead = useNotificationStore((s) => s.markAsRead);
  const removeNotification = useNotificationStore((s) => s.removeNotification);
  const addNotification = useNotificationStore((s) => s.addNotification);

  const [serverRequests, setServerRequests] = useState<FundNotification[]>([]);
  const [serverMemberNotifications, setServerMemberNotifications] = useState<FundNotification[]>([]);
  const [popoverFilter, setPopoverFilter] = useState<'all' | 'system' | 'funds' | 'personal'>('all');
  const [popoverUnreadOnly, setPopoverUnreadOnly] = useState<boolean>(false);
  const [popoverOpen, setPopoverOpen] = useState(false);

  useEffect(() => {
    setPopoverOpen(false);
  }, [location.pathname]);

  const [rejectModalVisible, setRejectModalVisible] = useState(false);
  const [rejectingItem, setRejectingItem] = useState<FundNotification | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  const fetchPendingFundRequests = async () => {
    if (!user?.email) {
      setServerRequests([]);
      setServerMemberNotifications([]);
      return;
    }
    try {
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
      setServerRequests(txLists.flat());
      const myNotificationsRes = await request.get('/funds/my-notifications');
      setServerMemberNotifications(myNotificationsRes.data || []);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchPendingFundRequests();
    window.addEventListener('transaction-approved', fetchPendingFundRequests);
    const timer = window.setInterval(fetchPendingFundRequests, 30000);
    return () => {
      window.removeEventListener('transaction-approved', fetchPendingFundRequests);
      window.clearInterval(timer);
    };
  }, [user?.email]);

  useEffect(() => {
    const token = localStorage.getItem('token') || localStorage.getItem('access_token');
    if (!user?.email || !token) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      onConnect: () => {
        client.subscribe('/user/queue/notifications', (frame) => {
          const notification = JSON.parse(frame.body);
          addNotification({
            ...notification,
            id: String(notification.id || `ws_${Date.now()}`),
            read: false,
          });
        });
      },
      onStompError: (frame) => {
        console.error(frame.headers.message);
      },
    });
    client.activate();
    return () => {
      if (client.active) {
        client.deactivate();
      }
    };
  }, [user?.email, addNotification]);

  const visibleNotifications = [
    ...serverRequests,
    ...serverMemberNotifications,
    ...notifications.filter((n) => !(n.targetRole === 'OWNER' && n.type.includes('REQUEST')))
  ];

  const top15Notifications = visibleNotifications.slice(0, 15);

  const popoverNotifications = top15Notifications.filter((n) => {
    if (popoverUnreadOnly && n.read) return false;
    if (popoverFilter === 'system') {
      return n.id.startsWith('dash') || n.id.startsWith('goal') || n.id.startsWith('tx');
    }
    if (popoverFilter === 'personal') {
      return n.id.startsWith('pf');
    }
    if (popoverFilter === 'funds') {
      return n.id.startsWith('fund') || n.type.includes('REQUEST') || n.type.includes('APPROVED') || n.type.includes('REJECTED');
    }
    return true;
  });

  const visibleUnreadCount = top15Notifications.filter((n) => !n.read).length;

  const handleMarkAsRead = async (id: string) => {
    try {
      await request.post('/funds/my-notifications/read', { id });
      fetchPendingFundRequests();
    } catch {
      markAsRead(id);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await request.post('/funds/my-notifications/read-all');
      fetchPendingFundRequests();
      message.success('Đã đánh dấu tất cả thông báo là đã đọc');
    } catch {
      notifications.forEach(n => {
        if (!n.read) markAsRead(n.id);
      });
      fetchPendingFundRequests();
    }
  };

  const handleApproveRequest = async (req: FundNotification, action: 'approved' | 'rejected', reason?: string) => {
    try {
      const data = await request.post('/funds/approve-transaction', {
        requestId: req.id,
        action,
        rejectReason: reason
      });
      if (data.success) {
        if (action === 'approved') {
          message.success('Đã duyệt thành công!');
          fetchPendingFundRequests();
          window.dispatchEvent(new Event('transaction-approved'));
        } else {
          message.info('Đã từ chối yêu cầu.');
        }
        removeNotification(req.id);
        fetchPendingFundRequests();
      }
    } catch {
      message.error('Lỗi xử lý duyệt yêu cầu!');
    }
  };

  const getInlineIcon = (item: FundNotification) => {
    const id = item.id.toLowerCase();
    if (id.startsWith('dash')) {
      return <RobotOutlined style={{ color: '#1a73e8', marginRight: 6 }} />;
    }
    if (id.startsWith('goal')) {
      return <FlagOutlined style={{ color: '#f9ab00', marginRight: 6 }} />;
    }
    if (id.startsWith('pf')) {
      return <WalletOutlined style={{ color: '#d93025', marginRight: 6 }} />;
    }
    if (id.startsWith('tx')) {
      return <DollarCircleOutlined style={{ color: '#a142f4', marginRight: 6 }} />;
    }
    return <TeamOutlined style={{ color: '#137333', marginRight: 6 }} />;
  };

  const getSourcePageName = (item: FundNotification) => {
    const id = item.id.toLowerCase();
    if (id.startsWith('dash')) return 'Tổng quan & AI';
    if (id.startsWith('goal')) return 'Mục tiêu tiết kiệm';
    if (id.startsWith('pf')) return 'Quỹ cá nhân';
    if (id.startsWith('tx')) return 'Quản lý giao dịch';
    return 'Quỹ nhóm';
  };

  return (
    <>
      <Popover
        trigger="click"
        placement="bottomRight"
        open={popoverOpen}
        onOpenChange={setPopoverOpen}
        overlayClassName="custom-notification-popover"
        overlayStyle={{ width: 420 }}
        overlayInnerStyle={{ width: 420, padding: 0 }}
        arrow={false}
        title={null}
        content={
          <div className={styles.notificationList}>
            <div className={styles.popoverTitleBar}>
              <span className={styles.popoverTitleText}>Thông báo</span>
              <Dropdown
                menu={{
                  items: [
                    {
                      key: 'readAll',
                      icon: <CheckOutlined />,
                      label: 'Đánh dấu tất cả đã đọc',
                      onClick: handleMarkAllAsRead
                    },
                    {
                      type: 'divider',
                    },
                    {
                      key: 'filterAll',
                      label: 'Hiện tất cả thông báo',
                      onClick: () => setPopoverFilter('all'),
                      className: popoverFilter === 'all' ? styles.menuItemSelected : ''
                    },
                    {
                      key: 'filterSystem',
                      label: 'Chỉ hiện thông báo hệ thống',
                      onClick: () => setPopoverFilter('system'),
                      className: popoverFilter === 'system' ? styles.menuItemSelected : ''
                    },
                    {
                      key: 'filterPersonal',
                      label: 'Chỉ hiện thông báo quỹ cá nhân',
                      onClick: () => setPopoverFilter('personal'),
                      className: popoverFilter === 'personal' ? styles.menuItemSelected : ''
                    },
                    {
                      key: 'filterFunds',
                      label: 'Chỉ hiện thông báo quỹ & giao dịch',
                      onClick: () => setPopoverFilter('funds'),
                      className: popoverFilter === 'funds' ? styles.menuItemSelected : ''
                    }
                  ]
                }}
                trigger={['click']}
                placement="bottomRight"
              >
                <Button
                  type="text"
                  shape="circle"
                  icon={<MoreOutlined style={{ fontSize: 18 }} />}
                />
              </Dropdown>
            </div>

            <div className={styles.popoverFilters}>
              <button
                className={`${styles.filterPill} ${!popoverUnreadOnly ? styles.active : ''}`}
                onClick={() => setPopoverUnreadOnly(false)}
              >
                Tất cả
              </button>
              <button
                className={`${styles.filterPill} ${popoverUnreadOnly ? styles.active : ''}`}
                onClick={() => setPopoverUnreadOnly(true)}
              >
                Chưa đọc
              </button>
            </div>

            <div className={styles.popoverSectionHeader}>
              <span className={styles.sectionTitle}>Trước đó</span>
              <Link
                to="/notifications"
                className={styles.viewAllLink}
                onClick={() => setPopoverOpen(false)}
              >
                Xem tất cả
              </Link>
            </div>

            {popoverNotifications.length === 0 ? (
              <div className={styles.emptyNotifications}>
                Không có thông báo
              </div>
            ) : (
              <List
                dataSource={popoverNotifications}
                renderItem={(item: FundNotification) => {
                  const isRequest = item.type === 'DEPOSIT_REQUEST' || item.type === 'WITHDRAW_REQUEST';
                  const pageName = getSourcePageName(item);
                  return (
                    <List.Item
                      className={`${styles.notificationItem} ${item.read ? '' : styles.unread}`}
                      onClick={() => {
                        if (!item.read) handleMarkAsRead(item.id);
                      }}
                    >
                      <div className={styles.notifContent}>
                        <div className={styles.notifHeader}>
                          <Tag
                            color={
                              item.type === 'SYSTEM_INFO' ? 'default' :
                              item.type === 'FUND_INVITATION' ? 'purple' :
                              item.type === 'FUND_DISBAND_PROPOSAL' ? 'warning' :
                              item.type === 'FUND_MEMBER_REMOVED' ? 'error' :
                              item.type.includes('REQUEST') ? 'processing' :
                              item.type.includes('APPROVED') ? 'success' : 'error'
                            }
                            className={styles.notifTag}
                          >
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
                          </Tag>
                          <span className={styles.notifDate}>{item.date}</span>
                        </div>

                        <div className={styles.notifSourceRow}>
                          {getInlineIcon(item)}
                          <span className={styles.notifSourceName}>{pageName}</span>
                        </div>

                        <div className={styles.notifDesc}>
                          {item.type === 'SYSTEM_INFO' || item.type === 'FUND_INVITATION' || item.type === 'FUND_DISBAND_PROPOSAL' || item.type === 'FUND_MEMBER_REMOVED' || item.type.includes('REJECTED') ? item.description : (item.type.includes('WITHDRAW') ? `Lý do: ${item.description}` : `Nội dung: ${item.description}`)}
                        </div>

                        {item.type !== 'SYSTEM_INFO' && item.type !== 'FUND_INVITATION' && item.type !== 'FUND_DISBAND_PROPOSAL' && item.type !== 'FUND_MEMBER_REMOVED' && item.amount > 0 && (
                          <div className={styles.notifAmount}>
                            Số tiền: <span className={styles.amountVal}>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.amount)}</span>
                          </div>
                        )}
                        {item.bankAccount && (
                          <div className={styles.notifBank}>
                            TK: {item.bankAccount} - {item.bankName}
                          </div>
                        )}

                        <div className={styles.notifSenderName}>
                          Người gửi: <strong>{item.requesterName}</strong> {item.fundName ? `• Quỹ: "${item.fundName}"` : ''}
                        </div>

                        {!item.read && isRequest && (
                          <div className={styles.notifActions} onClick={(e) => e.stopPropagation()}>
                            <Button
                              type="primary"
                              size="small"
                              icon={<CheckCircleOutlined />}
                              onClick={() => handleApproveRequest(item, 'approved')}
                            >
                              Duyệt
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
            )}
          </div>
        }
      >
        <Badge count={visibleUnreadCount} size="small" offset={[-2, 2]} className={styles.bellBadge}>
          <div className={styles.bellWrapper}>
            <BellFilled className={styles.bellIcon} />
          </div>
        </Badge>
      </Popover>

      <Modal
        title="Lý do từ chối"
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
        okText="Từ chối yêu cầu"
        cancelText="Hủy"
        okButtonProps={{ danger: true }}
      >
        <Input.TextArea
          rows={4}
          placeholder="Nhập lý do từ chối để thông báo lại cho thành viên..."
          value={rejectReason}
          onChange={(e) => setRejectReason(e.target.value)}
        />
      </Modal>
    </>
  );
}
