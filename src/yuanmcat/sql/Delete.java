package yuanmcat.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 删
 *
 * @author wuyuan
 * @date 2020/1/18
 */
//可加在方法上
@Target(ElementType.METHOD)
//保留到运行时 -- 因为后面需要通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
public @interface Delete {
    /**
     * sql条件
     */
    String[] SqlCondition() default {};
}
