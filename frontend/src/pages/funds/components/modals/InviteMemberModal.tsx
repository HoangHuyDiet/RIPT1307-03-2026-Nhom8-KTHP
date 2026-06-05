import React from 'react';
import { Modal, Form, Input, Space, Button, message } from 'antd';
import request from '@/utils/request';
import { GroupFund } from '../../types';
import { useNotificationStore } from '@/store/useNotificationStore';

interface InviteMemberModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedGroup: GroupFund | null;
}

export default function InviteMemberModal({ isOpen, onClose, selectedGroup }: InviteMemberModalProps) {
  const [inviteForm] = Form.useForm();
  const addNotification = useNotificationStore(state => state.addNotification);

  const handleInvite = async (values: any) => {
    if (!selectedGroup) return;

    try {
      const data = await request.post(`/funds/${selectedGroup.id}/invite`, { email: values.email });

      if (data.success) {
        message.success(`Đã gửi lời mời tham gia quỹ đến email: ${values.email}. Vui lòng chờ người dùng xác nhận.`);
        addNotification({
          id: `sys_${Date.now()}`,
          type: 'SYSTEM_INFO',
          fundId: selectedGroup.id,
          fundName: selectedGroup.name,
          amount: 0,
          description: `Đã gửi lời mời tham gia đến ${values.email}`,
          requesterName: 'Hệ thống',
          date: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
          read: false,
          targetRole: 'OWNER'
        });
        onClose();
        inviteForm.resetFields();
      } else {
        message.error(data.message || 'Lỗi khi gửi lời mời!');
      }
    } catch {
      message.error('Lỗi kết nối khi gửi lời mời!');
    }
  };

  return (
    <Modal
      title={<span style={{ fontWeight: 700 }}>Mời thành viên tham gia quỹ</span>}
      open={isOpen}
      onCancel={() => { onClose(); inviteForm.resetFields(); }}
      footer={null}
      destroyOnClose
      centered
    >
      <Form form={inviteForm} layout="vertical" onFinish={handleInvite} size="large" style={{ marginTop: 12 }}>
        <Form.Item
          label="Nhập Email người muốn mời"
          name="email"
          rules={[
            { required: true, message: 'Vui lòng nhập email!' },
            { type: 'email', message: 'Email chưa đúng định dạng!' }
          ]}
        >
          <Input placeholder="vi-du@gmail.com" style={{ borderRadius: 12 }} />
        </Form.Item>
        <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
          <Space>
            <Button onClick={() => { onClose(); inviteForm.resetFields(); }} style={{ borderRadius: 16 }}>Hủy</Button>
            <Button type="primary" htmlType="submit" style={{ borderRadius: 16 }}>Gửi lời mời</Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
}
