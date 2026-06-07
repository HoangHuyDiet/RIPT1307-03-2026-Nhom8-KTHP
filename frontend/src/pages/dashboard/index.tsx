import React, { useState, useEffect, useMemo } from 'react';
import { Row, Col, Card, Typography, Space, Button, Skeleton, Select, Avatar, Alert, Tag } from 'antd';
import {
  PlusOutlined,
  UserAddOutlined,
  FlagOutlined,
  WalletOutlined,
  RobotOutlined,
  MoreOutlined,
  CoffeeOutlined,
  WarningOutlined,
  LaptopOutlined,
  CheckCircleOutlined,
  GlobalOutlined,
  CalendarOutlined,
} from '@ant-design/icons';
import { Pie, Column } from '@ant-design/charts';
import styles from './index.less';
import { history } from 'umi';
import dayjs from 'dayjs';
import api from '../../utils/api';
import { getAiInsight, getAiStatus, type AiAvailability, type AiInsight } from '@/services/ai';

const { Title, Text } = Typography;

const CHART_COLORS = {
  pieColors: [],
  columnColors: [],
};

const FREE_AI_TIPS = [
  'Theo dõi 3 nhóm chi tiêu lớn nhất mỗi tuần để phát hiện khoản vượt ngân sách sớm.',
  'Ưu tiên giữ quỹ dự phòng tối thiểu 1-3 tháng chi phí thiết yếu trước khi tăng đầu tư.',
  'Nếu tiết kiệm ròng đang thấp, hãy thử đặt giới hạn chi tiêu theo danh mục trước ngày nhận lương tiếp theo.',
];

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

