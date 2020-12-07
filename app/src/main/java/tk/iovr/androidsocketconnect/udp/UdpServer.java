package tk.iovr.androidsocketconnect.udp;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import tk.iovr.androidsocketconnect.SocketServer;
import tk.iovr.androidsocketconnect.Utils;

public class UdpServer  implements Runnable{

    public  static String TAG = "UdpServer";
    private DatagramSocket socket;
    private List<String> listQuotes = new ArrayList<String>();
    private Random random;


    public UdpServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
        random = new Random();
    }

    private void service() throws IOException {
        while (true) {
            DatagramPacket request = new DatagramPacket(new byte[1], 1);
            socket.receive(request);

            String message ="hello hello";
            byte[] buffer = message.getBytes();

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);
        }
    }


    /**
     * 向客户端发送消息
     * @param msg
     */
    public  void SendToClient(String msg){
        try{

        }
        catch(Exception e){
            Log.e("向客户端发送消息败了", e.toString());
            StartServer();//消息发送失败，重启服务器
        }
    }

    public void StartServer(){
        try
        {
            UdpServer server = new UdpServer(55550);
//            server.loadQuotesFromFile(message);
            server.service();
        }
        catch (IOException e)
        {
            Log.e(TAG, e.toString());
        }

    }

    @Override
    public void run() {

    }
}
