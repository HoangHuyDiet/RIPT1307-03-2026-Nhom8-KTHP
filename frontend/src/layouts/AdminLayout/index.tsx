import React, { useState } from 'react';
import { Link, Outlet, useLocation } from 'umi';
import { Layout, Menu, Button, Typography, Space } from 'antd';
import type { MenuProps } from 'antd';
import {
  TeamOutlined,
  LogoutOutlined,
  SafetyOutlined,
  BankOutlined,
} from '@ant-design/icons';
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
  ];

  const handleLogout = () => {
    window.location.href = '/auth/login';
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
              <Text className={styles.adminBadgeText}>Quyền Admin</Text>
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
