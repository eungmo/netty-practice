package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EchoClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group) // 서버와 다르게 연결된 채널이 하나이므로 이벤트 루프 그룹이 하나만 설정
                    .channel(NioSocketChannel.class) // 클라이언트가 생성하는 채널읠 종류
                    .handler(new ChannelInitializer<SocketChannel>() { // 채널 파이프라인의 설정에 일반 소켓 채널 클래스 사용
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new EchoClientHandler());

                        }
                    });

            // 비동기 입출력 메서드 connect()를 호출
            // 메서드 호출 결과인 ChannelFuture 객체를 통해 비동기 메서드 처리 결과를 확인할 수 있다.
            ChannelFuture f = b.connect("localhost", 8888).sync();
            f.channel().closeFuture().sync(); // sync()는 객체의요청ㅇ 완료돌 때까지 대기하고, 요청이 실패하면 예외를 던진다.
        } finally {
            group.shutdownGracefully();
        }
    }
}