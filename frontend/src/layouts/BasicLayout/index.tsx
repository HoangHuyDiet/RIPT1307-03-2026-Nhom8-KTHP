import React, { useState } from 'react';
import { Link, Outlet, useLocation } from 'umi';
import { Layout, Menu, Avatar, Dropdown, Space, Input } from 'antd';
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
  DollarOutlined,
} from '@ant-design/icons';
import styles from './index.less';
import NotificationsPopover from '../NotificationsLayout';

const { Header, Sider, Content } = Layout;

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
      key: 'notifications',
      icon: <BellOutlined />,
      label: <Link to="/notifications">Trung tâm thông báo</Link>,
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
            <NotificationsPopover />
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
    </Layout>
  );
}
