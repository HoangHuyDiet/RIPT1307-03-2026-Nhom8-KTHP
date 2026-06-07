import React, { useEffect, useRef, useState } from 'react';
import { Alert, Button, Input, Space, Spin, Tag, Typography } from 'antd';
import { CloseOutlined, RobotOutlined, SendOutlined } from '@ant-design/icons';
import { getAiStatus, sendAiChat, type AiAvailability, type AiCitation } from '@/services/ai';
import { useAuthStore } from '@/store/useAuthStore';
import styles from './index.less';

const { Text } = Typography;
const SESSION_STORAGE_KEY = 'smart_finance_ai_session_id';
const CANNOT_ANSWER_TEXT = 'Hiện tôi đang không thể trả lời câu hỏi này, để giải quyết thắc mắc này bạn vui lòng liên hệ với support admin.';
const CANNOT_ANSWER_CODES = new Set([
  'AI_DISABLED',
  'AI_UNAVAILABLE',
  'AI_RATE_LIMITED',
  'OUTPUT_VALIDATION_FAILED',
  'AI_SERVICE_ERROR',
]);

type ChatMessage = {
  id: string;
  role: 'user' | 'ai';
  content: string;
  citations?: AiCitation[];
  errorCode?: string;
};

const errorText: Record<string, string> = {
  AI_DISABLED: 'AI đang tắt. Hãy bật AI_ENABLED và cấu hình GEMINI_API_KEY.',
  AI_UNAVAILABLE: 'AI tạm thời chưa sẵn sàng.',
  AI_RATE_LIMITED: 'Bạn đang gửi quá nhanh. Hãy thử lại sau ít phút.',
  AI_PRO_REQUIRED: 'Vui lòng nâng cấp gói Pro để sử dụng trợ lý AI tài chính.',
  OUTPUT_VALIDATION_FAILED: 'AI chưa tạo được câu trả lời đủ tin cậy.',
};

function parseBold(text: string, boldColor: string = 'inherit'): React.ReactNode {
  const parts: React.ReactNode[] = [];
  let currentIndex = 0;
  const boldRegex = /\*\*(.*?)\*\*/g;
  let match;
  
  while ((match = boldRegex.exec(text)) !== null) {
    if (match.index > currentIndex) {
      parts.push(text.substring(currentIndex, match.index));
    }
    parts.push(
      <strong key={match.index} style={{ fontWeight: 700, color: boldColor }}>
        {match[1]}
      </strong>
    );
    currentIndex = boldRegex.lastIndex;
  }
  
  if (currentIndex < text.length) {
    parts.push(text.substring(currentIndex));
  }
  
  return parts.length > 0 ? parts : text;
}

