# EasyMeeting 简历项目模拟面试

> 适用项目描述：基于 WebRTC 的在线会议系统，后端技术栈包含 Spring Boot、MySQL、Redis、RabbitMQ、Netty，支持音视频通话、屏幕共享、会议管理、实时聊天。
> 本文按“简历表达 + 源码深挖 + 场景追问”组织，适合一面到三面的项目拷打。

## 使用方式

- 如果你要自己演练，先只看题目作答，再对照“回答要点”修正表达。
- 如果你要让别人帮你模拟，可以按顺序提问，重点追打“代码锚点”和“风险边界”两列。
- 这份文档故意区分“已经实现”和“建议优化”，面试时不要把建议方案说成现有能力。

## 先说清楚的项目事实

- 这个项目当前是 `WebRTC Mesh + Netty 信令服务`，服务端不转发媒体流，主要负责信令、会议状态和消息同步。
- 跨节点会议事件总线固定使用 `RabbitMQ`，不再保留 Redis Pub/Sub/Redisson 作为候选通道；`messaging.handle.channel` 固定为 `rabbitmq`。
- 会议成员运行态主要放在 Redis，持久化信息落 MySQL。

  ```
  Redis 里当前源码实际存的，主要是这 5 类：
    - 验证码
    在 RedisComponent.java 里用 easymeeting:checkcode... 保存，TTL 5 分钟，对应 key 常量在 Constants.java。

    - 登录会话 / token
    在 RedisComponent.java 保存两份映射：
    token -> TokenUserInfoDto
    userId -> token
    TTL 是 1 天。TokenUserInfoDto 里保存的不只是 userId/nickName/sex，还包括 currentMeetingId、currentNickName、myMeetingNo、admin，见 TokenUserInfoDto.java。HTTP 鉴权和 WebSocket 握手都是先查这个 Redis token。

    - 会议房间运行态
    在 RedisComponent.java 用 easymeeting:meeting:room:{meetingId} 的 Hash 保存 MeetingMemberDto。
    里面是会中实时成员数据：userId、nickName、sex、joinTime、status、memberType、openVideo、openAudio，见 MeetingMemberDto.java。
    所以“当前会议有哪些人、谁开了摄像头/麦克风、谁已退出/被踢”这类高频状态，都是 Redis 查。

    - WebSocket 心跳
    HandlerWebSocket 收到 ping 后，会用 easymeeting:ws:user:heartbeat{userId} 保存最近心跳时间，TTL 1 分钟；连接断开时清理该 key。

    - 消息幂等标记
    MeetingEventDeduplicationService 用 easymeeting:meeting:event:processed:{messageId} 记录已处理消息，避免 RabbitMQ 至少一次投递下重复消费直接重复执行业务。

  MySQL 持久化存的，是这些业务数据：
    - 用户主数据 user_info
    用户账号基础信息：userId、email、nickName、sex、password、status、createTime、lastLoginTime、lastOffTime、meetingNo，见 UserInfo.java。

    - 会议主表 meeting_info
    会议本身的信息：meetingId、meetingNo、meetingName、createUserId、joinType、joinPassword、startTime、endTime、status，见 MeetingInfo.java。

    - 会议成员关系表 meeting_member
    谁参加过哪个会议、角色是什么、最后一次入会时间、成员状态、会议状态，见 MeetingMember.java。
    这个表是“持久关系和历史状态”，不是实时媒体状态。

    - 邀请记录表 meeting_invite_record
    谁邀请谁入会、邀请文案、处理状态、处理时间，见 20260329_feature_invitation_record.sql 和 MeetingInviteRecord.java。

    - 联系人和联系人申请
    user_contact、user_contact_apply，见 20260329_feature_contacts_reserve.sql 和 20260329_feature_contacts_reserve.sql，对应实体是 UserContact.java、UserContactApply.java。

    - 会议文件记录 meeting_file_record
    上传文件的元数据：fileId、meetingId、uploadUserId、fileName、filePath、fileSize、fileType、fileSuffix、createTime，见 20260329_feature_meeting_file_record.sql 和 MeetingFileRecord.java。

    - 聊天消息表 meeting_chat_message_xx
    聊天记录和文件消息元数据是落 MySQL 的，而且按 meetingId 分到 32 张表，见 MeetingChatMessageServiceImpl.java、TableSplitUtils.java、MeetingChatMessageMapper.xml。
    字段包括 messageId、meetingId、messageType、messageContent、sendUserId、sendUserNickName、sendTime、receiveType、receiveUserId、文件相关字段等，见 MeetingChatMessage.java。
  ```

