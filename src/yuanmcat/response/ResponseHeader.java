package yuanmcat.response;


import yuanmcat.constant.HttpConstant;
import yuanmcat.http.HttpHeader;
import yuanmcat.http.StatusCode;

/**
 * 返回头信息
 *
 * @author wuyuan
 * @date 2019/12/30
 */
public class ResponseHeader extends HttpHeader {
    
    public ResponseHeader(int code) {
        this.code = code;
        this.server = HttpConstant.SERVER_NAME;
        this.phrase = StatusCode.queryPhrase(code);
        this.version = HttpConstant.HTTP_VERSION;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %d %s\r\n", getVersion(), code, phrase));
        sb.append(String.format("ContentType: %s\r\n", contentType));
        sb.append(String.format("ContentLength: %d\r\n", contentLength));
        sb.append(String.format("Server: %s\r\n", server));
        sb.append("\r\n");
        return sb.toString();
    }
}
