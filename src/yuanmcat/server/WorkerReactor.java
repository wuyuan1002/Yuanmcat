package yuanmcat.server;

import yuanmcat.server.command.Handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * worker负责监听连接的读写事件并分发处理
 *
 * @author wuyuan
 * @date 2019/8/18
 */
public class WorkerReactor extends AbstractReactor {
    //worker线程
    private final Thread workerThread;
    //线程池用来异步执行各个连接的读写事件
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(7, 27, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<>(23));
    
    WorkerReactor(ThreadFactory threadFactory) throws IOException {
        super.selector = Selector.open();
        
        //创建并启动worker线程
        this.workerThread = threadFactory.newThread(this);
        this.workerThread.start();
    }
    
    /**
     * 分发 -- 命令模式
     *
     * 都是调用了命令类 handler的execute方法，但是不同的命令对其的实现不同
     * 由于读命令类和写命令类对其的实现不同，最终execute命令的行为也会不同
     */
    @Override
    protected void dispatch(SelectionKey key) {
        //获取事件上面绑定的对象(可能是Reader可能是Sender)，并执行该对象的方法来处理该事件
        Handler handler = (Handler) (key.attachment());
        if (handler != null) {
            //在另一个线程中处理，实现异步操作
            // this.threadPool.execute(handler);
            handler.execute();
        }
    }
    
    /**
     * 获取选择器
     */
    public Selector getSelector() {
        return super.selector;
    }
    
    /**
     * 获取worker线程
     */
    protected Thread getWorkerThread() {
        return this.workerThread;
    }
    
    /**
     * 终止WorkerReactor
     */
    protected static void stopWorkerReactor() {
        Thread.currentThread().interrupt();
    }
}
