import { Button, Form, Input, Typography, Divider, message } from 'antd';
import { LockOutlined, MailOutlined, GoogleOutlined, AppleOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons';
import { Link } from 'umi';
import styles from './index.less';
import { useState } from 'react';
import { history } from 'umi';
import api from '../../../utils/api';
import { useGoogleLogin } from '@react-oauth/google';
import { useAuthStore } from '../../../store/useAuthStore';


const { Title, Text } = Typography;

export default function Register() {
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
      await api.post('/auth/register', {
        displayName: values.name,
        email: values.email,
        password: values.password,
      });

      message.success('Đã gửi mã xác nhận qua email của bạn!');

      history.push('/auth/otp', { email: values.email, type: 'register' });

    } catch (error: any) {
      console.error('Registration failed:', error);
      const errorMsg = error.response?.data?.message || 'Đăng ký thất bại, vui lòng thử lại!';
      message.error(errorMsg);
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
          label={<span className={styles.inputLabel}>Họ và tên</span>}
          name="name"
          rules={[
            { required: true, message: 'Vui lòng nhập họ và tên của bạn!' }
          ]}
        >
          <Input prefix={<UserOutlined className={styles.inputIcon} />} placeholder="Nhập họ và tên của bạn" />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Email</span>}
          name="email"
          rules={[
            { required: true, message: 'Vui lòng nhập email của bạn!' },
            { type: 'email', message: 'Email không đúng định dạng!' }
          ]}
        >
          <Input prefix={<MailOutlined className={styles.inputIcon} />} placeholder="Nhập email của bạn" />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Password</span>}
          name="password"
          rules={[{ required: true, message: 'Vui lòng nhập mật khẩu!' }, { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự!' }]}
        >
          <Input.Password
            prefix={<LockOutlined className={styles.inputIcon} />}
            placeholder="••••••••"
          />
        </Form.Item>

        <Form.Item
          label={<span className={styles.inputLabel}>Xác nhận mật khẩu</span>}
          name="confirm"
          dependencies={['password']}
          rules={[
            { required: true, message: 'Vui lòng xác nhận mật khẩu!' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('password') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('Mật khẩu không trùng khớp!'));
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

        <div className={styles.loginLink}>
          Already have an account? <Link to="/auth/login">Sign in</Link>
        </div>
      </Form>
    </div>
  );
}
