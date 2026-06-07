import React, { useState, useEffect } from 'react';
import {
  Space,
  Button,
  Input,
  List,
  Avatar,
  Tag,
  message,
  Spin,
  Typography
} from 'antd';
import {
  SendOutlined,
  UserOutlined,
  InfoCircleOutlined
} from '@ant-design/icons';
import api from '@/utils/api';
import styles from './index.less';

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

const QUICK_REPLIES = [
  { key: '/chao', label: '/chao - Lời chào khách hàng VIP', text: 'Xin chào quý khách VIP, bộ phận hỗ trợ kỹ thuật có thể giúp gì cho quý khách ạ?' },
  { key: '/giaiquyet', label: '/giaiquyet - Thông báo đang xử lý', text: 'Yêu cầu của quý khách đã được ghi nhận. Hệ thống đang kiểm tra và sẽ phản hồi trong vòng 5 phút.' },
  { key: '/kethuc', label: '/kethuc - Chào tạm biệt khách hàng', text: 'Vấn đề của quý khách đã được xử lý xong. Cảm ơn quý khách đã liên hệ với Support VIP!' },
  { key: '/nap-tien', label: '/nap-tien - Lỗi giao dịch nạp quỹ', text: 'Quý khách vui lòng cung cấp mã giao dịch và ảnh chụp biên lai chuyển tiền để chúng tôi đối soát.' },
  { key: '/quen-pass', label: '/quen-pass - Hướng dẫn khôi phục mật khẩu', text: 'Quý khách hãy nhấn vào liên kết "Quên mật khẩu" tại trang đăng nhập và kiểm tra hòm thư Email.' }
];

const { Text, Title, Paragraph } = Typography;

