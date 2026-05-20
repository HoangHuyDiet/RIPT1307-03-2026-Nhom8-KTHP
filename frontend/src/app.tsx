import React from 'react';
import { ConfigProvider } from 'antd';
import { themeConfig } from './theme/themeConfig';

export function rootContainer(container: React.ReactNode) {
  return (
    <ConfigProvider theme={themeConfig}>
      {container}
    </ConfigProvider>
  );
}
