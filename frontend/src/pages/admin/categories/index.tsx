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
      message.error(error.response?.data?.message || 'Khong the tai danh muc he thong');
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
        message.success('Da cap nhat danh muc he thong');
      } else {
        await categoryApi.adminCreate({
          name: values.name,
          type: activeType,
          description: values.description,
        });
        message.success('Da tao danh muc he thong');
      }

      setModalOpen(false);
      fetchCategories();
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.response?.data?.message || 'Khong the luu danh muc');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (category: Category) => {
    Modal.confirm({
      title: 'Xoa danh muc he thong',
      content: `Ban co chac muon xoa danh muc "${category.name}"? Danh muc se khong hien trong lua chon moi.`,
      okText: 'Xoa',
      okType: 'danger',
      cancelText: 'Huy',
      onOk: async () => {
        try {
          await categoryApi.adminRemove(category.id);
          message.success('Da xoa danh muc he thong');
          fetchCategories();
        } catch (error: any) {
          message.error(error.response?.data?.message || 'Khong the xoa danh muc');
        }
      },
    });
  };

  const columns = [
    {
      title: 'Ten danh muc',
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
      title: 'Loai',
      dataIndex: 'type',
      key: 'type',
      width: 140,
      render: (type: CategoryType) => (
        <Tag color={type === 'INCOME' ? 'success' : 'processing'}>
          {type === 'INCOME' ? 'Thu nhap' : 'Chi tieu'}
        </Tag>
      ),
    },
    {
      title: 'Mo ta',
      dataIndex: 'description',
      key: 'description',
      render: (description: string) => description || <Text type="secondary">-</Text>,
    },
    {
      title: 'Thao tac',
      key: 'actions',
      width: 180,
      render: (_: any, record: Category) => {
        if (!isAdmin()) {
          return <Text type="secondary">Chi xem</Text>;
        }
        return (
          <Space>
            <Button type="text" size="small" icon={<EditOutlined />} onClick={() => openEditModal(record)}>
              Sua
            </Button>
            <Button type="text" danger size="small" icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
              Xoa
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
            <Text strong style={{ fontSize: 20 }}>Danh muc he thong</Text>
            <div style={{ color: '#5F6368', marginTop: 4 }}>
              Bo danh muc mac dinh dung chung cho toan bo nguoi dung.
            </div>
          </div>
          <div className={styles.toolbarRight}>
            <Button icon={<ReloadOutlined />} onClick={fetchCategories} loading={loading} style={{ marginRight: 8 }}>
              Lam moi
            </Button>
            {isAdmin() && (
              <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
                Them danh muc
              </Button>
            )}
          </div>
        </div>

        <Segmented
          value={activeType}
          onChange={(value) => setActiveType(value as CategoryType)}
          options={[
            { label: 'Chi tieu', value: 'EXPENSE' },
            { label: 'Thu nhap', value: 'INCOME' },
          ]}
          style={{ marginBottom: 18 }}
        />

        <Table
          rowKey="id"
          columns={columns}
          dataSource={visibleCategories}
          loading={loading}
          className={styles.table}
          pagination={{ pageSize: 10, showTotal: (total) => `Tong ${total} danh muc` }}
        />
      </Card>

      <Modal
        title={editingCategory ? 'Sua danh muc he thong' : 'Them danh muc he thong'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        confirmLoading={submitting}
        okText={editingCategory ? 'Luu thay doi' : 'Tao danh muc'}
        cancelText="Huy"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="Loai danh muc">
            <Tag color={activeType === 'INCOME' ? 'success' : 'processing'}>
              {activeType === 'INCOME' ? 'Thu nhap' : 'Chi tieu'}
            </Tag>
          </Form.Item>
          <Form.Item
            name="name"
            label="Ten danh muc"
            rules={[{ required: true, message: 'Vui long nhap ten danh muc' }]}
          >
            <Input placeholder="Vi du: Du lich, Mua nha, Luong..." />
          </Form.Item>
          <Form.Item name="description" label="Mo ta">
            <Input.TextArea rows={3} placeholder="Mo ta ngan gon ve danh muc" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
