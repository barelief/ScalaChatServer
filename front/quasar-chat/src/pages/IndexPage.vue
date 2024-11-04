<template>
  <q-page>
    <LoginForm v-if="!isConnected" @join="handleJoin" />
    <ChatRoom v-else :messages="messages" :members="members" :user-name="userName" @send-message="handleSendMessage" />
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useWebSocket } from '../services/websockets';
import LoginForm from '../components/LoginForm.vue';
import ChatRoom from '../components/ChatRoom.vue';

const userName = ref('');

// Destructure the WebSocket composition API functions
const { connect, sendMessage, messages, members, isConnected } = useWebSocket();

// Handles the join event from the LoginForm component
const handleJoin = (name: string) => {
  userName.value = name;
  connect(name);
};

// Handles the send-message event from the ChatRoom component
const handleSendMessage = (message: string) => {
  sendMessage(message);
};
</script>
