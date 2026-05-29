import React from 'react';
import { Modal, Form, Input, Space, Button, message } from 'antd';
import request from '@/utils/request';
import { useAuthStore } from '@/store/useAuthStore';
import { useNotificationStore } from '@/store/useNotificationStore';
import { GroupFund } from '../../types';

interface DepositModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedGroup: GroupFund | null;
}

export default function DepositModal({ isOpen, onClose, selectedGroup }: DepositModalProps) {
  const [depositForm] = Form.useForm();
  const user = useAuthStore(state => state.user);
  const addNotification = useNotificationStore(state => state.addNotification);

  const handleDeposit = async (values: any) => {
    if (!selectedGroup) return;
    const amount = parseFloat(values.amount);
    try {
      const data = await request.post('/funds/transaction-request', {
        fundId: selectedGroup.id,
        type: 'INCOME',
        amount,
        description: values.description || 'Đóng góp vào quỹ',
        requesterName: user?.display_name || user?.email || 'Người dùng ẩn danh'
      });
      if (data.success) {
        if (data.data.isApproved) {
          message.success('Đã nạp tiền vào quỹ thành công!');
          window.dispatchEvent(new Event('transaction-approved'));
        } else {
          addNotification({
            id: data.data.requestId,
            type: 'DEPOSIT_REQUEST',
            fundId: selectedGroup.id,
            fundName: selectedGroup.name,
            amount,
            description: values.description || 'Đóng góp vào quỹ',
            requesterName: user?.display_name || user?.email || 'Người dùng ẩn danh',
            date: new Date().toISOString().slice(0, 10),
            read: false,
            targetRole: 'OWNER'
          });
          message.success('Đã gửi yêu cầu nạp tiền! Chờ chủ quỹ duyệt.');
        }
        onClose();
        depositForm.resetFields();
      }
    } catch {
      message.error('Lỗi kết nối!');
    }
  };

  return (
    <Modal
      title={<span style={{ fontWeight: 700 }}>Đóng góp vào Quỹ nhóm</span>}
      open={isOpen}
      onCancel={() => { onClose(); depositForm.resetFields(); }}
      footer={null}
      destroyOnClose
      centered
    >
      <Form form={depositForm} layout="vertical" onFinish={handleDeposit} size="large" style={{ marginTop: 12 }}>
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Số tiền nạp (đ)</span>}
          name="amount"
          rules={[
            { required: true, message: 'Vui lòng nhập số tiền!' },
            { validator: (_, val) => val && parseFloat(val) <= 0 ? Promise.reject('Số tiền phải lớn hơn 0!') : Promise.resolve() }
          ]}
        >
          <Input type="number" placeholder="Nhập số tiền nạp" style={{ borderRadius: 12 }} />
        </Form.Item>
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Nội dung đóng góp</span>}
          name="description"
          rules={[{ required: true, message: 'Vui lòng nhập nội dung!' }]}
        >
          <Input placeholder="Ví dụ: Đóng góp tháng 8, Tiết kiệm du lịch..." style={{ borderRadius: 12 }} />
        </Form.Item>
        <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
          <Space>
            <Button onClick={() => { onClose(); depositForm.resetFields(); }} style={{ borderRadius: 16 }}>Hủy</Button>
            <Button type="primary" htmlType="submit" style={{ borderRadius: 16 }}>Gửi yêu cầu nạp</Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
}
