import React, { useState } from 'react';
import {
  Card,
  Table,
  Input,
  Button,
  Tag,
  Badge,
  Typography,
  Space,
  message,
  Statistic,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  LockOutlined,
  UnlockOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  StopOutlined,
  UserOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Text } = Typography;

export interface User {
  id: number;
  email: string;
  displayName: string;
  role: 'ADMIN' | 'USER';
  status: 'ACTIVE' | 'INACTIVE' | 'BANNED';
}

export default function UserManagement() {
  const [users, setUsers] = useState<User[]>([]);
  const [searchText, setSearchText] = useState<string>('');

  const handleToggleLock = (userId: number) => {
    setUsers((prevUsers) =>
      prevUsers.map((user) => {
        if (user.id === userId) {
          const nextStatus = user.status === 'BANNED' ? 'ACTIVE' : 'BANNED';
          return { ...user, status: nextStatus };
        }
        return user;
      })
    );
    message.success('Đã cập nhật trạng thái');
  };

  const handleSearch = (value: string) => {
    setSearchText(value);
  };

  const totalUsers = users.length;
  const activeUsers = users.filter((u) => u.status === 'ACTIVE').length;
  const bannedUsers = users.filter((u) => u.status === 'BANNED').length;
  const adminUsers = users.filter((u) => u.role === 'ADMIN').length;

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 70,
      sorter: (a: User, b: User) => a.id - b.id,
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Tên hiển thị',
      dataIndex: 'displayName',
      key: 'displayName',
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      key: 'role',
      width: 110,
      render: (role: 'ADMIN' | 'USER') =>
        role === 'ADMIN' ? (
          <Tag color="blue" className={styles.roleTag}>{role}</Tag>
        ) : (
          <Tag className={styles.roleTagDefault}>{role}</Tag>
        ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 130,
      render: (status: 'ACTIVE' | 'INACTIVE' | 'BANNED') => {
        switch (status) {
          case 'ACTIVE':
            return <Badge status="success" text="ACTIVE" />;
          case 'INACTIVE':
            return <Badge status="default" text="INACTIVE" />;
          case 'BANNED':
            return <Badge status="error" text="BANNED" />;
          default:
            return <Badge status="default" text={status} />;
        }
      },
    },
    {
      title: 'Hành động',
      key: 'action',
      width: 130,
      render: (_: any, record: User) => {
        const isBanned = record.status === 'BANNED';
        return (
          <Button
            type="link"
            danger={!isBanned}
            size="small"
            icon={isBanned ? <UnlockOutlined /> : <LockOutlined />}
            onClick={() => handleToggleLock(record.id)}
            className={isBanned ? styles.unlockBtn : styles.lockBtn}
          >
            {isBanned ? 'Mở khóa' : 'Khóa'}
          </Button>
        );
      },
    },
  ];

  return (
    <div className={styles.container}>
      <Row gutter={[16, 16]} className={styles.statsRow}>
        <Col xs={12} sm={6}>
          <Card bordered={false} className={styles.statCard}>
            <Statistic
              title={<Text className={styles.statLabel}>Tổng người dùng</Text>}
              value={totalUsers}
              prefix={<TeamOutlined className={styles.statIconBlue} />}
              valueStyle={{ color: '#1A73E8', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} className={styles.statCard}>
            <Statistic
              title={<Text className={styles.statLabel}>Đang hoạt động</Text>}
              value={activeUsers}
              prefix={<CheckCircleOutlined className={styles.statIconGreen} />}
              valueStyle={{ color: '#34A853', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} className={styles.statCard}>
            <Statistic
              title={<Text className={styles.statLabel}>Bị khóa</Text>}
              value={bannedUsers}
              prefix={<StopOutlined className={styles.statIconRed} />}
              valueStyle={{ color: '#EA4335', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} className={styles.statCard}>
            <Statistic
              title={<Text className={styles.statLabel}>Quản trị viên</Text>}
              value={adminUsers}
              prefix={<UserOutlined className={styles.statIconOrange} />}
              valueStyle={{ color: '#F9AB00', fontWeight: 700 }}
            />
          </Card>
        </Col>
      </Row>

      <Card bordered={false} className={styles.tableCard}>
        <div className={styles.toolbar}>
          <div className={styles.toolbarLeft}>
            <Input.Search
              placeholder="Tìm kiếm email, tên..."
              allowClear
              onSearch={handleSearch}
              className={styles.searchBar}
            />
          </div>
          <div className={styles.toolbarRight}>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              className={styles.addBtn}
              onClick={() => message.info('Chức năng thêm tài khoản sẽ được tích hợp sau')}
            >
              Thêm tài khoản
            </Button>
          </div>
        </div>

        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          className={styles.table}
          pagination={{
            defaultPageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Tổng số ${total} tài khoản`,
          }}
          locale={{
            emptyText: (
              <div className={styles.emptyState}>
                <TeamOutlined className={styles.emptyIcon} />
                <div className={styles.emptyText}>Chưa có dữ liệu người dùng</div>
                <div className={styles.emptyHint}>Kết nối API để hiển thị danh sách</div>
              </div>
            ),
          }}
        />
      </Card>
    </div>
  );
}
