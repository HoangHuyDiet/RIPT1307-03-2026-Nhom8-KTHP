import React, { useState, useEffect, useRef } from 'react';
import {
  Modal,
  Form,
  Input,
  Select,
  Button,
  Space,
  Avatar,
  Tag,
  message,
  Spin,
  Typography,
  Badge
} from 'antd';
import {
  CustomerServiceOutlined,
  SendOutlined,
  UserOutlined,
  InfoCircleOutlined,
  CheckCircleOutlined,
  CloseOutlined
} from '@ant-design/icons';
import api from '../../utils/api';
import { useAuthStore } from '../../store/useAuthStore';
import styles from './index.less';

const { Text, Paragraph, Title } = Typography;
const { TextArea } = Input;

interface Message {
  id: number;
  sender: 'user' | 'admin';
  content: string;
  time: string;
}

interface ChatRequest {
  id: number;
  name: string;
  email: string;
  lastMessage: string;
  time: string;
  status: 'PENDING' | 'RESOLVED';
  messages: Message[];
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
}

export default function SupportWidget() {
  const { user, roles, isPro } = useAuthStore();
  const [modalVisible, setModalVisible] = useState(false); 
  const [drawerVisible, setDrawerVisible] = useState(false); 
  const [submitting, setSubmitting] = useState(false);
  const [chatLoading, setChatLoading] = useState(false);
  const [chatData, setChatData] = useState<ChatRequest | null>(null);
  const [chatInput, setChatInput] = useState('');
  const [sending, setSending] = useState(false);
  const [form] = Form.useForm();
  
  const chatBodyRef = useRef<HTMLDivElement>(null);
  const pollIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const lastMsgCountRef = useRef(0);

  const isVipUser = roles.includes('VIP') || roles.includes('VIP_USER') || user?.email === 'admin@smartfinance.com' || isPro();

  const fetchVipChatSession = async (showLoading = false) => {
    if (showLoading) setChatLoading(true);
    try {
      const res = await api.get('/user/support/chat-session');
      const allChats: ChatRequest[] = res.data.data || [];
      const myChat = allChats.length > 0 ? allChats[0] : null;
      setChatData(myChat || null);
    } catch (error: any) {
      console.error('Failed to fetch support chat session:', error);
    } finally {
      if (showLoading) setChatLoading(false);
    }
  };

  const isNearBottom = () => {
    if (!chatBodyRef.current) return false;
    const { scrollTop, scrollHeight, clientHeight } = chatBodyRef.current;
    return scrollHeight - scrollTop - clientHeight < 100;
  };

  useEffect(() => {
    if (!chatBodyRef.current || !chatData?.messages) return;

    const currentCount = chatData.messages.length;
    const lastCount = lastMsgCountRef.current;

    const isInitialLoad = lastCount === 0;
    const hasNewMessage = currentCount > lastCount;
    const lastMessageIsFromUser = chatData.messages[currentCount - 1]?.sender === 'user';

    if (isInitialLoad || (hasNewMessage && (lastMessageIsFromUser || isNearBottom()))) {
      chatBodyRef.current.scrollTop = chatBodyRef.current.scrollHeight;
    }

    lastMsgCountRef.current = currentCount;
  }, [chatData?.messages]);

  const handleSupportClick = () => {
    if (isVipUser) {
      setDrawerVisible(true);
      fetchVipChatSession(true);
      
      pollIntervalRef.current = setInterval(() => {
        fetchVipChatSession(false);
      }, 3000);
    } else {
      setModalVisible(true);
    }
  };

  const handleCloseDrawer = () => {
    setDrawerVisible(false);
    if (pollIntervalRef.current) {
      clearInterval(pollIntervalRef.current);
      pollIntervalRef.current = null;
    }
  };

  useEffect(() => {
    return () => {
      if (pollIntervalRef.current) {
        clearInterval(pollIntervalRef.current);
      }
    };
  }, []);

  const handleRequestSubmit = async (values: any) => {
    setSubmitting(true);
    try {
      await api.post('/user/support/chat-session/create', {
        title: values.title,
        description: values.description,
        priority: values.priority,
        email: user?.email || 'guest@smartfinance.vn',
        name: user?.name || 'Khách hàng vãng lai'
      });
      message.success('Gửi yêu cầu hỗ trợ thành công! Kỹ thuật viên sẽ liên hệ lại qua email.');
      form.resetFields();
      setModalVisible(false);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Gửi yêu cầu hỗ trợ thất bại');
    } finally {
      setSubmitting(false);
    }
  };

  const handleStartVipSession = async () => {
    setChatLoading(true);
    try {
      const res = await api.post('/user/support/chat-session/create', {
        title: 'Hỗ trợ Pro trực tuyến',
        description: 'Bắt đầu phiên hỗ trợ Pro',
        priority: 'HIGH',
        email: user?.email,
        name: user?.name
      });
      setChatData(res.data.data);
      message.success('Đã kết nối trực tiếp với Support Admin!');
    } catch (error: any) {
      message.error('Không thể khởi tạo phiên kết nối support');
    } finally {
      setChatLoading(false);
    }
  };

  const handleSendChatMessage = async () => {
    if (!chatInput.trim() || !chatData) return;
    setSending(true);
    try {
      const now = new Date();
      const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
      const newMsg: Message = {
        id: (chatData.messages?.length || 0) + 1,
        sender: 'user',
        content: chatInput,
        time: timeStr
      };
      
      await api.post('/user/support/chat-session/send', {
        chatId: chatData.id,
        message: newMsg
      });
      
      setChatInput('');
      setChatData(prev => {
        if (!prev) return null;
        return {
          ...prev,
          messages: [...(prev.messages || []), newMsg],
          lastMessage: newMsg.content,
          time: newMsg.time
        };
      });
    } catch (error: any) {
      message.error('Gửi tin nhắn thất bại');
    } finally {
      setSending(false);
    }
  };

  return (
    <>
      <div className={styles.supportWrapper} onClick={handleSupportClick} title="Tổng đài hỗ trợ kỹ thuật">
        <CustomerServiceOutlined className={styles.supportIcon} />
      </div>

      <Modal
        title={
          <Space>
            <CustomerServiceOutlined style={{ color: '#1A73E8' }} />
            <span>Tổng đài gửi yêu cầu hỗ trợ</span>
          </Space>
        }
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        destroyOnClose
      >
        <div style={{ marginBottom: 20 }}>
          <Text type="secondary">
            Bạn đang sử dụng tài khoản thông thường. Vui lòng gửi yêu cầu hỗ trợ dưới đây, đội ngũ Support sẽ phản hồi bạn sớm nhất qua email.
          </Text>
        </div>

        <Form form={form} layout="vertical" onFinish={handleRequestSubmit}>
          <Form.Item
            name="title"
            label="Tiêu đề yêu cầu hỗ trợ"
            rules={[{ required: true, message: 'Vui lòng nhập tiêu đề yêu cầu!' }]}
          >
            <Input placeholder="Ví dụ: Lỗi rút tiền, Không nhận được OTP,..." />
          </Form.Item>

          <Form.Item
            name="description"
            label="Mô tả chi tiết lỗi"
            rules={[
              { required: true, message: 'Vui lòng mô tả chi tiết vấn đề!' },
              { min: 10, message: 'Nội dung mô tả quá ngắn (tối thiểu 10 ký tự)!' }
            ]}
          >
            <TextArea
              rows={4}
              placeholder="Nhập chi tiết lỗi, mã giao dịch (nếu có), thời gian xảy ra..."
            />
          </Form.Item>

          <Form.Item
            name="priority"
            label="Mức độ ảnh hưởng"
            initialValue="MEDIUM"
            rules={[{ required: true }]}
          >
            <Select>
              <Select.Option value="LOW">Thấp (Chỉ là lỗi hiển thị nhẹ)</Select.Option>
              <Select.Option value="MEDIUM">Trung bình (Ảnh hưởng trải nghiệm nhưng chưa mất mát)</Select.Option>
              <Select.Option value="HIGH">Cao (Giao dịch bị lỗi, tài khoản bị hạn chế)</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setModalVisible(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit" loading={submitting}>
                Gửi yêu cầu
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {drawerVisible && (
        <div className={styles.floatingChatBox}>
          <div className={styles.chatHeader}>
            <div className={styles.drawerTitleWrapper}>
              <Badge dot={chatLoading ? false : chatData?.status === 'PENDING'} color="#52c41a" offset={[-2, 28]}>
                <Avatar size={36} style={{ backgroundColor: '#fff2e8', color: '#fa8c16' }} icon={<CustomerServiceOutlined />} />
              </Badge>
              <div className={styles.headerTextGroup}>
                <div className={styles.drawerMainTitle}>Hỗ Trợ Trực Tiếp</div>
                <div className={styles.drawerSubtitle}>
                  {chatLoading ? (
                    'Đang kết nối...'
                  ) : chatData ? (
                    chatData.status === 'PENDING' ? (
                      <span style={{ color: '#52c41a', fontWeight: 500 }}>Trực tuyến</span>
                    ) : (
                      <span style={{ color: '#8c8c8c' }}>Đã giải quyết</span>
                    )
                  ) : (
                    'Sẵn sàng kết nối'
                  )}
                </div>
              </div>
            </div>
            
            <Button
              type="text"
              icon={<CloseOutlined style={{ fontSize: 15, color: '#64748b' }} />}
              onClick={handleCloseDrawer}
              className={styles.closeChatBtn}
            />
          </div>

          <div className={styles.chatContentArea}>
            {chatLoading ? (
              <div className={styles.chatLoadingContainer}>
                <Spin size="large" tip="Đang kết nối tổng đài ..." />
              </div>
            ) : chatData ? (
              <div className={styles.chatContainer}>
                <div className={styles.chatBody} ref={chatBodyRef}>
                  {chatData.messages && chatData.messages.map((msg, index) => (
                    <div
                      key={index}
                      className={`${styles.msgRow} ${msg.sender.toLowerCase() === 'user' ? styles.msgRowRight : styles.msgRowLeft}`}
                    >
                      {msg.sender.toLowerCase() === 'admin' && (
                        <Avatar icon={<UserOutlined />} size="small" style={{ marginRight: 8, backgroundColor: '#1A73E8', flexShrink: 0 }} />
                      )}
                      <div className={styles.msgBubbleAndTime}>
                        <div className={styles.msgBubble}>
                          <Paragraph style={{ margin: 0, color: 'inherit', wordBreak: 'break-word' }}>{msg.content}</Paragraph>
                        </div>
                        <span className={styles.msgTime}>{msg.time}</span>
                      </div>
                    </div>
                  ))}
                </div>

                <div className={styles.chatFooter}>
                  {chatData.status === 'PENDING' ? (
                    <div className={styles.inputArea}>
                      <Input
                        placeholder="Nhập nội dung nhắn cho Support Admin..."
                        value={chatInput}
                        onChange={e => setChatInput(e.target.value)}
                        onPressEnter={handleSendChatMessage}
                        className={styles.messageInput}
                        disabled={sending}
                      />
                      <Button
                        type="primary"
                        icon={<SendOutlined />}
                        onClick={handleSendChatMessage}
                        loading={sending}
                      />
                    </div>
                  ) : (
                    <div className={styles.chatResolvedOverlay}>
                      <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 24, marginBottom: 8 }} />
                      <Text type="secondary" style={{ fontSize: 13, display: 'block', textAlign: 'center' }}>
                        Yêu cầu hỗ trợ này đã được Admin giải quyết đóng lại.
                      </Text>
                      <Button type="primary" size="small" onClick={handleStartVipSession} style={{ marginTop: 12 }}>
                        Tạo cuộc trò chuyện mới
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className={styles.noChatContainer}>
                <InfoCircleOutlined style={{ fontSize: 48, color: '#fa8c16', marginBottom: 16 }} />
                <Title level={4} style={{ margin: '0 0 8px 0' }}>Kết nối Hotline Pro</Title>
                <Paragraph style={{ color: '#5f6368', marginBottom: 24 }}>
                  Chào mừng quý khách ! Quý khách có quyền truy cập kênh chat bảo mật trực tiếp 24/7 với Support Admin của Smart Finance.
                </Paragraph>
                <Button type="primary" size="large" onClick={handleStartVipSession} style={{ marginTop: 16 }}>
                  Bắt đầu kết nối chat ngay
                </Button>
              </div>
            )}
          </div>
        </div>
      )}
    </>
  );
}
