package yuanmcat.constant;

/**
 * 异常常量
 *
 * @author wuyuan
 * @date 2020/1/18
 */
public class ExceptionConstant {
    public static final String NUMBER_ERROR = "@CommandOrder注解的优先级必须大于0";
    public static final String ORDER_SAME = "CommandLineRunner的优先级相同或有多个CommandLineRunner未使用@CommandOrder注解导致无法明确执行顺序";
}
