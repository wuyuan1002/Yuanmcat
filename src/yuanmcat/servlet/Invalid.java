package yuanmcat.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使 servlet, CommandLineRunner, SqlElement失效 -- 类都会被各自的类加载器正常加载，只是不能使用
 *
 * @author wuyuan
 * @date 2019/12/31
 */
//可加在接口、类、枚举、注解上
@Target(ElementType.TYPE)
//保留到运行时 -- 因为后面需要通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
public @interface Invalid {
}
