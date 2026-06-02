import React, { useState, useEffect } from 'react';
import { Modal, Form, Input, Space, Button, message, Select } from 'antd';
import request from '@/utils/request';
import { useAuthStore } from '@/store/useAuthStore';
import { GroupFund } from '../../types';

interface DepositModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedGroup: GroupFund | null;
}

export default function DepositModal({ isOpen, onClose, selectedGroup }: DepositModalProps) {
  const [depositForm] = Form.useForm();
  const user = useAuthStore(state => state.user);
  const [personalFunds, setPersonalFunds] = useState<any[]>([]);

  useEffect(() => {
    if (isOpen) {
      request.get('/personal-funds').then((res: any) => {
        if (res.success) setPersonalFunds(res.data);
      });
    }
  }, [isOpen]);

  const handleDeposit = async (values: any) => {
    if (!selectedGroup) return;
    const amount = parseFloat(values.amount);
    
    if (values.personalFundId) {
      const selectedFund = personalFunds.find(f => f.id === values.personalFundId);
      if (selectedFund && amount > selectedFund.balance) {
        message.error('Số tiền nạp không được vượt quá số dư hiện tại của quỹ cá nhân!');
        return;
      }
    }

    try {
      const data = await request.post('/funds/transaction-request', {
        fundId: selectedGroup.id,
        type: 'INCOME',
        amount,
        description: values.description || 'Đóng góp vào quỹ',
        requesterName: user?.display_name || user?.email || 'Người dùng ẩn danh',
        personalFundId: values.personalFundId
      });
      if (data.success) {
        if (data.data.isApproved) {
          message.success('Đã nạp tiền vào quỹ thành công!');
          window.dispatchEvent(new Event('transaction-approved'));
        } else {
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
          label={<span style={{ fontWeight: 600 }}>Nguồn tiền (Quỹ cá nhân)</span>}
          name="personalFundId"
          rules={[{ required: true, message: 'Vui lòng chọn nguồn tiền!' }]}
        >
          <Select placeholder="Chọn quỹ cá nhân để trích tiền">
            {personalFunds.map(fund => (
              <Select.Option key={fund.id} value={fund.id}>
                {fund.name} (Số dư: {new Intl.NumberFormat('vi-VN').format(fund.balance)}đ)
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
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
