import React, { useState, useEffect } from 'react';
import {
  Card,
  Input,
  Avatar,
  Tag,
  Badge,
  Typography,
  Space,
  Button,
  Popconfirm,
  Switch,
  Table,
  Modal,
  Form,
  message
} from 'antd';
import {
  UserOutlined,
  InfoCircleOutlined,
  DeleteOutlined
} from '@ant-design/icons';
import api from '@/utils/api';
import styles from './index.less';

interface UserAccount {
  id: number;
  email: string;
  name: string;
  status: 'ACTIVE' | 'BANNED';
  role: string;
  balance: number;
  createdAt: string;
}

interface AuditLog {
  key: string;
  time: string;
  action: string;
  targetUser: string;
  ip: string;
  status: string;
}

const { Title, Text } = Typography;

export default function AccountControl() {
  const [users, setUsers] = useState<UserAccount[]>([]);
  const [loading, setLoading] = useState(true);
  const [userSearch, setUserSearch] = useState('');
  const [selectedUser, setSelectedUser] = useState<UserAccount | null>(null);
  const [lockModalVisible, setLockModalVisible] = useState(false);
  const [lockReason, setLockReason] = useState('');
  const [lockUser, setLockUser] = useState<UserAccount | null>(null);
  const [lockRequests, setLockRequests] = useState<any[]>([]);
  const [loadingRequests, setLoadingRequests] = useState(false);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [auditLogsLoading, setAuditLogsLoading] = useState(false);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/support/users');
      setUsers(res.data.data || []);
    } catch (error: any) {
      message.error('Không thể tải danh sách tài khoản');
    } finally {
      setLoading(false);
    }
  };

  const fetchLockRequests = async () => {
    setLoadingRequests(true);
    try {
      const res = await api.get('/support/lock-requests');
      setLockRequests(res.data.data || []);
    } catch (error: any) {
      message.error('Không thể tải lịch sử yêu cầu khóa');
    } finally {
      setLoadingRequests(false);
    }
  };

  const handleDeleteLockRequest = async (id: number) => {
    try {
      await api.delete(`/support/lock-requests/${id}`);
      message.success('Xóa yêu cầu thành công!');
      fetchLockRequests();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Xóa yêu cầu thất bại');
    }
  };

  const fetchAuditLogs = async (email: string) => {
    setAuditLogsLoading(true);
    try {
      const res = await api.get('/support/audit-logs', { params: { email } });
      setAuditLogs(res.data.data?.[email] || res.data.data || []);
    } catch (error: any) {
      message.error('Không thể tải lịch sử hoạt động');
    } finally {
      setAuditLogsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
    fetchLockRequests();
  }, []);

  useEffect(() => {
    if (selectedUser) {
      fetchAuditLogs(selectedUser.email);
    } else {
      setAuditLogs([]);
    }
  }, [selectedUser]);

  const handleSendLockRequest = async () => {
    if (!lockReason.trim()) {
      message.error('Vui lòng nhập lý do khóa tài khoản!');
      return;
    }

    if (lockUser) {
      try {
        await api.post('/support/users/lock-request', {
          email: lockUser.email,
          reason: lockReason
        });
        message.success(`Đã gửi yêu cầu khóa tài khoản ${lockUser.email} lên Admin phê duyệt!`);
        await fetchLockRequests();
        if (selectedUser) fetchAuditLogs(selectedUser.email);
      } catch (error: any) {
        message.error(error.response?.data?.message || 'Gửi yêu cầu khóa thất bại');
      }
    }

    setLockModalVisible(false);
    setLockReason('');
    setLockUser(null);
  };

  const handleUserSearch = (value: string) => {
    const found = users.find(u =>
      u.email.toLowerCase().includes(value.toLowerCase()) ||
      u.name.toLowerCase().includes(value.toLowerCase())
    );
    if (found) {
      setSelectedUser(found);
    } else {
      message.error('Không tìm thấy tài khoản người dùng tương ứng.');
    }
  };

  const handleToggleUserStatus = async (checked: boolean, user: UserAccount) => {
    try {
      await api.post('/support/users/toggle-status', {
        email: user.email,
        checked
      });
      message.success(`Đã cập nhật trạng thái tài khoản ${user.email} thành công!`);
      await fetchUsers();

      const newStatus: 'ACTIVE' | 'BANNED' = checked ? 'ACTIVE' : 'BANNED';
      if (selectedUser && selectedUser.id === user.id) {
        const updated = { ...selectedUser, status: newStatus };
        setSelectedUser(updated);
        fetchAuditLogs(updated.email);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Cập nhật trạng thái thất bại');
    }
  };

  return (
    <Card className={styles.tableCard}>
      <div className={styles.searchSection}>
        <Input.Search
          placeholder="Tìm tài khoản theo email hoặc tên..."
          allowClear
          onSearch={handleUserSearch}
          value={userSearch}
          onChange={e => setUserSearch(e.target.value)}
          className={styles.searchBar}
          enterButton
          loading={loading}
        />
      </div>

      <div style={{ marginBottom: 24 }}>
        <div className={styles.tableTitleSection} style={{ marginBottom: 12 }}>
          <Title level={5}>Lịch sử yêu cầu khóa tài khoản</Title>
        </div>
        <Table
          dataSource={lockRequests}
          rowKey="id"
          loading={loadingRequests}
          columns={[
            {
              title: 'Mã yêu cầu',
              dataIndex: 'id',
              key: 'id',
              width: 120,
              render: (id: number) => <Text strong>#REQ-{id}</Text>
            },
            {
              title: 'Tài khoản',
              key: 'user',
              width: 220,
              render: (_: any, record: any) => (
                <div>
                  <div style={{ fontWeight: 600 }}>{record.name}</div>
                  <Text type="secondary" style={{ fontSize: 12 }}>{record.email}</Text>
                </div>
              )
            },
            {
              title: 'Lý do khóa',
              dataIndex: 'reason',
              key: 'reason',
              render: (reason: string) => (
                <span style={{ fontSize: '13px' }}>{reason}</span>
              )
            },
            {
              title: 'Thời gian',
              dataIndex: 'time',
              key: 'time',
              width: 150,
            },
            {
              title: 'Trạng thái',
              dataIndex: 'status',
              key: 'status',
              width: 130,
              render: (status: 'PENDING' | 'APPROVED' | 'REJECTED') => {
                const config = {
                  PENDING: { color: 'warning', text: 'Chờ duyệt' },
                  APPROVED: { color: 'error', text: 'Đã duyệt khóa' },
                  REJECTED: { color: 'default', text: 'Từ chối' },
                };
                return <Badge status={config[status].color as any} text={config[status].text} />;
              }
            },
            {
              title: 'Hành động',
              key: 'action',
              width: 100,
              render: (_: any, record: any) => (
                <Popconfirm
                  title="Xóa lịch sử yêu cầu này?"
                  onConfirm={() => handleDeleteLockRequest(record.id)}
                  okText="Xóa"
                  cancelText="Hủy"
                  disabled={record.status === 'PENDING'}
                >
                  <Button
                    type="text"
                    danger
                    icon={<DeleteOutlined />}
                    disabled={record.status === 'PENDING'}
                  />
                </Popconfirm>
              )
            }
          ]}
          pagination={{
            pageSize: 5,
            showTotal: (total) => `Tổng số ${total} yêu cầu`,
          }}
          className={styles.table}
        />
      </div>

      {selectedUser ? (
        <div className={styles.userDetailSection}>
          <Card className={styles.userProfileCard}>
            <div className={styles.userProfileGrid}>
              <div className={styles.profileLeft}>
                <Avatar size={64} icon={<UserOutlined />} className={styles.largeAvatar} />
                <div>
                  <Title level={4} style={{ margin: 0 }}>{selectedUser.name}</Title>
                  <Text type="secondary">{selectedUser.email}</Text>
                  <div style={{ marginTop: 8 }}>
                    <Tag color="blue">{selectedUser.role}</Tag>
                    <Tag color={selectedUser.status === 'ACTIVE' ? 'green' : 'red'}>
                      {selectedUser.status}
                    </Tag>
                  </div>
                </div>
              </div>

              <div className={styles.profileRight}>
                <div className={styles.balanceInfo}>
                  <Text type="secondary">Số dư tài khoản</Text>
                  <Title level={3} style={{ margin: 0, color: '#1A73E8' }}>
                    {selectedUser.balance.toLocaleString()} VND
                  </Title>
                </div>

                <div className={styles.profileActions}>
                  <Space size="middle">
                    <div className={styles.statusSwitchWrapper}>
                      <Text style={{ marginRight: 8 }}>Trạng thái tài khoản:</Text>
                      <Popconfirm
                        title="Thay đổi trạng thái tài khoản"
                        description={selectedUser.status === 'ACTIVE' ? "Gửi yêu cầu khóa tài khoản này lên Admin phê duyệt?" : "Bạn có chắc muốn MỞ KHÓA tài khoản này?"}
                        onConfirm={() => {
                          if (selectedUser.status === 'ACTIVE') {
                            setLockUser(selectedUser);
                            setLockModalVisible(true);
                          } else {
                            handleToggleUserStatus(true, selectedUser);
                          }
                        }}
                        okText="Xác nhận"
                        cancelText="Hủy"
                        okButtonProps={{ danger: selectedUser.status === 'ACTIVE' }}
                      >
                        <Switch
                          checked={selectedUser.status === 'ACTIVE'}
                          checkedChildren="ACTIVE"
                          unCheckedChildren="BANNED"
                        />
                      </Popconfirm>
                    </div>
                  </Space>
                </div>
              </div>
            </div>
          </Card>

          <div className={styles.tableTitleSection}>
            <Title level={5}>Lịch sử hoạt động của người dùng (Audit Logs)</Title>
          </div>
          <Table
            dataSource={auditLogs}
            loading={auditLogsLoading}
            columns={[
              { title: 'Thời gian', dataIndex: 'time', key: 'time' },
              { title: 'Hành động', dataIndex: 'action', key: 'action' },
              { title: 'Đối tượng', dataIndex: 'targetUser', key: 'targetUser' },
              { title: 'Địa chỉ IP', dataIndex: 'ip', key: 'ip' },
              {
                title: 'Trạng thái',
                dataIndex: 'status',
                key: 'status',
                render: (status: string) => (
                  <Tag color={status === 'SUCCESS' ? 'green' : status === 'WARNING' ? 'orange' : 'red'}>
                    {status}
                  </Tag>
                )
              }
            ]}
            pagination={{ pageSize: 5 }}
            className={styles.table}
          />
        </div>
      ) : (
        <div className={styles.emptyState}>
          <InfoCircleOutlined className={styles.emptyIcon} />
          <Text>Vui lòng tìm kiếm tài khoản người dùng để xem thông tin chi tiết</Text>
        </div>
      )}

      <Modal
        title="Yêu cầu khóa tài khoản gửi Admin"
        open={lockModalVisible}
        onOk={handleSendLockRequest}
        onCancel={() => {
          setLockModalVisible(false);
          setLockReason('');
          setLockUser(null);
        }}
        okText="Gửi yêu cầu"
        cancelText="Hủy"
        okButtonProps={{ danger: true }}
      >
        <div style={{ marginBottom: 16 }}>
          <Text>Bạn đang gửi yêu cầu khóa tài khoản của <strong>{lockUser?.name}</strong> ({lockUser?.email}) lên Ban quản trị (Admin).</Text>
        </div>
        <Form.Item label="Lý do khóa tài khoản" required>
          <Input.TextArea
            rows={4}
            placeholder="Nhập lý do khóa tài khoản chi tiết để gửi Admin xét duyệt..."
            value={lockReason}
            onChange={(e) => setLockReason(e.target.value)}
          />
        </Form.Item>
      </Modal>
    </Card>
  );
}
