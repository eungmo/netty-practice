package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

// ChannelInboundHandlerAdapter: 입력된 데이터를 처리하는 이벤트 핸들러
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    // 데이터 수신 이벤트 처리 메서드
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        // 네티 바이트 버퍼 객체로부터 문자열을 읽어온다.
        String readMessage = ((ByteBuf)msg).toString(Charset.defaultCharset());
        System.out.println("수신한 문자열 [" + readMessage + "]");
        // 채널 파이프라인에 대한 이벤트를 처리한다. (여기서는 입력받은 데이터를 그대로 전송)
        ctx.write(msg);
    }

    // channelRead 이벤트 처리 완료 후, 자동으로 수행되는 이벤트 메서드
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
