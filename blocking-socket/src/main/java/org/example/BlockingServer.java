package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServer {
    public static void main(String[] args) throws IOException {
        BlockingServer server = new BlockingServer();
        server.run();
    }

    private void run() throws IOException {
        ServerSocket server = new ServerSocket(8888);
        System.out.println("접속 대기 중");

        while(true) {
            // 연결되는 클래스가 없으면 프로그램은 아무런 동작도 하지 않고 스레드 또한 해당 함수의 완료를 기다리며 대기
            Socket sock = server.accept();
            System.out.println("클라이언트 연결됨");

            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();

            while (true) {
                try {
                    int request = in.read();
                    out.write(request);
                } catch (IOException e) {
                    break;
                }
            }
        }
    }}