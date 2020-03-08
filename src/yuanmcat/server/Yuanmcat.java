package yuanmcat.server;

import yuanmcat.classloader.ServeltClassLoader;
import yuanmcat.commandrunner.CommandLineRunner;
import yuanmcat.commandrunner.Runners;
import yuanmcat.constant.SeverConstant;
import yuanmcat.log.Logger;
import yuanmcat.servlet.Servlet;
import yuanmcat.sql.SqlUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Yuanmcat
 *
 * @author wuyuan
 * @date 2019/3/27
 */
public class Yuanmcat {
    // 用来获取用户应用上级文件夹的class类
    protected static Class runnerClass;
    // 端口号
    private int port;
    // 存放请求地址集合和servlet的Class对象的映射和应用路径
    public static final Map<Map<Set<String>, Class>, String> SERVLET_URL = new HashMap<>();
    // 存放已创建的servlet，确保每个servlet只创建一个
    public static final Map<String, Servlet> SERVLET_MAP = new HashMap<>();
    
    private Yuanmcat(Class runnerClass, int port) {
        Yuanmcat.runnerClass = runnerClass;
        this.port = port;
    }
    
    /**
     * 获取sql -- 若没有则返回 null
     */
    public static String getSql(Class clazz, String sqlName) {
        return Yuanmcat.getSql(clazz, sqlName, (String) null);
    }
    
    /**
     * 获取sql -- 若没有则返回 null
     */
    public static String getSql(Class clazz, String sqlName, String... values) {
        return SqlUtil.getSql(clazz, sqlName, values);
    }
    
    /**
     * 将请求路径和servlet对应关系存入map
     */
    private void initServletMapping() {
        ServletMappingConfig.servletMappingConfig.forEach((servletMapping, serverPath) -> {
            Map<Set<String>, Class> servletUrl = new HashMap<>();
            servletUrl.put(servletMapping.getUrls(), servletMapping.getClazz());
            Yuanmcat.SERVLET_URL.put(servletUrl, serverPath);
        });
    }
    
    /**
     * 执行 CommandLineRunner
     */
    private void runCommandLineRunner() {
        Logger.out(LocalTime.now() + SeverConstant.COMMAND_LINE_RUNNER);
        ServletMappingConfig.classLoaderMap.forEach((name, classLoader) -> {
            if (classLoader instanceof ServeltClassLoader) {
                Runners runners = ((ServeltClassLoader) classLoader).getRunners();
                //应用的初始化器有效时才执行
                if (runners != null && runners.isValide()) {
                    //获取到该应用的所有初始化器
                    Set<Class<CommandLineRunner>> commandLineRunners = runners.getCommandLineRunners();
                    //按照顺序挨个执行 -- TreeSet已经按执行顺序优先级排序
                    commandLineRunners.forEach(runner -> {
                        try {
                            //获取构造函数
                            Constructor constructor = runner.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            //new一个对象
                            CommandLineRunner commandLineRunner = (CommandLineRunner) constructor.newInstance();
                            //获取run方法
                            Method run = runner.getMethod(SeverConstant.RUN);
                            //执行commandLineRunner的run方法
                            run.invoke(commandLineRunner);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }
    
    /**
     * 创建 BossReactor
     */
    private void createReactor() throws IOException, InterruptedException {
        new BossReactor(this.port);
    }
    
    public void start() {
        // 初始化servlet映射关系
        initServletMapping();
        // 执行CommandLineRunner
        runCommandLineRunner();
        
        boolean success = true;
        try {
            createReactor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            BossReactor.stopBossReactor();
            Logger.err(SeverConstant.FAILED_STARET);
            success = false;
        }
        if (success) {
            Logger.err(SeverConstant.STARTED);
        }
    }
    
    public static void run(Class runnerClass, int port) {
        new Yuanmcat(runnerClass, port).start();
    }
    
    public static void run(Class runnerClass) {
        new Yuanmcat(runnerClass, SeverConstant.DEFAULT_PORT).start();
    }
}
