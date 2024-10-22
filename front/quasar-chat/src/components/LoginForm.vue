<template>
  <div class="q-pa-md">
    <q-card class="login-card">
      <q-card-section>
        <div class="text-h6">Enter your name to join the chat</div>
      </q-card-section>

      <q-card-section>
        <q-input
          v-model="name"
          label="Your name"
          @keyup.enter="emit('join', name)"
        />
      </q-card-section>

      <q-card-actions align="right">
        <q-btn
          label="Join Chat"
          color="primary"
          :disable="!name"
          @click="emit('join', name)"
        />
      </q-card-actions>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits<{
  (e: 'join', name: string): void;
}>();

const name = ref('');
</script>
