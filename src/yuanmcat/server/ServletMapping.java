package yuanmcat.server;

import java.util.Objects;
import java.util.Set;

/**
 * 存储要访问的servlet的信息的实体类
 *
 * @author wuyuan
 * @date 2019/3/27
 */
class ServletMapping {
    private Set<String> urls;
    private Class<?> clazz;
    
    ServletMapping(Set<String> urls, Class<?> clazz) {
        this.urls = urls;
        this.clazz = clazz;
    }
    
    Set<String> getUrls() {
        return urls;
    }
    
    Class<?> getClazz() {
        return clazz;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServletMapping that = (ServletMapping) o;
        return Objects.equals(urls, that.urls) &&
                Objects.equals(clazz, that.clazz);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(urls, clazz);
    }
}
