import React, { useState } from 'react';
import { Row, Col, Card, Typography, Space, Button, Skeleton, Select } from 'antd';
import {
  PlusOutlined,
  UserAddOutlined,
  FlagOutlined,
  WalletOutlined,
  RobotOutlined,
  MoreOutlined,
} from '@ant-design/icons';
import { Pie, Column } from '@ant-design/charts';
import styles from './index.less';

const { Title, Text } = Typography;

const CHART_COLORS = {
  pieColors: [],
  columnColors: [],
};

export default function DashboardBlank() {
  const [pieData, setPieData] = useState<any[]>([]);
  const [columnData, setColumnData] = useState<any[]>([]);

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
          <Button icon={<FlagOutlined />}>Mục tiêu mới</Button>
          <Button icon={<UserAddOutlined />}>Mời</Button>
          <Button type="primary" icon={<PlusOutlined />}>Thêm giao dịch</Button>
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
                <span style={{ fontSize: '13px', color: '#bfbfbf', cursor: 'not-allowed' }}>Xem tất cả</span>
              </div>
              
              <div style={{ marginTop: 24 }}>
                <Skeleton active avatar paragraph={{ rows: 4 }} />
              </div>
            </Card>

          </Space>
        </Col>
      </Row>
    </div>
  );
}
