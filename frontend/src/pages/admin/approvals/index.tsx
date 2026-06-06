import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Tag,
  Badge,
  Typography,
  Space,
  message,
  Modal,
  Row,
  Col,
  Statistic
} from 'antd';
import {
  ReloadOutlined,
  StopOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  InboxOutlined
} from '@ant-design/icons';
import api from '../../../utils/api';
import styles from '../users/index.less';

const { Text, Title } = Typography;

interface LockRequest {
  id: number;
  email: string;
  name: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  time: string;
}

export default function LockApprovals() {
  const [lockRequests, setLockRequests] = useState<LockRequest[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchLockRequests = async () => {
    setLoading(true);
    try {
      const res = await api.get('/support/lock-requests');
      setLockRequests(res.data.data || []);
    } catch (error: any) {
      message.error('Không thể tải danh sách yêu cầu khóa tài khoản');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLockRequests();
  }, []);

  const handleApproveLock = (id: number, email: string) => {
    Modal.confirm({
      title: 'Phê duyệt khóa tài khoản',
      icon: <ExclamationCircleOutlined />,
      content: `Bạn có chắc chắn muốn PHÊ DUYỆT yêu cầu khóa tài khoản "${email}"?`,
      okText: 'Xác nhận khóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await api.post(`/support/lock-requests/${id}/approve`);
          message.success('Đã phê duyệt khóa tài khoản thành công');
          fetchLockRequests();
        } catch (error: any) {
          message.error(error.response?.data?.message || 'Thao tác thất bại');
        }
      }
    });
  };

  const handleRejectLock = (id: number, email: string) => {
    Modal.confirm({
      title: 'Từ chối khóa tài khoản',
      icon: <ExclamationCircleOutlined />,
      content: `Bạn có chắc chắn muốn TỪ CHỐI yêu cầu khóa tài khoản "${email}"?`,
      okText: 'Xác nhận từ chối',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await api.post(`/support/lock-requests/${id}/reject`);
          message.success('Đã từ chối yêu cầu thành công');
          fetchLockRequests();
        } catch (error: any) {
          message.error(error.response?.data?.message || 'Thao tác thất bại');
        }
      }
    });
  };

  const totalRequests = lockRequests.length;
  const pendingRequests = lockRequests.filter(r => r.status === 'PENDING').length;
  const approvedRequests = lockRequests.filter(r => r.status === 'APPROVED').length;
  const rejectedRequests = lockRequests.filter(r => r.status === 'REJECTED').length;

  const columns = [
    {
      title: 'Mã yêu cầu',
      dataIndex: 'id',
      key: 'id',
      width: 120,
      render: (id: number) => <Text strong>#REQ-{id}</Text>
    },
    {
      title: 'Người dùng bị đề xuất khóa',
      key: 'user',
      width: 250,
      render: (_: any, record: LockRequest) => (
        <div>
          <div style={{ fontWeight: 600 }}>{record.name}</div>
          <Text type="secondary" style={{ fontSize: 13 }}>{record.email}</Text>
        </div>
      )
    },
    {
      title: 'Lý do yêu cầu',
      dataIndex: 'reason',
      key: 'reason',
      render: (reason: string) => (
        <div style={{ background: '#fffbe6', padding: '10px 14px', borderRadius: 8, border: '1px solid #ffe58f', color: '#d46b08', fontSize: '13px' }}>
          {reason}
        </div>
      )
    },
    {
      title: 'Thời gian gửi',
      dataIndex: 'time',
      key: 'time',
      width: 160,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 140,
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
      title: 'Thao tác',
      key: 'action',
      width: 220,
      render: (_: any, record: LockRequest) => {
        if (record.status !== 'PENDING') {
          return <Text type="secondary">-</Text>;
        }
        return (
          <Space size="small">
            <Button
              type="primary"
              danger
              size="small"
              icon={<StopOutlined />}
              onClick={() => handleApproveLock(record.id, record.email)}
            >
              Duyệt khóa
            </Button>
            <Button
              size="small"
              onClick={() => handleRejectLock(record.id, record.email)}
            >
              Từ chối
            </Button>
          </Space>
        );
      }
    }
  ];

  return (
    <div className={styles.container}>
      <Row gutter={[16, 16]} className={styles.statsRow}>
        <Col xs={12} sm={6}>
          <Card bordered={false} className={styles.statCard}>
            <Statistic
              title={<Text className={styles.statLabel}>Tổng yêu cầu</Text>}
              value={totalRequests}
              prefix={<InboxOutlined className={styles.statIconBlue} />}
              valueStyle={{ color: '#1A73E8', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} className={styles.statCard}>
            <Statistic
              title={<Text className={styles.statLabel}>Chờ phê duyệt</Text>}
              value={pendingRequests}
              prefix={<ClockCircleOutlined className={styles.statIconOrange} />}
              valueStyle={{ color: '#F9AB00', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} className={styles.statCard}>
            <Statistic
              title={<Text className={styles.statLabel}>Đã duyệt khóa</Text>}
              value={approvedRequests}
              prefix={<StopOutlined className={styles.statIconRed} />}
              valueStyle={{ color: '#EA4335', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} className={styles.statCard}>
            <Statistic
              title={<Text className={styles.statLabel}>Đã từ chối</Text>}
              value={rejectedRequests}
              prefix={<CheckCircleOutlined className={styles.statIconGreen} />}
              valueStyle={{ color: '#34A853', fontWeight: 700 }}
            />
          </Card>
        </Col>
      </Row>

      <Card bordered={false} className={styles.tableCard}>
        <div className={styles.toolbar}>
          <div className={styles.toolbarLeft}>
            <Title level={4} style={{ margin: 0 }}>Danh sách yêu cầu khóa tài khoản</Title>
            <Text type="secondary" style={{ fontSize: 13 }}>
              Danh sách yêu cầu được gửi từ các Support Admin để khóa người dùng vi phạm điều khoản
            </Text>
          </div>
          <div className={styles.toolbarRight}>
            <Button
              icon={<ReloadOutlined />}
              onClick={fetchLockRequests}
              loading={loading}
            >
              Làm mới
            </Button>
          </div>
        </div>

        <Table
          dataSource={lockRequests}
          rowKey="id"
          loading={loading}
          columns={columns}
          className={styles.table}
          pagination={{
            defaultPageSize: 8,
            showSizeChanger: true,
            showTotal: (total) => `Tổng số ${total} yêu cầu`,
          }}
          locale={{
            emptyText: (
              <div className={styles.emptyState}>
                <CheckCircleOutlined className={styles.emptyIcon} style={{ color: '#52c41a' }} />
                <div className={styles.emptyText}>Không có yêu cầu khóa tài khoản nào</div>
                <div className={styles.emptyHint}>Tất cả các tài khoản đều ở trạng thái bình thường</div>
              </div>
            )
          }}
        />
      </Card>
    </div>
  );
}
