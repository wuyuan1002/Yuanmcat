package yuanmcat.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * Reactor抽象类 -- 是所有 boss和 worker的父类
 *
 * @author wuyuan
 * @date 2020/1/17
 */
abstract class AbstractReactor implements Runnable {
    //选择器对象
    protected Selector selector;
    
    @Override
    public void run() {
        runReactor();
    }
    
    /**
     * 监听事件并分发
     */
    public void runReactor() {
        try {
            while (!Thread.interrupted()) {
                if (this.selector.select() == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    dispatch(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 分发 -- 模板方法模式
     *
     * @param key 客户端监听事件的key
     */
    protected abstract void dispatch(SelectionKey key);
}
