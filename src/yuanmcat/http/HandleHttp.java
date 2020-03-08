package yuanmcat.http;

import yuanmcat.GetStatic;
import yuanmcat.constant.HttpConstant;
import yuanmcat.response.ResponseHeader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 处理错误状态和默认请求
 *
 * @author wuyuan
 * @date 2019/12/30
 */
public class HandleHttp {
    /**
     * 400
     */
    public static void handleBadRequest(SocketChannel channel) {
        try {
            handleError(channel, StatusCode.BAD_REQUEST.getCode());
        } catch (Exception e) {
            handleInternalServerError(channel);
        }
    }
    
    /**
     * 403
     */
    public static void handleForbidden(SocketChannel channel) {
        try {
            handleError(channel, StatusCode.FORBIDDEN.getCode());
        } catch (Exception e) {
            handleInternalServerError(channel);
        }
    }
    
    /**
     * 404
     */
    public static void handleNotFound(SocketChannel channel) {
        try {
            handleError(channel, StatusCode.NOT_FOUND.getCode());
        } catch (Exception e) {
            handleInternalServerError(channel);
        }
    }
    
    /**
     * 500
     */
    public static void handleInternalServerError(SocketChannel channel) {
        try {
            handleError(channel, StatusCode.INTERNAL_SERVER_ERROR.getCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 处理正确请求
     */
    public static void handleOk(SocketChannel channel, String url, String data) throws IOException {
        //体
        ByteBuffer bodyBuffer = ByteBuffer.wrap(data.getBytes());
        //头
        ResponseHeader headers = new ResponseHeader(StatusCode.OK.getCode());
        headers.setContentLength(bodyBuffer.capacity());
        headers.setContentType(ContentTypeUtils.getContentType(getExtension(url)));
        ByteBuffer headerBuffer = ByteBuffer.wrap(headers.toString().getBytes());
        
        channel.write(new ByteBuffer[]{headerBuffer, bodyBuffer});
    }
    
    /**
     * 处理错误请求
     */
    public static void handleError(SocketChannel channel, int statusCode) throws IOException {
        //体
        ByteBuffer bodyBuffer = readStaticFile(String.format(HttpConstant.D_HTML, statusCode));
        //头
        ResponseHeader headers = new ResponseHeader(statusCode);
        headers.setContentLength(bodyBuffer.capacity());
        headers.setContentType(ContentTypeUtils.getContentType(HttpConstant.HTML));
        ByteBuffer headerBuffer = ByteBuffer.wrap(headers.toString().getBytes());
        
        channel.write(new ByteBuffer[]{headerBuffer, bodyBuffer});
    }
    
    /**
     * 读取静态文件
     */
    public static ByteBuffer readStaticFile(String path) throws IOException {
        InputStream is = GetStatic.class.getResourceAsStream(HttpConstant.STATIC + path);
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteBuffer buffer = ByteBuffer.allocate(HttpConstant.DEFAULT_BUFFER_SIZE);
        byte[] by = new byte[HttpConstant.DEFAULT_BUFFER_SIZE];
        int len = -1;
        while ((len = bis.read(by)) != -1) {
            buffer.put(by, 0, len);
        }
        bis.close();
        buffer.flip();
        return buffer;
    }
    
    /**
     * 获取文件拓展名
     */
    private static String getExtension(String path) {
        if (path.endsWith(HttpConstant.SLASH) || !path.contains(HttpConstant.POINT)) {
            return HttpConstant.HTML;
        }
        String finename = path.substring(path.lastIndexOf(HttpConstant.SLASH) + 1);
        int index = finename.lastIndexOf(HttpConstant.POINT);
        return index == -1 ? HttpConstant.ASTERISK : finename.substring(index + 1);
    }
}
