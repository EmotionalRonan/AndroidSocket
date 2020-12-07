package tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message;


import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.base.SuperResponse;

/**
 * Author：Alex
 * Date：2019/6/6
 * Note：
 */
public class ServerHeartBeat extends SuperResponse {

    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
