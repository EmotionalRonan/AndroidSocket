package tk.iovr.androidsocketconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity" ;


    Button startServer;
    Button startClient;
    TextView status;
    TextView ipInfo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startServer = findViewById(R.id.start_server);
        startClient = findViewById(R.id.start_client);
        status = findViewById(R.id.info);
        ipInfo = findViewById(R.id.ipInfo);

        startServer.setOnClickListener(v -> {
            try{

                status.setText("Server start");
                SocketServer server =new SocketServer();
                server.SendToClient("本消息来自服务器");

            }
            catch(Exception ex){
                Log.e(TAG, "start server: "+ex.getMessage() );
            }
        });

        startClient.setOnClickListener(v -> {
            try{
                status.setText("Client start");

                final SocketClient tcp =SocketClient.instanceClientSocket();

                new Thread(tcp::ConnectionService).start();
            }
            catch(Exception ex){
                Log.e(TAG, "start client: "+ex.getMessage() );
            }
        });


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


}