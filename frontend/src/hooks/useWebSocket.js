import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const useWebSocket = (topicHandlers = {}) => {
  const clientRef = useRef(null);

  useEffect(() => {
    // Factory for SockJS connection fallback
    const rawApiUrl = (import.meta.env.VITE_API_URL || '').replace(/\/+$/, '').replace(/\/api$/, '');
    const wsUrl = import.meta.env.VITE_WS_URL || (rawApiUrl ? `${rawApiUrl}/ws` : '/ws');
    const socketFactory = () => new SockJS(wsUrl);

    const client = new Client({
      webSocketFactory: socketFactory,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        // Register subscriptions for specified topics
        Object.entries(topicHandlers).forEach(([topic, handler]) => {
          client.subscribe(topic, (message) => {
            try {
              const data = JSON.parse(message.body);
              handler(data);
            } catch (err) {
              console.error(`Error parsing message on ${topic}:`, err);
            }
          });
        });
      },
      onStompError: (frame) => {
        console.warn('Broker reported error: ' + frame.headers['message']);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, []);

  return clientRef.current;
};
