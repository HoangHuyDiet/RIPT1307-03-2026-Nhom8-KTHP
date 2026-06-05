import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Space, Button, message } from 'antd';
import request from '@/utils/request';
import { GroupFund } from '../../types';
import { useNotificationStore } from '@/store/useNotificationStore';

interface RenameFundModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedGroup: GroupFund | null;
  onSuccess: (newName: string) => void;
}

export default function RenameFundModal({ isOpen, onClose, selectedGroup, onSuccess }: RenameFundModalProps) {
  const [renameForm] = Form.useForm();
  const addNotification = useNotificationStore(state => state.addNotification);

  useEffect(() => {
    if (isOpen && selectedGroup) {
      renameForm.setFieldsValue({ name: selectedGroup.name });
    }
  }, [isOpen, selectedGroup, renameForm]);

  const handleRenameGroup = async (values: any) => {
    if (!selectedGroup) return;
    const newName = values.name.trim();
    try {
      const data = await request.put('/funds/rename', { fundId: selectedGroup.id, newName });
      if (data.success) {
        onSuccess(newName);
        message.success(`Đã đổi tên quỹ thành "${newName}" thành công!`);
        addNotification({
          id: `sys_${Date.now()}`,
          type: 'SYSTEM_INFO',
          fundId: selectedGroup.id,
          fundName: newName,
          amount: 0,
          description: `Tên quỹ đã được đổi từ "${selectedGroup.name}" sang "${newName}"`,
          requesterName: 'Hệ thống',
          date: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
          read: false,
          targetRole: 'OWNER'
        });
        onClose();
        renameForm.resetFields();
      } else {
        message.error(data.message || 'Đổi tên thất bại!');
      }
    } catch (err) {
      message.error('Không thể kết nối đến máy chủ!');
    }
  };

  return (
    <Modal
      title={<span style={{ fontWeight: 700 }}>Đổi tên Quỹ nhóm</span>}
      open={isOpen}
      onCancel={() => { onClose(); renameForm.resetFields(); }}
      footer={null}
      destroyOnClose
      centered
      width={420}
    >
      <Form form={renameForm} layout="vertical" onFinish={handleRenameGroup} size="large">
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Tên quỹ mới</span>}
          name="name"
          rules={[
            { required: true, message: 'Vui lòng nhập tên quỹ!' },
            { max: 100, message: 'Tên quỹ không được vượt quá 100 ký tự!' },
            { whitespace: true, message: 'Tên quỹ không được chỉ có khoảng trắng!' }
          ]}
        >
          <Input
            placeholder="Nhập tên quỹ mới..."
            style={{ borderRadius: 12 }}
            showCount
            maxLength={100}
          />
        </Form.Item>
        <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
          <Space>
            <Button onClick={() => { onClose(); renameForm.resetFields(); }} style={{ borderRadius: 16 }}>Hủy</Button>
            <Button type="primary" htmlType="submit" style={{ borderRadius: 16 }}>Lưu tên mới</Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
}
