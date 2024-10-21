# Chat Client with Scala Backend

A real-time chat application with a **Scala backend** and **HTML/JavaScript frontend**. Users can join a chat room, send messages, and view connected members.

![Screenshot_1](https://github.com/user-attachments/assets/72e1228f-7bf1-4864-a50b-34334e828e77)

## Features

- Real-time messaging with WebSockets.
- Connected members display.
- Display join messages
- Simple login by username.

## Project Structure

- **Frontend**: 
  - `index.html`: HTML/CSS/JavaScript for the chat interface. Connects to the WebSocket server.
- **Backend**: 
  - `ChatServer.scala`: Scala-based WebSocket server to handle connections and broadcast messages.

## Server

1. `ChatServer.scala`: Contains the main entry point and server setup.
2. `ChatRoomActor.scala`: Defines the chat room actor and its messages.
3. `WebSocketFlow.scala`: Handles the WebSocket flow and message passing.
4. `JsonMessageAdapter.scala`: Manages JSON serialization and deserialization.

## Requirements

- **Frontend**: Any modern web browser.
- **Backend**: Scala and WebSocket server at `ws://localhost:8080/ws-chat/`.

## Getting Started

1. **Frontend**:
   - Open `index.html` in a browser.
   
2. **Backend**:
   - Set up and run the Scala server using `sbt run`.
