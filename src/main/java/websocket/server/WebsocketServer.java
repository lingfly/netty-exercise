package websocket.server;

import config.Constant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;

public class WebsocketServer {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WebsocketInitializer());

        ChannelFuture future = bootstrap.bind(8080).sync();
        future.channel().closeFuture().sync();
    }

    static class WebsocketInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new LoggingHandler(Constant.LOG_LEVEL))
                    .addLast(new HttpServerCodec())
                    .addLast(new HttpObjectAggregator(Constant.MAX_CONTENT_LENGTH))
                    .addLast(new WebSocketServerProtocolHandler("/ws"))
                    .addLast(new WebsocketServerHandler());

        }
    }
}
