package tk.iovr.androidsocketconnect.easySocket.SocketServer;

import android.util.Log;

import com.google.gson.Gson;

import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.IMessageProtocol;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.MessageID;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.CallbackResponse;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.DelayResponse;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.ServerHeartBeat;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.TestResponse;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.base.SuperClient;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.base.SuperResponse;
import tk.iovr.androidsocketconnect.easySocket.SocketServer.iowork.IWriter;

import static tk.iovr.androidsocketconnect.Utils.getTimeInfo;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：处理io信息
 */
public class HandlerIO {

    private static final String TAG ="Handler Message" ;
    private final IWriter easyWriter;
    private final IMessageProtocol messageProtocol;

    public HandlerIO(IWriter easyWriter) {
        this.easyWriter = easyWriter;
        messageProtocol = ServerConfig.getInstance().getMessageProtocol();
    }

    /**
     * 处理接收的信息
     *
     * @param receiver
     */
    public void handReceiveMsg(String receiver) {
        try {
            System.out.println(getTimeInfo()+" receive message:" + receiver);

            SuperClient clientMsg = new Gson().fromJson(receiver, SuperClient.class);
            String id = clientMsg.getMsgId(); //消息ID
            String callbackId = clientMsg.getCallbackId(); //回调ID
            SuperResponse superResponse = null;

            switch (id) {
                case MessageID.CALLBACK_MSG: //回调消息
                    superResponse = new CallbackResponse();
                    (superResponse).setCallbackId(callbackId);
                    superResponse.setMsgId(MessageID.CALLBACK_MSG);
                    ((CallbackResponse) superResponse).setFrom("我来自server");
                    break;

                case MessageID.TEST_MSG: //测试消息
                    superResponse = new TestResponse();
                    superResponse.setMsgId(MessageID.TEST_MSG);
                    ((TestResponse) superResponse).setFrom("server");
                    break;
                case MessageID.HEARTBEAT: //心跳包
                    superResponse = new ServerHeartBeat();
                    ((ServerHeartBeat) superResponse).setFrom("server");
                    superResponse.setMsgId(MessageID.HEARTBEAT);
                    break;

                case MessageID.DELAY_MSG: //延时消息
                    superResponse = new DelayResponse();
                    (superResponse).setCallbackId(callbackId);
                    superResponse.setMsgId(MessageID.DELAY_MSG);
                    ((DelayResponse) superResponse).setFrom("server");
                    try {
                        Thread.sleep(1000 * 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    Log.e(TAG, "handReceiveMsg: Test id : "+ id );

            }

            if (superResponse == null) return;
            System.out.println("send message:" + convertObjectToJson(superResponse));
            byte[] bytes = convertObjectToJson(superResponse).getBytes();
            System.out.println("send message:" + bytes.length);
            // 自定义消息协议
            if (messageProtocol != null) {
                bytes = messageProtocol.pack(bytes);
            }
            easyWriter.offer(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String convertObjectToJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

}
