# EasyMeeting Backend Runtime Upgrade

## Runtime Topology

EasyMeeting now uses RabbitMQ as the only cross-node meeting event bus.

- Spring Boot HTTP: account, contact, meeting, file, admin, health APIs on `6060`.
- Netty WebSocket: token validation, connection management, WebRTC signaling on `6061`.
- RabbitMQ: meeting event exchange, per-node consumer queue, dead-letter queue.
- Redis: captcha, token, current meeting, online meeting members, message dedup keys.
- MySQL: user, meeting, member, file, invite, chat and meeting event log persistence.

## RabbitMQ Topology

- Exchange: `easymeeting.meeting.event.exchange`
- Dead-letter exchange: `easymeeting.meeting.event.dlx`
- Dead-letter queue: `easymeeting.meeting.event.dlq`
- Consumer queue: `easymeeting.meeting.event.queue.${app.instance-id}`

The consumer uses manual ACK. A failed delivery is republished to the node queue with a `retryCount` header. After 3 failed attempts it is rejected and routed to the dead-letter exchange.

## Operations APIs

- `GET /api/health/detail`
- `GET /api/admin/metrics/meeting`
- `POST /api/admin/message/retryDeadLetter?maxCount=10`

Admin APIs still pass through the existing `@GlobalInterceptor(checkAdmin = true)` flow.

## Local Docker Run

```bash
docker compose up -d --build
```

RabbitMQ management UI is available at `http://localhost:15672` with `guest / guest`.

The compose file mounts `src/main/resources/sql` into MySQL initialization. Keep the base schema and incremental feature SQL in that directory before first startup.
