import { ref, onUnmounted } from 'vue';

interface Message {
  sender: string;
  content: string;
  type?: string;
}

// Custom hook for WebSocket connection
export function useWebSocket() {
  // Reactive variables
  const messages = ref<Message[]>([]);
  // List of chat members
  const members = ref<string[]>([]);
  // Connection status
  const isConnected = ref(false);
  // WebSocket instance
  let ws: WebSocket | null = null;
  // Reconnection timer
  let reconnectTimer: number | null = null;
  // Reconnection attempts counter
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
        // Update list of chat members
        members.value = data.members;
        // Add system message
        messages.value.push({
          sender: 'System',
          content: `${data.name} joined the chat`,
          type: 'system',
        });
        break;

      case 'members':
        // Update list of chat members
        members.value = data.members;
        break;

      default:
        // Add new message to chat
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
    // Close existing WebSocket connection
    if (ws?.readyState === WebSocket.OPEN) {
      return;
    }

    // Create new WebSocket connection
    ws = new WebSocket(`ws://localhost:8080/ws-chat/${userName}`);

    // WebSocket event listeners
    ws.onopen = () => {
      console.log('Connected to chat server');
      isConnected.value = true;
      reconnectAttempts = 0;
    };

    // Handle incoming messages
    ws.onmessage = handleMessage;

    // Handle connection close
    ws.onclose = () => {
      console.log('Disconnected from chat server');
      isConnected.value = false;
      tryReconnect(userName);
    };

    // Handle WebSocket errors
    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      ws?.close();
    };
  };

  /**
   * Sends a message through WebSocket
   */
  const sendMessage = (content: string) => {
    //
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

  // Expose functions and reactive variables
  return {
    connect,
    sendMessage,
    messages,
    members,
    isConnected,
  };
}
