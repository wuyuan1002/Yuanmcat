package yuanmcat.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表注解
 * 注解后代表该类代表数据库的一张表, 类名是表名
 *
 * @author wuyuan
 * @date 2020/1/3
 */
//可加在接口、类、枚举、注解上
@Target(ElementType.TYPE)
//保留到运行时 -- 因为后面需要通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
}
