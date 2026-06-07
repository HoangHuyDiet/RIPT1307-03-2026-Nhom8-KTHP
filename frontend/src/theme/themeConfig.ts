import type { ThemeConfig } from 'antd';

export const themeConfig: ThemeConfig = {
    token: {
        fontFamily: '"Inter", sans-serif',
        colorPrimary: '#1A73E8',
        colorSuccess: '#34A853',
        colorError: '#EA4335',
        colorBgLayout: '#F1F4F7',
        borderRadius: 12,
    },
    components: {
        Card: {
            colorBgContainer: '#FFFFFF',
            borderRadius: 12,
        },
        Button: {
            borderRadius: 12,
        },
        Layout: {
            bodyBg: '#F1F4F7',
            headerBg: '#FFFFFF',
            siderBg: '#FFFFFF',
        },
    },
};
