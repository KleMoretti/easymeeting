# EasyMeeting 简历项目模拟面试

> 适用项目描述：基于 WebRTC 的在线会议系统，后端技术栈包含 Spring Boot、MySQL、Redis、RabbitMQ、Netty，支持音视频通话、屏幕共享、会议管理、实时聊天。
> 本文按“简历表达 + 源码深挖 + 场景追问”组织，适合一面到三面的项目拷打。

## 使用方式

- 如果你要自己演练，先只看题目作答，再对照“回答要点”修正表达。
- 如果你要让别人帮你模拟，可以按顺序提问，重点追打“代码锚点”和“风险边界”两列。
- 这份文档故意区分“已经实现”和“建议优化”，面试时不要把建议方案说成现有能力。

## 先说清楚的项目事实

- 这个项目当前是 `WebRTC Mesh + Netty 信令服务`，服务端不转发媒体流，主要负责信令、会议状态和消息同步。
- 集群消息总线支持 `RabbitMQ` 和 `Redis Pub/Sub` 两套实现，通过配置项 `messaging.handle.channel` 切换。
- 会议成员运行态主要放在 Redis，持久化信息落 MySQL。
- 聊天消息按 `meetingId` 哈希到 32 张表，不是按用户分表。
- 主持人控制能力主要是踢出、拉黑、结束会议、邀请成员。
- 当前消息可靠性语义更接近“至少一次”，不是 exactly-once。
- 当前代码里有 `currentMeetingId`，但断线回调会主动退会，所以还不能算完整的“无感重连”。

## 一分钟开场题

### 1. 你先用 1 分钟介绍一下这个项目和你负责的部分

回答要点：
- 这是一个基于 WebRTC 的在线会议系统，媒体层采用 Mesh，服务端不做音视频转发，重点承担信令交换、会议管理、状态同步和聊天消息能力。
- 我主要负责后端，技术上用了 Spring Boot 提供业务接口，用 Netty 承载 WebSocket 长连接和 WebRTC 信令，用 Redis 维护 token 和会议成员运行态，用 RabbitMQ/Redis 做集群消息同步，用 MySQL 持久化会议、成员、邀请、聊天等数据。
- 项目里我重点做了 4 件事：一是信令链路和长连接管理，二是会议状态与主持人权限控制，三是邀请和实时消息同步，四是聊天消息分表和文件上传下载安全控制。

建议表达：
- 不要说“我做了 WebRTC 服务端转发”，因为代码里没有 SFU/MCU。
- 要强调“后端负责控制面，不负责媒体面”。

代码锚点：
- `src/main/java/com/easymeeting/websocket/netty/NettyWebSocketStarter.java`
- `src/main/java/com/easymeeting/service/impl/MeetingInfoServiceImpl.java`
- `src/main/java/com/easymeeting/websocket/message/MessageHandler4rRabbitMq.java`

## 架构与链路题

### 2. 为什么这个项目选择 WebRTC Mesh，而不是一开始就上 SFU？

回答要点：
- 项目初期优先目标是中小规模会议的可用性和迭代速度，Mesh 不需要服务端承担媒体转发压力，后端只需要把控制面和信令面做好。
- 这样可以把复杂度集中在会议管理、权限、状态同步、消息可靠性这些更直接影响业务交付的部分。
- 现在的代码结构已经把信令转发和业务控制拆开了，后续如果上 SFU，主要是媒体路径变化，会议控制层可以继续复用。

面试官继续追问时可以补：
- Mesh 的瓶颈在端侧上行带宽和多 Peer 连接数。
- 这个项目更适合小中型会议；如果目标是更大规模会议，需要引入 SFU。

代码锚点：
- `src/main/java/com/easymeeting/websocket/netty/HandlerWebSocket.java`
- `src/main/java/com/easymeeting/entity/dto/PeerConnectionDataDto.java`

### 3. Netty 这一层你是怎么设计的？Pipeline 里每一层做什么？

