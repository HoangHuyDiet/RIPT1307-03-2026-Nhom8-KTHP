import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Form, Input, message, Modal, Segmented, Space, Table, Tag, Typography } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined, TagsOutlined } from '@ant-design/icons';
import { Category, categoryApi, CategoryType } from '../../../services/categories';
import { useAuthStore } from '../../../store/useAuthStore';
import styles from '../users/index.less';

const { Text } = Typography;

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [activeType, setActiveType] = useState<CategoryType>('EXPENSE');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [form] = Form.useForm();
  const { isAdmin } = useAuthStore();

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const data = await categoryApi.adminList();
      setCategories(data);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Không thể tải danh mục hệ thống');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const visibleCategories = useMemo(
    () => categories.filter((category) => category.type === activeType),
    [categories, activeType],
  );

  const openCreateModal = () => {
    setEditingCategory(null);
    form.resetFields();
    form.setFieldsValue({ type: activeType });
    setModalOpen(true);
  };

  const openEditModal = (category: Category) => {
    setEditingCategory(category);
    form.resetFields();
    form.setFieldsValue({
      name: category.name,
      description: category.description,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);

      if (editingCategory) {
        await categoryApi.adminUpdate(editingCategory.id, {
          name: values.name,
          description: values.description,
        });
        message.success('Đã cập nhật danh mục hệ thống');
      } else {
        await categoryApi.adminCreate({
          name: values.name,
          type: activeType,
          description: values.description,
        });
        message.success('Đã tạo danh mục hệ thống');
      }

      setModalOpen(false);
      fetchCategories();
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.response?.data?.message || 'Không thể lưu danh mục');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (category: Category) => {
    Modal.confirm({
      title: 'Xóa danh mục hệ thống',
      content: `Bạn có chắc muốn xóa danh mục "${category.name}"? Danh mục sẽ không hiện trong lựa chọn mới.`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await categoryApi.adminRemove(category.id);
          message.success('Đã xóa danh mục hệ thống');
          fetchCategories();
        } catch (error: any) {
          message.error(error.response?.data?.message || 'Không thể xóa danh mục');
        }
      },
    });
  };

  const columns = [
    {
      title: 'Tên danh mục',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: Category) => (
        <Space>
          <TagsOutlined style={{ color: record.type === 'INCOME' ? '#34A853' : '#1A73E8' }} />
          <Text strong>{name}</Text>
        </Space>
      ),
    },
    {
      title: 'Loại',
      dataIndex: 'type',
      key: 'type',
      width: 140,
      render: (type: CategoryType) => (
        <Tag color={type === 'INCOME' ? 'success' : 'processing'}>
          {type === 'INCOME' ? 'Thu nhập' : 'Chi tiêu'}
        </Tag>
      ),
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      key: 'description',
      render: (description: string) => description || <Text type="secondary">-</Text>,
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 180,
      render: (_: any, record: Category) => {
        if (!isAdmin()) {
          return <Text type="secondary">Chỉ xem</Text>;
        }
        return (
          <Space>
            <Button type="text" size="small" icon={<EditOutlined />} onClick={() => openEditModal(record)}>
              Sửa
            </Button>
            <Button type="text" danger size="small" icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
              Xóa
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div className={styles.container}>
      <Card bordered={false} className={styles.tableCard}>
        <div className={styles.toolbar}>
          <div className={styles.toolbarLeft}>
            <Text strong style={{ fontSize: 20 }}>Danh mục hệ thống</Text>
            <div style={{ color: '#5F6368', marginTop: 4 }}>
              Bộ danh mục mặc định dùng chung cho toàn bộ người dùng.
            </div>
          </div>
          <div className={styles.toolbarRight}>
            <Button icon={<ReloadOutlined />} onClick={fetchCategories} loading={loading} style={{ marginRight: 8 }}>
              Làm mới
            </Button>
            {isAdmin() && (
              <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
                Thêm danh mục
              </Button>
            )}
          </div>
        </div>

        <Segmented
          value={activeType}
          onChange={(value) => setActiveType(value as CategoryType)}
          options={[
            { label: 'Chi tiêu', value: 'EXPENSE' },
            { label: 'Thu nhập', value: 'INCOME' },
          ]}
          style={{ marginBottom: 18 }}
        />

        <Table
          rowKey="id"
          columns={columns}
          dataSource={visibleCategories}
          loading={loading}
          className={styles.table}
          pagination={{ pageSize: 10, showTotal: (total) => `Tổng ${total} danh mục` }}
        />
      </Card>

      <Modal
        title={editingCategory ? 'Sửa danh mục hệ thống' : 'Thêm danh mục hệ thống'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        confirmLoading={submitting}
        okText={editingCategory ? 'Lưu thay đổi' : 'Tạo danh mục'}
        cancelText="Hủy"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="Loại danh mục">
            <Tag color={activeType === 'INCOME' ? 'success' : 'processing'}>
              {activeType === 'INCOME' ? 'Thu nhập' : 'Chi tiêu'}
            </Tag>
          </Form.Item>
          <Form.Item
            name="name"
            label="Tên danh mục"
            rules={[{ required: true, message: 'Vui lòng nhập tên danh mục' }]}
          >
            <Input placeholder="Ví dụ: Du lịch, Mua nhà, Lương..." />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={3} placeholder="Mô tả ngắn gọn về danh mục" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
