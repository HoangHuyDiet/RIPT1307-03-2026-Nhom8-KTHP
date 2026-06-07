import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Form, Input, message, Modal, Segmented, Space, Table, Tag, Typography } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined, TagsOutlined } from '@ant-design/icons';
import { Category, categoryApi, CategoryType } from '../../services/categories';
import styles from './index.less';

const { Text } = Typography;

export default function CategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [activeType, setActiveType] = useState<CategoryType>('EXPENSE');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [form] = Form.useForm();

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const data = await categoryApi.list();
      setCategories(data);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Không thể tải danh mục');
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
      type: category.type,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);

      if (editingCategory) {
        await categoryApi.update(editingCategory.id, {
          name: values.name,
          description: values.description,
        });
        message.success('Đã cập nhật danh mục');
      } else {
        await categoryApi.create({
          name: values.name,
          type: activeType,
          description: values.description,
        });
        message.success('Đã tạo danh mục');
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
      title: 'Xóa danh mục',
      content: `Bạn có chắc muốn xóa danh mục "${category.name}"?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await categoryApi.remove(category.id);
          message.success('Đã xóa danh mục');
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
      title: 'Phạm vi',
      dataIndex: 'system',
      key: 'system',
      width: 140,
      render: (system: boolean) => (
        <Tag color={system ? 'default' : 'blue'}>{system ? 'Hệ thống' : 'Cá nhân'}</Tag>
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
        if (record.system) {
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
    <div className={styles.fundsWrapper}>
      <div className={styles.headerSection}>
        <div className={styles.titleInfo}>
          <h1 className={styles.pageTitle}>Danh mục giao dịch</h1>
          <p className={styles.pageSubtitle}>Thiết lập và quản lý danh mục thu/chi để phân loại giao dịch hiệu quả.</p>
        </div>
        <div className={styles.headerActions}>
          <Button icon={<ReloadOutlined />} className={styles.actionBtn} onClick={fetchCategories} loading={loading}>
            Làm mới
          </Button>
          <Button type="primary" icon={<PlusOutlined />} className={styles.actionBtn} onClick={openCreateModal}>
            Thêm danh mục
          </Button>
        </div>
      </div>

      <Card bordered={false} className={styles.tableCard}>

        <Segmented
          value={activeType}
          onChange={(value) => setActiveType(value as CategoryType)}
          options={[
            { label: 'Chi tiêu', value: 'EXPENSE' },
            { label: 'Thu nhập', value: 'INCOME' },
          ]}
          className={styles.typeTabs}
        />

        <Table
          rowKey="id"
          columns={columns}
          dataSource={visibleCategories}
          loading={loading}
          pagination={{ pageSize: 10, showTotal: (total) => `Tổng ${total} danh mục` }}
          className={styles.table}
        />
      </Card>

      <Modal
        title={editingCategory ? 'Sửa danh mục cá nhân' : 'Thêm danh mục cá nhân'}
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
            <Input placeholder="Ví dụ: Du lịch Đà Nẵng, Ăn ngoài..." />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={3} placeholder="Ghi chú ngắn về mục đích danh mục" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
