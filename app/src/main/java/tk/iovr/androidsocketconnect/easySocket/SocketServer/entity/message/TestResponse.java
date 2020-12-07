package tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message;


import tk.iovr.androidsocketconnect.easySocket.SocketServer.entity.message.base.SuperResponse;

/**
 * Author：Alex
 * Date：2019/12/6
 * Note：服务器返回的测试消息
 */
public class TestResponse extends SuperResponse {

    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
