import React from 'react';
import { Layout, Row, Col, Typography, Space, Divider } from 'antd';
import { 
  BankOutlined, 
  FacebookOutlined, 
  LinkedinOutlined, 
  TwitterOutlined,
  MailOutlined,
  PhoneOutlined
} from '@ant-design/icons';
import { Link } from 'umi';
import styles from './index.less';

const { Footer } = Layout;
const { Title, Text, Paragraph } = Typography;

export default function DefaultFooter() {
  return (
    <Footer className={styles.footerContainer}>
      <div className={styles.footerContent}>
        <Row gutter={[32, 32]}>
          <Col xs={24} sm={24} md={8} lg={8}>
            <div className={styles.brandSection}>
              <Space align="center" className={styles.logoSpace}>
                <BankOutlined className={styles.logoIcon} />
                <span className={styles.logoText}>Smart Finance</span>
              </Space>
              <Paragraph className={styles.descriptionText}>
                Nền tảng giúp bạn kiểm soát dòng tiền, tối ưu hóa chi tiêu và dễ dàng đạt được mọi mục tiêu tài chính.
              </Paragraph>
              <Space size="middle" className={styles.socialLinks}>
                <a href="#" className={styles.socialIcon}><FacebookOutlined /></a>
                <a href="#" className={styles.socialIcon}><LinkedinOutlined /></a>
                <a href="#" className={styles.socialIcon}><TwitterOutlined /></a>
              </Space>
            </div>
          </Col>

          <Col xs={24} sm={12} md={5} lg={5}>
            <Title level={5} className={styles.colTitle}>Sản phẩm</Title>
            <Space direction="vertical" size="small" className={styles.linkList}>
              <Link to="/dashboard" className={styles.footerLink}>Tổng quan</Link>
              <Link to="/personal-funds" className={styles.footerLink}>Quỹ cá nhân</Link>
              <Link to="/saving-goals" className={styles.footerLink}>Mục tiêu tiết kiệm</Link>
              <Link to="/subscriptions" className={styles.footerLink}>Gói dịch vụ</Link>
            </Space>
          </Col>

          <Col xs={24} sm={12} md={6} lg={6}>
            <Title level={5} className={styles.colTitle}>Hỗ trợ & Chính sách</Title>
            <Space direction="vertical" size="small" className={styles.linkList}>
              <Link to="#" className={styles.footerLink}>Trung tâm trợ giúp (FAQ)</Link>
              <Link to="#" className={styles.footerLink}>Điều khoản sử dụng</Link>
              <Link to="#" className={styles.footerLink}>Chính sách bảo mật</Link>
              <Link to="#" className={styles.footerLink}>Chính sách hoàn tiền</Link>
            </Space>
          </Col>

          <Col xs={24} sm={24} md={5} lg={5}>
            <Title level={5} className={styles.colTitle}>Liên hệ</Title>
            <Space direction="vertical" size="small" className={styles.linkList}>
              <Space className={styles.contactItem}>
                <MailOutlined className={styles.contactIcon} />
                <Text className={styles.contactText}>support@smartfinance.vn</Text>
              </Space>
              <Space className={styles.contactItem}>
                <PhoneOutlined className={styles.contactIcon} />
                <Text className={styles.contactText}>1900 1234</Text>
              </Space>
            </Space>
          </Col>
        </Row>
      </div>
      <Divider className={styles.divider} />
      <div className={styles.footerBottom}>
        <Text className={styles.copyrightText}>
          © 2026 Smart Finance. Bản quyền thuộc về Nhóm 8.
        </Text>
      </div>
    </Footer>
  );
}
