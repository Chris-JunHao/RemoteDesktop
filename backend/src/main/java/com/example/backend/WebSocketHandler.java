package com.example.backend;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

import java.nio.charset.StandardCharsets;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private Channel vncChannel;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接到 VNC 服务器 (假设 VNC 服务器运行在 localhost:5900)
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder());
                    }
                });

        vncChannel = bootstrap.connect("localhost", 5900).sync().channel();

        // 从 VNC 接收数据并发送到 WebSocket
        vncChannel.pipeline().addLast(new SimpleChannelInboundHandler<byte[]>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
                session.sendMessage(new BinaryMessage(msg));
            }
        });
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 将 WebSocket 的数据转发到 VNC 服务器
        byte[] data = message.getPayload().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        vncChannel.writeAndFlush(buf);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (vncChannel != null) {
            vncChannel.close();
        }
    }
}