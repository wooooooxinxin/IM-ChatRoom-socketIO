package client;

import client.bean.ServerInfo;

import java.io.IOException;

public class Client {

    public static void main(String[] args){
        ServerInfo Info = UDPSearcher.searchServer(10000);
        System.out.println("Server info: " + Info);

        if (Info != null){
            try{
                TCPClient.linkWith(Info);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
