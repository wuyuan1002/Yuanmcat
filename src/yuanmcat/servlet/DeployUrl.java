package yuanmcat.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解配置请求路径
 *
 * @author wuyuan
 * @date 2019/8/12
 */
//可加在接口、类、枚举、注解上
@Target(ElementType.TYPE)
//保留到运行时 -- 因为后面需要通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
public @interface DeployUrl {
    String[] urlPatterns();
}
