package yuanmcat.server.command;

import yuanmcat.http.HandleHttp;
import yuanmcat.request.Request;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 读命令 -- 用来处理对应的客户端连接的读事件
 *
 * @author wuyuan
 * @date 2019/12/28
 */
public class Reader implements Handler {
    // 客户端监听事件的key
    private SelectionKey selectionKey;
    
    // 只有在有新的连接时才new一个Handler，执行该构造方法，并把handler绑定到key上。之后该连接有事件时会直接执行它绑定的handler的run方法
    public Reader(Selector selector, SocketChannel accept) {
        try {
            // 客户端连接通道
            accept.configureBlocking(false);
            selector.wakeup();
            // 把新的连接绑定到selector选择器上
            this.selectionKey = accept.register(selector, SelectionKey.OP_READ);
            // 给新的连接添加该Handler对象，用来处理该连接的事件
            this.selectionKey.attach(this);
            selector.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * 使用状态模式操作
     */
    @Override
    public void execute() {
        // 获取连接通道
        SocketChannel channel = (SocketChannel) this.selectionKey.channel();
        try {
            if (!(this.selectionKey.attachment() instanceof Sender) && this.selectionKey.isValid()) {
                Request request = new Request(this.selectionKey);
                if (request.getInvalidHeaderException() == null) {
                    // 是有效请求才注册写事件继续处理
                    this.selectionKey.interestOps(SelectionKey.OP_WRITE);
                    this.selectionKey.attach(new Sender(this.selectionKey, request));
                } else {
                    // 否则返回400,错误的请求
                    HandleHttp.handleBadRequest(channel);
                    channel.close();
                }
            } else {
                channel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
