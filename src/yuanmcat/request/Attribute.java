package yuanmcat.request;

/**
 * 用于往请求对象中添加数据，方便用户在请求转发后使用
 *
 * @author wuyuan
 * @date 2020/1/2
 */
public interface Attribute {
    /**
     * 获取数据
     *
     * @param key
     * @return
     */
    Object getAttribute(Object key);
    
    /**
     * 添加数据
     *
     * @param key
     * @param value
     */
    void setAttribute(Object key, Object value);
}
