package tk.iovr.androidsocketconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.easysocket.EasySocket;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.sql.RowSetListener;

import tk.iovr.androidsocketconnect.easySocket.SocketClient.CallbackIdKeyFactoryImpl;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity" ;


    Button startServer;
    Button startClient;
    Button startUdpServer;
    Button startUdpClient;

    TextView status;
    TextView ipInfo;
    EditText iPAddress;

    SocketServer server;
    SocketClient tcp;


    // ----------------------------

    private String IPAddress="";
    private final int port = 5111;
    // ----------------------------

    Button easySocket;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitView();
        SetListener();

        /********************************/

        String netWorkStatus = Utils.isNetworkAvailable(this) ? "Connected" : "Not Connected";

        Log.e(TAG, "NetWork Status: "+netWorkStatus
                         +"\tMac Address: "+Utils.getMacAddress(this)
                         +"\tIP Address: "+Utils.getIpAddress(this)
//                         +"\tGlobal IP Address:"+Utils.getGlobalIpAddress()

        );

        ipInfo.setText("NetWork Status:"+netWorkStatus
                        +"\nMac Address:"+Utils.getMacAddress(this)
                        +"\nIP Address:"+Utils.getIpAddress(this)
//                        +"\nGlobal IP Address:"+Utils.getGlobalIpAddress()

        );
        //获取公网 IP 地址
        getGlobalIpAddress();

    }

    //绑定View
    private void InitView() {

        startServer = findViewById(R.id.start_server);
        startClient = findViewById(R.id.start_client);
        startUdpServer = findViewById(R.id.start_udp_server);
        startUdpClient = findViewById(R.id.start_udp_client);
        iPAddress = findViewById(R.id.iPAddress);

        iPAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                IPAddress = iPAddress.getText().toString();
            }
        });

        status = findViewById(R.id.info);
        ipInfo = findViewById(R.id.ipInfo);


        easySocket = findViewById(R.id.easySocket);

    }


    //设置监听事件
    private void SetListener() {

        //
        startServer.setOnClickListener(v -> {
            try{
                status.setText("Server start");

                server =new SocketServer();
                server.SendToClient("本消息来自服务器");

            }
            catch(Exception ex){
                Log.e(TAG, "start server Exception: "+ex.getMessage() );
            }
        });
        startClient.setOnClickListener(v -> {
            try{

                if ("".equals(iPAddress.getText().toString())){
                    Toast.makeText(this, "请输入服务端 IP 地址！", Toast.LENGTH_SHORT).show();
                    return;
                }
                status.setText("Client start");

//                tcp =SocketClient.instanceClientSocket();
                tcp = new SocketClient(IPAddress,port);
                new Thread(tcp::ConnectionService).start();

            }
            catch(Exception ex){
                Log.e(TAG, "start client Exception: "+ex.getMessage() );
            }
        });
        //
        startUdpServer.setOnClickListener(v -> {
            //开启 UDP 服务端
            try{
                status.setText("Server UDP start");

            }
            catch(Exception ex){
                Log.e(TAG, "start client Exception: "+ex.getMessage() );
            }
        });
        startUdpClient.setOnClickListener(v -> {
            //开启 UDP 客户端
            try{
                status.setText("Client UDP start");


            }
            catch(Exception ex){
                Log.e(TAG, "start client Exception: "+ex.getMessage() );
            }

        });

        easySocket.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EasySocketActivity.class);
            startActivity(intent);
        });
    }

    /*********************************************/

    private String ip;
    public void getGlobalIpAddress() {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://cip.cc");
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                Scanner scanner = new Scanner(in).useDelimiter("\\A");
                String result =scanner.hasNext() ? scanner.next() : "";

                // Log.e(TAG, "result: "+result );

                if (!result.isEmpty()){
                    //解析
                    Document doc = (Document) Jsoup.parse(result);
                    Elements content = (Elements) doc.getElementsByTag("pre");
//                    Log.e(TAG, "content: "+content );

                    ip = content.text().split("\n")[0].split(":")[1].trim();
//                    Log.e(TAG, "ip: "+ip );

                }
                handler.sendEmptyMessage(0);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                ip = null;
                handler.sendEmptyMessage(0);
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        }).start();

    }

    //更新公网IP到界面
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "Global IP : "+ip );
            String textInfo = (String) ipInfo.getText();
            ipInfo.setText(textInfo +"\nGlobal IP:"+ip);
        }
    };

    /*********************************************/





}