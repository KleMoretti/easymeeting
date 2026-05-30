# EasyMeeting Frontend

React browser client for the EasyMeeting core meeting flow.

## Stack

- React 18 + TypeScript + Vite
- Ant Design for UI components
- Zustand for client state
- Native WebSocket for Netty signaling
- Native WebRTC Mesh for 2-4 person audio/video meetings

`Element Plus` and `Pinia` are Vue ecosystem libraries, so this React client uses Ant Design and Zustand instead.

## Run

```bash
npm install
npm run dev
```

The dev server runs on `http://localhost:5173`.

Vite proxies:

- `/api` -> `http://localhost:6060/api`
- `/ws` -> `ws://localhost:6061/ws`

The backend still needs MySQL, Redis, RabbitMQ, Spring Boot HTTP `6060`, and Netty WebSocket `6061` running for full meeting verification.

## Checks

```bash
npm run typecheck
npm test
npm run build
```

