import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Typography, Space, Button, Skeleton, Select, Avatar } from 'antd';
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

const { Title, Text } = Typography;

const CHART_COLORS = {
  pieColors: [],
  columnColors: [],
};

export default function DashboardBlank() {
  const [pieData, setPieData] = useState<any[]>([]);
  const [columnData, setColumnData] = useState<any[]>([]);
  const [recentTransactions, setRecentTransactions] = useState<any[]>([]);
  const [loadingRecent, setLoadingRecent] = useState(false);

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

  const columnConfig = {
    data: columnData,
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
    ...(CHART_COLORS.columnColors && CHART_COLORS.columnColors.length > 0 ? {
      scale: {
        color: {
          range: CHART_COLORS.columnColors,
        },
      },
    } : {}),
  };

  return (
    <div className={styles.dashboardContainer}>
      <div className={styles.header}>
        <div>
          <Title level={2} className={styles.title}>Tổng quan</Title>
          <Text className={styles.subtitle}>Dưới đây là thông tin phân tích tài chính của bạn hôm nay.</Text>
        </div>
        <Space size="middle">
          <Button icon={<FlagOutlined />}
            onClick={() => history.push('')}
          >
            Mục tiêu mới
          </Button>
          <Button icon={<UserAddOutlined />}
            onClick={() => history.push('')}
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
                    defaultValue="6m"
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
              </div>
              
              <div style={{ padding: '16px 0' }}>
                <Skeleton active paragraph={{ rows: 5 }} />
              </div>

              <Button block className={styles.reviewBtn} disabled>Xem tất cả gợi ý</Button>
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
