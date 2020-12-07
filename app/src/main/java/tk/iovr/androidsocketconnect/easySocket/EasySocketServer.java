package tk.iovr.androidsocketconnect.easySocket;

import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tk.iovr.androidsocketconnect.SocketServer;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.iowork.IOManager;

public class EasySocketServer implements Runnable{
    private static final int PORT = 5112;
    private static final String TAG = "EasySocketServer";
    private List<Socket> mList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService mExecutorService = null;

//    public static void main(String[] args) {
//        new EasySocketServer();
//        System.out.println("java running");
//    }

    public EasySocketServer() {
        try {
            server = new ServerSocket(PORT);
            //开启线程池
            mExecutorService = Executors.newCachedThreadPool();
            initConfig(); // 初始化配置信息
            System.out.println("server is running");

            //用来临时保存客户端连接的Socket对象
            new Thread(EasySocketServer.this).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initConfig() {
        // 默认的消息协议
        //ServerConfig.getInstance().setMessageProtocol(new DefaultMessageProtocol());
    }

    @Override
    public void run() {

        Socket client = null;

        try{

            while (true) {
                client = server.accept();
                mList.add(client);
                mExecutorService.execute(new EasySocketServer.Service(client));
            }
        }catch (Exception e){
            mList.remove(client);
            Log.e(TAG, e.toString());
        }

    }

    static class Service implements Runnable {
        private final Socket socket;
        IOManager ioManager;

        public Service(Socket socket) {
            this.socket = socket;
            System.out.println("connect server sucessful: " + socket.getInetAddress().getHostAddress());
        }

        @Override
        public void run() {
            ioManager = new IOManager(socket);
            ioManager.startIO();
        }
    }
}
