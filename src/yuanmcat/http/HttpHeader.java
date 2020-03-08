package yuanmcat.http;

/**
 * http请求头和响应头
 *
 * @author wuyuan
 * @date 2019/12/31
 */
public class HttpHeader {
    //请求路径
    protected String url;
    //请求方法
    protected String method;
    //版本号
    protected String version;
    //所有请求信息
    protected String allHttpRequest;
    //状态码
    protected int code;
    //状态描述
    protected String phrase;
    //内容类型
    protected String contentType;
    //内容长度
    protected int contentLength;
    //服务器名称
    protected String server;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getAllHttpRequest() {
        return allHttpRequest;
    }
    
    public void setAllHttpRequest(String allHttpRequest) {
        this.allHttpRequest = allHttpRequest;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getPhrase() {
        return phrase;
    }
    
    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public int getContentLength() {
        return contentLength;
    }
    
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
    
    public String getServer() {
        return server;
    }
    
    public void setServer(String server) {
        this.server = server;
    }
}
