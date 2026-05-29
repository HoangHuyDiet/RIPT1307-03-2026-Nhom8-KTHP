import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const useWebSocket = (fundId: number) => {
  const [stompClient, setStompClient] = useState<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('access_token')}`
      },
      debug: (str) => console.log('STOMP:', str),
      
      onConnect: () => {
        console.log('✅ Đã kết nối Socket thành công!');
        
        client.subscribe(`/topic/funds/${fundId}/chat`, (message) => {
          const newChatMsg = JSON.parse(message.body);
          console.log("Có tin nhắn mới:", newChatMsg);
        });

        client.subscribe(`/user/queue/notifications`, (message) => {
          const newNotif = JSON.parse(message.body);
          console.log("Có chuông thông báo mới:", newNotif);
        });
      },
      
      onStompError: (frame) => {
        console.error('Lỗi Socket STOMP: ', frame.headers['message']);
      },
    });

    client.activate();
    setStompClient(client);

    return () => {
      if (client.active) {
        client.deactivate();
      }
    };
  }, [fundId]);

  const sendMessage = (text: string) => {
    if (stompClient && stompClient.active) {
      stompClient.publish({
        destination: `/app/funds/${fundId}/chat.send`,
        body: JSON.stringify({ content: text }),
      });
    }
  };

  return { sendMessage };
};
