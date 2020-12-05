package tk.iovr.androidsocketconnect;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketServer  implements Runnable{

    private String TAG="SocketServer";
    //服务器端口
    private  final int SERVERPORT = 5111;
    //存储所有客户端Socket连接对象
    public List<Socket> mClientList = new ArrayList<Socket>();
    //线程池
    private ExecutorService mExecutorService;
    //ServerSocket对象
    private ServerSocket mServerSocket;
    private PrintWriter mPrintWriter;
    private  String  mStrMSG;



    Handler handler = new Handler();
    //定时发送消息
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                handler.postDelayed(this, 5000);
                SendToClient("this message from server:dongdongdong");
            } catch (Exception e) {
                Log.e(TAG+"->runnable定时器", e.toString());
            }
        }
    };


    public SocketServer()
    {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        StartServer();
        //间隔5s 执行runnable .run
        handler.postDelayed(runnable, 5000); //启动定时器
    }

    //接收线程发送过来信息，并用TextView显示
    @SuppressLint("HandlerLeak")
    public  Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            // 处理客户端发回来的信息
            Log.e(TAG,mStrMSG);

        }
    };


    //每个客户端单独开启一个线程
    class ThreadServer implements Runnable
    {
        private Socket mSocket;
        private DataInputStream dinput;
        public ThreadServer(Socket socket)
        {
            try{
                this.mSocket = socket;
                dinput = new DataInputStream(socket.getInputStream());
                //客户端连接时，发送问候语
                SendToClient("hello,I'm tcp server...");
            }
            catch(Exception e){
                Log.e(TAG, e.toString());
            }

        }
        public void run()
        {
            try
            {
                byte[] bbuf = new byte[10000];
                while (true) {

                    if (!mSocket.isClosed()) {
                        if (mSocket.isConnected()) {
                            if (!mSocket.isInputShutdown()) {
                                int length = dinput.read(bbuf);
                                mStrMSG = new String(bbuf, 0, length, StandardCharsets.UTF_8);
                                mStrMSG += "\n";
                                Log.e(TAG, mStrMSG);
                                mHandler.sendMessage(mHandler.obtainMessage());
                            }
                        }
                    }
                }
            }
            catch (IOException e)
            {
                Log.e(TAG, e.toString());
            }
        }
    }


    @Override
    public void run() {
        Socket client = null;
        try{
            while (true)
            {
                Log.d(TAG, "等待客户端连接...");
                //接收客户连接 并添加到list中
                client = mServerSocket.accept();
                mClientList.add(client);

                if(client!=null){
                    Log.e(TAG, "client "+client.getInetAddress()+":"+client.getPort()+" connected!" );
                    //开启一个客户端线程
                    mExecutorService.execute(new ThreadServer(client));
                }

            }
        }
        catch(Exception e){
            mClientList.remove(client);
            Log.e(TAG, e.toString());
        }
    }

    /**
     * 向客户端发送消息
     * @param msg
     */
    public  void SendToClient(String msg){
        try{
            if(mClientList.size()>0){
                for (Socket client : mClientList)
                {
                    mPrintWriter = new PrintWriter(client.getOutputStream(), true);
                    //TODO 这里会有 异常
                    //
                    // D/StrictMode: StrictMode policy violation; ~duration=4 ms: android.os.strictmode.NetworkViolation
                    mPrintWriter.println(msg);
                    Log.e(TAG, Utils.getTimeInfo()+"Service" +"发送("+ "："+ msg +") to "+client.getInetAddress()+":"+client.getPort());


                }
            }
        }
        catch(Exception e){
            Log.e("向客户端发送消息败了", e.toString());
            StartServer();//消息发送失败，重启服务器
        }
    }

    public void StartServer(){
        try
        {
            if(mServerSocket!=null){
                mServerSocket.close();
            }
            //设置服务器端口
            mServerSocket = new ServerSocket(SERVERPORT);
            //创建一个线程池
            mExecutorService = Executors.newCachedThreadPool();
            //用来临时保存客户端连接的Socket对象
            new Thread(SocketServer.this).start();
        }
        catch (IOException e)
        {
            Log.e(TAG, e.toString());
        }

    }

    public  void CloseServer(){
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
