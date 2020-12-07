package tk.iovr.androidsocketconnect.udp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import tk.iovr.androidsocketconnect.SocketClient;

public class UdpClient implements Runnable{
    private  String m_hostname;
    private int m_port;



    public UdpClient(String hostname,int port){
        this.m_hostname = hostname;
        this.m_port = port;


    }
    public  void  ConnectionService(){

        try{

            InetAddress address = InetAddress.getByName(this.m_hostname);
            DatagramSocket socket = new DatagramSocket();

            while (true) {

                DatagramPacket request = new DatagramPacket(new byte[1], 1, address, this.m_port);
                socket.send(request);

                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);

                String quote = new String(buffer, 0, response.getLength());

                System.out.println(quote);
                System.out.println();

                Thread.sleep(10000);
            }

        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

    }
}
