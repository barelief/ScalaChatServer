import { ref, onUnmounted } from 'vue';

interface Message {
  sender: string;
  content: string;
  type?: string;
}

export function useWebSocket() {
  const messages = ref<Message[]>([]);
  const members = ref<string[]>([]);
  const isConnected = ref(false);
  let ws: WebSocket | null = null;
  let reconnectTimer: number | null = null;
  let reconnectAttempts = 0;
  const MAX_RECONNECT_ATTEMPTS = 5;
  const RECONNECT_INTERVAL = 3000;

  /**
   * Handles incoming WebSocket messages
   */
  const handleMessage = (event: MessageEvent) => {
    const data = JSON.parse(event.data);

    switch (data.type) {
      case 'joined':
        members.value = data.members;
        messages.value.push({
          sender: 'System',
          content: `${data.name} joined the chat`,
          type: 'system',
        });
        break;

      case 'members':
        members.value = data.members;
        break;

      default:
        if (data.sender && data.content) {
          messages.value.push({
            sender: data.sender,
            content: data.content,
          });
        }
    }
  };

  /**
   * Attempts to reconnect to WebSocket server
   */
  const tryReconnect = (userName: string) => {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      console.error('Max reconnection attempts reached');
      return;
    }

    reconnectTimer = window.setTimeout(() => {
      reconnectAttempts++;
      connect(userName);
      console.log(`Reconnecting ${userName} (attempt ${reconnectAttempts})`);
    }, RECONNECT_INTERVAL);
  };

  /**
   * Establishes WebSocket connection
   */
  const connect = (userName: string) => {
    if (ws?.readyState === WebSocket.OPEN) {
      return;
    }

    ws = new WebSocket(`ws://localhost:8080/ws-chat/${userName}`);

    ws.onopen = () => {
      console.log('Connected to chat server');
      isConnected.value = true;
      reconnectAttempts = 0;
    };

    ws.onmessage = handleMessage;

    ws.onclose = () => {
      console.log('Disconnected from chat server');
      isConnected.value = false;
      tryReconnect(userName);
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      ws?.close();
    };
  };

  /**
   * Sends a message through WebSocket
   */
  const sendMessage = (content: string) => {
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ content }));
    } else {
      console.error('WebSocket is not connected');
    }
  };

  // Cleanup on component unmount
  onUnmounted(() => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
    }
    if (ws) {
      ws.close();
    }
  });

  return {
    connect,
    sendMessage,
    messages,
    members,
    isConnected,
  };
}
