import React, { useState, useEffect } from 'react';
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
  Modal,
  Form,
  Select,
  Tabs,
} from 'antd';
import {
  PlusOutlined,
  LockOutlined,
  UnlockOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  StopOutlined,
  UserOutlined,
  ExclamationCircleOutlined,
  CrownOutlined,
  CustomerServiceOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '../../../store/useAuthStore';
import api from '../../../utils/api';
import styles from './index.less';

const { Text } = Typography;

export interface User {
  id: number;
  email: string;
  displayName: string;
  roles: string[];
  status: 'ACTIVE' | 'INACTIVE' | 'BANNED';
  createdAt: string;
}

export default function UserManagement() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState<string>('');
  
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [editingUserId, setEditingUserId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const { isAdmin } = useAuthStore();

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/users');
      setUsers(res.data.data || []);
    } catch (error: any) {
      message.error('Không thể tải danh sách người dùng');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);



  const handleToggleLock = (user: User) => {
    const newStatus = user.status === 'BANNED' ? 'ACTIVE' : 'BANNED';
    const actionText = newStatus === 'BANNED' ? 'khóa' : 'mở khóa';

    Modal.confirm({
      title: `Xác nhận ${actionText} tài khoản`,
      icon: <ExclamationCircleOutlined />,
      content: `Bạn có chắc muốn ${actionText} tài khoản "${user.email}"?`,
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      okButtonProps: { danger: newStatus === 'BANNED' },
      onOk: async () => {
        try {
          await api.put(`/admin/users/${user.id}/status`, { status: newStatus });
          message.success(`Đã ${actionText} tài khoản ${user.email}`);
          fetchUsers();
        } catch (error: any) {
          message.error(error.response?.data?.message || `Không thể ${actionText} tài khoản`);
        }
      },
    });
  };

  const handleDeleteUser = (user: User) => {
    Modal.confirm({
      title: 'Xác nhận xóa tài khoản',
      icon: <ExclamationCircleOutlined style={{ color: 'red' }} />,
      content: `Bạn có chắc chắn muốn xóa vĩnh viễn tài khoản "${user.email}"? Hành động này không thể hoàn tác.`,
      okText: 'Xóa vĩnh viễn',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await api.delete(`/admin/users/${user.id}`);
          message.success(`Đã xóa tài khoản ${user.email}`);
          fetchUsers();
        } catch (error: any) {
          message.error(error.response?.data?.message || 'Không thể xóa tài khoản này');
        }
      },
    });
  };



  const openCreateModal = () => {
    setModalMode('create');
    setEditingUserId(null);
    form.resetFields();
    form.setFieldsValue({
      status: 'ACTIVE',
      roles: ['USER'],
    });
    setIsModalVisible(true);
  };

  const openEditModal = (user: User) => {
    setModalMode('edit');
    setEditingUserId(user.id);
    form.resetFields();
    form.setFieldsValue({
      email: user.email,
      displayName: user.displayName,
      status: user.status,
      roles: user.roles,
    });
    setIsModalVisible(true);
  };

  const handleModalClose = () => {
    setIsModalVisible(false);
    form.resetFields();
  };

  const handleModalSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);

      if (modalMode === 'create') {
        await api.post('/admin/users', values);
        message.success('Đã tạo tài khoản thành công');
      } else if (modalMode === 'edit' && editingUserId) {
        await api.put(`/admin/users/${editingUserId}`, values);
        message.success('Đã cập nhật thông tin tài khoản');
      }

      setIsModalVisible(false);
      fetchUsers();
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.response?.data?.message || 'Đã xảy ra lỗi, vui lòng thử lại');
    } finally {
      setSubmitting(false);
    }
  };



  const handleSearch = (value: string) => {
    setSearchText(value);
  };

  const filteredUsers = searchText
    ? users.filter(
        (u) =>
          u.email.toLowerCase().includes(searchText.toLowerCase()) ||
          u.displayName.toLowerCase().includes(searchText.toLowerCase())
      )
    : users;

  const totalUsers = users.length;
  const activeUsers = users.filter((u) => u.status === 'ACTIVE').length;
  const bannedUsers = users.filter((u) => u.status === 'BANNED').length;
  const adminUsers = users.filter((u) => u.roles?.includes('ADMIN') || u.roles?.includes('SUPPORT_ADMIN')).length;

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
      dataIndex: 'roles',
      key: 'roles',
      width: 180,
      render: (roles: string[]) => (
        <Space>
          {(roles || []).map((role) => {
            let color = 'default';
            let icon = <UserOutlined />;
            if (role === 'ADMIN') { color = 'red'; icon = <CrownOutlined />; }
            else if (role === 'SUPPORT_ADMIN') { color = 'orange'; icon = <CustomerServiceOutlined />; }
            else if (role === 'USER') { color = 'blue'; icon = <UserOutlined />; }
            return (
              <Tag key={role} color={color} icon={icon}>
                {role}
              </Tag>
            );
          })}
        </Space>
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
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (date: string) => date ? new Date(date).toLocaleDateString('vi-VN') : '-',
    },
    {
      title: 'Hành động',
      key: 'action',
      width: 250,
      render: (_: any, record: User) => {
        if (!isAdmin()) {
          return <Text type="secondary">Chỉ xem</Text>;
        }

        const isBanned = record.status === 'BANNED';
        const isSuperAdmin = record.roles?.includes('ADMIN');

        return (
          <Space>
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={() => openEditModal(record)}
              className={styles.editBtn}
            >
              Sửa
            </Button>
            
            {!isSuperAdmin ? (
              <>
                <Button
                  type="text"
                  danger={!isBanned}
                  size="small"
                  icon={isBanned ? <UnlockOutlined /> : <LockOutlined />}
                  onClick={() => handleToggleLock(record)}
                  className={isBanned ? styles.unlockBtn : styles.lockBtn}
                >
                  {isBanned ? 'Mở' : 'Khóa'}
                </Button>
                <Button
                  type="text"
                  danger
                  size="small"
                  icon={<DeleteOutlined />}
                  onClick={() => handleDeleteUser(record)}
                  className={styles.deleteBtn}
                >
                  Xóa
                </Button>
              </>
            ) : (
              <Text type="secondary" style={{ fontSize: '12px' }}>Không thể thao tác Admin</Text>
            )}
          </Space>
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
              icon={<ReloadOutlined />}
              onClick={fetchUsers}
              loading={loading}
              style={{ marginRight: 8 }}
            >
              Làm mới
            </Button>
            {isAdmin() && (
              <Button
                type="primary"
                icon={<PlusOutlined />}
                className={styles.addBtn}
                onClick={openCreateModal}
              >
                Thêm tài khoản
              </Button>
            )}
          </div>
        </div>

        <Table
          columns={columns}
          dataSource={filteredUsers}
          rowKey="id"
          loading={loading}
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

      <Modal
        title={modalMode === 'create' ? 'Tạo tài khoản mới' : 'Cập nhật tài khoản'}
        open={isModalVisible}
        onOk={handleModalSubmit}
        onCancel={handleModalClose}
        confirmLoading={submitting}
        okText={modalMode === 'create' ? 'Tạo mới' : 'Lưu thay đổi'}
        cancelText="Hủy"
        width={500}
      >
        <Form
          form={form}
          layout="vertical"
          style={{ marginTop: 24 }}
        >
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: 'Vui lòng nhập email!' },
              { type: 'email', message: 'Email không đúng định dạng!' }
            ]}
          >
            <Input placeholder="Nhập địa chỉ email" />
          </Form.Item>

          <Form.Item
            name="displayName"
            label="Tên hiển thị"
            rules={[{ required: true, message: 'Vui lòng nhập tên hiển thị!' }]}
          >
            <Input placeholder="Nhập tên người dùng" />
          </Form.Item>

          <Form.Item
            name="password"
            label="Mật khẩu"
            rules={[
              { required: modalMode === 'create', message: 'Vui lòng nhập mật khẩu!' },
              { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự!' }
            ]}
          >
            <Input.Password placeholder={modalMode === 'create' ? "Nhập mật khẩu" : "Nhập mật khẩu mới (bỏ trống nếu không đổi)"} />
          </Form.Item>

          <Form.Item
            name="status"
            label="Trạng thái"
            rules={[{ required: true, message: 'Vui lòng chọn trạng thái!' }]}
          >
            <Select>
              <Select.Option value="ACTIVE">ACTIVE</Select.Option>
              <Select.Option value="INACTIVE">INACTIVE</Select.Option>
              <Select.Option value="BANNED">BANNED</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="roles"
            label="Vai trò (Roles)"
            rules={[{ required: true, message: 'Vui lòng chọn ít nhất 1 vai trò!' }]}
          >
            <Select mode="multiple" placeholder="Chọn vai trò">
              <Select.Option value="USER">USER</Select.Option>
              <Select.Option value="SUPPORT_ADMIN">SUPPORT_ADMIN</Select.Option>
              <Select.Option value="ADMIN">ADMIN</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

