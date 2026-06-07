import React, { useState, useEffect } from 'react';
import { history, Outlet, useLocation, Link } from 'umi';
import { Layout, Menu, Button, Typography, Tag, message } from 'antd';
import {
  SafetyOutlined,
  LogoutOutlined,
  CustomerServiceOutlined,
  MessageOutlined,
  UserOutlined,
  BellOutlined,
  SettingOutlined,
  SolutionOutlined
} from '@ant-design/icons';
import { useAuthStore } from '@/store/useAuthStore';
import styles from './index.less';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

export default function SupportAdminLayout() {
  const { isAdmin, hasAdminAccess, logout, user } = useAuthStore();
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();

  // Get activeKey from location.pathname, e.g. /supportadmin/accounts -> accounts
  let activeKey = 'chat';
  if (location.pathname.includes('/accounts')) {
    activeKey = 'accounts';
  } else if (location.pathname.includes('/broadcast')) {
    activeKey = 'broadcast';
  } else if (location.pathname.includes('/tickets')) {
    activeKey = 'tickets';
  }

  useEffect(() => {
    if (!hasAdminAccess()) {
      message.error('Bạn không có quyền truy cập trang Support Admin');
      history.push('/dashboard');
    }
  }, []);

  const handleLogout = () => {
    logout();
    history.push('/auth/login');
  };

  const menuItems: any[] = [
    {
      key: '/supportadmin/chat',
      icon: <MessageOutlined />,
      label: <Link to="/supportadmin/chat">Hỗ trợ VIP</Link>,
    },
    {
      key: '/supportadmin/tickets',
      icon: <SolutionOutlined />,
      label: <Link to="/supportadmin/tickets">Hỗ trợ User</Link>,
    },
    {
      key: '/supportadmin/accounts',
      icon: <UserOutlined />,
      label: <Link to="/supportadmin/accounts">Tài khoản</Link>,
    },
    {
      key: '/supportadmin/broadcast',
      icon: <BellOutlined />,
      label: <Link to="/supportadmin/broadcast">Thông báo</Link>,
    },
    {
      type: 'divider',
    },
    ...(isAdmin() ? [
      {
        key: '/admin/users',
        icon: <SafetyOutlined />,
        label: <Link to="/admin/users">Về Admin</Link>,
      }
    ] : []),
    {
      key: '/dashboard',
      icon: <SettingOutlined />,
      label: <Link to="/dashboard">Về giao diện User</Link>,
    },
  ];

  const MODULE_DETAILS: Record<string, { title: string; subtitle: string }> = {
    chat: {
      title: 'Hỗ trợ VIP',
      subtitle: 'Kênh hỗ trợ và chat trực tuyến dành cho khách hàng VIP'
    },
    tickets: {
      title: 'Hỗ trợ User thường',
      subtitle: 'Quản lý, phân loại và giải quyết các yêu cầu hỗ trợ từ người dùng thông thường'
    },
    accounts: {
      title: 'Tài khoản',
      subtitle: 'Kiểm tra thông tin chi tiết, khóa/mở tài khoản và xem nhật ký hoạt động'
    },
    broadcast: {
      title: 'Thông báo',
      subtitle: 'Gửi thông báo toàn hệ thống hoặc gửi đích danh cho từng tài khoản người dùng'
    }
  };

  return (
    <Layout className={styles.layout}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={(value) => setCollapsed(value)}
        theme="light"
        width={240}
        className={styles.sider}
      >
        <div className={styles.logoContainer}>
          <div className={styles.logoIcon}>
            <CustomerServiceOutlined />
          </div>
          {!collapsed && (
            <div className={styles.logoText}>
              <div className={styles.brandTitle}>Support Panel</div>
              <div className={styles.brandSubtitle}>Smart Finance</div>
            </div>
          )}
        </div>

        <div className={styles.menuSection}>
          {!collapsed && <div className={styles.menuLabel}>VẬN HÀNH SUPPORTS</div>}
          <Menu
            theme="light"
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            className={styles.menu}
          />
        </div>

        {!collapsed && (
          <div className={styles.siderFooter}>
            <div className={styles.adminBadge}>
              <SafetyOutlined style={{ fontSize: 12 }} />
              <Text className={styles.adminBadgeText}>
                Quyền Support Admin
              </Text>
            </div>
          </div>
        )}
      </Sider>

      <Layout className={styles.mainLayout}>
        <Header className={styles.header}>
          <div className={styles.headerLeft}>
            <div className={styles.pageTitleWrapper}>
              <div className={styles.pageTitleTextGroup}>
                <div className={styles.pageTitle}>{MODULE_DETAILS[activeKey]?.title}</div>
                <div className={styles.pageSubtitle}>{MODULE_DETAILS[activeKey]?.subtitle}</div>
              </div>
            </div>
          </div>
          <div className={styles.headerRight}>
            <Tag color="orange" className={styles.roleTag} style={{ marginRight: 12 }}>SUPPORT ADMIN</Tag>
            <Text style={{ marginRight: 12, fontWeight: 500 }}>{user?.name || 'Support Admin'}</Text>
            <Button
              danger
              icon={<LogoutOutlined />}
              onClick={handleLogout}
              className={styles.logoutBtn}
            >
              Đăng xuất
            </Button>
          </div>
        </Header>

        <Content className={styles.content}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
