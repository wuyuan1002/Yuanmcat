package yuanmcat.server.command;

/**
 * 处理器 - 命令类
 *
 * @author wuyuan
 * @date 2020/2/12
 */
public interface Handler extends Runnable{
    
    /**
     * 执行命令
     */
    void execute();
    
    /**
     * 执行命令
     */
    @Override
    default void run(){
        execute();
    }
}
