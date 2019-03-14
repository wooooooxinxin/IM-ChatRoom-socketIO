package server;

import constants.TCPConstants;

import java.io.IOException;

public class Server {
    public static void main(String[] args){

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();

        if(!isSucceed){
            System.out.println("Start TCP Server fail.");
        }
        UDPProvider.start(TCPConstants.PORT_SERVER);

        try{
            System.in.read();

        }catch (IOException e){
            e.printStackTrace();
        }

        UDPProvider.stop();
        tcpServer.stop();
    }
}
