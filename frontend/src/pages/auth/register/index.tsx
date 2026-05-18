import { Button, Form, Input, Typography, Divider, message } from 'antd';
import { LockOutlined, MailOutlined, GoogleOutlined, AppleOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons';
import { Link } from 'umi';
import styles from './index.less';
import { useState } from 'react';

const { Title, Text } = Typography;

export default function Register() {
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: any) => {
    console.log('Received values of form: ', values);
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: values.name,
          email: values.email,
          password: values.password,
        }),
      });
      const data = await response.json();
      if (response.ok) {
        message.success('Đăng ký thành công, vui lòng đăng nhập!');
        window.location.href = '/auth/login';
      } else {
        message.error(data.message || 'Đăng ký thất bại, vui lòng thử lại!');
      }
    } catch (error) {
      console.error('Registration failed:', error);
      message.error('Đăng ký thất bại, vui lòng thử lại!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.registerContainer}>
      <div className={styles.brandHeader}>
        <SafetyCertificateOutlined className={styles.brandIcon} />
        <span className={styles.brandName}>Smart Finance</span>
      </div>

      <div className={styles.header}>
        <Title level={3} className={styles.title}>Create an account</Title>
        <Text type="secondary" className={styles.subtitle}>Sign up to get started with Smart Finance.</Text>
      </div>

      <Form
        name="normal_register"
        className={styles.registerForm}
        onFinish={onFinish}
        layout="vertical"
        size="large"
      >
        <Form.Item
          label={<span className={styles.inputLabel}>Full Name</span>}
          name="name"
          rules={[
            { required: true, message: 'Please enter your name!' }
          ]}
        >
          <Input prefix={<UserOutlined className={styles.inputIcon} />} placeholder="Enter your name" />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Email</span>}
          name="email"
          rules={[
            { required: true, message: 'Please enter your email!' },
            { type: 'email', message: 'Invalid email address!' }
          ]}
        >
          <Input prefix={<MailOutlined className={styles.inputIcon} />} placeholder="Enter your email" />
        </Form.Item>
        
        <Form.Item
          label={<span className={styles.inputLabel}>Password</span>}
          name="password"
          rules={[{ required: true, message: 'Please enter your password!' }]}
        >
          <Input.Password
            prefix={<LockOutlined className={styles.inputIcon} />}
            placeholder="••••••••"
          />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Confirm Password</span>}
          name="confirm"
          dependencies={['password']}
          rules={[
            { required: true, message: 'Please confirm your password!' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('password') === value) {
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

        <Form.Item style={{ marginBottom: '24px', marginTop: '12px' }}>
          <Button type="primary" htmlType="submit" className={styles.registerButton} block loading={loading}>
            Sign Up {!loading && <span className={styles.arrowIcon}>→</span>}
          </Button>
        </Form.Item>
        
        <Divider className={styles.divider}>Or continue with</Divider>

        <div className={styles.socialLogin}>
          <Button className={styles.socialBtn} icon={<GoogleOutlined />}>
            Google
          </Button>
          <Button className={styles.socialBtn} icon={<AppleOutlined />}>
            Apple
          </Button>
        </div>

        <div className={styles.loginLink}>
          Already have an account? <Link to="/auth/login">Sign in</Link>
        </div>
      </Form>
    </div>
  );
}
