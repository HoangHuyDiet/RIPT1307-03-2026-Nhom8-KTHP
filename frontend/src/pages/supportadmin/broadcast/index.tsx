import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  Card,
  Form,
  Input,
  Select,
  Radio,
  Button,
  List,
  Tag,
  Modal,
  Typography,
  message,
  AutoComplete
} from 'antd';
import {
  BellOutlined,
  WarningOutlined
} from '@ant-design/icons';
import api from '@/utils/api';
import styles from './index.less';

interface Broadcast {
  key: string;
  title: string;
  content: string;
  target: string;
  urgency: 'INFO' | 'WARNING' | 'CRITICAL';
  time: string;
}

interface UserAccount {
  id: number;
  email: string;
  name: string;
  status: string;
  role: string;
}

const { Text, Paragraph } = Typography;
const { TextArea } = Input;

export default function SystemBroadcast() {
  const [broadcasts, setBroadcasts] = useState<Broadcast[]>([]);
  const [users, setUsers] = useState<UserAccount[]>([]);
  const [loading, setLoading] = useState(true);
  const [broadcastForm] = Form.useForm();
  const [broadcastConfirmVisible, setBroadcastConfirmVisible] = useState(false);
  const [confirmInput, setConfirmInput] = useState('');
  const [pendingBroadcast, setPendingBroadcast] = useState<any>(null);
  const [submitting, setSubmitting] = useState(false);
  const [autocompleteOptions, setAutocompleteOptions] = useState<{ value: string; label: string }[]>([]);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const targetValue = Form.useWatch('target', broadcastForm);
  const specificUserValue = Form.useWatch('specificUser', broadcastForm) || '';

  const fetchBroadcasts = async () => {
    setLoading(true);
    try {
      const res = await api.get('/support/broadcasts');
      setBroadcasts(res.data.data || []);
    } catch (error: any) {
      message.error('Không thể tải danh sách thông báo');
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      const res = await api.get('/support/users');
      setUsers(res.data.data || []);
    } catch {
    }
  };

  const handleAutocompleteSearch = useCallback((value: string) => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (!value.trim()) {
      setAutocompleteOptions([]);
      return;
    }
    debounceRef.current = setTimeout(() => {
      const filtered = users
        .filter(u =>
          u.email.toLowerCase().includes(value.toLowerCase()) ||
          u.name.toLowerCase().includes(value.toLowerCase())
        )
        .map(u => ({ value: u.email, label: `${u.name} (${u.email})` }));
      setAutocompleteOptions(filtered);
    }, 250);
  }, [users]);

  useEffect(() => {
    fetchBroadcasts();
    fetchUsers();
  }, []);

  const handleBroadcastSubmit = (values: any) => {
    const finalTarget = values.target === 'SPECIFIC' ? values.specificUser : values.target;
    setPendingBroadcast({
      title: values.title,
      content: values.content,
      target: finalTarget,
      urgency: values.urgency
    });
    setConfirmInput('');
    setBroadcastConfirmVisible(true);
  };

  const handleConfirmBroadcast = async () => {
    if (confirmInput !== 'CONFIRM') {
      message.error('Vui lòng nhập chính xác từ "CONFIRM"');
      return;
    }
    setSubmitting(true);
    try {
      await api.post('/support/broadcasts/create', {
        title: pendingBroadcast.title,
        content: pendingBroadcast.content,
        target: pendingBroadcast.target,
        urgency: pendingBroadcast.urgency
      });
      setBroadcastConfirmVisible(false);
      setPendingBroadcast(null);
      broadcastForm.resetFields();
      message.success('Thông báo đã được tạo và gửi đi thành công!');
      await fetchBroadcasts();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Gửi thông báo thất bại');
    } finally {
      setSubmitting(false);
    }
  };

  // Helper to render target group nicely
  const renderTargetTag = (target: string) => {
    if (target === 'ALL') return <Tag color="blue">Tất cả</Tag>;
    if (target === 'VIP') return <Tag color="gold">VIP</Tag>;
    if (target === 'SUBSCRIBED') return <Tag color="purple">Liên kết NH</Tag>;
    return <Tag color="orange">Đích danh: {target}</Tag>;
  };

  return (
    <div className={styles.broadcastSplitGrid}>
      <Card className={styles.broadcastFormCard} title="Tạo thông báo mới">
        <Form
          form={broadcastForm}
          layout="vertical"
          onFinish={handleBroadcastSubmit}
          initialValues={{ target: 'ALL', urgency: 'INFO' }}
        >
          <Form.Item
            name="title"
            label="Tiêu đề thông báo"
            rules={[{ required: true, message: 'Vui lòng nhập tiêu đề thông báo!' }]}
          >
            <Input placeholder="Ví dụ: Cảnh báo bảo trì, thông báo xử lý hỗ trợ..." />
          </Form.Item>

          <Form.Item
            name="content"
            label="Nội dung chi tiết thông báo"
            rules={[{ required: true, message: 'Vui lòng nhập nội dung thông báo!' }]}
          >
            <TextArea rows={4} placeholder="Nhập nội dung thông báo chi tiết cho người nhận..." />
          </Form.Item>

          <Form.Item
            name="target"
            label="Đối tượng nhận thông báo"
            rules={[{ required: true }]}
          >
            <Select>
              <Select.Option value="ALL">Tất cả người dùng (All Users)</Select.Option>
              <Select.Option value="VIP">Nhóm thành viên VIP (VIP Users)</Select.Option>
              <Select.Option value="SUBSCRIBED">Người dùng liên kết ngân hàng</Select.Option>
              <Select.Option value="SPECIFIC">Đích danh (Cá nhân người dùng cụ thể)</Select.Option>
            </Select>
          </Form.Item>

          {targetValue === 'SPECIFIC' && (
                      <Form.Item
              name="specificUser"
              label="Nhập email người dùng nhận thông báo"
              rules={[
                { required: true, message: 'Vui lòng nhập email người dùng nhận thông báo!' },
                { type: 'email', message: 'Email không đúng định dạng!' }
              ]}
            >
              <AutoComplete
                placeholder="Ví dụ: user@gmail.com (Gõ để tìm kiếm gợi ý)"
                options={autocompleteOptions}
                onSearch={handleAutocompleteSearch}
                filterOption={false}
              />
            </Form.Item>
          )}

          <Form.Item
            name="urgency"
            label="Mức độ cảnh báo"
            rules={[{ required: true }]}
          >
            <Radio.Group>
              <Radio.Button value="INFO">Thường (INFO)</Radio.Button>
              <Radio.Button value="WARNING">Chú ý (WARNING)</Radio.Button>
              <Radio.Button value="CRITICAL">Khẩn cấp (CRITICAL)</Radio.Button>
            </Radio.Group>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" icon={<BellOutlined />} danger style={{ width: '100%' }}>
              Gửi thông báo ngay
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <div className={styles.broadcastHistoryContainer}>
        <div className={styles.historyHeader}>
          <Text strong>Lịch sử các thông báo gần đây</Text>
        </div>
        <List
          itemLayout="vertical"
          dataSource={broadcasts}
          loading={loading}
          renderItem={item => (
            <Card
              size="small"
              className={styles.broadcastHistoryCard}
              title={
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>{item.title}</span>
                  <Tag color={item.urgency === 'CRITICAL' ? 'red' : item.urgency === 'WARNING' ? 'orange' : 'blue'}>
                    {item.urgency}
                  </Tag>
                </div>
              }
            >
              <Paragraph style={{ fontSize: 13, marginBottom: 8 }}>{item.content}</Paragraph>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 11, color: '#888' }}>
                <span>Đối tượng nhận: {renderTargetTag(item.target)}</span>
                <span>{item.time}</span>
              </div>
            </Card>
          )}
        />
      </div>

      <Modal
        title={
          <span style={{ color: '#EA4335' }}>
            <WarningOutlined style={{ marginRight: 8 }} /> Xác nhận gửi thông báo
          </span>
        }
        open={broadcastConfirmVisible}
        onOk={handleConfirmBroadcast}
        onCancel={() => setBroadcastConfirmVisible(false)}
        okText="Xác nhận gửi"
        cancelText="Hủy bỏ"
        okButtonProps={{ danger: true, disabled: confirmInput !== 'CONFIRM', loading: submitting }}
      >
        <div style={{ marginBottom: 16 }}>
          <Paragraph>
            Hành động này sẽ gửi thông báo đến đối tượng nhận: <Text strong>{pendingBroadcast?.target}</Text>.
          </Paragraph>
          <Paragraph>
            Vui lòng nhập từ khóa <Text type="danger" strong>CONFIRM</Text> vào hộp nhập liệu bên dưới để xác thực thao tác này:
          </Paragraph>
          <Input
            placeholder="Nhập CONFIRM để tiếp tục"
            value={confirmInput}
            onChange={e => setConfirmInput(e.target.value)}
          />
        </div>
      </Modal>
    </div>
  );
}
