<template>
  <q-drawer
    v-model="rightDrawerOpen"
    side="right"
    bordered
    :breakpoint="700"
    class="bg-grey-1"
  >
    <q-scroll-area class="fit">
      <q-list padding>
        <q-item-label header>Online Members</q-item-label>

        <q-item v-for="member in props.members" :key="member">
          <q-item-section avatar>
            <q-avatar color="primary" text-color="white">
              {{ member.charAt(0).toUpperCase() }}
            </q-avatar>
          </q-item-section>

          <q-item-section>
            <q-item-label>{{ member }}</q-item-label>
            <q-item-label caption>
              {{ member === currentUser ? '(You)' : 'Online' }}
            </q-item-label>
          </q-item-section>

          <q-item-section side>
            <q-badge rounded color="green" class="q-px-xs" />
          </q-item-section>
        </q-item>
      </q-list>
    </q-scroll-area>
  </q-drawer>

  <!-- Mobile Toggle Button -->
  <q-page-sticky position="right" :offset="[18, 18]" class="lt-md">
    <q-btn round color="primary" icon="people" @click="toggleRightDrawer" />
  </q-page-sticky>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
  members: string[];
  currentUser: string;
}>();

const rightDrawerOpen = ref(true);

const toggleRightDrawer = () => {
  rightDrawerOpen.value = !rightDrawerOpen.value;
};
</script>
