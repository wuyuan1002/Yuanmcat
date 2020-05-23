package yuanmcat.server;

import yuanmcat.server.command.Handler;
import yuanmcat.server.command.Reader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * boss负责监听客户端的连接事件，当有连接时将连接分发，注册到worker的selector上，由worker监听连接的读写事件
 *
 * @author wuyuan
 * @date 2019/8/18
 */
public class BossReactor extends AbstractReactor {
    // worker
    private final WorkerReactor workerReactor;
    // boss
    private final Thread bossThread;
    
    BossReactor(int port) throws IOException, InterruptedException {
        //创建默认线程工厂
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        
        //首先创建一个服务器端连接通道，绑定在一个端口号上，然后注册在selector上监听连接事件，
        //若有连接过来，则获取真正的连接channel，并把该通道注册到selector上
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //将通道设置成非阻塞的(只有非阻塞的通道才可以注册到选择器Selector上)
        serverSocketChannel.configureBlocking(false);
        //获取通道对应的socket
        ServerSocket serverSocket = serverSocketChannel.socket();
        //serverSocketChannel对象绑定监听端口号，用来让客户端连接
        serverSocket.bind(new InetSocketAddress(port));
        //创建selector选择器对象
        super.selector = Selector.open();
        //boss只关注连接事件
        SelectionKey key = serverSocketChannel.register(super.selector, SelectionKey.OP_ACCEPT);
        key.attach(new Acceptor());
        
        //创建并启动worker线程
        this.workerReactor = new WorkerReactor(threadFactory);
        //创建并启动boss线程
        this.workerReactor.getWorkerThread().join(300);
        this.bossThread = threadFactory.newThread(this);
        this.bossThread.start();
    }
    
    /**
     * 终止 BossReactor
     */
    protected static void stopBossReactor() {
        //中断worker线程
        WorkerReactor.stopWorkerReactor();
        //中断boss线程
        Thread.currentThread().interrupt();
    }
    
    /**
     * 获取boss线程
     */
    protected Thread getBossThread() {
        return this.bossThread;
    }
    
    /**
     * 分发 -- 命令模式
     *
     * 都是调用了命令类 handler的execute方法，但是不同的命令对其的实现不同
     * 由于读命令类和写命令类对其的实现不同，最终execute命令的行为也会不同
     */
    @Override
    protected void dispatch(SelectionKey key) {
        //若事件已失效，则直接返回; 由于bossgroup只关注连接事件，所以此处isAcceptable()必为真
        if (key.isValid() && key.isAcceptable()) {
            Acceptor acceptor = (Acceptor) key.attachment();
            acceptor.key = key;
            acceptor.execute();
        }
    }
    
    /**
     * 接收器 - 命令
     */
    class Acceptor implements Handler {
        private SelectionKey key;
        
        @Override
        public void execute() {
            if (this.key != null) {
                try {
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel channel = serverSocketChannel.accept();
                    new Reader(BossReactor.this.workerReactor.getSelector(), channel);
                    this.key = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}