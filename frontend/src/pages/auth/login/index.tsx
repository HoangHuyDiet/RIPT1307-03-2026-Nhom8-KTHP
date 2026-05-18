import { Button, Form, Input, Checkbox, Typography, Divider, message } from 'antd';
import { LockOutlined, MailOutlined, GoogleOutlined, AppleOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { Link } from 'umi';
import styles from './index.less';
import { useState } from 'react';

const { Title, Text } = Typography;

export default function Login() {
  const [loading, setLoading] = useState(false);
  const onFinish = async (values: any) => {
    console.log('Received values of form: ', values);
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: values.email,
          password: values.password,
        }),
      });
      const data = await response.json();
      if (response.ok) {
        message.success('Đăng nhập thành công');
        localStorage.setItem('token', data.token);
        window.location.href = '/';
      } else {
        message.error(data.message ||'Đăng nhập thất bại, vui lòng thử lại!');
      }
    } catch (error) {
      console.error('Login failed:', error);
      message.error('Đăng nhập thất bại, vui lòng thử lại!');
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
        <Title level={3} className={styles.title}>Welcome back</Title>
        <Text type="secondary" className={styles.subtitle}>Please enter your details to sign in.</Text>
      </div>

      <Form
        name="normal_login"
        className={styles.loginForm}
        initialValues={{ remember: true }}
        onFinish={onFinish}
        layout="vertical" /* Hiển thị Label ở trên Input */
        size="large"
      >
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
        
        <div className={styles.formActions}>
          <Form.Item name="remember" valuePropName="checked" noStyle>
            <Checkbox className={styles.checkboxText}>Remember me</Checkbox>
          </Form.Item>
          <a className={styles.loginForgot} href="#">
            Forgot password?
          </a>
        </div>

        <Form.Item style={{ marginBottom: '24px' }}>
          <Button type="primary" htmlType="submit" className={styles.loginButton} block loading={loading}>
            Sign In {!loading &&<span className={styles.arrowIcon}>→</span>}
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

        <div className={styles.registerLink}>
          Don't have an account? <Link to="/auth/register">Sign up</Link>
        </div>
      </Form>
    </div>
  );
}
