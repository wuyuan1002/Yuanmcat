package yuanmcat.server;

import yuanmcat.classloader.ServeltClassLoader;
import yuanmcat.commandrunner.Runners;
import yuanmcat.constant.SeverConstant;
import yuanmcat.sql.SqlContainer;
import yuanmcat.servlet.DeployUrl;
import yuanmcat.servlet.Invalid;
import yuanmcat.servlet.Servlet;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 配置哪个url访问哪个servlet
 *
 * @author wuyuan
 * @date 2019/3/27
 */
public class ServletMappingConfig {
    // 存放所有servlet的请求路径和Class对象的映射
    protected static Map<ServletMapping, String> servletMappingConfig = new HashMap<>();
    // 存放每一个web应用的类加载器
    public static Map<String, ClassLoader> classLoaderMap = new HashMap<>();
    // 存储yuanmcat所在文件夹的上级文件夹
    private static ThreadLocal<String> THREADLOCAL = new ThreadLocal<>();
    
    static {
        // 获取runnerClass所在文件夹的路径
        String path = new File(Yuanmcat.runnerClass.getResource(Yuanmcat.runnerClass.getSimpleName() + SeverConstant.POINT_CLASS).getPath()).getParent();
        try {
            // 使用 utf-8 对路径进行编码
            path = URLDecoder.decode(path, SeverConstant.UTF_8);
            // 将目录名添加到threadlocal中
            THREADLOCAL.set(path);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 将应用类加载器存到classLoaderMap中,方便后面使用
        classLoaderMap.put(path, ClassLoader.getSystemClassLoader());
        // 调用此方法后，所有tomcat上层文件夹中的类都被加载了，但没有被初始化，在有请求时第一次实例化servlet时初始化
        findAndDefineServlet(new File(path));
        // 将线程上下文类加载器设置成应用类加载器
        ServletMappingConfig.getCurrentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        // 删除threadlocal中的数据
        THREADLOCAL.remove();
    }
    
    /**
     * 获取当前线程
     */
    public static Thread getCurrentThread() {
        return Thread.currentThread();
    }
    
    /**
     * 递归加载所有servlet
     */
    private static void findAndDefineServlet(File src) {
        // 如果是文件，则说明可能是个servlet
        if (src.isFile()) {
            // 如果文件不是以.class结尾,则说明该文件不是类,直接忽略跳过
            if (!src.toString().endsWith(SeverConstant.POINT_CLASS)) {
                return;
            }
            // 获取全类名
            String allClassName = new File(THREADLOCAL.get()).getName() + SeverConstant.POINT + src.toString()
                    .replace(THREADLOCAL.get() + SeverConstant.SLASH_1, SeverConstant.EMPTY)
                    .replace(SeverConstant.SLASH_1, SeverConstant.POINT)
                    .replace(SeverConstant.POINT_CLASS, SeverConstant.EMPTY);
            try {
                // 使用当前的线程上下文类加载器加载servlet -- 这样的话所有的类都会在yuanmcat启动时被加载，但没有被初始化
                ClassLoader classLoader = ServletMappingConfig.getCurrentThread().getContextClassLoader();
                Class aClass = classLoader.loadClass(allClassName);
                // 如果类实现了Servlet接口并且类上面有 DeployUrl 注解的话就说明这是一个servlet，否则忽略
                boolean isValidServlet = Servlet.class.isAssignableFrom(aClass)
                        && aClass.isAnnotationPresent(DeployUrl.class)
                        && !aClass.isAnnotationPresent(Invalid.class);
                // 只有应用内的servlet才有效，应用外的类即使实现servlet接口也不当成servlet看待
                // 也就是应用公共部分的类只能是工具类等，不能是servlet
                if (classLoader instanceof ServeltClassLoader && isValidServlet) {
                    // 获取servlet上面的 deployUrl 注解
                    DeployUrl ann = (DeployUrl) aClass.getAnnotation(DeployUrl.class);
                    // 获取该servlet的访问路径
                    Set<String> urls = Arrays.stream(ann.urlPatterns()).map(u -> u.startsWith(SeverConstant.SLASH_2) ? u : SeverConstant.SLASH_2 + u).collect(Collectors.toSet());
                    // 把应用路径和(servlet的请求路径和Class对象映射)存到map中
                    ServletMappingConfig.servletMappingConfig.put(new ServletMapping(urls, aClass), ((ServeltClassLoader) classLoader).getServerPath());
                }
                // 获取当前应用的初始化器CommandLineRunner
                Runners.addCommandLineRunner(classLoader, aClass);
                // 获取当前应用的sql
                SqlContainer.addSqlElement(classLoader, aClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String path = URLDecoder.decode(src.toString(), SeverConstant.UTF_8);
                // 如果该文件夹是runnerClass的同级文件夹,就认为它是一个独立的web应用,就为它新创建一个类加载器
                if (URLDecoder.decode(src.getParent(), SeverConstant.UTF_8).equals(THREADLOCAL.get())) {
                    ServeltClassLoader classLoader;
                    // 如果原来没有,则先创建一个
                    if ((classLoader = (ServeltClassLoader) classLoaderMap.get(path)) == null) {
                        String f = new File(THREADLOCAL.get()).getParent();
                        classLoader = new ServeltClassLoader(ClassLoader.getSystemClassLoader(),
                                path.replace(f + SeverConstant.SLASH_1, SeverConstant.EMPTY).replace(SeverConstant.SLASH_1, SeverConstant.POINT), path);
                        classLoader.setPath(f + SeverConstant.SLASH_1);
                        classLoaderMap.put(path, classLoader);
                    }
                    ServletMappingConfig.getCurrentThread().setContextClassLoader(classLoader);
                } else if (path.equals(THREADLOCAL.get())) {
                    // 如果是runnerClass所在的文件夹,则使用系统类加载器,这里的东西每一个web应用都可以使用
                    ServletMappingConfig.getCurrentThread().setContextClassLoader(classLoaderMap.get(THREADLOCAL.get()));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            
            // 递归
            File[] files;
            if ((files = src.listFiles()) != null) {
                for (File file : files) {
                    findAndDefineServlet(file);
                }
            }
        }
    }
}