function renderMarkdown(content: string): React.ReactNode {
  if (!content) return null;
  const lines = content.split('\n');
  const boldColor = '#0f172a';
  
  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      gap: '6px', 
      fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
      fontSize: '14px',
      lineHeight: '1.6',
      color: '#202124'
    }}>
      {lines.map((line, lineIndex) => {
        let trimmed = line.trim();
        
        if (trimmed === '---' || trimmed === '***' || trimmed === '___') {
          return <hr key={lineIndex} style={{ border: 'none', borderTop: '1px solid #e5e7eb', margin: '12px 0' }} />;
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
              color: '#0f172a' 
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
              <span style={{ color: '#1a73e8', flexShrink: 0, marginTop: '2px' }}>•</span>
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
              <span style={{ color: '#1a73e8', flexShrink: 0, fontWeight: 600 }}>{num}.</span>
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

export default function DashboardBlank() {
  const [pieData, setPieData] = useState<any[]>([]);
  const [columnData, setColumnData] = useState<any[]>([]);
  const [recentTransactions, setRecentTransactions] = useState<any[]>([]);
  const [loadingRecent, setLoadingRecent] = useState(false);
  const [aiStatus, setAiStatus] = useState<AiAvailability | null>(null);
  const [aiInsight, setAiInsight] = useState<AiInsight | null>(null);
  const [loadingAiInsight, setLoadingAiInsight] = useState(false);
  const [aiInsightError, setAiInsightError] = useState<string | null>(null);
  const [loadingStats, setLoadingStats] = useState(false);
  const [cashFlowFilter, setCashFlowFilter] = useState('6m');

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      setLoadingRecent(true);
      try {
        const res = await api.get('/transactions', {
          params: { page: 0, size: 5 }
        });
        setRecentTransactions(res.data.data?.content || []);
      } catch (err) {
        console.error('Không thể lấy danh sách hoạt động gần đây:', err);
      } finally {
        setLoadingRecent(false);
      }
    };
    fetchRecentTransactions();
  }, []);

  useEffect(() => {
    const fetchDashboardStats = async () => {
      setLoadingStats(true);
      try {
        const currentMonth = new Date().getMonth() + 1;
        const currentYear = new Date().getFullYear();

        const [pieRes, columnRes] = await Promise.all([
          api.get(`/statistics/expense-by-category?month=${currentMonth}&year=${currentYear}`),
          api.get(`/statistics/cash-flow?year=${currentYear}`)
        ]);

        const pieFormatted = pieRes.data.data.map((item: any) => ({
          category: item.categoryName,
          value: item.value
        }));
        setPieData(pieFormatted);

        const columnFormatted = columnRes.data.data.map((item: any) => ({
          month: `Tháng ${item.month}`,
          type: item.type === 'INCOME' ? 'Thu nhập' : 'Chi tiêu',
          amount: item.amount
        }));
        setColumnData(columnFormatted);

      } catch (error) {
        console.error("Lỗi khi tải dữ liệu thống kê:", error);
      } finally {
        setLoadingStats(false);
      }
    };
    fetchDashboardStats();
  }, []);

  const fetchAiInsight = async (forceRefresh = false) => {
    setLoadingAiInsight(true);
    setAiInsightError(null);
    try {
      const [status, insight] = await Promise.all([
        getAiStatus(),
        getAiInsight(undefined, forceRefresh),
      ]);
      setAiStatus(status);
      setAiInsight(insight);
    } catch (err: any) {
      setAiInsightError(err?.response?.data?.message || 'Không thể tải gợi ý AI.');
    } finally {
      setLoadingAiInsight(false);
    }
  };

  useEffect(() => {
    fetchAiInsight();
  }, []);

  const pieConfig = {
    data: pieData,
    angleField: 'value',
    colorField: 'category',
    radius: 0.8,
    label: {
      text: 'value',
      position: 'outside',
    },
    legend: {
      color: {
        position: 'right',
      },
    },
    tooltip: {
      items: [
        {
          channel: 'y',
          valueFormatter: (value: any) => `${value.toLocaleString('vi-VN')} đ`,
        },
      ],
    },
    ...(CHART_COLORS.pieColors && CHART_COLORS.pieColors.length > 0 ? {
      scale: {
        color: {
          range: CHART_COLORS.pieColors,
        },
      },
    } : {}),
  };

  const filteredColumnData = useMemo(() => {
    if (!columnData.length) return [];
    
    let monthsToKeep = 6;
    if (cashFlowFilter === '1m') monthsToKeep = 1;
    if (cashFlowFilter === '3m') monthsToKeep = 3;
    if (cashFlowFilter === '6m') monthsToKeep = 6;
    if (cashFlowFilter === '1y') monthsToKeep = 12;
    if (cashFlowFilter === '2y') monthsToKeep = 24;
    if (cashFlowFilter === '3y') monthsToKeep = 36;
    
    const uniqueMonths = Array.from(new Set(columnData.map(d => d.month)));
    
    uniqueMonths.sort((a, b) => {
      const m1 = parseInt(a.replace('Tháng ', ''));
      const m2 = parseInt(b.replace('Tháng ', ''));
      return m1 - m2;
    });

    const recentMonths = uniqueMonths.slice(-monthsToKeep);
    return columnData.filter(d => recentMonths.includes(d.month));
  }, [columnData, cashFlowFilter]);

  const columnConfig = {
    data: filteredColumnData,
    xField: 'month',
    yField: 'amount',
    colorField: 'type',
    isGroup: true,
    group: true,
    legend: false,
    tooltip: {
      items: [
        {
          channel: 'y',
          valueFormatter: (value: any) => `${value.toLocaleString('vi-VN')} đ`,
        },
      ],
    },
    scale: {
      color: {
        domain: ['Thu nhập', 'Chi tiêu'],
        range: ['#34A853', '#EA4335'],
      },
    },
    yAxis: {
      type: 'log',
      label: {
        formatter: (v: any) => {
          const num = Number(v);
          if (num >= 1000000000) return `${(num / 1000000000).toFixed(0)} Tỷ`;
          if (num >= 1000000) return `${(num / 1000000).toFixed(0)} Tr`;
          if (num >= 1000) return `${(num / 1000).toFixed(0)} N`;
          return num.toString();
        },
      },
    },
  };

  const showFreeAiTips = !!aiInsight?.errorCode || !!(aiStatus && !aiStatus.aiAvailable);

  return (
    <div className={styles.dashboardContainer}>
      <div className={styles.header}>
        <div>
          <Title level={2} className={styles.title}>Tổng quan</Title>
          <Text className={styles.subtitle}>Dưới đây là thông tin phân tích tài chính của bạn hôm nay.</Text>
        </div>
        <Space size="middle">
          <Button icon={<FlagOutlined />}
            onClick={() => history.push('/saving-goals')}
          >
            Mục tiêu mới
          </Button>
          <Button icon={<UserAddOutlined />}
            onClick={() => history.push('/shared-funds')}
          >
            Mời
          </Button>
          <Button type="primary" icon={<PlusOutlined />}
            onClick={() => history.push('/transactions')}
          >
            Thêm giao dịch
          </Button>
        </Space>
      </div>

      <Row gutter={[24, 24]} className={styles.mainContent}>
        <Col xs={24} lg={15} xl={16}>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            
            <Card bordered={false} className={styles.liquidityCard}>
              <div className={styles.cardHeaderWithMore}>
                <Space>
                  <div className={styles.iconWrapper}>
                    <WalletOutlined />
                  </div>
                  <Text strong style={{ fontSize: '16px' }}>Chi tiêu theo danh mục</Text>
                </Space>
                <Button type="text" icon={<MoreOutlined />} />
              </div>
              
              <div className={styles.liquidityContent}>
                <div className={styles.liquidityChart} style={{ height: 260 }}>
                  <Pie {...pieConfig} />
                </div>
              </div>
            </Card>

            <Card bordered={false} className={styles.cashFlowCard}>
              <div className={styles.cardHeader}>
                <Text strong style={{ fontSize: '16px' }}>Dòng tiền</Text>
                <Space size="middle">
                  <Space size="small">
                    <div
                      className={styles.legendColor}
                      style={{ backgroundColor: CHART_COLORS.columnColors[0] || '#34A853' }}
                    />
                    <Text type="secondary" style={{ fontSize: '12px' }}>Thu nhập</Text>
                  </Space>
                  <Space size="small">
                    <div
                      className={styles.legendColor}
                      style={{ backgroundColor: CHART_COLORS.columnColors[1] || '#EA4335' }}
                    />
                    <Text type="secondary" style={{ fontSize: '12px' }}>Chi phí</Text>
                  </Space>
                  <Select
                    value={cashFlowFilter}
                    onChange={(val) => setCashFlowFilter(val)}
                    size="small"
                    style={{ width: 110 }}
                    bordered={false}
                    className={styles.filterSelect}
                    options={[
                      { value: '1m', label: '1 tháng' },
                      { value: '3m', label: '3 tháng' },
                      { value: '6m', label: '6 tháng' },
                      { value: '1y', label: '1 năm' },
                      { value: '2y', label: '2 năm' },
                      { value: '3y', label: '3 năm' },
                    ]}
                  />
                </Space>
              </div>
              
              <div className={styles.cashFlowChart}>
                 <Column {...columnConfig} />
              </div>
            </Card>

          </Space>
        </Col>
        <Col xs={24} lg={9} xl={8}>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card bordered={true} className={styles.aiInsightsCard}>
              <div className={styles.cardHeader}>
                <Space>
                  <RobotOutlined style={{ color: '#1A73E8', fontSize: '18px' }} />
                  <Text strong style={{ fontSize: '16px' }}>Gợi ý từ AI</Text>
                </Space>
                {aiInsight?.fromCache && <Tag color="blue">Cache</Tag>}
              </div>

              <div style={{ padding: '16px 0' }}>
                {loadingAiInsight ? (
                  <Skeleton active paragraph={{ rows: 5 }} />
                ) : aiInsightError ? (
                  <Alert
                    type="error"
                    showIcon
                    message="AI chưa sẵn sàng"
                    description={aiInsightError}
                  />
                ) : aiInsight ? (
                  <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    {aiStatus?.proRequired && !aiStatus.aiAccessible && (
                      <Alert
                        type="warning"
                        showIcon
                        message="Tính năng AI dành cho gói Pro"
                        description={aiStatus.reason || 'Vui lòng nâng cấp gói Pro để sử dụng trợ lý AI tài chính.'}
                      />
                    )}
                    {aiStatus && !aiStatus.aiAvailable && !(aiStatus.proRequired && !aiStatus.aiAccessible) && (
                      <Alert
                        type="warning"
                        showIcon
                        message="AI đang tắt hoặc chưa cấu hình"
                        description={aiStatus.reason || 'Hãy kiểm tra AI_ENABLED và GEMINI_API_KEY.'}
                      />
                    )}
                    {aiStatus?.ragEnabled && !aiStatus.ragAvailable && (
                      <Alert
                        type="info"
                        showIcon
                        message="RAG chưa sẵn sàng"
                        description="AI vẫn phân tích dữ liệu cá nhân, nhưng chưa dùng kho kiến thức bổ trợ."
                      />
                    )}
                    {showFreeAiTips && (
                      <div className={styles.freeAiTips}>
                        <Text strong>Gợi ý miễn phí</Text>
                        {FREE_AI_TIPS.map((tip) => (
                          <div key={tip} className={styles.freeAiTipItem}>
                            <span>✓</span>
                            <Text>{tip}</Text>
                          </div>
                        ))}
                      </div>
                    )}
                    {!showFreeAiTips && (
                      <div style={{ color: '#202124' }}>
                        {renderMarkdown(aiInsight.summary)}
                      </div>
                    )}
                    {aiInsight.generatedAt && (
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        Cập nhật: {dayjs(aiInsight.generatedAt).format('DD/MM/YYYY HH:mm')}
                      </Text>
                    )}
                  </Space>
                ) : (
                  <Alert
                    type="info"
                    showIcon
                    message="Chưa có gợi ý AI"
                    description="Hãy thêm giao dịch để AI có dữ liệu phân tích."
                  />
                )}
              </div>

              <Button
                block
                className={styles.reviewBtn}
                loading={loadingAiInsight}
                disabled={!!(aiStatus?.proRequired && !aiStatus.aiAccessible)}
                onClick={() => fetchAiInsight(true)}
              >
                Làm mới gợi ý AI
              </Button>
            </Card>

            <Card bordered={false} className={styles.recentActivityCard}>
              <div className={styles.cardHeader}>
                <Text strong style={{ fontSize: '16px' }}>Hoạt động gần đây</Text>
                <Button
                  type="text"
                  onClick={() => history.push('/transactions')}
                  style={{ padding: 0, height: 'auto' }}
                >
                  <span style={{ fontSize: '13px', color: '#1a73e8', fontWeight: 600, cursor: 'pointer' }}>
                    Xem tất cả
                  </span>
                </Button>
              </div>
              
              <div style={{ marginTop: 20 }}>
                {loadingRecent ? (
                  <Skeleton active avatar paragraph={{ rows: 4 }} />
                ) : recentTransactions.length === 0 ? (
                  <div style={{ textAlign: 'center', padding: '24px 0', color: '#bfbfbf', fontSize: '13px' }}>
                    Chưa có giao dịch nào gần đây
                  </div>
                ) : (
                  <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    {recentTransactions.map((item: any) => {
                      let avatarIcon = <CoffeeOutlined />;
                      let avatarBg = '#f0f2f5';
                      let avatarColor = '#595959';

                      if (item.savingGoalId) {
                        avatarIcon = <FlagOutlined />;
                        avatarBg = '#FFFBE6';
                        avatarColor = '#FAAD14';
                      } else if (item.id === 25 || item.description?.includes('bất thường')) {
                        avatarIcon = <WarningOutlined />;
                        avatarBg = '#FFF1F0';
                        avatarColor = '#FF4D4F';
                      } else if (item.description?.includes('AWS')) {
                        avatarIcon = <LaptopOutlined />;
                        avatarBg = '#E6F7FF';
                        avatarColor = '#1890FF';
                      } else if (item.type === 'INCOME') {
                        avatarIcon = <CheckCircleOutlined />;
                        avatarBg = '#F6FFED';
                        avatarColor = '#52C41A';
                      } else if (item.description?.includes('Delta')) {
                        avatarIcon = <GlobalOutlined />;
                        avatarBg = '#F5F5F5';
                        avatarColor = '#8C8C8C';
                      }

                      const formattedAmount = new Intl.NumberFormat('vi-VN', { 
                        style: 'currency', 
                        currency: 'VND' 
                      }).format(item.amount);

                      return (
                        <div 
                          key={item.id} 
                          style={{ 
                            display: 'flex', 
                            justifyContent: 'space-between', 
                            alignItems: 'center', 
                            padding: '10px 0', 
                            borderBottom: '1px solid #f1f3f7' 
                          }}
                        >
                          <Space size="middle" align="center">
                            <Avatar 
                              size={36} 
                              icon={avatarIcon} 
                              style={{ backgroundColor: avatarBg, color: avatarColor }} 
                            />
                            <div style={{ display: 'flex', flexDirection: 'column' }}>
                              <Text 
                                strong 
                                style={{ 
                                  fontSize: '13px', 
                                  color: '#202124', 
                                  display: 'block', 
                                  maxWidth: '150px', 
                                  overflow: 'hidden', 
                                  textOverflow: 'ellipsis', 
                                  whiteSpace: 'nowrap' 
                                }} 
                                title={item.description}
                              >
                                {item.description}
                              </Text>
                              <Text type="secondary" style={{ fontSize: '11px' }}>
                                <CalendarOutlined style={{ marginRight: 4 }} />
                                {dayjs(item.date).format('DD/MM/YYYY')}
                              </Text>
                            </div>
                          </Space>
                          
                          <span style={{ 
                            fontWeight: 700, 
                            fontSize: '13px', 
                            color: item.type === 'INCOME' ? '#34a853' : '#1a1d20' 
                          }}>
                            {item.type === 'INCOME' ? `+${formattedAmount}` : `-${formattedAmount}`}
                          </span>
                        </div>
                      );
                    })}
                  </Space>
                )}
              </div>
            </Card>

          </Space>
        </Col>
      </Row>
    </div>
  );
}
