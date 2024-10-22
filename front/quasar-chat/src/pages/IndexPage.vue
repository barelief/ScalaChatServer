<template>
  <q-page>
    <LoginForm v-if="!isConnected" @join="handleJoin" />
    <ChatRoom
      v-else
      :messages="messages"
      :members="members"
      :user-name="userName"
      @send-message="handleSendMessage"
    />
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useWebSocket } from '../services/websockets';
import LoginForm from '../components/LoginForm.vue';
import ChatRoom from '../components/ChatRoom.vue';

const userName = ref('');
const { connect, sendMessage, messages, members, isConnected } = useWebSocket();

const handleJoin = (name: string) => {
  userName.value = name;
  connect(name);
};

const handleSendMessage = (message: string) => {
  sendMessage(message);
};
</script>
