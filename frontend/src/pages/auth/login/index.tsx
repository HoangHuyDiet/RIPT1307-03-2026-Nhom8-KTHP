import { Button, Form, Input, Checkbox, Typography, Divider, message } from 'antd';
import { LockOutlined, MailOutlined, GoogleOutlined, AppleOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { Link } from 'umi';
import styles from './index.less';
import { useState } from 'react';
import { history } from 'umi';
import api from '../../../utils/api';
import { useAuthStore } from '../../../store/useAuthStore';
import { useGoogleLogin } from '@react-oauth/google';


const { Title, Text } = Typography;

export default function Login() {
  const [loading, setLoading] = useState(false);
  const loginWithGoogle = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      setLoading(true);
      try {
        const response = await api.post('/auth/google', {
          token: tokenResponse.access_token,
        });

        const data = response.data;
        message.success('Đăng nhập bằng Google thành công!');

        useAuthStore.getState().setAuth(data.token, data.user);
        history.push('/dashboard');
      } catch (error: any) {
        message.error('Đăng nhập bằng Google thất bại!');
      } finally {
        setLoading(false);
      }
    },
    onError: () => {
      message.error('Không thể kết nối với tài khoản Google');
    }
  });

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      const response = await api.post('/auth/login', {
        email: values.email,
        password: values.password,
      });

      const data = response.data;
      
      message.success('Đăng nhập thành công');
      
      const roles = data.data.roles || [];
      useAuthStore.getState().setAuth(
        data.data.token, 
        { email: data.data.email, name: data.data.displayName },
        roles
      );
      
      // Phân quyền: Admin/Support Admin → /admin/users, User → /dashboard
      if (roles.includes('ADMIN') || roles.includes('SUPPORT_ADMIN')) {
        history.push('/admin/users');
      } else {
        history.push('/dashboard');
      }

    } catch (error: any) {
      console.error('Login failed:', error);
      const errorMsg = error.response?.data?.message || 'Đăng nhập thất bại, vui lòng thử lại!';
      message.error(errorMsg);
      
      // Nếu tài khoản chưa xác thực OTP, chuyển hướng họ sang trang nhập OTP
      if (errorMsg.includes('chưa xác thực OTP')) {
        history.push('/auth/otp', { email: values.email, type: 'register' });
      }
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
        <Text type="secondary" className={styles.subtitle}>Vui lòng nhập thông tin để đăng nhập.</Text>
      </div>

      <Form
        name="normal_login"
        className={styles.loginForm}
        initialValues={{ remember: true }}
        onFinish={onFinish}
        layout="vertical"
        size="large"
      >
        <Form.Item
          label={<span className={styles.inputLabel}>Email</span>}
          name="email"
          rules={[
            { required: true, message: 'Vui lòng nhập email!' },
            { type: 'email', message: 'Email không đúng định dạng!' }
          ]}
        >
          <Input prefix={<MailOutlined className={styles.inputIcon} />} placeholder="Nhập email của bạn" />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Password</span>}
          name="password"
          rules={[{ required: true, message: 'Please enter your password!' }, { min: 6, message: 'Password must be at least 6 characters!' }]}
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
          <a className={styles.loginForgot} href="/auth/forgot-password">
            Forgot password?
          </a>
        </div>

        <Form.Item style={{ marginBottom: '24px' }}>
          <Button type="primary" htmlType="submit" className={styles.loginButton} block loading={loading}>
            Sign In {!loading && <span className={styles.arrowIcon}>→</span>}
          </Button>
        </Form.Item>

        <Divider className={styles.divider}>Or continue with</Divider>

        <div className={styles.socialLogin}>
          <Button
            className={styles.socialBtn}
            icon={<GoogleOutlined />}
            onClick={() => loginWithGoogle()}
            loading={loading}
          >
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
