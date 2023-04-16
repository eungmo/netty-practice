package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.Charset;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {

    // 이벤트로 소켓 채널이 최초 활성화되었을 때 실행된다.
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //super.channelActive(ctx);
        String sendMessage = "Hello, Netty!";

        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes());

        StringBuilder sb = new StringBuilder();
        sb.append("전송한 문자열 [");
        sb.append(sendMessage);
        sb.append("]");

        System.out.println(sb.toString());
        ctx.writeAndFlush(messageBuffer); // 채널에 데이터를 쓰고 서버로 전송
    }

    // 서버로부터 수신된 데이터가 있을 때 호출되는 네티 이벤트 메서드
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        // 수신된 뎅터에서 문자열을 추출
        String readMessage = ((ByteBuf)msg).toString(Charset.defaultCharset());

        StringBuilder sb = new StringBuilder();
        sb.append("수신한 문자열 [");
        sb.append(readMessage);
        sb.append("]");

        System.out.println(sb.toString());
    }

    // 수신된 데이터를 모두 읽었을 때 호출되는 이벤트 메서드
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //super.channelReadComplete(ctx);
        // 데이터를 모두 읽은 후, 서버와 연결된 채널을 닫느다.
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}