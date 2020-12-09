package tk.iovr.androidsocketconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.easysocket.EasySocket;
import com.easysocket.callback.ProgressDialogCallBack;
import com.easysocket.callback.SimpleCallBack;
import com.easysocket.config.DefaultMessageProtocol;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.heartbeat.HeartManager;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.exception.NoNullException;
import com.easysocket.interfaces.callback.IProgressDialog;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import tk.iovr.androidsocketconnect.easySocket.EasySocketServer;
import tk.iovr.androidsocketconnect.easySocket.SocketClient.CallbackIdKeyFactoryImpl;
import tk.iovr.androidsocketconnect.easySocket.SocketClient.CallbackSender;
import tk.iovr.androidsocketconnect.easySocket.SocketClient.ClientHeartBeat;
import tk.iovr.androidsocketconnect.easySocket.SocketClient.TestMsg;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.CallbackResponse;

import static tk.iovr.androidsocketconnect.Utils.getTimeInfo;

public class EasySocketActivity extends AppCompatActivity {



    // 是否已连接
    private boolean isConnected;
    // 连接或断开连接的按钮
    private Button controlConnect;
    private TextView serResponse;
    private TextView info;

    private final String IPAddress= "192.168.2.21";;
    private final int port= 5112;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_socket);



        controlConnect = findViewById(R.id.ctrl_conn);
        serResponse = findViewById(R.id.serResponse);
        info = findViewById(R.id.info);


        //开启服务
        findViewById(R.id.start_server).setOnClickListener(v -> {
            new EasySocketServer();
            info.setText("Server started!");
        });

        // 创建socket连接
        findViewById(R.id.create_conn).setOnClickListener(v -> {
            // 初始化socket
            initEasySocket(IPAddress,port);
            // 监听socket行为
            EasySocket.getInstance().subscribeSocketAction(socketActionListener);
        });

        // 发送一个object
        findViewById(R.id.send_msg).setOnClickListener(v -> {
            try{
                sendMessage();
            }catch (NoNullException e){
                Toast.makeText(EasySocketActivity.this, "Socket 未创建链接！", Toast.LENGTH_SHORT).show();
            }
        });

        // 发送一个string
        findViewById(R.id.send_string).setOnClickListener(v -> {
            try{
                EasySocket.getInstance().upString("{\"from\":\"String\",\"msgId\":\"test_msg\"}");
            }catch (NoNullException e){
                Toast.makeText(EasySocketActivity.this, "Socket 未创建链接！", Toast.LENGTH_SHORT).show();
            }
        });

        // 发送有回调功能的消息
        findViewById(R.id.callback_msg).setOnClickListener(v -> {
            try{
                sendCallbackMsg();
            }catch (NoNullException e){
                Toast.makeText(EasySocketActivity.this, "Socket 未创建链接！", Toast.LENGTH_SHORT).show();
            }
        });

        // 启动心跳检测
        findViewById(R.id.start_heart).setOnClickListener(v -> {
            try {
                startHeartbeat();
            }catch (NoNullException e){
                Toast.makeText(EasySocketActivity.this, "Socket 未创建链接！", Toast.LENGTH_SHORT).show();
            }
        });

        // 有进度条的消息
        findViewById(R.id.progress_msg).setOnClickListener(new View.OnClickListener() {
            // 进度条接口
            private final IProgressDialog progressDialog = () -> {
                ProgressDialog dialog = new ProgressDialog(EasySocketActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle("正在加载...");
                return dialog;
            };

            @Override
            public void onClick(View v) {

                try{
                    CallbackSender sender = new CallbackSender();
                    sender.setFrom("android");
                    sender.setMsgId("delay_msg");
                    EasySocket.getInstance()
                            .upCallbackMessage(sender)
                            .onCallBack(new ProgressDialogCallBack<String>(progressDialog, true, true, sender.getCallbackId()) {
                                @Override
                                public void onResponse(String s) {
                                    LogUtil.d("进度条回调消息=" + s);
                                }

                                @Override
                                public void onError(Exception e) {
                                    super.onError(e);
                                    e.printStackTrace();
                                }
                            });
                }catch (NoNullException e){
                    Toast.makeText(EasySocketActivity.this, "Socket 未创建链接！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 连接或断开连接
        controlConnect.setOnClickListener(v -> {
            try{
                if (isConnected) {
                    EasySocket.getInstance().disconnect(false);
                } else {
                    EasySocket.getInstance().connect();
                }
            }catch (NoNullException e){
                Toast.makeText(EasySocketActivity.this, "Socket 未创建链接！", Toast.LENGTH_SHORT).show();

            }
//                catch (ConnectException e){
//                    Toast.makeText(MainActivity.this, "Socket 服务端未开启！", Toast.LENGTH_SHORT).show();
//
//                }

        });

        // 销毁socket连接
        findViewById(R.id.destroy_conn).setOnClickListener(v -> {
            try{
                EasySocket.getInstance().destroyConnection();
            }catch (NoNullException e){
                Toast.makeText(EasySocketActivity.this, "Socket 链接已销毁！", Toast.LENGTH_SHORT).show();

            }

        });
    }


    /**
     * 发送一个有回调的消息
     */
    private void sendCallbackMsg() {

        CallbackSender sender = new CallbackSender();
        sender.setMsgId("callback_msg");
        sender.setFrom("我来自android");
        EasySocket.getInstance().upCallbackMessage(sender)
                .onCallBack(new SimpleCallBack<CallbackResponse>(sender.getCallbackId()) {
                    @Override
                    public void onResponse(CallbackResponse response) {
                        LogUtil.d("回调消息=" + response.toString());
                        Toast.makeText(EasySocketActivity.this, "回调消息：" + response.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        e.printStackTrace();
                    }
                });
    }

    // 启动心跳检测功能
    private void startHeartbeat() {
        // 心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        // 心跳包类型可以是object、String、byte[]，实现HeartbeatListener接口，用于判断接收的消息是不是服务端心跳
        EasySocket.getInstance().startHeartBeat(clientHeartBeat, new HeartManager.HeartbeatListener() {
            @Override
            public boolean isServerHeartbeat(OriginReadData originReadData) {
                String msg = originReadData.getBodyString();
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    if ("heart_beat".equals(jsonObject.getString("msgId"))) {
                        LogUtil.d("收到服务器心跳");
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    /**
     * 发送一个的消息，
     */
    private void sendMessage() {
        TestMsg testMsg = new TestMsg();
        testMsg.setMsgId("test_msg");
        testMsg.setFrom("android");
        // 发送
        EasySocket.getInstance().upObject(testMsg);
    }





    /**
     * socket行为监听
     */
    private final ISocketActionListener socketActionListener = new SocketActionListener() {
        /**
         * socket连接成功
         * @param socketAddress
         */
        @Override
        public void onSocketConnSuccess(SocketAddress socketAddress) {
            super.onSocketConnSuccess(socketAddress);
            LogUtil.d("连接成功");
            controlConnect.setText("socket已连接，点击断开连接");
            isConnected = true;
        }

        /**
         * socket连接失败
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, Boolean isNeedReconnect) {
            super.onSocketConnFail(socketAddress, isNeedReconnect);
            controlConnect.setText("socket连接被断开，点击进行连接");
            isConnected = false;
        }

        /**
         * socket断开连接
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, Boolean isNeedReconnect) {
            super.onSocketDisconnect(socketAddress, isNeedReconnect);
            LogUtil.d("socket断开连接，是否需要重连：" + isNeedReconnect);
            controlConnect.setText("socket连接被断开，点击进行连接");
            isConnected = false;
        }

        /**
         * socket接收的数据
         * @param socketAddress
         * @param originReadData
         */
        @SuppressLint("SetTextI18n")
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d(getTimeInfo()+" socket监听器收到数据=" + originReadData.getBodyString());
            serResponse.setText(getTimeInfo()+": from server :"+originReadData.getBodyString());

        }
    };

    /**
     * 初始化EasySocket
     */
    private void initEasySocket(String IPAddress,int port) {
        // socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                .setSocketAddress(new SocketAddress(IPAddress, port)) // 主机地址
                .setCallbackIdKeyFactory(new CallbackIdKeyFactoryImpl())
                // 最好定义一个消息协议，方便解决 socket黏包、分包的问题
                 .setReaderProtocol( new DefaultMessageProtocol()) // 默认的消息协议
                .build();

        // 初始化EasySocket

        EasySocket.getInstance()
                .options(options) // 项目配置
                .createConnection();// 创建一个socket连接

    }

}