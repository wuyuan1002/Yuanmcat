package yuanmcat.classloader;

import yuanmcat.commandrunner.Runners;
import yuanmcat.constant.SeverConstant;
import yuanmcat.sql.SqlContainer;
import yuanmcat.log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 类加载器，只加载各个项目的servlet，公共部分交由它的父类加载器系统类加载器加载
 *
 * @author wuyuan
 * @date 2019/7/24
 */
public class ServeltClassLoader extends ClassLoader {
    // 定义类加载器的名字
    private String classLoaderName;
    // 要加载类的磁盘路径 -- D:\3 Code\Own\Yuanmcat\out\production\Yuanmcat\
    private String path;
    // 该类加载器对应应用的磁盘路径 -- D:\3 Code\Own\Yuanmcat\out\production\Yuanmcat\com\client1
    private String serverPath;
    // 所有当前类加载器加载的类的Class对象 -- 该应用的所有servlet
    private final Map<String, Class<?>> loadedClassMap = new HashMap<>();
    // 当前应用的初始化工具
    private Runners runners;
    // 当前应用的sql容器
    private SqlContainer sqlContainer;
    
    public ServeltClassLoader(ClassLoader parent, String classLoaderName, String serverPath) {
        super(parent);
        this.classLoaderName = classLoaderName;
        this.serverPath = serverPath;
    }
    
    /**
     * 重写ClassLoader的 findClass方法，该方法会在 loadClass方法中调用
     * -- 类加载器必须重写父类的findClass方法，并调用父类的 defineClass来加载类，生成类的Class对象
     *
     *
     * loadClass调用 findClass, findClass调用 defineClass
     *
     * loadClass:加载模型(顺序)，默认为双亲委派模型 -- 需要破坏双亲委派模型时重写，一般不建议重写
     * findClass:读取类的class文件内容，并调用defineClass方法加载类 -- 必须被重写
     * defineClass:真正加载类到JVM -- 此类不可以被重写
     */
    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        //获取类的字节码数据
        byte[] data = this.loadClassData(className);
        //生成类的Class对象，此时，类就被加载了
        Class clazz = super.defineClass(className, data, 0, data.length);
        
        //将加载的class对象保存到当前类加载器的map中
        this.loadedClassMap.put(className, clazz);
        
        return clazz;
    }
    
    /**
     * 破坏双亲委派模型
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        //首先判断是否已被当前类加载器加载
        Class<?> clazz = null;
        if (this.loadedClassMap.containsKey(name)) {
            clazz = this.loadedClassMap.get(name);
        }
        if (clazz == null) {
            //交给父类加载器加载
            if (name.contains(SeverConstant.YUANMCAT_POINT)
                    || !name.contains(this.classLoaderName)
                    || name.startsWith(SeverConstant.JAVA_POINT)
                    || name.startsWith(SeverConstant.SUN_POINT)
                    || name.startsWith(SeverConstant.JAVAX_POINT)) {
                clazz = this.getParent().loadClass(name);
            } else {
                //否则自己加载
                Logger.err(LocalTime.now() + SeverConstant.CLASSLOADER + this.classLoaderName + SeverConstant.LOAD + name);
                clazz = this.findClass(name);
            }
        }
        return clazz;
    }
    
    /**
     * 根据类的名字(如 com.jvm.classloader.MyClassLoader)，获取到类的字节码数据
     */
    private byte[] loadClassData(String className) {
        InputStream is = null;
        byte[] data = null;
        ByteArrayOutputStream baos = null;
        
        try {
            //把全类名的.替换成/，组合成文件路径的形式
            className = className.replaceAll(SeverConstant.SLASH_POINT, SeverConstant.SLASH_3);
            //定义字节码文件结尾名 -- 字节码文件都是以 .class 结尾的
            String fileExtension = SeverConstant.POINT_CLASS;
            is = new FileInputStream(new File(this.path + className + fileExtension));
            baos = new ByteArrayOutputStream();
            
            int ch = 0;
            while ((ch = is.read()) != -1) {
                baos.write(ch);
            }
            data = baos.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                is.close();
                baos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return data;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getClassLoaderName() {
        return classLoaderName;
    }
    
    public Runners getRunners() {
        return runners;
    }
    
    public void setRunners(Runners runners) {
        this.runners = runners;
    }
    
    public String getServerPath() {
        return serverPath;
    }
    
    public SqlContainer getSqlContainer() {
        return sqlContainer;
    }
    
    public boolean hasSqlContainer() {
        return this.sqlContainer != null;
    }
    
    public void setSqlContainer(SqlContainer sqlContainer) {
        this.sqlContainer = sqlContainer;
    }
}