回答要点：
- Pipeline 顺序是 HTTP 编解码、HTTP 聚合、空闲检测、心跳处理、token 校验、WebSocket 协议升级、业务消息处理。
- 这样做的原因是先用低成本逻辑把非法连接挡掉，再让真正的业务 Handler 只面对已经鉴权成功的 WebSocket 文本帧。
- `IdleStateHandler` 用来识别长时间无读事件，配合心跳处理，避免僵尸连接长期占用资源。

高质量补充：
- `HandlerTokenValidation` 是在握手阶段从 URI 里拿 `token` 去 Redis 校验，失败就直接返回 403。
- 连接建立后会把 `userId` 和 `Channel` 建立映射，后面才能做单播或会议室广播。

代码锚点：
- `src/main/java/com/easymeeting/websocket/netty/NettyWebSocketStarter.java`
- `src/main/java/com/easymeeting/websocket/netty/HandlerTokenValidation.java`
- `src/main/java/com/easymeeting/websocket/ChannelContextUtils.java`

### 4. 一条 WebRTC 信令消息在系统里是怎么流转的？

回答要点：
- 客户端通过 WebSocket 发文本消息，消息里带 `token`、`signalType`、`signalData`、`receiveUserId`。
- `HandlerWebSocket` 收到后先做 token 校验和当前会议校验，再把消息封装成统一的 `MessageSendDto`。
- 如果有目标用户就走单播，没有就走会议群播。
- 之后交给 `MessageHandler`，在单机模式下就是本地发送，在集群模式下则通过 RabbitMQ 或 Redis Pub/Sub 先同步，再由各节点路由到本地连接。

能拉开差距的表述：
- 我们把信令抽象成统一消息对象，是为了屏蔽底层消息中间件差异。
- 这套设计的重点不是“处理媒体数据”，而是“可靠地把协商信令送到正确的人”。

代码锚点：
- `src/main/java/com/easymeeting/websocket/netty/HandlerWebSocket.java`
- `src/main/java/com/easymeeting/entity/dto/MessageSendDto.java`
- `src/main/java/com/easymeeting/websocket/message/MessageHandler4rRabbitMq.java`
- `src/main/java/com/easymeeting/websocket/message/MessageHandler4Redis.java`

### 5. 为什么这里要自己用 Netty，而不是直接用 Spring WebSocket？

回答要点：
- 这个项目对连接生命周期、心跳、握手前鉴权、连接和会议室映射、单播/群播控制比较敏感，Netty 可控性更高。
- Netty 更方便直接操作 `Channel`、`ChannelGroup`，对高并发长连接场景更贴近底层。
- 代价是开发复杂度更高，但换来了链路行为更可预测，也更方便后续针对实时链路调优。

不要说过头：
- 不是说 Spring WebSocket 做不了，而是这个场景下 Netty 更适合精细控制。

## 状态同步与权限题

### 6. 会议成员状态为什么既写 MySQL，又放 Redis？

回答要点：
- MySQL 负责持久化，保留会议成员关系、状态、邀请记录等长期数据。
- Redis 负责运行态，保存当前会议成员列表、媒体开关状态、token 会话等，支撑高频读写和实时广播。
- 会议成员加入时会写库并写 Redis；退出时会更新 Redis 状态、更新数据库成员状态，同时清理 token 上的 `currentMeetingId`。

面试时可以顺手强调：
- 这是典型的“DB 保长期一致，Redis 保运行时性能”的分层。
- 代价是会有双写一致性问题，所以关键状态要有补偿和最终收敛思路。

代码锚点：
- `src/main/java/com/easymeeting/service/impl/MeetingInfoServiceImpl.java`
- `src/main/java/com/easymeeting/redis/RedisComponent.java`

### 7. 主持人踢人、拉黑是怎么做的？为什么说权限控制是可靠的？

