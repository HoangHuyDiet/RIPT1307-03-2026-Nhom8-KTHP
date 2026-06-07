import React, { useState, useEffect, useMemo } from 'react';
import {
  Card,
  Table,
  Tag,
  Button,
  Space,
  Select,
  Typography,
  Modal,
  Descriptions,
  message,
  Empty
} from 'antd';
import {
  FileTextOutlined,
  CalendarOutlined,
  AlertOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined
} from '@ant-design/icons';
import api from '@/utils/api';
import styles from './index.less';

const { Title, Text, Paragraph } = Typography;

interface Message {
  id: number;
  sender: 'user' | 'admin';
  content: string;
  time: string;
}

interface SupportTicket {
  id: number;
  name: string;
  email: string;
  lastMessage: string;
  time: string;
  status: 'PENDING' | 'RESOLVED';
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  messages: Message[];
}

export default function UserTicketsQueue() {
  const [tickets, setTickets] = useState<SupportTicket[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [sortBy, setSortBy] = useState<'NEWEST' | 'OLDEST'>('NEWEST');
  const [priorityFilter, setPriorityFilter] = useState<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PENDING' | 'RESOLVED'>('ALL');

  const [selectedTicket, setSelectedTicket] = useState<SupportTicket | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [resolving, setResolving] = useState(false);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const res = await api.get('/support/chat-requests', { params: { type: 'NORMAL' } });
      setTickets(res.data.data || []);
    } catch (error: any) {
      message.error('Không thể tải danh sách yêu cầu hỗ trợ');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, []);

  const handleResolveTicket = async (ticketId: number) => {
    setResolving(true);
    try {
      await api.post('/support/chat-requests/resolve', { chatId: ticketId });
      message.success('Đã giải quyết yêu cầu hỗ trợ thành công!');
      setModalVisible(false);
      setSelectedTicket(null);
      await fetchTickets();
    } catch (error: any) {
      message.error('Thao tác thất bại, vui lòng thử lại');
    } finally {
      setResolving(false);
    }
  };

  const processedTickets = useMemo(() => {
    let result = [...tickets];
    if (priorityFilter !== 'ALL') result = result.filter(t => t.priority === priorityFilter);
    if (statusFilter !== 'ALL') result = result.filter(t => t.status === statusFilter);
    result.sort((a, b) => sortBy === 'NEWEST' ? b.id - a.id : a.id - b.id);
    return result;
  }, [tickets, priorityFilter, statusFilter, sortBy]);

  const columns = [
    {
      title: 'Mã hỗ trợ',
      dataIndex: 'id',
      key: 'id',
      width: 100,
      render: (id: number) => <Text strong>#{id}</Text>
    },
    {
      title: 'Người gửi',
      key: 'user',
      width: 220,
      render: (_: any, record: SupportTicket) => (
        <div>
          <div style={{ fontWeight: 600 }}>{record.name}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.email}</Text>
        </div>
      )
    },
    {
      title: 'Yêu cầu hỗ trợ',
      key: 'request',
      render: (_: any, record: SupportTicket) => {
        const content = record.messages && record.messages[0] ? record.messages[0].content : record.lastMessage;
        return (
          <div style={{ maxWidth: 350 }}>
            <Paragraph ellipsis={{ rows: 2 }} style={{ margin: 0 }}>
              {content}
            </Paragraph>
          </div>
        );
      }
    },
    {
      title: 'Mức độ',
      dataIndex: 'priority',
      key: 'priority',
      width: 130,
      render: (priority: 'HIGH' | 'MEDIUM' | 'LOW') => {
        const colors = { HIGH: 'red', MEDIUM: 'orange', LOW: 'blue' };
        const text = { HIGH: 'Cao (HIGH)', MEDIUM: 'Trung bình', LOW: 'Thấp (LOW)' };
        return <Tag color={colors[priority]}>{text[priority]}</Tag>;
      }
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 130,
      render: (status: 'PENDING' | 'RESOLVED') => (
        <Tag color={status === 'PENDING' ? 'processing' : 'success'} icon={status === 'PENDING' ? <ClockCircleOutlined /> : <CheckCircleOutlined />}>
          {status === 'PENDING' ? 'Đang xử lý' : 'Đã giải quyết'}
        </Tag>
      )
    },
    {
      title: 'Thời gian gửi',
      dataIndex: 'time',
      key: 'time',
      width: 120,
      render: (time: string) => <Text type="secondary">{time}</Text>
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 140,
      render: (_: any, record: SupportTicket) => (
        <Button
          type="primary"
          ghost
          icon={<FileTextOutlined />}
          onClick={() => {
            setSelectedTicket(record);
            setModalVisible(true);
          }}
        >
          Chi tiết
        </Button>
      )
    }
  ];

  return (
    <Card className={styles.ticketsCard}>
      <div className={styles.filterToolbar}>
        <Space size="middle" wrap>
          <div>
            <Text type="secondary" style={{ marginRight: 8 }}><CalendarOutlined /> Sắp xếp:</Text>
            <Select value={sortBy} onChange={setSortBy} style={{ width: 140 }}>
              <Select.Option value="NEWEST">Mới nhất</Select.Option>
              <Select.Option value="OLDEST">Cũ nhất</Select.Option>
            </Select>
          </div>

          <div>
            <Text type="secondary" style={{ marginRight: 8 }}><AlertOutlined /> Mức độ:</Text>
            <Select value={priorityFilter} onChange={setPriorityFilter} style={{ width: 140 }}>
              <Select.Option value="ALL">Tất cả mức độ</Select.Option>
              <Select.Option value="HIGH">Cao</Select.Option>
              <Select.Option value="MEDIUM">Trung bình</Select.Option>
              <Select.Option value="LOW">Thấp</Select.Option>
            </Select>
          </div>

          <div>
            <Text type="secondary" style={{ marginRight: 8 }}><CheckCircleOutlined /> Trạng thái:</Text>
            <Select value={statusFilter} onChange={setStatusFilter} style={{ width: 140 }}>
              <Select.Option value="ALL">Tất cả trạng thái</Select.Option>
              <Select.Option value="PENDING">Đang xử lý</Select.Option>
              <Select.Option value="RESOLVED">Đã giải quyết</Select.Option>
            </Select>
          </div>
        </Space>

        <div>
          <Text strong type="secondary">
            Tổng số: {processedTickets.length} yêu cầu
          </Text>
        </div>
      </div>

      <Table
        dataSource={processedTickets}
        columns={columns}
        rowKey="id"
        loading={loading}
        className={styles.ticketsTable}
        pagination={{ pageSize: 8 }}
        locale={{
          emptyText: <Empty description="Không tìm thấy yêu cầu hỗ trợ nào." />
        }}
      />

      {selectedTicket && (
        <Modal
          title={
            <Space>
              <FileTextOutlined style={{ color: '#1A73E8' }} />
              <span>Chi tiết yêu cầu hỗ trợ #{selectedTicket.id}</span>
            </Space>
          }
          open={modalVisible}
          onCancel={() => {
            setModalVisible(false);
            setSelectedTicket(null);
          }}
          footer={
            <Space>
              <Button onClick={() => setModalVisible(false)}>Đóng</Button>
              {selectedTicket.status === 'PENDING' && (
                <Button
                  type="primary"
                  icon={<CheckCircleOutlined />}
                  loading={resolving}
                  onClick={() => handleResolveTicket(selectedTicket.id)}
                >
                  Xác nhận đã giải quyết
                </Button>
              )}
            </Space>
          }
          width={650}
          destroyOnClose
        >
          <Descriptions bordered column={2} className={styles.descriptions}>
            <Descriptions.Item label="Họ và tên" span={1}>
              <Text strong>{selectedTicket.name}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="Email liên hệ" span={1}>
              {selectedTicket.email}
            </Descriptions.Item>
            <Descriptions.Item label="Thời gian gửi" span={1}>
              {selectedTicket.time}
            </Descriptions.Item>
            <Descriptions.Item label="Mức độ" span={1}>
              <Tag color={selectedTicket.priority === 'HIGH' ? 'red' : selectedTicket.priority === 'MEDIUM' ? 'orange' : 'blue'}>
                {selectedTicket.priority}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Trạng thái" span={2}>
              <Tag color={selectedTicket.status === 'PENDING' ? 'processing' : 'success'}>
                {selectedTicket.status === 'PENDING' ? 'Đang xử lý' : 'Đã giải quyết'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Nội dung yêu cầu" span={2}>
              <div style={{ background: '#f8fafc', padding: '12px 16px', borderRadius: 8, border: '1px solid #e2e8f0', minHeight: 100 }}>
                {selectedTicket.messages && selectedTicket.messages[0] ? (
                  <Paragraph style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                    {selectedTicket.messages[0].content}
                  </Paragraph>
                ) : (
                  <Paragraph style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                    {selectedTicket.lastMessage}
                  </Paragraph>
                )}
              </div>
            </Descriptions.Item>
          </Descriptions>
        </Modal>
      )}
    </Card>
  );
}
