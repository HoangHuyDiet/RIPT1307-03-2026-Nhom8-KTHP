import React, { useState, useEffect } from 'react';
import { Link, Outlet, useLocation, history } from 'umi';
import { Layout, Menu, Button, Typography, Space, Tag, message } from 'antd';
import type { MenuProps } from 'antd';
import {
  TeamOutlined,
  LogoutOutlined,
  SafetyOutlined,
  BankOutlined,
  DashboardOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '../../store/useAuthStore';
import styles from './index.less';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

const PAGE_TITLES: Record<string, { title: string; icon: React.ReactNode; subtitle: string }> = {
  '/admin/users': {
    title: 'Quản lý Người dùng',
    icon: <TeamOutlined />,
    subtitle: 'Danh sách & phân quyền tài khoản',
  },
};

export default function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();
  const { hasAdminAccess, isAdmin, isSupportAdmin, user, logout } = useAuthStore();

  useEffect(() => {
    if (!hasAdminAccess()) {
      message.error('Bạn không có quyền truy cập trang quản trị');
      history.push('/dashboard');
    }
  }, []);

  const currentPage = PAGE_TITLES[location.pathname] ?? {
    title: 'Quản trị hệ thống',
    icon: <SafetyOutlined />,
    subtitle: '',
  };

  const menuItems: MenuProps['items'] = [
    {
      key: '/admin/users',
      icon: <TeamOutlined />,
      label: <Link to="/admin/users">Người dùng</Link>,
    },
    {
      type: 'divider',
    },
    {
      key: 'back-to-user',
      icon: <SettingOutlined />,
      label: <Link to="/dashboard">Về giao diện User</Link>,
    },
  ];

  const handleLogout = () => {
    logout();
    history.push('/auth/login');
  };

  // Xác định role hiển thị
  const roleBadge = isAdmin() ? 'ADMIN' : isSupportAdmin() ? 'SUPPORT' : '';
  const roleColor = isAdmin() ? 'red' : 'orange';

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
            <BankOutlined />
          </div>
          {!collapsed && (
            <div className={styles.logoText}>
              <div className={styles.brandTitle}>Admin Panel</div>
              <div className={styles.brandSubtitle}>Smart Finance</div>
            </div>
          )}
        </div>

        <div className={styles.menuSection}>
          {!collapsed && <div className={styles.menuLabel}>Quản trị</div>}
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
                Quyền {isAdmin() ? 'Admin' : 'Support Admin'}
              </Text>
            </div>
          </div>
        )}
      </Sider>

      <Layout>
        <Header className={styles.header}>
          <div className={styles.headerLeft}>
            <div className={styles.pageTitleWrapper}>
              <div className={styles.pageTitleTextGroup}>
                <div className={styles.pageTitle}>{currentPage.title}</div>
                {currentPage.subtitle && (
                  <div className={styles.pageSubtitle}>{currentPage.subtitle}</div>
                )}
              </div>
            </div>
          </div>
          <div className={styles.headerRight}>
            <Tag color={roleColor} style={{ fontWeight: 600, marginRight: 12 }}>
              {roleBadge}
            </Tag>
            <Text style={{ marginRight: 12, fontWeight: 500 }}>{user?.name || 'Admin'}</Text>
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

