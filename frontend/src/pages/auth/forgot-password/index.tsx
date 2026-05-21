import { Button, Form, Input, Typography, message } from 'antd';
import { LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { Link } from 'umi';
import styles from './index.less';
import { useState } from 'react';

const { Title, Text } = Typography;

export default function ForgotPassword() {
  const [loading, setLoading] = useState(false);

  const onFinish = (values: any) => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      message.success('Reset password successfully!');
    }, 1000);
  };

  return (
    <div className={styles.forgotContainer}>
      <div className={styles.brandHeader}>
        <SafetyCertificateOutlined className={styles.brandIcon} />
        <span className={styles.brandName}>Smart Finance</span>
      </div>

      <div className={styles.header}>
        <Title level={3} className={styles.title}>Reset Password</Title>
        <Text type="secondary" className={styles.subtitle}>
          Please enter your new password to restore access to your account.
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
          label={<span className={styles.inputLabel}>New Password</span>}
          name="newPassword"
          rules={[
            { required: true, message: 'Please enter your new password!' },
            { min: 6, message: 'Password must be at least 6 characters!' }
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className={styles.inputIcon} />}
            placeholder="••••••••"
          />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Confirm New Password</span>}
          name="confirmPassword"
          dependencies={['newPassword']}
          rules={[
            { required: true, message: 'Please confirm your password!' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('newPassword') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('The two passwords do not match!'));
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
            Reset Password {!loading && <span className={styles.arrowIcon}>→</span>}
          </Button>
        </Form.Item>

        <div className={styles.loginLink}>
          <Link to="/auth/login">Back to Login</Link>
        </div>
      </Form>
    </div>
  );
}
