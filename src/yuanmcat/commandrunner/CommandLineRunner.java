package yuanmcat.commandrunner;

/**
 * 容器启动时执行接口工具
 *
 * @author wuyuan
 * @date 2019/12/31
 */
public interface CommandLineRunner {
    
    /**
     * 服务器启动后执行
     *
     * @throws Exception 异常
     */
    void run() throws Exception;
}
