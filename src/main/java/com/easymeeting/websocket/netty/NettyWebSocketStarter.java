package com.easymeeting.websocket.netty;


import com.easymeeting.entity.config.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
@Slf4j
public class NettyWebSocketStarter implements Runnable {

    /**
     * boss线程组，用于处理链接
     */
    private EventLoopGroup bossGroup = new NioEventLoopGroup();

    /**
     * work线程，处理消息
     */
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Resource
    private HandlerTokenValidation handlerTokenValidation;
    @Resource
    private HandlerWebSocket handlerWebSocket;
    @Resource
    private AppConfig appConfig;

    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //对http协议的支持，使用http的编码器，解码器
                            pipeline.addLast(new HttpServerCodec());
                            //http消息聚合器,将分片的http消息聚合成完整的HttpRequest或者FullHttpResponse
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            //int readerIdleTimeSeconds, 一段时间未收到看客户端数据
                            // int writerIdleTimeSeconds,  一段时间没有向客服端数据
                            // int allIdleTimeSeconds  读取和写都无活动
                            pipeline.addLast(new IdleStateHandler(6, 0, 0));
                            pipeline.addLast(new HandlerHeartBeat());
                            //token校验,拦截channel事件
                            pipeline.addLast(handlerTokenValidation);
                            // websocket协议处理器
                            // String websocketPath, 路径
                            // String subprotocols, 指定支持的子协议
                            // boolean allowExtensions, 是否允许websocket拓展
                            // int maxFrameSize, 设置最大帧数
                            // boolean allowMaskMismatch, 是否允许掩码不匹配
                            // boolean checkStartsWith, 是否严格检查路径开头
                            // long handshakeTimeoutMillis 握手超时时间
                            pipeline.addLast(new WebSocketServerProtocolHandler(
                                    "/ws", null, true, 64 * 1024,
                                    true, true, 10000L
                            ));

                            pipeline.addLast(handlerWebSocket);
                        }
                    });

            Channel channel=serverBootstrap.bind(appConfig.getWsPort()).sync().channel();
            log.info("Netty服务启动成功,端口{}",appConfig.getWsPort());
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("NettyWebSocketStarter 启动失败", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    @PreDestroy
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}


