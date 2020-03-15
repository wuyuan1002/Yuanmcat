package yuanmcat.sql;

import yuanmcat.classloader.ServeltClassLoader;
import yuanmcat.servlet.Invalid;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * sql语句容器 -- 一个应用一个
 *
 * sql容器中实际存放的是sql名称和对应的sqlElement对象
 *
 * @author wuyuan
 * @date 2020/1/3
 */
public class SqlContainer {
    //存放当前应用的所有sql片段
    private final Map<String, SqlElement> sqlElements = new HashMap<>();
    
    /**
     * 添加应用的 sql
     */
    public static void addSqlElement(ClassLoader classLoader, Class aClass) {
        if (!(classLoader instanceof ServeltClassLoader && aClass.isAnnotationPresent(Table.class) && !aClass.isAnnotationPresent(Invalid.class))) {
            return;
        }
        SqlContainer sqlContainer;
        if ((sqlContainer = ((ServeltClassLoader) classLoader).getSqlContainer()) == null) {
            sqlContainer = new SqlContainer();
            ((ServeltClassLoader) classLoader).setSqlContainer(sqlContainer);
        }
        // 获取所有方法 -- 一个方法对应一条sql
        Method[] methods = aClass.getDeclaredMethods();
        if (methods.length != 0) {
            // 获取表名
            String tableName = aClass.getSimpleName();
            Object table = null;
            try {
                // 创建表对象
                Constructor<Object> constructor = aClass.getConstructor();
                table = constructor.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            String sqlName;
            SqlElement sqlElement;
            if (table != null) {
                boolean put = false;
                for (Method method : methods) {
                    try {
                        // 获取sql名 -- 表名+方法名
                        sqlName = tableName + method.getName();
                        // 获取对应的sqlElement -- 若强转失败直接到catch，后面代码都不执行了，相当于直接跳过这个方法 -- 说明用户返回的不是sqlElement对象
                        sqlElement = (SqlElement) method.invoke(table);
                        sqlElement.setTableName(tableName);
                        
                        if (method.isAnnotationPresent(Select.class)) {
                            sqlElement.setSqlMethod(SqlMethod.SELECT);
                            sqlElement.setSqlConditions(method.getAnnotation(Select.class).SqlCondition());
                            put = true;
                        } else if (method.isAnnotationPresent(Delete.class)) {
                            sqlElement.setSqlMethod(SqlMethod.DELETE);
                            sqlElement.setSqlConditions(method.getAnnotation(Delete.class).SqlCondition());
                            put = true;
                        } else if (method.isAnnotationPresent(Insert.class)) {
                            sqlElement.setSqlMethod(SqlMethod.INSERT);
                            sqlElement.setDefaultValue(method.getAnnotation(Insert.class).DefaultValues());
                            put = true;
                        } else if (method.isAnnotationPresent(Update.class)) {
                            sqlElement.setSqlMethod(SqlMethod.UPDATE);
                            Update update = method.getAnnotation(Update.class);
                            sqlElement.setSqlConditions(update.SqlCondition());
                            sqlElement.setDefaultValue(update.DefaultValues());
                            put = true;
                        }
                        if (put) {
                            // 将sqlElement存到容器中
                            sqlContainer.getSqlElements().put(sqlName, sqlElement);
                        }
                    } catch (Exception e) {
                        // 什么都不用做，走到这里说明sql方法不符合规范，直接跳过该方法即可
                    } finally {
                        put = false;
                    }
                }
            }
        }
    }
    
    public Map<String, SqlElement> getSqlElements() {
        return sqlElements;
    }
}
