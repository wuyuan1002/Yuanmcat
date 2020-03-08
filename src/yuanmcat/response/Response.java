package yuanmcat.response;

import yuanmcat.http.HandleHttp;
import yuanmcat.request.Request;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 返回对象
 *
 * @author wuyuan
 * @date 2019/3/27
 */
public class Response {
    private SocketChannel channel;
    private Request request;
    
    public Response(SocketChannel channel, Request request) {
        this.channel = channel;
        this.request = request;
    }
    
    /**
     * 向客户端返回数据
     */
    public void write(String data) {
        try {
            if (this.channel.isConnected()) {
                HandleHttp.handleOk(this.channel, this.request.getUrl(), data);
            }
        } catch (IOException e) {
            HandleHttp.handleInternalServerError(this.channel);
            e.printStackTrace();
        }
    }
}
