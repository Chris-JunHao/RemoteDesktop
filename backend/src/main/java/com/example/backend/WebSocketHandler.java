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
    private EventLoopGroup group;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder());
                    }
                });

        try {
            // Try to connect to the VNC server
            vncChannel = bootstrap.connect("localhost", 5900).sync().channel();

            // Notify client that the connection was successful
            session.sendMessage(new TextMessage("VNC connection established successfully."));

            // From VNC receive data and send to WebSocket
            vncChannel.pipeline().addLast(new SimpleChannelInboundHandler<byte[]>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
                    session.sendMessage(new BinaryMessage(msg));
                }
            });
        } catch (Exception e) {
            // Notify client that the connection failed
            session.sendMessage(new TextMessage("Failed to connect to VNC server: " + e.getMessage()));
            group.shutdownGracefully();
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Forward WebSocket data to the VNC server
        if (vncChannel != null && vncChannel.isActive()) {
            byte[] data = message.getPayload().getBytes(StandardCharsets.UTF_8);
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            vncChannel.writeAndFlush(buf);
        } else {
            session.sendMessage(new TextMessage("VNC connection is not active."));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (vncChannel != null) {
            vncChannel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
