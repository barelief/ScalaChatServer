import { ref } from 'vue';

interface Message {
  sender: string;
  content: string;
  type?: string;
  members?: string[];
}

export const useWebSocket = () => {
  const socket = ref<WebSocket | null>(null);
  const members = ref<string[]>([]);
  const messages = ref<Message[]>([]);
  const isConnected = ref(false);

  const connect = (userName: string) => {
    socket.value = new WebSocket(
      `ws://localhost:8080/ws-chat/${encodeURIComponent(userName)}`
    );

    socket.value.onopen = () => {
      isConnected.value = true;
      addMessage({ sender: 'System', content: 'You connected to chat room' });
    };

    socket.value.onmessage = (event) => {
      const data = JSON.parse(event.data);
      if (data.type === 'joined') {
        members.value = data.members;
        if (data.name !== userName) {
          addMessage({
            sender: 'System',
            content: `${data.name} has joined the chat room`,
          });
        }
      } else if (data.type === 'members') {
        members.value = data.members;
      } else if (data.sender && data.content) {
        addMessage(data);
      }
    };

    socket.value.onclose = () => {
      isConnected.value = false;
      addMessage({ sender: 'System', content: 'Disconnected from chat room' });
    };
  };

  const sendMessage = (content: string) => {
    if (socket.value?.readyState === WebSocket.OPEN && content.trim()) {
      socket.value.send(JSON.stringify({ content }));
    }
  };

  const addMessage = (message: Message) => {
    messages.value.push(message);
  };

  const requestMemberList = () => {
    if (socket.value?.readyState === WebSocket.OPEN) {
      socket.value.send(JSON.stringify({ type: 'getMembers' }));
    }
  };

  return {
    connect,
    sendMessage,
    messages,
    members,
    isConnected,
    requestMemberList,
  };
};
