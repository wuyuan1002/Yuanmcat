package yuanmcat.commandrunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CommandLineRunner执行顺序，数字越小越优先执行，最小为 1
 *
 * 未实现该接口的 CommandLineRunner优先级为 0，最先执行
 *
 * @author wuyuan
 * @date 2019/12/31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandOrder {
    int Order();
}
