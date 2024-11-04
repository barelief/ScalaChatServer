<template>
  <div class="q-pa-md row justify-between">
    <div class="col-12 col-md-9">
      <q-card class="chat-card">
        <q-card-section class="chat-messages" ref="messagesContainer">
          <!-- Chat Bubble -->
          <q-chat-message v-for="(message, index) in props.messages" :key="index" :text="[message.content]"
            :name="message.sender === props.userName ? 'Me' : message.sender"
            :sent="message.sender === props.userName" />
        </q-card-section>

        <!-- Chat input for sending messagesz -->
        <q-card-section>
          <q-form @submit.prevent="sendMessage">
            <div class="row q-gutter-sm">
              <q-input v-model="newMessage" class="col" placeholder="Type a message" dense outlined
                @keyup.enter="sendMessage" />
              <q-btn icon="send" color="primary" round type="submit" :disable="!newMessage.trim()" />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </div>

    <!-- Sidebar Trigger -->

    <q-btn icon="group" @click="toggleSidebar" class="q-mb-md absolute" round color="green"
      style="top: 20px; right: 20px; z-index: 1000" />

    <!-- MEMBERS Sidebar -->

    <q-drawer class="q-pb-10" v-model="sidebar" side="right" overlay @click.self="toggleSidebar">
      <q-card>
        <q-card-section>
          <div class="text-h6">Members</div>
          <q-list>
            <q-item v-for="member in props.members" :key="member">
              <q-item-section>
                <q-item-label>{{ member }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
      </q-card>
    </q-drawer>
  </div>
</template>

<script setup lang="ts">

// Import Vue composition API functions
import { ref, nextTick, watch } from 'vue';

interface Message {
  sender: string;
  content: string;
  type?: string;
}

const props = defineProps<{
  messages: Message[];
  members: string[];
  userName: string;
}>();

const emit = defineEmits<{
  (e: 'send-message', message: string): void;
}>();

// Reactive references
const newMessage = ref('');
const messagesContainer = ref<HTMLElement | null>(null);
const sidebar = ref(false);

// Keep track of message count to know when new messages arrive
let messageCount = ref(props.messages.length);

// Handler for sending messages
const sendMessage = () => {
  if (newMessage.value.trim()) {
    emit('send-message', newMessage.value);
    newMessage.value = '';
  }

  // setTimeout(scrollToBottom, 1000);
};

// Watch for changes in the messages prop
watch(() => props.messages, async (newMessages) => {
  // Only scroll if messages were added (not on initial load)
  if (newMessages.length > messageCount.value) {
    messageCount.value = newMessages.length;
    scrollToBottom();
  }
}, { deep: true });

// Handler for toggling the sidebar
const toggleSidebar = () => {
  sidebar.value = !sidebar.value;
};

// Scroll to the bottom of the chat messages after sending a message
const scrollToBottom = async () => {
  await nextTick();
  const messages = document.querySelectorAll('.q-message');
  if (messages.length) {
    messages[messages.length - 1].scrollIntoView({ behavior: 'smooth' });
  }
};

</script>

<style scoped>
.chat-card {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex-grow: 1;
  overflow-y: auto;
}

.sticky-button {
  position: sticky;
  top: 10px;
  z-index: 1000;
  display: flex;
  justify-content: center;
}
</style>
