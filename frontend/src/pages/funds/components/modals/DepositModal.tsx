import React, { useEffect, useMemo, useState } from 'react';
import { Modal, Form, Input, Space, Button, message, Select, DatePicker, Typography } from 'antd';
import request from '@/utils/request';
import { useAuthStore } from '@/store/useAuthStore';
import { GroupFund } from '../../types';
import dayjs from 'dayjs';

const { Text } = Typography;

const formatCurrency = (value: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0);

interface DepositModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedGroup: GroupFund | null;
}

export default function DepositModal({ isOpen, onClose, selectedGroup }: DepositModalProps) {
  const [depositForm] = Form.useForm();
  const user = useAuthStore(state => state.user);
  const [categories, setCategories] = useState<any[]>([]);
  const [personalFunds, setPersonalFunds] = useState<any[]>([]);
  const amount = Form.useWatch('amount', depositForm);
  const personalFundId = Form.useWatch('personalFundId', depositForm);

  const incomeCategories = useMemo(
    () => categories.filter((category) => category.type === 'INCOME'),
    [categories],
  );

  const selectedPersonalFund = useMemo(
    () => personalFunds.find((fund) => fund.id === personalFundId),
    [personalFunds, personalFundId],
  );

  const sourceBalanceAfter = useMemo(() => {
    if (!selectedPersonalFund || !amount) return undefined;
    return Number(selectedPersonalFund.balance || 0) - Number(amount || 0);
  }, [selectedPersonalFund, amount]);

  const fetchOptions = async () => {
    try {
      const [categoryRes, fundRes] = await Promise.all([
        request.get('/categories'),
        request.get('/personal-funds/list'),
      ]);
      const categoryData = categoryRes?.data ?? categoryRes ?? [];
      const fundData = fundRes?.data ?? fundRes ?? [];
      setCategories(Array.isArray(categoryData) ? categoryData : []);
      setPersonalFunds(Array.isArray(fundData) ? fundData : []);
    } catch (error) {
      console.warn(error);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchOptions();
      depositForm.setFieldsValue({ date: dayjs() });
    }
  }, [isOpen]);

  useEffect(() => {
    if (isOpen && incomeCategories.length > 0 && !depositForm.getFieldValue('categoryId')) {
      depositForm.setFieldValue('categoryId', incomeCategories[0].id);
    }
  }, [isOpen, incomeCategories]);

  useEffect(() => {
    if (isOpen && personalFunds.length > 0 && !depositForm.getFieldValue('personalFundId')) {
      depositForm.setFieldValue('personalFundId', personalFunds[0].id);
    }
  }, [isOpen, personalFunds]);

  const handleDeposit = async (values: any) => {
    if (!selectedGroup) return;
    const amount = parseFloat(values.amount);
    try {
      const data = await request.post('/funds/transaction-request', {
        fundId: selectedGroup.id,
        type: 'INCOME',
        amount,
        categoryId: values.categoryId,
        personalFundId: values.personalFundId,
        date: values.date ? values.date.format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD'),
        description: values.description || 'Đóng góp vào quỹ',
        requesterName: user?.display_name || user?.email || 'Người dùng ẩn danh'
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
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Nguồn tiền cá nhân</span>}
          name="personalFundId"
          rules={[{ required: true, message: 'Vui lòng chọn nguồn tiền!' }]}
        >
          <Select
            placeholder="Chọn ví hoặc tài khoản để đóng góp"
            options={personalFunds.map((fund) => ({
              value: fund.id,
              label: `${fund.name} - ${formatCurrency(Number(fund.balance))}`,
            }))}
          />
        </Form.Item>

        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Danh mục thu nhập</span>}
          name="categoryId"
          rules={[{ required: true, message: 'Vui lòng chọn danh mục!' }]}
        >
          <Select
            placeholder="Chọn danh mục cho khoản đóng góp"
            options={incomeCategories.map((category) => ({
              value: category.id,
              label: category.name,
            }))}
          />
        </Form.Item>

        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Ngày đóng góp</span>}
          name="date"
          rules={[{ required: true, message: 'Vui lòng chọn ngày đóng góp!' }]}
        >
          <DatePicker format="DD/MM/YYYY" style={{ width: '100%', borderRadius: 12 }} />
        </Form.Item>

        {selectedPersonalFund && (
          <div style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 12,
            padding: '12px 14px',
            marginBottom: 16,
            borderRadius: 12,
            backgroundColor: '#F7FAFF',
            border: '1px solid #E8EEF8',
          }}>
            <div>
              <Text type="secondary" style={{ display: 'block', fontSize: 12 }}>Số dư nguồn</Text>
              <Text strong>{formatCurrency(Number(selectedPersonalFund.balance || 0))}</Text>
            </div>
            <div>
              <Text type="secondary" style={{ display: 'block', fontSize: 12 }}>Sau đóng góp</Text>
              <Text strong style={{ color: sourceBalanceAfter !== undefined && sourceBalanceAfter < 0 ? '#ff4d4f' : '#34A853' }}>
                {sourceBalanceAfter !== undefined ? formatCurrency(sourceBalanceAfter) : '-'}
              </Text>
            </div>
          </div>
        )}

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
