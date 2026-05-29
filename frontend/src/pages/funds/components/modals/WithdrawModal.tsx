import React from 'react';
import { Modal, Form, Input, Space, Button, message } from 'antd';
import request from '@/utils/request';
import { useAuthStore } from '@/store/useAuthStore';
import { useNotificationStore } from '@/store/useNotificationStore';
import { GroupFund } from '../../types';

interface WithdrawModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedGroup: GroupFund | null;
}

export default function WithdrawModal({ isOpen, onClose, selectedGroup }: WithdrawModalProps) {
  const [withdrawForm] = Form.useForm();
  const user = useAuthStore(state => state.user);
  const addNotification = useNotificationStore(state => state.addNotification);

  const handleWithdraw = async (values: any) => {
    if (!selectedGroup) return;
    const amount = parseFloat(values.amount);
    if (amount > selectedGroup.balance) {
      message.error('Số tiền rút vượt quá số dư quỹ!');
      return;
    }
    try {
      const data = await request.post('/funds/transaction-request', {
        fundId: selectedGroup.id,
        type: 'EXPENSE',
        amount,
        description: values.reason || 'Rút tiền từ quỹ',
        bankAccount: values.bankAccount,
        bankName: values.bankName,
        requesterName: user?.display_name || user?.email || 'Người dùng ẩn danh'
      });
      if (data.success) {
        addNotification({
          id: data.requestId,
          type: 'WITHDRAW_REQUEST',
          fundId: selectedGroup.id,
          fundName: selectedGroup.name,
          amount,
          description: values.reason || 'Rút tiền từ quỹ',
          requesterName: user?.display_name || user?.email || 'Người dùng ẩn danh',
          bankAccount: values.bankAccount,
          bankName: values.bankName,
          date: new Date().toISOString().slice(0, 10),
          read: false,
          targetRole: 'OWNER'
        });

        message.success('Đã gửi yêu cầu rút tiền! Chờ chủ quỹ duyệt.');
        onClose();
        withdrawForm.resetFields();
      }
    } catch {
      message.error('Lỗi kết nối!');
    }
  };

  return (
    <Modal
      title={<span style={{ fontWeight: 700 }}>Rút tiền khỏi Quỹ nhóm</span>}
      open={isOpen}
      onCancel={() => { onClose(); withdrawForm.resetFields(); }}
      footer={null}
      destroyOnClose
      centered
    >
      <Form form={withdrawForm} layout="vertical" onFinish={handleWithdraw} size="large" style={{ marginTop: 12 }}>
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Số tiền muốn rút (đ)</span>}
          name="amount"
          rules={[
            { required: true, message: 'Vui lòng nhập số tiền!' },
            { validator: (_, val) => val && parseFloat(val) <= 0 ? Promise.reject('Số tiền phải lớn hơn 0!') : Promise.resolve() }
          ]}
        >
          <Input type="number" placeholder="Nhập số tiền rút" style={{ borderRadius: 12 }} />
        </Form.Item>
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Lý do rút tiền</span>}
          name="reason"
          rules={[{ required: true, message: 'Vui lòng nhập lý do!' }]}
        >
          <Input placeholder="Ví dụ: Thanh toán vé máy bay..." style={{ borderRadius: 12 }} />
        </Form.Item>
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Số tài khoản nhận</span>}
          name="bankAccount"
          rules={[{ required: true, message: 'Vui lòng nhập số tài khoản!' }]}
        >
          <Input placeholder="Nhập số tài khoản ngân hàng" style={{ borderRadius: 12 }} />
        </Form.Item>
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Tên ngân hàng</span>}
          name="bankName"
          rules={[{ required: true, message: 'Vui lòng chọn ngân hàng!' }]}
        >
          <Input placeholder="Ví dụ: Vietcombank, MB Bank, Techcombank..." style={{ borderRadius: 12 }} />
        </Form.Item>
        <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
          <Space>
            <Button onClick={() => { onClose(); withdrawForm.resetFields(); }} style={{ borderRadius: 16 }}>Hủy</Button>
            <Button type="primary" danger htmlType="submit" style={{ borderRadius: 16 }}>Gửi yêu cầu rút</Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
}
