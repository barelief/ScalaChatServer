# Chat Client with Scala Backend

A real-time chat application with a **Scala backend** and **HTML/JavaScript frontend**. Users can join a chat room, send messages, and view connected members.

## Features

- Real-time messaging with WebSockets.
- Connected members display.
- Simple login by username.

## Project Structure

- **Frontend**: 
  - `index.html`: HTML/CSS/JavaScript for the chat interface. Connects to the WebSocket server.
- **Backend**: 
  - `ClaudeChatServer.scala`: Scala-based WebSocket server to handle connections and broadcast messages.

## Requirements

- **Frontend**: Any modern web browser.
- **Backend**: Scala and WebSocket server at `ws://localhost:8080/ws-chat/`.

## Getting Started

1. **Frontend**:
   - Open `index.html` in a browser.
   
2. **Backend**:
   - Set up and run the Scala server using `sbt run`.