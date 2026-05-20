import React from 'react';
import { Row, Col, Card, Typography, Space, Button, Skeleton, Divider, Select } from 'antd';
import {
  PlusOutlined,
  UserAddOutlined,
  FlagOutlined,
  WalletOutlined,
  RobotOutlined,
  MoreOutlined,
  ArrowUpOutlined,
  CoffeeOutlined,
  BankOutlined,
  ShoppingCartOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Title, Text } = Typography;

export default function DashboardBlank() {
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
                  <Text strong style={{ fontSize: '16px' }}>Tổng thanh khoản</Text>
                </Space>
                <Button type="text" icon={<MoreOutlined />} />
              </div>
              
              <div className={styles.liquidityContent}>
                <div className={styles.liquidityValue}>
                  <Skeleton.Input active size="large" style={{ width: 250, height: 48 }} />
                  <div style={{ marginTop: 8 }}>
                    <Skeleton.Input active size="small" style={{ width: 120 }} />
                  </div>
                </div>
                <div className={styles.liquidityChart}>
                  <Skeleton.Node active style={{ width: '100%', height: 120 }}>
                    <span style={{ color: '#bfbfbf' }}>[Biểu đồ đường]</span>
                  </Skeleton.Node>
                </div>
              </div>
            </Card>
            <Card bordered={false} className={styles.cashFlowCard}>
              <div className={styles.cardHeader}>
                <Text strong style={{ fontSize: '16px' }}>Dòng tiền</Text>
                <Space size="middle">
                  <Space size="small">
                    <div className={styles.legendColor} style={{ backgroundColor: '#34A853' }} />
                    <Text type="secondary" style={{ fontSize: '12px' }}>Thu nhập</Text>
                  </Space>
                  <Space size="small">
                    <div className={styles.legendColor} style={{ backgroundColor: '#EA4335' }} />
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
                 <Skeleton.Node active style={{ width: '100%', height: 250 }}>
                   <span style={{ color: '#bfbfbf' }}>[Biểu đồ cột]</span>
                 </Skeleton.Node>
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
