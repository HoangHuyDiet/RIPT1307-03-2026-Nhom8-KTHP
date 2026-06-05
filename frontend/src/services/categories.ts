import api from '../utils/api';

export type CategoryType = 'INCOME' | 'EXPENSE';

export interface Category {
  id: number;
  name: string;
  type: CategoryType;
  description?: string;
  system?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CategoryPayload {
  name: string;
  type: CategoryType;
  description?: string;
}

const unwrap = (res: any) => res.data?.data ?? res.data ?? [];

export const categoryApi = {
  async list(type?: CategoryType): Promise<Category[]> {
    const res = await api.get('/categories', { params: type ? { type } : undefined });
    return unwrap(res);
  },

  async create(data: CategoryPayload): Promise<Category> {
    const res = await api.post('/categories', data);
    return unwrap(res);
  },

  async update(id: number, data: Partial<Pick<CategoryPayload, 'name' | 'description'>>): Promise<Category> {
    const res = await api.put(`/categories/${id}`, data);
    return unwrap(res);
  },

  async remove(id: number): Promise<void> {
    await api.delete(`/categories/${id}`);
  },

  async adminList(type?: CategoryType): Promise<Category[]> {
    const res = await api.get('/admin/categories', { params: type ? { type } : undefined });
    return unwrap(res);
  },

  async adminCreate(data: CategoryPayload): Promise<Category> {
    const res = await api.post('/admin/categories', data);
    return unwrap(res);
  },

  async adminUpdate(id: number, data: Partial<Pick<CategoryPayload, 'name' | 'description'>>): Promise<Category> {
    const res = await api.put(`/admin/categories/${id}`, data);
    return unwrap(res);
  },

  async adminRemove(id: number): Promise<void> {
    await api.delete(`/admin/categories/${id}`);
  },
};
