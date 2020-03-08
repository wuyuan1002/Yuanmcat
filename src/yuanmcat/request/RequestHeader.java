package yuanmcat.request;

import yuanmcat.constant.HttpConstant;
import yuanmcat.exception.InvalidHeaderException;
import yuanmcat.http.HttpHeader;

import java.nio.channels.SelectionKey;

/**
 * 请求头 -- 获取请求数据
 *
 * @author wuyuan
 * @date 2020/1/2
 */
public class RequestHeader extends HttpHeader {
    //客户端监听事件的key
    protected SelectionKey selectionKey;
    //无效请求异常
    private InvalidHeaderException invalidHeaderException;
    
    protected RequestHeader(SelectionKey selectionKey) {
        this.invalidHeaderException = null;
        this.selectionKey = selectionKey;
    }
    
    /**
     * 解析请求头并把信息存入headers中
     */
    protected int parseHeader(String headerStr) throws InvalidHeaderException {
        if (headerStr == null || headerStr.isEmpty()) {
            throw new InvalidHeaderException();
        }
        
        // 解析请求头第一行
        int index = headerStr.indexOf(HttpConstant.CRLF);
        if (index == -1) {
            throw new InvalidHeaderException();
        }
        String firstLine = headerStr.substring(0, index);
        String[] parts = firstLine.split(" ");
        
        //请求头的第一行必须由三部分构成， GET /yuanmcat HTTP/1.1
        if (parts.length < 3) {
            throw new InvalidHeaderException();
        }
        super.method = parts[0];
        super.url = parts[1];
        super.version = parts[2];
        super.allHttpRequest = headerStr;
        return index;
    }
    
    protected void setInvalidHeaderException(InvalidHeaderException invalidHeaderException) {
        this.invalidHeaderException = invalidHeaderException;
    }
    
    public InvalidHeaderException getInvalidHeaderException() {
        return invalidHeaderException;
    }
}
