package yuanmcat.server.command;

import yuanmcat.constant.HttpConstant;
import yuanmcat.constant.SeverConstant;
import yuanmcat.http.HandleHttp;
import yuanmcat.http.StatusCode;
import yuanmcat.log.Logger;
import yuanmcat.request.Request;
import yuanmcat.response.Response;
import yuanmcat.response.ResponseHeader;
import yuanmcat.server.Yuanmcat;
import yuanmcat.servlet.Servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

/**
 * 写命令 -- 用来处理对应的客户端连接的写事件
 *
 * 客户端的请求必须是由servlet处理，不能直接访问 html文件。(第 94 行)
 * html文件只有在请求转发时才可以访问，若客户端直接访问的话不知道时哪个应用的html，此时同名html会引发歧义
 *
 * @author wuyuan
 * @date 2019/12/28
 */
public class Sender implements Handler {
    // 连接
    private SocketChannel channel;
    // 客户端请求对象
    private Request request;
    // 该handler的request对象是否变成了另一个，如果没有改变则说明是第一次请求，若改变了则说明发生了请求转发
    private boolean hasForword = false;
    
    
    public Sender(SelectionKey selectionKey, Request request) {
        this.channel = (SocketChannel) selectionKey.channel();
        this.request = request;
    }
    
