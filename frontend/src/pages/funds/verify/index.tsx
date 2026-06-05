import React, { useEffect, useState } from 'react';
import { Result, Button, Spin, Card } from 'antd';
import { history, useLocation } from 'umi';
import request from '@/utils/request';

export default function VerifyFundAction() {
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');
  const location = useLocation() as any;

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');

    if (!token) {
      setStatus('error');
      setMessage('Không tìm thấy mã xác nhận (token) trong đường dẫn.');
      return;
    }

    verifyToken(token);
  }, [location.search]);

  const verifyToken = async (token: string) => {
    try {
      const res = await request.post('/funds/verify-token', { data: { token } });
      if (res.success) {
        setStatus('success');
        setMessage(res.message || 'Xác nhận thành công!');
      } else {
        setStatus('error');
        setMessage(res.message || 'Xác nhận thất bại hoặc mã đã hết hạn.');
      }
    } catch (error: any) {
      setStatus('error');
      setMessage(error?.response?.data?.message || 'Không thể kết nối đến máy chủ.');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh', padding: 24 }}>
      <Card bordered={false} style={{ width: '100%', maxWidth: 500, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
        {status === 'loading' ? (
          <div style={{ textAlign: 'center', padding: '40px 0' }}>
            <Spin size="large" />
            <div style={{ marginTop: 24, color: '#8c8c8c' }}>Đang xác thực thông tin...</div>
          </div>
        ) : status === 'success' ? (
          <Result
            status="success"
            title="Thành công"
            subTitle={message}
            extra={[
              <Button type="primary" key="console" onClick={() => history.push('/funds')}>
                Trở về Quản lý Quỹ
              </Button>
            ]}
          />
        ) : (
          <Result
            status="error"
            title="Thất bại"
            subTitle={message}
            extra={[
              <Button type="primary" key="console" onClick={() => history.push('/funds')}>
                Trở về Quản lý Quỹ
              </Button>
            ]}
          />
        )}
      </Card>
    </div>
  );
}