export default function HelpdeskVIP() {
  const [chatRequests, setChatRequests] = useState<ChatRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedChatId, setSelectedChatId] = useState<number | null>(null);
  const [chatInput, setChatInput] = useState('');
  const [showQuickReply, setShowQuickReply] = useState(false);
  const [sending, setSending] = useState(false);

  const fetchChatRequests = async () => {
    setLoading(true);
    try {
      const res = await api.get('/support/chat-requests');
      const data: ChatRequest[] = res.data.data || [];
      setChatRequests(data);
      if (!selectedChatId && data.length > 0) {
        setSelectedChatId(data[0].id);
      }
    } catch (error: any) {
      message.error('Không thể tải danh sách yêu cầu hỗ trợ');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchChatRequests();
  }, []);

  const activeChat = chatRequests.find(c => c.id === selectedChatId) || chatRequests[0];

  const handleSendChatMessage = async () => {
    if (!chatInput.trim() || !activeChat) return;
    setSending(true);
    try {
      const now = new Date();
      const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
      const newMsg: Message = {
        id: activeChat.messages.length + 1,
        sender: 'admin',
        content: chatInput,
        time: timeStr
      };
      await api.post('/support/chat-requests/send', {
        chatId: activeChat.id,
        message: newMsg
      });
      setChatRequests(prev =>
        prev.map(c =>
          c.id === activeChat.id
            ? { ...c, messages: [...c.messages, newMsg], lastMessage: newMsg.content, time: timeStr }
            : c
        )
      );
      setChatInput('');
      setShowQuickReply(false);
    } catch (error: any) {
      message.error('Gửi tin nhắn thất bại');
    } finally {
      setSending(false);
    }
  };

  const handleChatInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setChatInput(value);
    setShowQuickReply(value.endsWith('/'));
  };

  const selectQuickReply = (text: string) => {
    setChatInput(text);
    setShowQuickReply(false);
  };

  const handleResolveChat = async (id: number) => {
    try {
      await api.post('/support/chat-requests/resolve', { chatId: id });
      message.success('Đã giải quyết yêu cầu hỗ trợ thành công!');
      await fetchChatRequests();
    } catch (error: any) {
      message.error('Không thể giải quyết yêu cầu hỗ trợ');
    }
  };

  if (loading && chatRequests.length === 0) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" tip="Đang tải dữ liệu hỗ trợ..." />
      </div>
    );
  }

  return (
    <div className={styles.chatSplitLayout}>
      <div className={styles.chatSider}>
        <div className={styles.siderHeader}>
          <Text className={styles.siderTitle}>Yêu cầu hỗ trợ VIP</Text>
        </div>
        <List
          itemLayout="horizontal"
          dataSource={chatRequests}
          loading={loading}
          renderItem={item => (
            <List.Item
              className={`${styles.chatItem} ${selectedChatId === item.id ? styles.activeChatItem : ''}`}
              onClick={() => setSelectedChatId(item.id)}
            >
              <List.Item.Meta
                avatar={<Avatar icon={<UserOutlined />} className={styles.avatar} />}
                title={
                  <div className={styles.chatTitleBar}>
                    <span className={styles.chatName}>{item.name}</span>
                    <span className={styles.chatTime}>{item.time}</span>
                  </div>
                }
                description={
                  <div className={styles.chatDescBar}>
                    <Text ellipsis className={styles.chatLastMsg}>{item.lastMessage}</Text>
                    <Space size={4}>
                      <Tag color={item.priority === 'HIGH' ? 'red' : item.priority === 'MEDIUM' ? 'orange' : 'blue'} className={styles.statusTag}>
                        {item.priority}
                      </Tag>
                      <Tag color={item.status === 'PENDING' ? 'processing' : 'success'} className={styles.statusTag}>
                        {item.status === 'PENDING' ? 'Chờ xử lý' : 'Đã xong'}
                      </Tag>
                    </Space>
                  </div>
                }
              />
            </List.Item>
          )}
        />
      </div>

      <div className={styles.chatWindow}>
        {activeChat ? (
          <>
            <div className={styles.chatHeader}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <Avatar icon={<UserOutlined />} className={styles.avatar} />
                <div>
                  <Title level={5} style={{ margin: 0 }}>{activeChat.name}</Title>
                  <Text type="secondary" style={{ fontSize: 12 }}>{activeChat.email}</Text>
                </div>
              </div>
              <div>
                {activeChat.status === 'PENDING' && (
                  <Button
                    type="primary"
                    size="small"
                    onClick={() => handleResolveChat(activeChat.id)}
                  >
                    Đóng yêu cầu
                  </Button>
                )}
              </div>
            </div>

            <div className={styles.chatBody}>
              {activeChat.messages.map(msg => (
                <div
                  key={msg.id}
                  className={`${styles.msgRow} ${msg.sender === 'admin' ? styles.msgRowRight : styles.msgRowLeft}`}
                >
                  {msg.sender === 'user' && (
                    <Avatar icon={<UserOutlined />} size="small" style={{ marginRight: 8 }} />
                  )}
                  <div className={styles.msgBubble}>
                    <Paragraph style={{ margin: 0, color: 'inherit' }}>{msg.content}</Paragraph>
                    <span className={styles.msgTime}>{msg.time}</span>
                  </div>
                </div>
              ))}
            </div>

            <div className={styles.chatFooter}>
              {showQuickReply && (
                <div className={styles.quickReplyPopup}>
                  <div className={styles.quickReplyHeader}>
                    <Text strong style={{ fontSize: 11 }}>Mẫu trả lời nhanh (Phím tắt: /)</Text>
                  </div>
                  <div className={styles.quickReplyList}>
                    {QUICK_REPLIES.map(qr => (
                      <div
                        key={qr.key}
                        className={styles.quickReplyItem}
                        onClick={() => selectQuickReply(qr.text)}
                      >
                        {qr.label}
                      </div>
                    ))}
                  </div>
                </div>
              )}
              <div className={styles.inputArea}>
                <Input
                  placeholder="Nhập nội dung tin nhắn hỗ trợ kỹ thuật... (Gõ '/' để mở mẫu câu nhanh)"
                  value={chatInput}
                  onChange={handleChatInputChange}
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
            </div>
          </>
        ) : (
          <div className={styles.emptyState}>
            <InfoCircleOutlined className={styles.emptyIcon} />
            <Text>Không có yêu cầu hỗ trợ nào</Text>
          </div>
        )}
      </div>
    </div>
  );
}
