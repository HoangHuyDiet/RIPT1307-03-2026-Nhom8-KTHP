import React, { useState, useEffect, useRef } from 'react';
import { Card, Button, Input, Avatar, Space, Typography, Badge } from 'antd';
import { 
  MessageFilled, 
  CloseOutlined, 
  SendOutlined, 
  PaperClipOutlined,
  MinusOutlined
} from '@ant-design/icons';
import styles from '../index.less';
import { GroupFund } from '../types';
import { useAuthStore } from '@/store/useAuthStore';

interface FloatingChatProps {
  group: GroupFund;
  discussions: any[];
  sendMessage: (text: string) => void;
}

export default function FloatingChat({ group, discussions, sendMessage }: FloatingChatProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [messageText, setMessageText] = useState('');
  const chatAreaRef = useRef<HTMLDivElement | null>(null);
  const user = useAuthStore(state => state.user);

  const groupDiscussions = discussions.filter(item => item.groupId === group.id);

  useEffect(() => {
    if (isOpen && chatAreaRef.current) {
      setTimeout(() => {
        chatAreaRef.current?.scrollTo({
          top: chatAreaRef.current.scrollHeight,
          behavior: 'smooth'
        });
      }, 50);
    }
  }, [groupDiscussions, isOpen]);

  const handleSendMessage = () => {
    if (!messageText.trim()) return;
    sendMessage(messageText);
    setMessageText('');
  };

  const getInitials = (name: string) => {
    if (name === 'Nguyễn Văn An') return 'AN';
    if (name === 'Bùi Minh') return 'BM';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  };

  if (!isOpen) {
    return (
      <div className={styles.floatingChatBubble} onClick={() => setIsOpen(true)}>
        <Badge dot={true} offset={[-5, 5]} color="#ff4d4f">
          <Button
            type="primary"
            shape="circle"
            className={styles.bubbleBtn}
            icon={<MessageFilled style={{ fontSize: '24px' }} />}
          />
        </Badge>
      </div>
    );
  }

  return (
    <div className={styles.floatingChatWindow}>
      <div className={styles.floatingHeader} onClick={() => setIsOpen(false)}>
        <Space align="center">
          <Avatar size="small" style={{ backgroundColor: group.themeColor }}>
            {group.name.charAt(0).toUpperCase()}
          </Avatar>
          <span className={styles.headerTitle}>{group.name}</span>
        </Space>
        <Space>
          <Button 
            type="text" 
            icon={<MinusOutlined style={{ color: '#fff' }} />} 
            onClick={(e) => { e.stopPropagation(); setIsOpen(false); }} 
            className={styles.headerActionBtn}
          />
          <Button 
            type="text" 
            icon={<CloseOutlined style={{ color: '#fff' }} />} 
            onClick={(e) => { e.stopPropagation(); setIsOpen(false); }} 
            className={styles.headerActionBtn}
          />
        </Space>
      </div>

      <div className={styles.chatArea} ref={chatAreaRef}>
        {groupDiscussions.map((item) => {
          if (item.type === 'system') {
            return (
              <div key={item.id} className={styles.systemMessage}>
                <span className={styles.systemText}>{item.text}</span>
                <span className={styles.systemTime}>{item.time}</span>
              </div>
            );
          }

          const isMe = item.isMe || (user && (item.senderName === user.display_name || item.senderName === user.email || item.senderName === user.email.split('@')[0]));

          return (
            <div
              key={item.id}
              className={`${styles.chatMessageRow} ${isMe ? styles.chatRowMe : styles.chatRowOther}`}
            >
              {!isMe && (
                <Avatar className={styles.chatAvatarLeft}>
                  {getInitials(item.senderName || '')}
                </Avatar>
              )}
              <div className={isMe ? styles.bubbleMe : styles.bubbleOther}>
                <div className={styles.msgText}>{item.text}</div>
                <span className={isMe ? styles.msgTimeMe : styles.msgTimeOther}>{item.time}</span>
              </div>
              {isMe && (
                <Avatar className={styles.chatAvatarRight}>
                  {getInitials(item.senderName || '')}
                </Avatar>
              )}
            </div>
          );
        })}
      </div>

      <div className={styles.inputArea}>
        <Button type="text" icon={<PaperClipOutlined />} className={styles.attachBtn} />
        <Input
          placeholder="Nhập tin nhắn..."
          value={messageText}
          onChange={(e) => setMessageText(e.target.value)}
          onPressEnter={handleSendMessage}
          className={styles.chatInput}
          bordered={false}
        />
        <Button
          type="primary"
          shape="circle"
          icon={<SendOutlined />}
          onClick={handleSendMessage}
          className={styles.sendChatBtn}
        />
      </div>
    </div>
  );
}
