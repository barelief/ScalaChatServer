<template>
  <div class="q-pa-md row justify-between">
    <div class="col-12 col-md-9">
      <q-card class="chat-card">
        <q-card-section class="chat-messages" ref="messagesContainer">
          <!-- Chat Bubble -->
          <q-chat-message
            v-for="(message, index) in props.messages"
            :key="index"
            :text="[message.content]"
            :name="message.sender === props.userName ? 'Me' : message.sender"
            :sent="message.sender === props.userName"
          />
        </q-card-section>

        <!-- Chat input for sending messagesz -->
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
                type="submit"
                :disable="!newMessage.trim()"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </div>

    <!-- Sidebar Trigger -->

    <q-btn
      icon="group"
      @click="toggleSidebar"
      class="q-mb-md absolute"
      round
      color="green"
      style="top: 20px; right: 20px; z-index: 1000"
    />

    <!-- MEMBERS Sidebar -->

    <q-drawer
      class="q-pb-10"
      v-model="sidebar"
      side="right"
      overlay
      @click.self="toggleSidebar"
    >
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
import { ref, onMounted, onUpdated, nextTick } from 'vue';

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

const newMessage = ref('');
const messagesContainer = ref<HTMLElement | null>(null);
const sidebar = ref(false);

const sendMessage = () => {
  if (newMessage.value.trim()) {
    emit('send-message', newMessage.value);
    newMessage.value = '';
  }
  scrollToBottom();
};

const toggleSidebar = () => {
  sidebar.value = !sidebar.value;
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
  top: 10px; /* Adjust as needed */
  z-index: 1000; /* Ensure it stays on top */
  display: flex;
  justify-content: center; /* Centering the button */
}
</style>
