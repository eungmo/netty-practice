package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class NonBlockingServer {
    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);

    private void startEchoServer() {
        try (
                // 자신에게 등록된 채널에 변경 사항이 발생했는지 검사하고 그 채널에 대한 접그을 가능케 해준다.
                Selector selector = Selector.open();
                // 블로킹 소켓과 다르게 논블로킹 서버 소켓은 소켓 채널을 먼저 생성하고 사용할 포트를 바인딩하다.
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()
            ) {

            if ((serverSocketChannel.isOpen()) && (selector.isOpen())) {
                // 디폴트는 true
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.bind(new InetSocketAddress(8888));

                // 연결 요청인 ACCEPT를 감지한다.
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                System.out.println("접속 대기 중");

                while (true) {
                    // Selector에 등록된 채널에서 변경 사항이 발생했는지 검사한다.
                    selector.select();
                    // 채널 중 이벤트가 발생한 채널 목록
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey)keys.next();
                        // 채널에서 동일한 이벤트가 감지되는 것을 방지하기 위해서 제거한다.
                        keys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) { // 연결 요청이라면
                            this.acceptOP(key, selector);
                        } else if (key.isReadable()) { // 데이터 수신인지
                            this.readOP(key);
                        } else if (key.isWritable()) { // 데이터 쓰기인지
                            this.writeOP(key);
                        }
                    }
                }
            } else {
                System.out.println("서버 소켓을 생성하지 못했습니다.");
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        // 연결 요청 이벤트가 발생한 채널은 항상 ServerSocketChannel이므로 이벤트가 발생한 ServerSocketChannel로 캐스티한다.
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        // 클라이언트 연결을 수락하고 연결된 소켓 채널을 가져온다.
        SocketChannel socketChannel = serverChannel.accept();
        // 논블록킹 모드로 설정
        socketChannel.configureBlocking(false);

        System.out.println("클라이언트 연결됨: " + socketChannel.getRemoteAddress());
        keepDataTrack.put(socketChannel, new ArrayList<>());
        // 클라이언트 소켓 채널을 Selector에 등록하여 I/O 이벤틀 감시
        socketChannel.register(selector, SelectionKey.OP_READ);

    }

    private void readOP(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numRead = -1;
            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                System.err.println("데이터 읽기 에러!");
            }

            if (numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                System.out.println("클라이언트 연결 종료: " + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }
            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            System.out.println(new String(data, "UTF-8") + " from " + socketChannel.getRemoteAddress());
            doEchoJob(key, data);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        channelData.add(data);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void writeOP(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> its = channelData.iterator();

        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        NonBlockingServer main = new NonBlockingServer();
        main.startEchoServer();
    }
}