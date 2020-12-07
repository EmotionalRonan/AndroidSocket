package tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message;


import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.base.SuperResponse;

/**
 * Author：Alex
 * Date：2019/6/11
 * Note：
 */
public class DelayResponse extends SuperResponse {

    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