回答要点：
- 主持人控制入口在会议服务层，不是前端自己决定。
- `forceExitMeeting` 会先校验当前操作人是不是会议创建者，不通过直接拒绝。
- 如果目标用户在线，就直接走退会逻辑；如果不在线，也会更新 Redis 和数据库里的成员状态，再广播退出事件。
- 用户再次入会前会同时检查数据库和 Redis 里的成员状态，只要命中黑名单就拒绝入会。

高分补充：
- 权限不是单点校验，而是“入口校验 + 服务层校验 + 入会前状态校验”三层结合。
- 面试官如果问“被拉黑还能不能通过别的接口绕过”，你要回答不能，因为最终以服务端状态校验为准。

代码锚点：
- `src/main/java/com/easymeeting/service/impl/MeetingInfoServiceImpl.java`
- `src/main/java/com/easymeeting/controller/MeetingInfoController.java`

### 8. 实时邀请是怎么设计的？怎么避免重复邀请？

回答要点：
- 邀请前会校验几件事：不能邀请自己、邀请人必须在会中、只能邀请联系人、被邀请人必须在线、不能已经在当前会议里。
- 邀请记录先落库到 `meeting_invite_record`，状态初始为 `PENDING`，然后再通过实时消息通知目标用户。
- 为了避免重复邀请，发送前会查同一个会议下同一个接收人的待处理邀请，如果已存在就直接拒绝。
- 被邀请人入会后会把对应待处理邀请更新为 `ACCEPT`，拒绝和撤回也都是状态机流转。

代码锚点：
- `src/main/java/com/easymeeting/service/impl/MeetingInfoServiceImpl.java`
- `src/main/resources/com/easymeeting/mappers/MeetingInviteRecordMapper.xml`
- `src/main/resources/sql/20260329_feature_invitation_record.sql`

### 9. 用户开关摄像头和麦克风时，为什么先改 Redis 再广播？

回答要点：
- 因为这是典型的会议运行态，变化频繁，应该先更新高频读写的运行态缓存，再把变更消息广播给房间内成员。
- 当前实现会先读取旧值，再更新 Redis 里的成员媒体状态，然后根据变化类型分别广播视频变化、音频变化，以及一个总的媒体变化事件。
- 这样前端既可以按细粒度事件更新，也可以按总状态刷新。

代码锚点：
- `src/main/java/com/easymeeting/service/impl/MeetingInfoServiceImpl.java`
- `src/main/java/com/easymeeting/entity/dto/MeetingMediaStatusDto.java`

## 消息可靠性与集群题

### 10. 你简历里写 RabbitMQ 保障集群下消息同步，具体是怎么做的？

回答要点：
- 代码里消息总线抽象成 `MessageHandler` 接口，当前有 RabbitMQ 和 Redis 两套实现，通过配置切换。
- RabbitMQ 实现使用 fanout exchange 广播到所有节点，各节点消费后再把消息路由给本机用户连接。
- 消费端采用手动 ACK；处理失败会读消息头中的重试次数，最多重试 3 次，超过后拒绝。

一定要说实话的边界：
- 这不是 exactly-once，而是“至少一次 + 业务侧幂等/收敛”。
- 目前没有完整 outbox，也没有死信队列治理闭环，所以这部分属于可继续增强的点。

代码锚点：
- `src/main/java/com/easymeeting/websocket/message/MessageHandler4rRabbitMq.java`
- `src/main/java/com/easymeeting/entity/constants/Constants.java`

### 11. 如果面试官问你：那 Redis Pub/Sub 和 RabbitMQ 的区别是什么？

回答要点：
- Redis Pub/Sub 实现更轻，适合同机房、低复杂度、快速同步场景。
- RabbitMQ 更适合强调消息确认、失败重试和跨节点广播的场景。
- 所以项目里把消息发送抽象成统一接口，后面可以按环境切换，不把业务代码绑定死到某一种中间件。

不要踩坑：
- 不要把 Redis Pub/Sub 说成“可靠消息队列”，它更像广播机制，不自带像 MQ 那样的确认和持久化能力。

