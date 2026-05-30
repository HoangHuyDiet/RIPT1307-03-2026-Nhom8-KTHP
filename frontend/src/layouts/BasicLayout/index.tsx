import React, { useState } from 'react';
import { Link, Outlet, useLocation } from 'umi';
import { Layout, Menu, Avatar, Dropdown, Space, Typography, Button, Input, Badge, Popover, List, Tag, message, Modal } from 'antd';
import type { MenuProps } from 'antd';
import {
  AppstoreOutlined,
  AppstoreFilled,
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  BankOutlined,
  SearchOutlined,
  FlagOutlined,
  FlagFilled,
  WalletOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import styles from './index.less';
import { useNotificationStore, FundNotification } from '@/store/useNotificationStore';
import request from '@/utils/request';


const { Header, Sider, Content } = Layout;
const { Title, Text } = Typography;

const PiggyBankIcon = (props: any) => (
  <svg
    viewBox="0 0 24 24"
    width="1.15em"
    height="1.15em"
    fill="none"
    stroke="currentColor"
    strokeWidth="2.1"
    strokeLinecap="round"
    strokeLinejoin="round"
    style={{ verticalAlign: 'middle', ...props.style }}
  >
    <path d="M19 5c-1.5 0-2.8 1.4-3 2-2.2-1.9-6.8-1.9-9 0-.2-.6-1.5-2-3-2C2.5 5 2 7 2 9c0 4.4 3.6 8 8 8h4c4.4 0 8-3.6 8-8 0-2-.5-4-2-4Z" />
    <path d="M12 17v3" />
    <path d="M8 17v3" />
    <path d="M16 17v3" />
    <circle cx="9.5" cy="11" r="1.2" fill="currentColor" />
    <circle cx="14.5" cy="11" r="1.2" fill="currentColor" />
    <path d="M22 9h-2" />
  </svg>
);

export default function BasicLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();
  const notifications = useNotificationStore((s) => s.notifications);
  const unreadCount = useNotificationStore((s) => s.unreadCount);
  const markAsRead = useNotificationStore((s) => s.markAsRead);
  const removeNotification = useNotificationStore((s) => s.removeNotification);
  const addNotification = useNotificationStore((s) => s.addNotification);

  const [rejectModalVisible, setRejectModalVisible] = useState(false);
  const [rejectingItem, setRejectingItem] = useState<FundNotification | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  const handleApproveRequest = async (req: FundNotification, action: 'approved' | 'rejected', reason?: string) => {
    try {
      const data = await request.post('/funds/approve-transaction', {
        requestId: req.id,
        action,
        rejectReason: reason
      });
      if (data.success) {
        if (action === 'approved') {
          message.success(`Đã duyệt thành công!`);
          window.dispatchEvent(new Event('transaction-approved'));
        } else {
          message.info(`Đã từ chối yêu cầu.`);
          addNotification({
            id: `rej_${Date.now()}`,
            type: req.type === 'DEPOSIT_REQUEST' ? 'DEPOSIT_REJECTED' : 'WITHDRAW_REJECTED',
            fundId: req.fundId,
            fundName: req.fundName,
            amount: req.amount,
            description: reason ? `Lý do: ${reason}` : 'Trưởng nhóm không nêu lý do',
            requesterName: req.requesterName,
            date: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
            read: false,
            targetRole: 'MEMBER'
          });
        }

        removeNotification(req.id);
      }
    } catch {
      message.error('Lỗi xử lý duyệt yêu cầu!');
    }
  };

  const menuItems: MenuProps['items'] = [
    {
      key: '/dashboard',
      icon: location.pathname === '/dashboard' ? <AppstoreFilled /> : <AppstoreOutlined />,
      label: <Link to="/dashboard">Tổng quan</Link>,
    },
    {
      key: '/saving-goals',
      icon: location.pathname === '/saving-goals' ? <FlagFilled /> : <FlagOutlined />,
      label: <Link to="/saving-goals">Mục tiêu</Link>,
    },
    {
      key: '/personal-funds',
      icon: <PiggyBankIcon />,
      label: <Link to="/personal-funds">Quỹ cá nhân</Link>,
    },
    {
      key: '/transactions',
      icon: <DollarOutlined />,
      label: <Link to="/transactions">Quản lý giao dịch</Link>,
    },
    {
      key: '/funds',
      icon: <BankOutlined />,
      label: <Link to="/funds">Quản lý quỹ nhóm</Link>,
    },
  ];

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Hồ sơ cá nhân',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Đăng xuất',
      danger: true,
      onClick: () => {
        window.location.href = '/auth/login';
      }
    },
  ];

  return (
    <Layout className={styles.layout}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={(value) => setCollapsed(value)}
        theme="light"
        width={260}
        className={styles.sider}
      >
        <div className={styles.logoContainer}>
          <div className={styles.logoIcon}>
            <BankOutlined />
          </div>
          {!collapsed && (
            <div className={styles.logoText}>
              <div className={styles.brandTitle}>Smart Finance <span className={styles.brandAi}></span></div>
              <div className={styles.brandSubtitle}>Precision Intelligence</div>

            </div>
          )}
        </div>
        <div style={{ padding: '0 12px' }}>
          <Menu
            theme="light"
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            className={styles.menu}
          />
        </div>
      </Sider>
      <Layout className={styles.mainLayout}>
        <Header className={styles.header}>
          <div className={styles.headerLeft}>
            <div className={styles.searchWrapper}>
              <Input
                prefix={<SearchOutlined style={{ color: '#8c8c8c' }} />}
                placeholder="Tìm kiếm giao dịch, mục tiêu hoặc gợi ý..."
                className={styles.searchBar}
              />
            </div>
          </div>

          <Space size="large" align="center" className={styles.rightActions}>
            <Popover
              trigger="click"
              placement="bottomRight"
              title={<span style={{ fontWeight: 700 }}>Thông báo</span>}
              content={
                <div style={{ width: 380, maxHeight: 400, overflowY: 'auto' }}>
                  {notifications.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: 24, color: '#8c98a5' }}>
                      Không có thông báo
                    </div>
                  ) : (
                    <List
                      dataSource={notifications}
                      renderItem={(item: FundNotification) => {
                        const isRequest = item.type === 'DEPOSIT_REQUEST' || item.type === 'WITHDRAW_REQUEST';
                        return (
                          <List.Item 
                            style={{ 
                              padding: '12px 16px', 
                              borderBottom: '1px solid #f1f3f4',
                              background: item.read ? '#fff' : '#f0f7ff',
                              cursor: 'pointer'
                            }}
                            onClick={() => {
                              if (!item.read) markAsRead(item.id);
                            }}
                          >
                            <div style={{ width: '100%' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                                <Tag color={item.type === 'SYSTEM_INFO' ? 'default' : item.type.includes('REQUEST') ? 'processing' : item.type.includes('APPROVED') ? 'success' : 'error'} style={{ borderRadius: 8, fontSize: 11, margin: 0 }}>
                                  {item.type === 'DEPOSIT_REQUEST' && '💰 Yêu cầu nạp tiền'}
                                  {item.type === 'WITHDRAW_REQUEST' && '💸 Yêu cầu rút tiền'}
                                  {item.type === 'DEPOSIT_APPROVED' && '✅ Nạp tiền được duyệt'}
                                  {item.type === 'DEPOSIT_REJECTED' && '❌ Nạp tiền bị từ chối'}
                                  {item.type === 'WITHDRAW_APPROVED' && '✅ Rút tiền được duyệt'}
                                  {item.type === 'WITHDRAW_REJECTED' && '❌ Rút tiền bị từ chối'}
                                  {item.type === 'SYSTEM_INFO' && 'ℹ️ Hệ thống'}
                                </Tag>
                                <span style={{ fontSize: 11, color: '#8c98a5' }}>{item.date}</span>
                              </div>
                              <div style={{ fontSize: 13, fontWeight: item.read ? 600 : 700, color: '#202124' }}>
                                {item.requesterName} {item.fundName ? `• Quỹ "${item.fundName}"` : ''}
                              </div>
                              {item.type !== 'SYSTEM_INFO' && (
                                <div style={{ fontSize: 13, color: '#5f6368', fontWeight: 500 }}>
                                  Số tiền: <span style={{ color: '#1890ff' }}>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.amount)}</span>
                                </div>
                              )}
                              {item.description && (
                                <div style={{ fontSize: 12, color: '#8c98a5', marginTop: 2 }}>
                                  {item.type === 'SYSTEM_INFO' ? item.description : (item.type.includes('WITHDRAW') ? `Lý do: ${item.description}` : `Nội dung: ${item.description}`)}
                                </div>
                              )}
                              {item.bankAccount && (
                                <div style={{ fontSize: 12, color: '#8c98a5' }}>
                                  TK: {item.bankAccount} - {item.bankName}
                                </div>
                              )}

                              {!item.read && isRequest && (
                                <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
                                  <Button 
                                    type="primary" 
                                    size="small" 
                                    icon={<CheckCircleOutlined />} 
                                    onClick={(e) => { e.stopPropagation(); handleApproveRequest(item, 'approved'); }}
                                  >
                                    Duyệt
                                  </Button>
                                  <Button 
                                    danger 
                                    size="small" 
                                    icon={<CloseCircleOutlined />} 
                                    onClick={(e) => { 
                                      e.stopPropagation(); 
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
              <Badge count={unreadCount()} size="small" offset={[-2, 2]}>
                <BellOutlined className={styles.bellIcon} />
              </Badge>
            </Popover>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight" arrow>
              <Space align="center" className={styles.avatarWrapper}>
                <Avatar size={40} className={styles.avatar} src="https://api.dicebear.com/7.x/notionists/svg?seed=Admin" />
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content className={styles.content}>
          <Outlet />
        </Content>
      </Layout>

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
    </Layout>
  );
}
