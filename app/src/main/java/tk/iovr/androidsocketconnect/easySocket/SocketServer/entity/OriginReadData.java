package tk.iovr.androidsocketconnect.easySocket.SocketServer.entity;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：读的原始数据
 */
public class OriginReadData implements Serializable {

    /**
     * 包头数据
     */
    private byte[] headerData;
    /**
     * 包体数据
     */
    private byte[] bodyData;

    public byte[] getHeaderData() {
        return headerData;
    }

    public void setHeaderData(byte[] headerData) {
        this.headerData = headerData;
    }

    public byte[] getBodyData() {
        return bodyData;
    }

    public void setBodyData(byte[] bodyData) {
        this.bodyData = bodyData;
    }

    /**
     * 获取原始数据body的string形式
     * @return
     */
    public String getBodyString(){
        return new String(getBodyData(), StandardCharsets.UTF_8);
    }
}
