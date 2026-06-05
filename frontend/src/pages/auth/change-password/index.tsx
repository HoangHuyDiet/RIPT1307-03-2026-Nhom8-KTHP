import { Button, Form, Input, Typography, message } from 'antd';
import { LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { Link, history } from 'umi';
import { useState } from 'react';
import styles from './index.less';
import api from '../../../utils/api';
import { useAuthStore } from '../../../store/useAuthStore';

const { Title, Text } = Typography;

export default function ChangePassword() {
  const [loading, setLoading] = useState(false);
  const user = useAuthStore((s) => s.user);

  const onFinish = async (values: any) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('Mật khẩu xác nhận không khớp!');
      return;
    }

    if (values.newPassword === values.oldPassword) {
      message.error('Mật khẩu mới phải khác mật khẩu hiện tại!');
      return;
    }

    setLoading(true);
    
    try {
      await api.post('/auth/request-password-change', {
        oldPassword: values.oldPassword,
      });
      message.success('Mã OTP đã được gửi đến email của bạn!');
      history.push('/auth/otp', { 
        email: user?.email, 
        type: 'change-password', 
        oldPassword: values.oldPassword,
        newPassword: values.newPassword
      });
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || 'Không thể yêu cầu đổi mật khẩu. Vui lòng thử lại!';
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.loginContainer}>
      <div className={styles.brandHeader}>
        <SafetyCertificateOutlined className={styles.brandIcon} />
        <span className={styles.brandName}>Smart Finance</span>
      </div>

      <div className={styles.header}>
        <Title level={3} className={styles.title}>Thay đổi mật khẩu</Title>
        <Text type="secondary" className={styles.subtitle}>
          Vui lòng nhập mật khẩu hiện tại và mật khẩu mới của bạn.
        </Text>
      </div>

      <Form
        name="change_password"
        className={styles.loginForm}
        onFinish={onFinish}
        layout="vertical"
        size="large"
      >
        <Form.Item
          label={<span className={styles.inputLabel}>Mật khẩu hiện tại</span>}
          name="oldPassword"
          rules={[
            { required: true, message: 'Vui lòng nhập mật khẩu hiện tại!' },
            { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự!' }
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className={styles.inputIcon} />}
            placeholder="••••••••"
          />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Mật khẩu mới</span>}
          name="newPassword"
          rules={[
            { required: true, message: 'Vui lòng nhập mật khẩu mới!' },
            { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự!' }
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className={styles.inputIcon} />}
            placeholder="••••••••"
          />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Xác nhận mật khẩu mới</span>}
          name="confirmPassword"
          rules={[
            { required: true, message: 'Vui lòng xác nhận mật khẩu mới!' }
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className={styles.inputIcon} />}
            placeholder="••••••••"
          />
        </Form.Item>

        <Form.Item style={{ marginBottom: '16px' }}>
          <Button type="primary" htmlType="submit" className={styles.loginButton} block loading={loading}>
            Tiếp tục {!loading && <span className={styles.arrowIcon}>→</span>}
          </Button>
        </Form.Item>

        <div className={styles.backLink}>
          <Link to="/dashboard">Hủy và quay lại</Link>
        </div>
      </Form>
    </div>
  );
}
