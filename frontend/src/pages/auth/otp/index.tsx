import React, { useState, useEffect } from 'react';
import { Button, Typography, Divider, message } from 'antd';
import { Input } from 'antd';
import { SafetyCertificateOutlined } from '@ant-design/icons';
import { useLocation, history } from 'umi';
import styles from './index.less';
import api from '../../../utils/api';
import { useAuthStore } from '../../../store/useAuthStore';

const { Title, Text } = Typography;

interface LocationState {
  email?: string;
  type?: 'register' | 'login' | 'reset' | 'change-password';
  oldPassword?: string;
  newPassword?: string;
}

export default function OTPVerification() {
  const location = useLocation();
  const state = location.state as LocationState;

  const email = state?.email || '';
  const type = state?.type || 'register';
  const oldPassword = state?.oldPassword || '';
  const newPassword = state?.newPassword || '';

  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(60);
  const [canResend, setCanResend] = useState(false);

  useEffect(() => {
    if (!email) {
      message.error('Thiếu thông tin Email xác thực, đang quay lại trang đăng ký...');
      history.push('/auth/register');
      return;
    }

    let timer: NodeJS.Timeout;
    if (countdown > 0) {
      timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    } else {
      setCanResend(true);
    }
    return () => clearTimeout(timer);
  }, [countdown, email]);

  const onFinishOTP = async (otpValue: string) => {
    if (otpValue.length < 6) return;

    setLoading(true);
    try {
      if (type === 'change-password') {
        await api.post('/auth/change-password', {
          oldPassword: oldPassword,
          newPassword: newPassword,
          otpCode: otpValue,
        });
        message.success('Đổi mật khẩu thành công! Vui lòng đăng nhập lại.');
        useAuthStore.getState().logout();
        history.push('/auth/login');
        return;
      }

      if (type === 'reset') {
        await api.post('/auth/reset-password', {
          email: email,
          otpCode: otpValue,
          newPassword: newPassword,
        });
        message.success('Đổi mật khẩu thành công! Hãy đăng nhập với mật khẩu mới.');
        history.push('/auth/login');
      } else {
        const response = await api.post('/auth/verify-account', {
          email: email,
          otpCode: otpValue,
        });

        const data = response.data;
        message.success('Xác thực OTP thành công!');

        if (type === 'login') {
          useAuthStore.getState().setAuth(data.token, data.user);
          history.push('/dashboard');
        } else {
          history.push('/auth/login');
        }
      }
    } catch (error: any) {
      console.error('OTP Verification failed:', error);
      const errorMsg = error.response?.data?.message || 'Mã OTP không chính xác hoặc đã hết hạn!';
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!canResend) return;
    setLoading(true);
    try {
      await api.post('/2fa/resend-otp', { email });
      message.success('Đã gửi lại mã OTP mới vào Email của bạn!');
      setCountdown(60);
      setCanResend(false);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Không thể gửi lại mã OTP!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.otpContainer}>
      <div className={styles.brandHeader}>
        <SafetyCertificateOutlined className={styles.brandIcon} />
        <span className={styles.brandName}>Smart Finance</span>
      </div>

      <div className={styles.header}>
        <Title level={3} className={styles.title}>Xác thực OTP</Title>
        <Text type="secondary" className={styles.subtitle}>
          Mã xác thực đã được gửi tới email <br />
          <strong style={{ color: '#1A73E8' }}>{email}</strong>
        </Text>
      </div>

      <div className={styles.otpForm}>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 24 }}>
          <Input.OTP
            length={6}
            formatter={(str) => str.toUpperCase()}
            onChange={onFinishOTP}
            disabled={loading}
            size="large"
          />
        </div>

        <Button
          type="primary"
          block
          size="large"
          loading={loading}
          disabled={loading}
          className={styles.submitBtn}
        >
          {loading ? 'Đang xác nhận...' : 'Vui lòng nhập đủ 6 số'}
        </Button>

        <Divider className={styles.divider} />

        <div className={styles.resendWrapper}>
          {canResend ? (
            <span className={styles.resendLink} onClick={handleResend}>
              Gửi lại mã OTP
            </span>
          ) : (
            <span className={styles.countdownText}>
              Gửi lại mã sau <strong>{countdown}</strong> giây
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
