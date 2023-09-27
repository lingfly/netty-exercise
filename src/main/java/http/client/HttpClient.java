package http.client;

import com.alibaba.fastjson.JSON;
import config.Constant;
import entity.Data;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Scanner;

public class HttpClient {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new HttpClientInitializer());


        bootstrap.remoteAddress("127.0.0.1", 8080);
        ChannelFuture future = bootstrap.connect().sync();
        System.out.println("connected");

        Scanner in = new Scanner(System.in);

        while (in.hasNextLine()) {
            String line = in.nextLine();
            future.channel().writeAndFlush(json(line));
        }

        future.channel().closeFuture().sync();
    }


    static class HttpClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("logging", new LoggingHandler(Constant.LOG_LEVEL))
                    .addLast(new HttpClientCodec())
                    .addLast(new HttpObjectAggregator(Constant.MAX_CONTENT_LENGTH))
                    .addLast(new HttpClientHandler());
        }
    }

    static FullHttpRequest json(String msg) {
        Data data = new Data();
        data.setMsg(msg);
        byte[] bytes = JSON.toJSONBytes(data);
        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "/", buf);
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        req.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        return req;
    }

    static FullHttpRequest test(String msg) {
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "/");
        req.content().writeBytes(msg.getBytes());
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, req.content().readableBytes());
        req.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/text");
        return req;
    }

}
