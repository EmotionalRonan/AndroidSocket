package tk.iovr.androidsocketconnect;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient  implements Runnable {

    private static final String TAG = "SocketClient";
    private static SocketClient mClientSocket;
    private static String Tag="SocketTCPClient";


    private Socket socket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String content = "";

    public static String server_ip = "192.168.2.21";
    public static String server_port ="5111" ;
    public static boolean SocketStatus = false;


    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                handler.postDelayed(this, 1000);
                if (socket==null||socket.isOutputShutdown()||!SocketStatus||!socket.isConnected()) {
                    SocketStatus=false;
                    new Thread(() -> ConnectionService()).start();
                }
                //设置状态
                // 这里连接成功
                Log.d(TAG, "Client run:SocketStatus "+SocketStatus);

            } catch (Exception e) {
                Log.e(Tag+"->runnable定时器", e.toString());
            }
        }
    };


    public SocketClient(){
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
            handler.postDelayed(runnable, 1000); //启动定时器

        } catch (Exception ex) {
            Log.e(Tag+"->SocketTCPClient", ex.getMessage());
            SocketStatus=false;

        }
    }

    public static final SocketClient instanceClientSocket(){
        synchronized (SocketClient.class) {
            if(mClientSocket==null) {
                mClientSocket = new SocketClient();
            }
        }
        return mClientSocket;
    }

    public  void  ConnectionService(){
        try {

            if(socket!=null){
                socket.close();
            }
            socket = new Socket(server_ip, Integer.parseInt(server_port));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);
            SocketStatus=true;
            //启动线程，接收服务器发送过来的数据
            new Thread(SocketClient.this).start();
        }
        catch (IOException ex) {
            SocketStatus=false;

        }
    }


    //接收线程发送过来信息
    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //处理接收的信息
            Log.e(TAG, Utils.getTimeInfo()+" Client 接收("+content+ ")：");

            if(content.contains("Z0")){
                sendMsgToServer("Z0\r\n");
            }


        }
    };

    @Override
    public void run() {

        try {
            char [] bbuf = new char[10000];
            StringBuilder temp = new StringBuilder();
            while (true) {
                if (!socket.isClosed()) {
                    if (socket.isConnected()) {
                        SocketStatus=true;
                        if (!socket.isInputShutdown()) {
                            int leng=in.read(bbuf);
                            content  = new String(bbuf, 0, leng);
                            temp.append(content);
                            //if (temp.toString().contains("Z")){
                            content=temp.toString();
                            temp =new StringBuilder();
                            mHandler.sendMessage(mHandler.obtainMessage());
                            //}
                            SocketStatus=true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            SocketStatus=false;
            Log.e(Tag+"->run()", e.getMessage());
        }
    }

    public void sendMsgToServer(Object msg){
        if (socket.isConnected()) {
            if (!socket.isOutputShutdown()) {
                out.println(msg);
                Log.e(TAG, "发送("+Utils.getTimeInfo()+")："+msg);
            }
        }
    }


}
