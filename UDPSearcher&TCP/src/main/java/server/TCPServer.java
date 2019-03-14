package server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private final int port;
    private ClientListener mListener;

    public TCPServer(int port){
        this.port = port;
    }

    public boolean start(){
        try{
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop(){
        if(mListener != null){
            mListener.exit();
        }
    }

    private static class ClientListener extends Thread{
        private ServerSocket server;
        private boolean done = false;

        private ClientListener(int port) throws IOException{
            this.server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress() + " Port: " + server.getLocalPort());
        }

        public void run(){
            super.run();

            System.out.println("服务器准备就绪：");

            do{
                Socket client;
                try{
                    client = server.accept();
                }catch (IOException e){
                    continue;
                }

                ClientHandler clientHandler = new ClientHandler(client);
                clientHandler.start();

            }while (!done);

            System.out.println("服务器已关闭");
        }

        public void exit(){
            done = true;
            try{
                server.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static class ClientHandler extends Thread{
        private Socket socket;
        private boolean flag = true;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        public void run(){
            super.run();
            System.out.println("新客户端连接： " + socket.getInetAddress() + " Port: " + socket.getPort());

            try{
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do{
                    String str = socketInput.readLine();
                    if("bye".equalsIgnoreCase(str)){
                        flag = false;
                        socketOutput.println("bye");
                    }else{
                        System.out.println(str);
                        socketOutput.println("回送： "+ str.length());
                    }
                }while(flag);

                socketInput.close();
                socketOutput.close();


            }catch(IOException e){
                System.out.println("连接异常断开");
            }finally {
                try{
                    socket.close();
                }catch(IOException e){
                    e.printStackTrace();
                }

            }

            System.out.println("客户端已退出： "+ socket.getInetAddress() + "P: " + socket.getPort());

        }
    }
}