代码锚点：
- `src/main/java/com/easymeeting/websocket/message/MessageHandler4Redis.java`
- `src/main/java/com/easymeeting/websocket/message/MessageHandler4rRabbitMq.java`

### 12. 这个项目怎么保证消息有序和状态一致？

回答要点：
- 当前没有做全局强顺序，核心策略是“关键状态先落地，广播消息做通知，客户端以最终状态收敛”。
- 统一消息对象里会补 `messageId` 和 `sendTime`，用于排序和幂等辅助。
- 对踢人、退会、结束会议这类关键动作，服务端先改状态，再广播事件，避免只靠广播本身决定真相。

更成熟的回答方式：
- 我不会说这个系统保证全局强一致或绝对有序。
- 我会说它目前是工程化的最终一致模型，如果继续增强，可以引入会议内 sequence、版本号、outbox 和幂等表。

代码锚点：
- `src/main/java/com/easymeeting/websocket/ChannelContextUtils.java`
- `src/main/java/com/easymeeting/entity/dto/MessageSendDto.java`
- `src/main/java/com/easymeeting/service/impl/MeetingInfoServiceImpl.java`

## 分表与存储题

### 13. 聊天消息为什么要分表？按什么维度分？

回答要点：
- 聊天消息是明显的高写入场景，单表数据量变大后索引会膨胀，插入和按会议查询的性能都会受影响。
- 当前分表维度是 `meetingId`，通过 MurmurHash 路由到 32 张物理表。
- 这样会议内的消息查询天然命中单表，比较符合业务主路径。

能体现你真的看过代码的细节：
- 表名不是硬编码拼接 32 次，而是统一通过 `TableSplitUtils.getMeetingChatMessageTable(meetingId)` 算出来。
- MyBatis 的 Mapper 用 `${tableName}` 做动态表名路由。

代码锚点：
- `src/main/java/com/easymeeting/service/impl/MeetingChatMessageServiceImpl.java`
- `src/main/java/com/easymeeting/utils/TableSplitUtils.java`
- `src/main/resources/com/easymeeting/mappers/MeetingChatMessageMapper.xml`

### 14. 分表后会带来什么问题？你怎么回答才显得成熟？

回答要点：
- 优点是单会议消息读写性能更稳，热点不至于全部压在一张总表上。
- 问题是跨会议聚合变复杂，固定 32 张表后续扩容和迁移也更麻烦。
- 当前设计明显是为了优先优化在线主路径，不是为了做全局分析型查询。

建议你补一句：
- 如果后续需要做跨会议报表，我会走离线链路或汇总表，而不是在在线链路里跨 32 张表硬查。

## 安全与边界题

### 15. 文件上传下载这块你做了什么安全控制？

回答要点：
- 上传时先校验用户是否处于当前会议中，避免非会中用户上传会议文件。
- 文件名会先做归一化处理，去掉路径部分，防止用户通过文件名注入路径。
- 下载时会拿文件根目录和目标文件做 canonical path 校验，确保目标路径一定落在指定根目录下，防止目录穿越。

这题很加分，因为很多人只会说上传下载，不会说安全边界。

代码锚点：
- `src/main/java/com/easymeeting/controller/FileController.java`
- `src/main/resources/sql/20260329_feature_meeting_file_record.sql`

### 16. 登录和单端登录是怎么做的？

回答要点：
- 登录成功后会生成 token，Redis 同时保存 `token -> 用户会话` 和 `userId -> token` 两种映射，TTL 是 1 天。
- 登录时会比较 `lastLoginTime` 和 `lastOffTime`，如果判断账号仍在其他设备在线，就拒绝重复登录。
- WebSocket 握手时再拿 token 去 Redis 校验，业务接口则通过全局切面读取请求头里的 token 完成登录校验和管理员校验。

代码锚点：
- `src/main/java/com/easymeeting/service/impl/UserInfoServiceImpl.java`
- `src/main/java/com/easymeeting/redis/RedisComponent.java`
- `src/main/java/com/easymeeting/annotation/aspect/GlobalOperationAspect.java`

