import React from 'react';
import { Modal, Form, Input, Row, Col, Space, Button, message } from 'antd';
import { useAuthStore } from '@/store/useAuthStore';
import request from '@/utils/request';
import styles from '../../index.less';
import { GroupFund } from '../../types';

interface CreateFundModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (newFund: GroupFund, newActivity: any) => void;
}

export default function CreateFundModal({ isOpen, onClose, onSuccess }: CreateFundModalProps) {
  const [form] = Form.useForm();
  const user = useAuthStore(state => state.user);

  const handleCreateFund = async (values: any) => {
    const target = parseFloat(values.target);
    const balance = values.initialContribution ? parseFloat(values.initialContribution) : 0;
    
    if (balance > target) {
      message.error('Số tiền đóng góp ban đầu không được lớn hơn số tiền mục tiêu!');
      return;
    }

    try {
      const data = await request.post('/funds', {
        name: values.name,
        target,
        initialContribution: balance,
        createdBy: user?.email || 'Người dùng ẩn danh'
      });
      
      if (data.success) {
        const newActivity = {
          id: Date.now(),
          type: 'create',
          text: `Bạn đã tạo quỹ '${values.name}'`,
          time: 'Hôm nay, vừa xong',
          color: '#1A73E8'
        };
        onSuccess(data.data, newActivity);
        message.success('Đã mở quỹ nhóm thành công!');
        onClose();
        form.resetFields();
      } else {
        message.error(data.message || 'Lỗi khi tạo quỹ!');
      }
    } catch (err) {
      message.error('Lỗi kết nối khi tạo quỹ!');
    }
  };

  return (
    <Modal
      title={<span style={{ fontSize: '18px', fontWeight: 700 }}>Tạo Quỹ Nhóm Mới</span>}
      open={isOpen}
      onCancel={() => { onClose(); form.resetFields(); }}
      footer={null}
      destroyOnClose
      centered
      width={500}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleCreateFund}
        size="large"
        initialValues={{ initialContribution: '0' }}
        style={{ marginTop: 12 }}
      >
        <Form.Item
          label={<span style={{ fontWeight: 600 }}>Tên Quỹ Nhóm</span>}
          name="name"
          rules={[{ required: true, message: 'Vui lòng nhập tên quỹ!' }]}
        >
          <Input placeholder="Ví dụ: Du lịch nhóm, Chi phí văn phòng..." style={{ borderRadius: 12 }} />
        </Form.Item>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              label={<span style={{ fontWeight: 600 }}>Số tiền mục tiêu (đ)</span>}
              name="target"
              rules={[
                { required: true, message: 'Vui lòng nhập số tiền mục tiêu!' },
                { validator: (_, val) => val && parseFloat(val) <= 0 ? Promise.reject('Số tiền phải lớn hơn 0!') : Promise.resolve() }
              ]}
            >
              <Input type="number" placeholder="Nhập số tiền" style={{ borderRadius: 12 }} />
            </Form.Item>
          </Col>
          
          <Col span={12}>
            <Form.Item
              label={<span style={{ fontWeight: 600 }}>Đóng góp ban đầu (đ)</span>}
              name="initialContribution"
              rules={[
                { validator: (_, val) => val && parseFloat(val) < 0 ? Promise.reject('Đóng góp không được âm!') : Promise.resolve() }
              ]}
            >
              <Input type="number" placeholder="Nhập số tiền" style={{ borderRadius: 12 }} />
            </Form.Item>
          </Col>
        </Row>
        <div style={{ padding: '8px 0', color: '#8c8c8c', fontSize: '13px' }}>
          💡 Bạn có thể tự mình tạo quỹ nhóm trước, và thêm thành viên khác qua email sau bất kỳ lúc nào.
        </div>

        <Form.Item style={{ marginBottom: 0, marginTop: 24, textAlign: 'right' }}>
          <Space size="middle">
            <Button onClick={() => { onClose(); form.resetFields(); }} style={{ borderRadius: 16, minWidth: 80 }}>Hủy</Button>
            <Button type="primary" htmlType="submit" className={styles.submitBtn} style={{ borderRadius: 16, minWidth: 120 }}>Tạo Quỹ</Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
}
