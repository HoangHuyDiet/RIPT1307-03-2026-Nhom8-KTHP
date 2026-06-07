import { create } from 'zustand';

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: any | null;
  roles: string[];
  setAuth: (token: string, user?: any, roles?: string[], refreshToken?: string) => void;
  logout: () => void;
  isAdmin: () => boolean;
  isSupportAdmin: () => boolean;
  hasAdminAccess: () => boolean;
  isPro: () => boolean;
  updateUser: (updates: any) => void;
}

const cleanRoles = (roles: any): string[] => {
  if (!Array.isArray(roles)) return [];
  return roles.map(r => typeof r === 'string' ? (r.startsWith('ROLE_') ? r.slice(5) : r) : String(r));
};

export const useAuthStore = create<AuthState>((set, get) => ({
  token: localStorage.getItem('token') || null,
  refreshToken: localStorage.getItem('refresh_token') || null,
  user: (() => {
    try {
      const u = localStorage.getItem('user');
      return u ? JSON.parse(u) : null;
    } catch { return null; }
  })(),
  roles: (() => {
    try {
      const r = localStorage.getItem('roles');
      return r ? cleanRoles(JSON.parse(r)) : [];
    } catch { return []; }
  })(),
  setAuth: (token, user, roles = [], refreshToken) => {
    localStorage.setItem('token', token);
    if (refreshToken) localStorage.setItem('refresh_token', refreshToken);
    if (user) localStorage.setItem('user', JSON.stringify(user));
    const cleaned = cleanRoles(roles);
    if (cleaned.length > 0) {
      localStorage.setItem('roles', JSON.stringify(cleaned));
    } else {
      localStorage.removeItem('roles');
    }
    set({ token, refreshToken: refreshToken || get().refreshToken, user: user || null, roles: cleaned });
  },
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
    localStorage.removeItem('roles');
    set({ token: null, refreshToken: null, user: null, roles: [] });
  },
  isAdmin: () => get().roles.includes('ADMIN'),
  isSupportAdmin: () => get().roles.includes('SUPPORT_ADMIN'),
  hasAdminAccess: () => {
    const roles = get().roles;
    return roles.includes('ADMIN') || roles.includes('SUPPORT_ADMIN');
  },
  isPro: () => get().roles.includes('PRO'),
  updateUser: (updates) => {
    const currentUser = get().user;
    if (currentUser) {
      const updatedUser = { ...currentUser, ...updates };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      set({ user: updatedUser });
    }
  },
}));
