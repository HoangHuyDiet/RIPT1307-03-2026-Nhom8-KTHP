import { create } from 'zustand';

export interface FundNotification {
  id: string;
  type: 'DEPOSIT_REQUEST' | 'WITHDRAW_REQUEST' | 'DEPOSIT_APPROVED' | 'DEPOSIT_REJECTED' | 'WITHDRAW_APPROVED' | 'WITHDRAW_REJECTED' | 'SYSTEM_INFO' | 'FUND_INVITATION' | 'FUND_DISBAND_PROPOSAL' | 'FUND_MEMBER_REMOVED';
  fundId: number;
  fundName: string;
  amount: number;
  description: string;
  requesterName: string;
  bankAccount?: string;
  bankName?: string;
  date: string;
  read: boolean;
  targetRole: 'OWNER' | 'MEMBER';
  link_action?: string;
  actionUrl?: string;
}

interface NotificationState {
  notifications: FundNotification[];
  addNotification: (notif: FundNotification) => void;
  removeNotification: (id: string) => void;
  markAsRead: (id: string) => void;
  clearAll: () => void;
  unreadCount: () => number;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],

  addNotification: (notif) =>
    set((state) => ({ notifications: [notif, ...state.notifications] })),

  removeNotification: (id) =>
    set((state) => ({ notifications: state.notifications.filter(n => n.id !== id) })),

  markAsRead: (id) =>
    set((state) => ({
      notifications: state.notifications.map(n => n.id === id ? { ...n, read: true } : n)
    })),

  clearAll: () => set({ notifications: [] }),

  unreadCount: () => get().notifications.filter(n => !n.read).length,
}));
