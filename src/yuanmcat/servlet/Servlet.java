package yuanmcat.servlet;

import yuanmcat.request.Request;
import yuanmcat.response.Response;

/**
 * @author wuyuan
 * @date 2019/3/27
 */
public interface Servlet {
    
    /**
     * servlet初始化时执行
     */
    void init();
    
    /**
     * get请求时执行
     *
     * @param request
     * @param response
     */
    void doGet(Request request, Response response);
    
    /**
     * post请求时执行
     *
     * @param request
     * @param response
     */
    void doPost(Request request, Response response);
    
    /**
     * 根据请求类型执行对应方法
     *
     * @param request
     * @param response
     */
    default void service(Request request, Response response) {
        final String get = "GET";
        final String post = "POST";
        
        if (get.equalsIgnoreCase(request.getMethod())) {
            this.doGet(request, response);
        } else if (post.equalsIgnoreCase(request.getMethod())) {
            this.doPost(request, response);
        }
    }
    
}
