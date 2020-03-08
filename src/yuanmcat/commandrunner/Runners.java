package yuanmcat.commandrunner;

import yuanmcat.classloader.ServeltClassLoader;
import yuanmcat.constant.ExceptionConstant;
import yuanmcat.exception.RunnerException;
import yuanmcat.servlet.Invalid;

import java.util.TreeSet;

/**
 * 存储 CommandLineRunner和一些相关验证
 *
 * 每个应用都有自己的 Runners对象
 *
 * @author wuyuan
 * @date 2019/12/31
 */
public class Runners {
    //该应用的初始化器是否有效
    private boolean isValide = true;
    //该应用是否已经有未被注解的CommandLineRunner
    private boolean isContain = false;
    //存放当前应用的所有CommandLineRunner
    private TreeSet<Class<CommandLineRunner>> commandLineRunners = new TreeSet<>((c1, c2) -> {
        if (c1.equals(c2)) {
            return 0;
        }
        int i1 = 0, i2 = 0;
        if (c1.isAnnotationPresent(CommandOrder.class)) {
            CommandOrder commandOrder1 = c1.getAnnotation(CommandOrder.class);
            i1 = commandOrder1.Order();
        } else {
            this.isContain = true;
        }
        if (c2.isAnnotationPresent(CommandOrder.class)) {
            CommandOrder commandOrder2 = c2.getAnnotation(CommandOrder.class);
            i2 = commandOrder2.Order();
        } else {
            this.isContain = true;
        }
        try {
            if (i1 < 0 || i2 < 0) {
                // @CommandOrder注解的优先级必须大于0
                throw new RunnerException(ExceptionConstant.NUMBER_ERROR);
            } else if (i1 == i2 || (isContain && i1 == 0)) {
                // 若优先级相同或者是有多于一个未注解的CommandLineRunner接口 -- 会无法明确执行顺序
                throw new RunnerException(ExceptionConstant.ORDER_SAME);
            }
        } catch (RunnerException e) {
            this.isValide = false;
            System.err.println(e.getMsg());
        }
        return i1 - i2;
    });
    
    public boolean isValide() {
        return this.isValide;
    }
    
    public TreeSet<Class<CommandLineRunner>> getCommandLineRunners() {
        return this.commandLineRunners;
    }
    
    /**
     * 获取应用的初始化器 CommandLineRunner
     */
    public static void addCommandLineRunner(ClassLoader classLoader, Class aClass) {
        if (classLoader instanceof ServeltClassLoader && CommandLineRunner.class.isAssignableFrom(aClass) && !aClass.isAnnotationPresent(Invalid.class)) {
            Runners runners;
            if ((runners = ((ServeltClassLoader) classLoader).getRunners()) == null) {
                runners = new Runners();
                ((ServeltClassLoader) classLoader).setRunners(runners);
            }
            runners.getCommandLineRunners().add(aClass);
        }
    }
}