function renderMessageContent(content: string, isAi: boolean = true): React.ReactNode {
  if (!content) return null;
  const lines = content.split('\n');
  const boldColor = isAi ? '#000000' : '#ffffff';
  
  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      gap: '6px', 
      fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
      fontSize: '14px',
      lineHeight: '1.6',
      color: isAi ? '#202124' : '#ffffff'
    }}>
      {lines.map((line, lineIndex) => {
        let trimmed = line.trim();
        
        if (trimmed === '---' || trimmed === '***' || trimmed === '___') {
          return <hr key={lineIndex} style={{ border: 'none', borderTop: '1px solid #e8eaed', margin: '8px 0' }} />;
        }
        
        if (trimmed.startsWith('#')) {
          const headingLevel = (trimmed.match(/^#+/) || [''])[0].length;
          const headingText = trimmed.replace(/^#+\s*/, '');
          const fontSize = headingLevel === 1 ? '18px' : headingLevel === 2 ? '16px' : '15px';
          return (
            <div key={lineIndex} style={{ 
              fontWeight: 700, 
              fontSize, 
              marginTop: '10px', 
              marginBottom: '4px', 
              color: isAi ? '#111111' : '#ffffff' 
            }}>
              {parseBold(headingText, boldColor)}
            </div>
          );
        }
        
        const bulletMatch = trimmed.match(/^[\*\-]\s+(.*)$/);
        if (bulletMatch) {
          const restText = bulletMatch[1];
          return (
            <div key={lineIndex} style={{ display: 'flex', gap: '8px', paddingLeft: '8px', alignItems: 'flex-start' }}>
              <span style={{ color: isAi ? '#1a73e8' : '#ffffff', flexShrink: 0, marginTop: '2px' }}>•</span>
              <div style={{ flex: 1 }}>{parseBold(restText, boldColor)}</div>
            </div>
          );
        }
        
        const numMatch = trimmed.match(/^(\d+)\.\s+(.*)$/);
        if (numMatch) {
          const num = numMatch[1];
          const restText = numMatch[2];
          return (
            <div key={lineIndex} style={{ display: 'flex', gap: '8px', paddingLeft: '8px', alignItems: 'flex-start' }}>
              <span style={{ color: isAi ? '#1a73e8' : '#ffffff', flexShrink: 0, fontWeight: 600 }}>{num}.</span>
              <div style={{ flex: 1 }}>{parseBold(restText, boldColor)}</div>
            </div>
          );
        }
        
        if (trimmed === '') {
          return <div key={lineIndex} style={{ height: '6px' }} />;
        }
        
        return (
          <div key={lineIndex}>
            {parseBold(trimmed, boldColor)}
          </div>
        );
      })}
    </div>
  );
}

export default function AiChatbot() {
  const user = useAuthStore((state) => state.user);
  const userIdentity = user?.id || user?.email || user?.username || user?.name;
  const userName = user?.name || user?.username || user?.email?.split('@')[0] || 'bạn';
  const userSessionKey = userIdentity ? `${SESSION_STORAGE_KEY}_${userIdentity}` : SESSION_STORAGE_KEY;

  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [sessionId, setSessionId] = useState<string | undefined>(() => {
    return localStorage.getItem(userSessionKey) || undefined;
  });
  const [status, setStatus] = useState<AiAvailability | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      role: 'ai',
      content: `Chào ${userName}, mình có thể hỗ trợ đọc tình hình thu chi, mục tiêu tiết kiệm và gợi ý hành động tài chính.`,
    },
  ]);
  const [loading, setLoading] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (userSessionKey !== SESSION_STORAGE_KEY) {
      localStorage.removeItem(SESSION_STORAGE_KEY);
    }
    const storedSessionId = localStorage.getItem(userSessionKey);
    setSessionId(storedSessionId || undefined);
    setMessages([
      {
        id: 'welcome',
        role: 'ai',
        content: `Chào ${userName}, mình có thể hỗ trợ đọc tình hình thu chi, mục tiêu tiết kiệm và gợi ý hành động tài chính.`,
      },
    ]);
  }, [userIdentity, userSessionKey, userName]);

  useEffect(() => {
    getAiStatus()
      .then(setStatus)
      .catch(() => setStatus(null));
  }, []);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' });
  }, [messages, loading]);

  const saveSessionId = (nextSessionId?: string) => {
    setSessionId(nextSessionId);
    if (nextSessionId) {
      localStorage.setItem(userSessionKey, nextSessionId);
    }
  };

  const resolveResponseContent = (response: Awaited<ReturnType<typeof sendAiChat>>) => {
    if (response.errorCode && CANNOT_ANSWER_CODES.has(response.errorCode)) {
      return CANNOT_ANSWER_TEXT;
    }
    if (response.errorCode) {
      return errorText[response.errorCode] || CANNOT_ANSWER_TEXT;
    }
    return response.reply || CANNOT_ANSWER_TEXT;
  };

  const appendAiResponse = (response: Awaited<ReturnType<typeof sendAiChat>>) => {
    saveSessionId(response.sessionId);
    setMessages((prev) => [
      ...prev,
      {
        id: `${Date.now()}-a`,
        role: 'ai',
        content: resolveResponseContent(response),
        citations: response.citations || [],
        errorCode: response.errorCode,
      },
    ]);
  };

  const isSessionOwnerError = (error: any) => {
    const statusCode = error?.response?.status;
    const message = error?.response?.data?.message || '';
    return statusCode === 403 && /session does not belong/i.test(message);
  };

  const handleSend = async () => {
    const text = input.trim();
    if (!text || loading) {
      return;
    }

    setInput('');
    setLoading(true);
    setMessages((prev) => [...prev, { id: `${Date.now()}-u`, role: 'user', content: text }]);

    try {
      const response = await sendAiChat(text, sessionId);
      setSessionId(response.sessionId);
      if (response.sessionId) {
        localStorage.setItem(userSessionKey, response.sessionId);
      }
      setMessages((prev) => [
        ...prev,
        {
          id: `${Date.now()}-a`,
          role: 'ai',
          content: resolveResponseContent(response),
          citations: response.citations || [],
          errorCode: response.errorCode,
        },
      ]);
      getAiStatus().then(setStatus).catch(() => undefined);
    } catch (error: any) {
      if (isSessionOwnerError(error)) {
        localStorage.removeItem(userSessionKey);
        setSessionId(undefined);
        try {
          const response = await sendAiChat(text);
          appendAiResponse(response);
          getAiStatus().then(setStatus).catch(() => undefined);
          return;
        } catch (retryError: any) {
          error = retryError;
        }
      }
      setMessages((prev) => [
        ...prev,
        {
          id: `${Date.now()}-e`,
          role: 'ai',
          content: CANNOT_ANSWER_TEXT,
          errorCode: 'AI_UNAVAILABLE',
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const renderStatus = () => {
    if (!status) {
      return null;
    }
    if (status.proRequired && !status.aiAccessible) {
      return (
        <Alert
          className={styles.status}
          type="warning"
          showIcon
          message={status.reason || 'Vui lòng nâng cấp gói Pro để sử dụng trợ lý AI tài chính.'}
        />
      );
    }
    if (!status.aiAvailable) {
      return (
        <Alert
          className={styles.status}
          type="warning"
          showIcon
          message={status.reason || 'AI chưa sẵn sàng'}
        />
      );
    }
    if (status.ragEnabled && !status.ragAvailable) {
      return (
        <Alert
          className={styles.status}
          type="info"
          showIcon
          message="AI sẵn sàng, RAG đang chưa có dữ liệu hoặc đang rebuild."
        />
      );
    }
    return null;
  };

  return (
    <div className={styles.aiChatbot}>
      <div className={styles.headerIcon} onClick={() => setOpen(!open)}>
        <RobotOutlined style={{ color: open ? '#1a73e8' : undefined }} />
      </div>

      {open && (
        <div className={styles.panel}>
        <div className={styles.header}>
          <div className={styles.headerTitle}>
            <Text strong style={{ color: '#fff' }}>Smart Finance AI</Text>
            <Text style={{ color: 'rgba(255,255,255,0.82)', fontSize: 12 }}>
              Tư vấn tài chính
            </Text>
          </div>
          <Button type="text" icon={<CloseOutlined />} onClick={() => setOpen(false)} style={{ color: '#fff' }} />
        </div>

        <div className={styles.messages} ref={scrollRef}>
          {renderStatus()}
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`${styles.messageRow} ${msg.role === 'user' ? styles.messageRowUser : ''}`}
            >
              <div className={`${styles.bubble} ${msg.role === 'user' ? styles.userBubble : styles.aiBubble}`}>
                {msg.errorCode && <Tag color="warning">{msg.errorCode}</Tag>}
                <div>{renderMessageContent(msg.content, msg.role === 'ai')}</div>
                {!!msg.citations?.length && (
                  <div className={styles.citations}>
                    {msg.citations.map((citation, index) => (
                      <Tag key={`${citation.sourceKey || citation.title || index}`} color="blue">
                        {citation.title || citation.sourceKey || `Nguồn ${index + 1}`}
                      </Tag>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}
          {loading && (
            <Space>
              <Spin size="small" />
              <Text type="secondary">AI đang phân tích...</Text>
            </Space>
          )}
        </div>

        <div className={styles.footer}>
          <Input.Search
            value={input}
            placeholder="Hỏi về chi tiêu, tiết kiệm, ngân sách..."
            enterButton={<SendOutlined />}
            loading={loading}
            onChange={(e) => setInput(e.target.value)}
            onSearch={handleSend}
            disabled={loading || !!(status?.proRequired && !status.aiAccessible)}
          />
        </div>
        </div>
      )}
    </div>
  );
}