    @Override
    public void execute() {
        String url = this.request.getUrl();
        // 获取访问路径是否为默认路径
        boolean isIndex = HttpConstant.DEFAULT_URL_1.equals(url) || HttpConstant.DEFAULT_URL_2.equals(url);
        // 获取路径是否存在，若存在则获取servlet对应的应用路径和Class对象
        boolean isUrlExist = false;
        String serverPath = null;
        Class servletClass = null;
        if (!isIndex && !url.endsWith(SeverConstant.POINT_HTML)) {
            // 若是默认路径或访问html文件，则必然不存在servlet，isUrlExist必然为false，此处可不执行从而提高速度
            for (Map.Entry<Map<Set<String>, Class>, String> entry : Yuanmcat.SERVLET_URL.entrySet()) {
                if (this.hasForword) {
                    // 若发生了请求转发，则request中已经存上了应用路径，请求转发只在相同应用中查找对应的servlet
                    if (!entry.getValue().equals(this.request.getServerPath())) {
                        continue;
                    }
                }
                for (Map.Entry<Set<String>, Class> servletEntry : entry.getKey().entrySet()) {
                    for (String u : servletEntry.getKey()) {
                        if (u.equals(url)) {
                            isUrlExist = true;
                            serverPath = entry.getValue();
                            servletClass = servletEntry.getValue();
                            break;
                        }
                    }
                    if (isUrlExist) {
                        break;
                    }
                }
                if (isUrlExist) {
                    break;
                }
            }
        }
        
        // 开始处理请求
        String printMsg;
        if (this.hasForword) {
            printMsg = SeverConstant.HANDLE_FORWARD_REQUEST;
        } else {
            printMsg = SeverConstant.HANDLE_REQUEST;
        }
        Logger.out(LocalTime.now() + printMsg + this.request.getUrl());
        
        if (!this.hasForword && !isUrlExist && !isIndex) {
            // 在没有发生请求转发的情况下，若没有对应的servlet并且不是访问的默认路径则返回404
            Logger.err(LocalTime.now() + SeverConstant.REQUEST_PATH + this.request.getUrl() + SeverConstant.NOT_EXIST);
            try {
                HandleHttp.handleNotFound(this.channel);
                this.channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        } else if (this.request.getInvalidHeaderException() == null) {
            // 若为null，说明请求头正常，处理请求
            try {
                if (isIndex || url.endsWith(SeverConstant.POINT_HTML)) {
                    // 处理默认页面请求或者请求转发访问html页面
                    // 直接访问html只能是请求转发时才会走到这儿(因为不通过转发访问html的话找不到servlet，在95行就直接报404了)，若客户端直接访问的话不知道时哪个应用的html，此时同名html会引发歧义
                    ResponseHeader headers;
                    ByteBuffer bodyBuffer;
                    if (isIndex) {
                        bodyBuffer = HandleHttp.readStaticFile(HttpConstant.INDEX_PAGE);
                    } else {
                        String pathServer = this.request.getServerPath();
                        String pathHtml = (url.startsWith(SeverConstant.SLASH_2) ? url.substring(url.lastIndexOf(SeverConstant.SLASH_2) + 1) : url);
                        bodyBuffer = readApplicationFile(new File(pathServer), pathHtml);
                        if (bodyBuffer == null) {
                            throw new FileNotFoundException();
                        }
                    }
                    headers = new ResponseHeader(StatusCode.OK.getCode());
                    headers.setContentLength(bodyBuffer.capacity());
                    ByteBuffer headerBuffer = ByteBuffer.wrap(headers.toString().getBytes());
                    channel.write(new ByteBuffer[]{headerBuffer, bodyBuffer});
                } else if (servletClass != null) {
                    // 处理找到了servlet的其他请求(包括直接访问和请求转发)
                    Response response;
                    // 请求转发后request中就已经存上了response
                    if ((response = this.request.getResponse()) == null) {
                        response = new Response(this.channel, this.request);
                        this.request.setResponse(response);
                    }
                    this.request.setServerPath(serverPath);
                    initServlet(url, servletClass).service(this.request, response);
                } else if (this.hasForword) {
                    // 若发生了请求转发却没有找到servlet且不是访问的默认页面
                    Logger.err(LocalTime.now() + SeverConstant.REQUEST_PATH + this.request.getUrl() + SeverConstant.NOT_EXIST);
                    HandleHttp.handleNotFound(this.channel);
                } else {
                    // 此处不会执行到
                    throw new Exception();
                }
            } catch (FileNotFoundException e) {
                HandleHttp.handleNotFound(this.channel);
            } catch (Exception e) {
                HandleHttp.handleInternalServerError(this.channel);
            } finally {
                // 若发生了请求转发则不关闭连接，会在request的forword()中进行关闭
                if (!this.hasForword) {
                    try {
                        this.channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        }
        
        // 处理无效请求
        try {
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 根据请求路径实例化对应的servlet
     */
    private Servlet initServlet(String url, Class urlClass) {
        Servlet servlet;
        //如果处理该请求的servlet还没有被创建则创建它，否则直接从servletMap中获取
        if ((servlet = Yuanmcat.SERVLET_MAP.get(url)) == null) {
            try {
                //加锁防止有多个线程同时创建同一个servlet
                synchronized (Sender.class) {
                    //双重if判断 -- 可参考懒汉式单例模式的实现(也是双重if判断)
                    if ((servlet = Yuanmcat.SERVLET_MAP.get(url)) == null) {
                        //直接使用Class对象的newInstance方法实例化对象，只能使用默认的公共无参构造方法
                        servlet = (Servlet) urlClass.newInstance();
                        //调用servlet的init()方法 -- 只在servlet被创建时调用一次
                        servlet.init();
                        //将创建的servlet添加到servletMap中
                        Yuanmcat.SERVLET_MAP.put(url, servlet);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return servlet;
    }
    
    /**
     * 递归读取应用的 html文件
     */
    public ByteBuffer readApplicationFile(File pathServer, String pathHtml) throws IOException {
        if (pathServer.isFile() && pathServer.getAbsolutePath().endsWith(pathHtml)) {
            RandomAccessFile raf = new RandomAccessFile(pathServer.getAbsoluteFile(), "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();
            return buffer;
        } else if (pathServer.isDirectory()) {
            ByteBuffer buffer;
            File[] files = pathServer.listFiles();
            for (File file : files) {
                if ((buffer = readApplicationFile(file, pathHtml)) != null) {
                    return buffer;
                }
            }
        }
        return null;
    }
    
    public Request getRequest() {
        return request;
    }
    
    public void setRequest(Request request) {
        this.request = request;
        this.hasForword = true;
    }
}
