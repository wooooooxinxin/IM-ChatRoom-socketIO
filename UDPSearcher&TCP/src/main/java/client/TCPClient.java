package client;

import client.bean.ServerInfo;
import server.Server;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient {

    public static void linkWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();

        socket.setSoTimeout(30000);

        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()),info.getPort()),3000);

        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息："+ socket.getLocalAddress() + " Port: " + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " Port: " + socket.getPort());

        try{
            todo(socket);
        }catch (Exception e){
            System.out.println("异常关闭");
        }

        socket.close();
        System.out.println("客户端已退出～");
    }
    public static void todo(Socket client) throws IOException{
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        InputStream inputStream = client.getInputStream();
        BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true;
        do{
            String str = input.readLine();
            socketPrintStream.println(str);

            String echo = socketBufferedReader.readLine();
            if("bye".equalsIgnoreCase(echo)){
                flag = false;
            }else{
                System.out.println(echo);
            }
        }while (flag);

        socketPrintStream.close();
        socketBufferedReader.close();

    }


}
