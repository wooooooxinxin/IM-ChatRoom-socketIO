package server;


import clink.net.xinxin.clink.utils.ByteUtils;
import constants.UDPConstants;
import constants.TCPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.UUID;


public class UDPProvider {
    private static Provider PROVIDER_INSTANCE;

    static void start(int port){
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn,port);
        provider.start();
        PROVIDER_INSTANCE = provider;

    }

    static void stop(){
        if(PROVIDER_INSTANCE != null){
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread{
        private static int port;
        private static byte[] sn;
        private boolean done = false;
        private DatagramSocket ds = null;

        final byte[] buffer = new byte[128];

        Provider(String sn, int port){
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        public void run() {
            super.run();

            System.out.println("UDPProvider Started.");

            try{
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                DatagramPacket receivePack = new DatagramPacket(buffer,buffer.length);

                while (!done){
                    ds.receive(receivePack);

                    String clientIP = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientdatalen = receivePack.getLength();
                    byte[] clientdata = receivePack.getData();
                    //2:short存储的指令，4：回送的客户端端口
                    boolean isValid = clientdatalen >= (UDPConstants.Header.length + 2 + 4) && ByteUtils.startsWith(clientdata,UDPConstants.Header);

                    System.out.println("UDPProvider receive data from ip: " + clientIP + "\tport: " + clientPort + "\tdata: " + clientdata);

                    if(!isValid){
                        continue;
                    }

                    //解析命令与回送端口；
                    int index = UDPConstants.Header.length;
                    short cmd = (short)((clientdata[index++] << 8) | (clientdata[index++] & 0xff));
                    int responsePort = (int)(clientdata[index++] << 24)|((clientdata[index++] & 0xff) << 16)|((clientdata[index++] & 0xff) << 8)
                            |(clientdata[index] & 0xff);

                    //1表示搜索命令
                    if((cmd == 1) && (responsePort >0)){
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.Header);
                        byteBuffer.putShort((short)2);
                        byteBuffer.putInt(30401);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();

                        DatagramPacket datagramPacket = new DatagramPacket(buffer,len,receivePack.getAddress(),responsePort);

                        ds.send(datagramPacket);

                        System.out.println("UDPProvider response to IP:" + clientIP + "\tport:" + responsePort);

                    }else{
                        System.out.println("UDPProvider receive nonsupport cmd:" + cmd );
                    }

                }

            } catch (IOException ignored) {
            }finally {
                close();
            }

            System.out.println("UDPProvider stop.");

        }

        void close(){
            if(ds != null){
                ds.close();
                ds = null;
            }
        }

        void exit(){
            done = true;
            close();
        }

    }

}
