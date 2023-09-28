package websocket.client;

import config.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.logging.LoggingHandler;

import java.util.Scanner;

public class WebsocketClient {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new WebsocketInitializer())
                .remoteAddress("127.0.0.1", 8080);
        ChannelFuture future = bootstrap.connect().sync();

        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            TextWebSocketFrame text = new TextWebSocketFrame(line);
            future.channel().writeAndFlush(text);
        }

        future.channel().closeFuture().sync();
    }

    static class WebsocketInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            WebSocketClientProtocolConfig.Builder builder = WebSocketClientProtocolConfig.newBuilder()
                    .webSocketUri("http://127.0.0.1:8080/ws");
            WebSocketClientProtocolConfig clientProtocolConfig = builder.build();


            ch.pipeline().addLast(new LoggingHandler())
                    .addLast(new HttpClientCodec())
                    .addLast(new HttpObjectAggregator(Constant.MAX_CONTENT_LENGTH))
                    .addLast(new WebSocketClientProtocolHandler(clientProtocolConfig))
                    .addLast(new WebsocketClientHandler());
        }
    }

    static FullHttpRequest text(String msg) {
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "/");
        req.content().writeBytes(msg.getBytes());
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, req.content().readableBytes());
        req.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/text");
        return req;
    }
}
