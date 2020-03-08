package yuanmcat.log;

/**
 * 日志
 *
 * @author wuyuan
 * @date 2020/1/17
 */
public class Logger {
    public static void out(String log) {
        System.out.println(log);
    }
    
    public static void err(String log) {
        System.err.println(log);
    }
}