## 边界与改进题

### 17. 掉线重连是怎么做的？当前实现有什么不足？

回答要点：
- 当前代码里会把 `currentMeetingId` 存在 token 会话里，连接建立时如果发现用户仍有会议上下文，可以把连接重新加入会议房间映射。
- 但要注意，`channelInactive` 里当前会主动执行 `exitMeetingRoom`，并把 token 中的 `currentMeetingId` 清掉。
- 所以这版实现更接近“掉线即退会，再重新入会”，不是完整意义上的无感重连。

这一题是很好的真实感来源：
- 你可以主动承认当前方案的边界，再给出优化思路，例如增加短暂保活窗口、区分临时断线和主动退会、延迟清理会议态。

代码锚点：
- `src/main/java/com/easymeeting/websocket/netty/HandlerWebSocket.java`
- `src/main/java/com/easymeeting/websocket/ChannelContextUtils.java`
- `src/main/java/com/easymeeting/service/impl/MeetingInfoServiceImpl.java`

### 18. 如果让你继续优化这个项目，你会优先做什么？

回答要点：
- 第一优先级是容量升级：从纯 Mesh 往 SFU 演进，至少要支持按会议规模做路由策略切换。
- 第二优先级是一致性升级：补 outbox、死信队列治理、会议内顺序号和幂等控制。
- 第三优先级是连接体验升级：把当前掉线即退会改造成短暂保活和真正的重连恢复。
- 第四优先级是观测性：补链路日志、消息堆积监控、会议维度延迟指标、信令失败率和邀请成功率。

高分结尾：
- 我会强调优化顺序不是“想到什么做什么”，而是先解决容量上限和一致性短板，再做体验细化。

## 面试官很爱追的 6 个补刀问题

### 19. 你们现在消息可靠吗？

推荐回答：
- 当前是“至少一次”语义，RabbitMQ 侧有手动 ACK 和 3 次重试，但还没有做到全链路幂等和死信治理闭环，所以我不会把它描述成强一致消息系统。

### 20. 你们现在支持大规模会议吗？

推荐回答：
- 当前更适合中小规模会议，因为媒体层是 Mesh。服务端压力主要在信令和控制面，大规模会议的核心瓶颈还是端侧连接数和上行带宽，后续需要靠 SFU 解决。

### 21. 你们的 Redis 挂了怎么办？

推荐回答：
- Redis 现在承担 token 会话和会议运行态，是关键依赖。短期可以通过主从和高可用兜底；从架构上看，必须接受“没有 Redis 就没有实时会中状态”的现实，所以需要监控、主备和必要的降级方案。

### 22. 聊天消息分表为什么不是按用户分？

推荐回答：
- 因为业务主查询维度是会议，不是用户。按会议路由能让会中查询天然命中单表，跟访问模型更一致。

### 23. 踢人和用户自己退会同时发生怎么办？

推荐回答：
- 当前思路是状态优先，先更新成员状态，再广播事件，客户端以服务端最终状态收敛。它不是强顺序模型，但在当前复杂度下更现实。

### 24. 你觉得这套代码最明显的工程短板是什么？

推荐回答：
- 我会优先点三个：没有完整 outbox 和死信闭环；掉线重连语义还不完整；Mesh 架构下会议规模上限明显。

## 最后给你的答题提醒

- 不要把“WebRTC 在线会议”答成“我做了音视频编解码和媒体转发”。后端在这个项目里更核心的是信令、状态、权限和消息同步。
- 不要把“RabbitMQ 做了”答成“消息绝不丢失且绝不重复”。当前实现没有支撑这种表述。
- 不要把“currentMeetingId 存在 Redis”答成“已经无感重连”。当前源码还达不到。
- 如果被问优化，优先说 `SFU / 一致性 / 重连 / 可观测性` 这四个方向，顺序也要说清楚。

