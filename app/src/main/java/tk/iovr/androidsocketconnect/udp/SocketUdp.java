package tk.iovr.androidsocketconnect.udp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SocketUdp {
    private static final String TAG = "SocketUdp";
    Thread mReceiveThread;
    DatagramSocket serverReceive;
    DatagramSocket serverSend;
    InetAddress local = null;
    //构造方法
    public SocketUdp(){
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

            if(serverReceive!=null){
                serverReceive.close();
                serverReceive=null;
            }
            serverReceive=new DatagramSocket(null);
            serverReceive.setReuseAddress(true);
            serverReceive.setBroadcast(true);
            serverReceive=new DatagramSocket(5061);
            mReceiveThread= new Thread(updateThread);
            mReceiveThread.start();


            local = InetAddress.getByName("192.168.1.51");
            //server.setReuseAddress(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    // 接收数据处理
    @SuppressLint("HandlerLeak")
    final Handler updateBarHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
            String strDate=df.format(new Date());
            String  receiveString =(msg.getData()).getString("data");
            Log.e(TAG, "Receiver : "+strDate +", "+receiveString);
//            SysLogActivity.AddToLog(strDate+"  "+receiveString);
            Log.e("接收："+strDate,receiveString);

            // 接收数据处理

        }
    };

    // 线程类，该类使用匿名内部类的方式进行声明
    Runnable updateThread = new Runnable() {

        public void run() {
            try{
                // 得到一个消息对象，Message类是android系统提供的
                Message msg = new Message();
                Bundle b = new Bundle();
                // 定义缓冲区
                byte[] buffer = new byte[1024];
                // 定义接收数据包
                DatagramPacket packet = new DatagramPacket(buffer,
                        buffer.length);
                while (true) {
                    msg = updateBarHandler.obtainMessage();
                    // 接收数据
                    serverReceive.receive(packet);
                    // 判断是否收到数据，然后输出字符串
                    if (packet.getLength() > 0) {
                        String str = new String(buffer, 0, packet
                                .getLength());
                        b.putString("data", str + "\n");
                        msg.setData(b);
                        // 将Message对象加入到消息队列当中
                        updateBarHandler.sendMessage(msg);
                    }
                }
            }
            catch(Exception ex){
                Log.e("socketUdp",ex.toString());
            }
        }
    };

    public void SendBuffer(byte[] buffer){
        try {
            serverSend =new DatagramSocket();
            DatagramPacket p = new DatagramPacket(buffer, buffer.length, local,5060);
            serverSend.send(p);
            serverSend.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CloseMe(){
        serverReceive.close();
    }
}
