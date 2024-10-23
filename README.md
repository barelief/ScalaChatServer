# Chat Client with Scala Backend

This is a real-time chat application using a Scala backend and a Vue.js-based frontend with the Quasar Framework, organized as a monorepo. Users can join chat rooms, send messages, and view connected members in real-time.

![Screenshot (73)](https://github.com/user-attachments/assets/a44a5391-f9a3-464e-9156-2c87a34e0418)

## Features

- Real-time messaging using WebSockets.
- Connected members display with live updates.
- Display of join and leave messages.
- Simple username-based login.

### **Frontend (Quasar-based Vue.js)**

- **Folder**: `front/quasar-chat/src/`
  - **Components**:
    - `ChatRoom.vue`: Core chat room UI.
    - `LoginForm.vue`: Handles user login input.
  - **Pages**:
    - `IndexPage.vue`: The main page, which leads from login to the chat room.
  - **Layouts**:
    - `MainLayout.vue`: Layout used across the app.
  - **Services**:
    - `websockets.ts`: Manages WebSocket connections to the backend.
  - **Router**:
    - `routes.ts`: Configures app routing for navigation between pages.

### **Backend (Scala with Akka Streams)**

- **Folder**: `project/src/main/scala/`
  - **Main.scala**: Entry point for the backend server.
  - **ChatServer.scala**: Sets up WebSocket connections and manages messaging.
  - **ChatRoomActor.scala**: Manages chat room actors for message handling.
  - **WebSocketFlow.scala**: Handles WebSocket flows for message routing.
  - **JsonMessageAdapter.scala**: Manages JSON serialization and deserialization for WebSocket communication.
  - **Logging.scala**: Handles logging for the backend using Logback.

### **Other Files**

- **Frontend**:
  - `public/index.html`: Entry point for the frontend, working js prototype.
- **Backend**:
  - `resources/logback.xml`: Configuration for logging, using Logback.

## Requirements

**Frontend**: Quasar Framework (Vue.js). Full details on dependencies in `package.json`.

**Backend**: Scala with Akka Streams and WebSocket support. Full details of dependencies in `build.sbt`.

## Getting Started

Clone the repository.

### **Frontend Setup**

1. Navigate to the frontend folder: `cd front/quasar-chat`.
2. Install dependencies using `npm install` or `yarn`.
3. Start the development server with `quasar dev`.
4. Open `http://localhost:8080` in your browser.

### **Backend Setup**

1. Navigate to the root repo folder
2. Run the Scala backend server using `sbt run`.
3. The WebSocket server will be accessible at `ws://localhost:8080/ws-chat/`.

## TODO

### **Frontend**

- [ ] Write Comments!
- [ ] Fix buggy member list slider on desktop.
- [ ] Automatically scroll to the bottom of the `chatmessages` area when new messages arrive.
- [ ] Add auto-reconnect functionality for WebSockets after idle.

### **Backend**

- [ ] Write Comments!
- [ ] Implement message persistence with a database.
- [ ] Add user authentication.
- [ ] Resolve duplicate user error on login.
