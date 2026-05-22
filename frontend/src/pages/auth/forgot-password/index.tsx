import { Button, Form, Input, Typography, message } from 'antd';
import { LockOutlined, MailOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { Link, history } from 'umi';
import styles from './index.less';
import { useState } from 'react';
import api from '../../../utils/api';

const { Title, Text } = Typography;

export default function ForgotPassword() {
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await api.post('/auth/forgot-password', {
        email: values.email,
        newPassword: values.newPassword,
      });

      message.success('Đã gửi mã xác nhận OTP qua email của bạn!');

      history.push('/auth/otp', { email: values.email, type: 'reset', newPassword: values.newPassword });
    } catch (error: any) {
      console.error('Reset password request failed:', error);
      const errorMsg = error.response?.data?.message || 'Email không tồn tại trong hệ thống!';
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.forgotContainer}>
      <div className={styles.brandHeader}>
        <SafetyCertificateOutlined className={styles.brandIcon} />
        <span className={styles.brandName}>Smart Finance</span>
      </div>

      <div className={styles.header}>
        <Title level={3} className={styles.title}>Đặt lại mật khẩu</Title>
        <Text type="secondary" className={styles.subtitle}>
          Vui lòng nhập email và mật khẩu mới để phục hồi tài khoản của bạn.
        </Text>
      </div>

      <Form
        name="reset_password"
        className={styles.forgotForm}
        onFinish={onFinish}
        layout="vertical"
        size="large"
      >
        <Form.Item
          label={<span className={styles.inputLabel}>Email của bạn</span>}
          name="email"
          rules={[
            { required: true, message: 'Vui lòng nhập Email!' },
            { type: 'email', message: 'Email không đúng định dạng!' }
          ]}
        >
          <Input
            prefix={<MailOutlined className={styles.inputIcon} />}
            placeholder="Nhập email tài khoản"
          />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Mật khẩu mới</span>}
          name="newPassword"
          rules={[
            { required: true, message: 'Vui lòng nhập mật khẩu mới!' },
            { min: 6, message: 'Mật khẩu phải dài từ 6 ký tự trở lên!' }
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
          dependencies={['newPassword']}
          rules={[
            { required: true, message: 'Vui lòng xác nhận mật khẩu mới!' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('newPassword') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('Hai mật khẩu nhập vào không khớp!'));
              },
            }),
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className={styles.inputIcon} />}
            placeholder="••••••••"
          />
        </Form.Item>

        <Form.Item style={{ marginBottom: '24px', marginTop: '24px' }}>
          <Button type="primary" htmlType="submit" className={styles.submitButton} block loading={loading}>
            Tiếp tục {!loading && <span className={styles.arrowIcon}>→</span>}
          </Button>
        </Form.Item>

        <div className={styles.loginLink}>
          <Link to="/auth/login">Quay lại Đăng nhập</Link>
        </div>
      </Form>
    </div>
  );
}