- 聊天消息按 `meetingId` 哈希到 32 张表，不是按用户分表。
- 主持人控制能力主要是踢出、拉黑、结束会议、邀请成员。
- 当前消息可靠性语义更接近“至少一次”，不是 exactly-once。
- 当前代码里有 `currentMeetingId`，但断线回调会主动退会，所以还不能算完整的“无感重连”。

## 一分钟开场题

### 1. 你先用 1 分钟介绍一下这个项目和你负责的部分

回答要点：

- 这是一个基于 WebRTC 的在线会议系统，媒体层采用 Mesh，服务端不做音视频转发，重点承担信令交换、会议管理、状态同步和聊天消息能力。
- 我主要负责后端，技术上用了 Spring Boot 提供业务接口，用 Netty 承载 WebSocket 长连接和 WebRTC 信令，用 Redis 维护 token、当前会议、在线成员和心跳状态，用 RabbitMQ 做跨节点会议事件广播，用 MySQL 持久化会议、成员、邀请、聊天等数据。
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

- 这套 Netty 设计，本质上是把它当成“实时信令层”，而不是业务层。业务状态还是在 Spring Service + Redis + MySQL，Netty 只负责长连接接入、鉴权、连接管理和消息转发。
  ```
  Netty 服务本身在 NettyWebSocketStarter.java，用的是标准的 bossGroup + workerGroup + ServerBootstrap + NioServerSocketChannel。
  ```
- Pipeline 顺序是 HTTP 编解码、HTTP 聚合、空闲检测、心跳处理、token 校验、WebSocket 协议升级、业务消息处理。

  ```
  1. HttpServerCodec
  作用是先支持 HTTP 编解码，因为 WebSocket 握手本质上先走 HTTP。

  2. HttpObjectAggregator
  把分片 HTTP 聚合成完整请求，方便后续统一拿到 FullHttpRequest。

  3. IdleStateHandler(6, 0, 0)
  6 秒没收到客户端数据就认为读空闲，交给心跳处理器。

  4. HandlerHeartBeat
  在 HandlerHeartBeat.java (line 14)，主要处理空闲事件。当前实现重点是“客户端长时间不发心跳就断开连接”。

  5. HandlerTokenValidation
  在握手阶段拦截 FullHttpRequest，从 URI 参数里拿 token，去 Redis 校验。代码在 HandlerTokenValidation.java (line 27)。
  如果 token 无效，直接返回 403，连接不升级。
  如果有效，就把 userId -> Channel 注册到连接上下文里。

  6. WebSocketServerProtocolHandler("/ws", ...)
  完成 HTTP 到 WebSocket 的协议升级，路径固定是 /ws。

  7. HandlerWebSocket
  真正处理 TextWebSocketFrame 文本消息，在 HandlerWebSocket.java (line 29)。
  ```

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
- 之后交给 `MessageHandler`，在单机模式下就是本地发送，在集群模式下则通过 RabbitMQ 先同步，再由各节点路由到本地连接。

能拉开差距的表述：

- 我们把信令抽象成统一会议事件，是为了让 Netty 接入层和 RabbitMQ 事件总线解耦。
- 这套设计的重点不是“处理媒体数据”，而是“可靠地把协商信令送到正确的人”。

代码锚点：

- `src/main/java/com/easymeeting/websocket/netty/HandlerWebSocket.java`
- `src/main/java/com/easymeeting/entity/dto/MessageSendDto.java`
- `src/main/java/com/easymeeting/websocket/message/MessageHandler4rRabbitMq.java`
- `src/main/java/com/easymeeting/websocket/message/MeetingEventDeduplicationService.java`

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
- Redis 负责运行态，保存验证码、token 会话、用户当前会议、会议在线成员、心跳和媒体开关状态等，支撑高频读写和实时广播。
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

