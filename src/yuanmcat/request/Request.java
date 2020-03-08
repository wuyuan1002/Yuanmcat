package yuanmcat.request;

import yuanmcat.constant.HttpConstant;
import yuanmcat.exception.InvalidHeaderException;
import yuanmcat.response.Response;
import yuanmcat.server.command.Sender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 *
 * @author wuyuan
 * @date 2019/3/27
 */
public class Request extends RequestHeader implements Attribute {
    //请求对应的返回对象 -- 若未为null则是第一次请求，若不为null则是请求转发，存储了共享的response
    private Response response = null;
    //该请求所属应用的磁盘路径
    private String serverPath;
    //存储请求体信息
    private Map<String, String> headerMap = new HashMap<>();
    //存储请求中用户添加的数据
    private Map<Object, Object> attributes = new HashMap<>();
    
    public Request(SelectionKey selectionKey) throws IOException {
        super(selectionKey);
        String headerStr = HttpConstant.EMPTY;
        int index = 0;
        try {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            ByteBuffer buffer = ByteBuffer.allocate(HttpConstant.DEFAULT_BUFFER_SIZE);
            channel.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            headerStr = new String(bytes);
            //解析请求头
            index = super.parseHeader(headerStr);
        } catch (InvalidHeaderException e) {
            super.setInvalidHeaderException(e);
        } catch (IOException e) {
            selectionKey.channel().close();
        }
        // 解析请求体 -- 若有请求体
        if (index + 1 <= headerStr.length()) {
            String[] parts = headerStr.substring(index + 1).split(HttpConstant.CRLF);
            String key, value;
            for (String part : parts) {
                index = part.indexOf(HttpConstant.SEMICOLON);
                if (index == -1) {
                    continue;
                }
                key = part.substring(0, index);
                if (index + 1 >= part.length()) {
                    this.headerMap.put(key, HttpConstant.EMPTY);
                    continue;
                }
                value = part.substring(index + 1);
                this.headerMap.put(key, value);
            }
        }
    }
    
    /**
     * 请求转发 -- 请求的延申，共享 request和 response
     */
    public void forword(String url) {
        //request对象不变，只是改变一下request的url
        super.setUrl(url);
        
        Runnable handler = (Runnable) (super.selectionKey.attachment());
        if (handler instanceof Sender && super.selectionKey.isValid()) {
            // 将request重新绑定到handler上 -- 对象没变只是里面的url变了
            ((Sender) handler).setRequest(this);
            handler.run();
        }
        try {
            super.selectionKey.channel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Object getAttribute(Object key) {
        return this.attributes.get(key);
    }
    
    @Override
    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }
    
    public Response getResponse() {
        return this.response;
    }
    
    public void setResponse(Response response) {
        this.response = response;
    }
    
    public String getServerPath() {
        return this.serverPath;
    }
    
    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }
}
