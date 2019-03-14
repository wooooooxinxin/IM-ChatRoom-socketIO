package client;

import client.bean.ServerInfo;
import clink.net.xinxin.clink.utils.ByteUtils;
import constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UDPSearcher {

    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher started.");

        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = listen(receiveLatch);
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("UDPSearcher finished.");

        List<ServerInfo> devices = listener.getDevicesAndClose();
        if(devices.size() > 0){
            return devices.get(0);
        }
        return null;
    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen");
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(startDownLatch, receiveLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast Started");

        DatagramSocket ds = new DatagramSocket();

        ByteBuffer byteBuffer = ByteBuffer.allocate(128);

        byteBuffer.put(UDPConstants.Header);
        byteBuffer.putShort((short) 1);
        byteBuffer.putInt(LISTEN_PORT);

        //直接根据发送者构建一份回送信息
        DatagramPacket requestPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.position() + 1);

        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        //向20000端口发送信息
        requestPacket.setPort(UDPConstants.PORT_SERVER);

        ds.send(requestPacket);
        ds.close();

        System.out.println("UDPSearcher sendBroadcast finished.");

    }

    private static class Listener extends Thread {
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveLatch;
        private final List<ServerInfo> devices = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.Header.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(CountDownLatch startLatch, CountDownLatch receiveLatch) {
            super();
            this.startDownLatch = startLatch;
            this.receiveLatch = receiveLatch;

        }


        public void run() {
            super.run();

            //通知已启动
            startDownLatch.countDown();
            try {
                ds = new DatagramSocket(LISTEN_PORT);

                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {

                    ds.receive(receivePack);

                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    byte[] data = receivePack.getData();
                    boolean isValid = dataLen >= minLen && ByteUtils.startsWith(data,UDPConstants.Header);

                    System.out.println("UDPSearcher receive from ip:" + ip + "\tport" + port + "\tdata:" + data + "\tdataValid:" + isValid);

                    if(!isValid){
                        continue;
                    }
                    //跳过头的字节
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer,UDPConstants.Header.length,dataLen);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();

                    if(cmd != 2 || serverPort < 0){
                        System.out.println("UDPSearcher receive cmd: " + cmd + "\tserverport: " + serverPort);
                        continue;
                    }

                    String sn = new String(buffer,minLen,dataLen - minLen);
                    if (sn != null) {
                        ServerInfo device = new ServerInfo(sn,serverPort,ip);
                        devices.add(device);
                    }
                    receiveLatch.countDown();
                }
            } catch (Exception ignored) {
            } finally {
                close();
            }
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        List<ServerInfo> getDevicesAndClose() {
            done = true;
            close();
            return devices;
        }

    }

}
