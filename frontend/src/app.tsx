import React from 'react';
import { ConfigProvider } from 'antd';
import { themeConfig } from './theme/themeConfig';
import { GoogleOAuthProvider } from '@react-oauth/google';

export function rootContainer(container: React.ReactNode) {
  return (
    <GoogleOAuthProvider clientId="1022591081822-j6pfdt0cpnjc7dgjm34t352o360j3kel.apps.googleusercontent.com">
      <ConfigProvider theme={themeConfig}>
        {container}
      </ConfigProvider>
    </GoogleOAuthProvider>
  );
}
