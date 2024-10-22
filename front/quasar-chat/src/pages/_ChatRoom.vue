<template>
  <div class="q-pa-md">
    <div class="chat-container">
      <q-card class="chat-card">
        <q-card-section class="chat-messages" ref="messagesContainer">
          <q-chat-message
            v-for="(message, index) in props.messages"
            :key="index"
            :text="[message.content]"
            :name="message.sender"
            :sent="message.sender === props.userName"
          />
        </q-card-section>

        <q-card-section>
          <q-form @submit.prevent="sendMessage">
            <div class="row q-gutter-sm">
              <q-input
                v-model="newMessage"
                class="col"
                placeholder="Type a message"
                dense
                outlined
                @keyup.enter="sendMessage"
              />
              <q-btn
                icon="send"
                color="primary"
                round
                dense
                type="submit"
                :disable="!newMessage.trim()"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUpdated, nextTick } from 'vue';

interface Message {
  sender: string;
  content: string;
  type?: string;
}

const props = defineProps<{
  messages: Message[];
  userName: string;
}>();

const emit = defineEmits<{
  (e: 'send-message', message: string): void;
}>();

const newMessage = ref('');
const messagesContainer = ref<HTMLElement | null>(null);

const sendMessage = () => {
  if (newMessage.value.trim()) {
    emit('send-message', newMessage.value);
    newMessage.value = '';
  }
};

const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

onMounted(scrollToBottom);
onUpdated(scrollToBottom);
</script>

<style scoped>
.chat-container {
  max-width: 1200px;
  margin: 0 auto;
}

.chat-card {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex-grow: 1;
  overflow-y: auto;
}
</style>