- 代码里保留 `MessageHandler` 抽象，但跨节点默认实现只使用 RabbitMQ。
- RabbitMQ 实现使用 durable fanout exchange 广播到所有节点，各节点消费后再把消息路由给本机用户连接。
- 发送端启用 publisher confirm；消费端采用 manual ACK，处理失败按 `retryCount` 最多重试 3 次，超过后进入 DLQ。
- 消费侧用 `messageId` 做幂等去重，并通过 `meeting_event_log` 记录投递状态、重试次数和错误原因。

一定要说实话的边界：

- 这不是 exactly-once，而是“至少一次 + 业务侧幂等/收敛”。
- 目前没有完整 outbox，但已经具备 DLX/DLQ、失败重试、消息日志和管理员死信重试入口，所以这部分属于可继续增强的点。

代码锚点：

- `src/main/java/com/easymeeting/websocket/message/MessageHandler4rRabbitMq.java`
- `src/main/java/com/easymeeting/entity/constants/Constants.java`

### 11. 如果面试官问你：为什么不再用 Redis 做消息广播？

回答要点：

- Redis 在当前项目里只负责验证码、token、用户当前会议、在线成员和心跳等运行态缓存。
- 跨节点会议事件需要确认、失败重试、死信、幂等和可追踪日志，RabbitMQ 更符合这条可靠链路。
- 所以代码保留 `MessageHandler` 抽象，但不再把 Redis Pub/Sub 作为候选消息通道，避免职责混乱。

不要踩坑：

- 不要把 Redis 说成“可靠消息队列”。在这个项目里 Redis 是状态缓存，不承担跨实例广播职责。

代码锚点：

- `src/main/java/com/easymeeting/websocket/message/MeetingEventDeduplicationService.java`
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
- 第二优先级是一致性升级：补 outbox、死信队列治理与手动重试、会议内顺序号和幂等控制。
- 第三优先级是连接体验升级：把当前掉线即退会改造成短暂保活和真正的重连恢复。
- 第四优先级是观测性：补链路日志、消息堆积监控、会议维度延迟指标、信令失败率和邀请成功率。

高分结尾：

- 我会强调优化顺序不是“想到什么做什么”，而是先解决容量上限和一致性短板，再做体验细化。

## 面试官很爱追的 6 个补刀问题

### 19. 你们现在消息可靠吗？

推荐回答：

- 当前是“至少一次”语义，RabbitMQ 侧有 publisher confirm、手动 ACK、3 次重试、DLQ 和消费幂等，但还没有做到全链路幂等和死信治理闭环，所以我不会把它描述成强一致消息系统。

### 20. 你们现在支持大规模会议吗？

推荐回答：

- 当前更适合中小规模会议，因为媒体层是 Mesh。服务端压力主要在信令和控制面，大规模会议的核心瓶颈还是端侧连接数和上行带宽，后续需要靠 SFU 解决。

### 21. 你们的 Redis 挂了怎么办？

推荐回答：

- Redis 现在只承担验证码、token 会话、用户当前会议、会议在线成员和心跳等运行态，是关键依赖。短期可以通过主从和高可用兜底；从架构上看，必须接受“没有 Redis 就没有实时会中状态”的现实，所以需要监控、主备和必要的降级方案。

### 22. 聊天消息分表为什么不是按用户分？

推荐回答：

- 因为业务主查询维度是会议，不是用户。按会议路由能让会中查询天然命中单表，跟访问模型更一致。

### 23. 踢人和用户自己退会同时发生怎么办？

推荐回答：

- 当前思路是状态优先，先更新成员状态，再广播事件，客户端以服务端最终状态收敛。它不是强顺序模型，但在当前复杂度下更现实。

### 24. 你觉得这套代码最明显的工程短板是什么？

推荐回答：

- 我会优先点三个：没有完整 outbox，死信闭环已通过 DLQ 和手动重试入口补齐基础能力；掉线重连语义还不完整；Mesh 架构下会议规模上限明显。

## 最后给你的答题提醒

- 不要把“WebRTC 在线会议”答成“我做了音视频编解码和媒体转发”。后端在这个项目里更核心的是信令、状态、权限和消息同步。
- 不要把“RabbitMQ 做了”答成“消息绝不丢失且绝不重复”。当前实现没有支撑这种表述。
- 不要把“currentMeetingId 存在 Redis”答成“已经无感重连”。当前源码还达不到。
- 如果被问优化，优先说 `SFU / 一致性 / 重连 / 可观测性` 这四个方向，顺序也要说清楚。
