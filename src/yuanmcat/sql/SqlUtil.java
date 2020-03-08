package yuanmcat.sql;

import yuanmcat.classloader.ServeltClassLoader;
import yuanmcat.constant.SeverConstant;
import yuanmcat.constant.SqlConstant;
import yuanmcat.server.ServletMappingConfig;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * sql工具类
 *
 * @author wuyuan
 * @date 2020/1/3
 */
public class SqlUtil {
    
    /**
     * 生成 sql语句
     */
    public static String createSql(SqlElement sqlElement, String[] values) {
        //若是sql常量且已被生成则直接返回sql
        if (sqlElement.isConstantSql() && sqlElement.getConstantSql() != null) {
            return sqlElement.getConstantSql();
        }
        
        StringBuilder sql = new StringBuilder();
        String tableName = sqlElement.getTableName();
        switch (sqlElement.getSqlMethod()) {
            case DELETE: {
                //delete from person where id = 1 and name = "zhangsan";
                sql.append(SqlConstant.DELETE);
                sql.append(tableName);
                SqlUtil.addWhere(sql, sqlElement, values, 0);
                break;
            }
            case INSERT: {
                //insert into person (id,name,age,phone,address) values (1,'yang',22,'123232323','中国');
                sql.append(SqlConstant.INSERT);
                sql.append(tableName);
                sql.append(SqlConstant.SPACE);
                sql.append(SqlConstant.LEFT);
                SqlUtil.addAttributes(sql, sqlElement);
                sql.append(SqlConstant.RIGHT);
                sql.append(SqlConstant.VALUES);
                sql.append(SqlConstant.LEFT);
                SqlUtil.addValues(sql, sqlElement, values);
                sql.append(SqlConstant.RIGHT);
                break;
            }
            case UPDATE: {
                //update person set address='浙江', id=12 where id = 1;
                sql.append(SqlConstant.UPDATE);
                sql.append(tableName);
                sql.append(SqlConstant.SET);
                SqlUtil.addWhere(sql, sqlElement, values, SqlUtil.addValues(sql, sqlElement, values));
                break;
            }
            case SELECT: {
                //SELECT column1, column2, columnN FROM table_name where id = 1 and name = 'yuanyuan';
                sql.append(SqlConstant.SELECT);
                SqlUtil.addAttributes(sql, sqlElement);
                sql.append(SqlConstant.FROM);
                sql.append(tableName);
                SqlUtil.addWhere(sql, sqlElement, values, 0);
                break;
            }
            default: {
                return null;
            }
        }
        //若是sql常量则存到sqlElement中，下次直接获取即可
        String s = sql.append(SqlConstant.SEMICOLON).toString();
        if (sqlElement.isConstantSql()) {
            sqlElement.setConstantSql(s);
        }
        return s;
    }
    
    /**
     * 获取sql -- 若没有则返回 null
     */
    public static String getSql(Class clazz, String sqlName, String... values) {
        String serverPath = null;
        try {
            serverPath = URLDecoder.decode(new File(clazz.getResource(SeverConstant.EMPTY).getPath()).getAbsolutePath(), SeverConstant.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (serverPath != null) {
            boolean isThisSever = false;
            for (Map.Entry<String, ClassLoader> sever : ServletMappingConfig.classLoaderMap.entrySet()) {
                // 判断是否为clazz对应的应用
                if (serverPath.contains(sever.getKey()) && sever.getValue() instanceof ServeltClassLoader) {
                    isThisSever = true;
                    // 若应用有sql容器
                    if (((ServeltClassLoader) sever.getValue()).hasSqlContainer()) {
                        // 获取应用的sql容器
                        Map<String, SqlElement> sqlElements = ((ServeltClassLoader) sever.getValue()).getSqlContainer().getSqlElements();
                        for (Map.Entry<String, SqlElement> sqlElementEntry : sqlElements.entrySet()) {
                            // 获取对应的sql
                            if (sqlElementEntry.getKey().equals(sqlName)) {
                                return SqlUtil.createSql(sqlElementEntry.getValue(), values);
                            }
                        }
                    }
                }
                if (isThisSever) {
                    break;
                }
            }
        }
        return null;
    }
    
    /**
     * 添加字段名
     */
    private static void addAttributes(StringBuilder sql, SqlElement sqlElement) {
        String[] tableAttributes = sqlElement.getTableAttributes();
        for (int i = 0; i < tableAttributes.length; ++i) {
            sql.append(tableAttributes[i]);
            if (i < tableAttributes.length - 1) {
                sql.append(SqlConstant.COMMA);
            }
        }
    }
    
    /**
     * 添加字段的对应值
     */
    private static int addValues(StringBuilder sql, SqlElement sqlElement, String[] values) {
        String[] tableAttributes = sqlElement.getTableAttributes();
        String value = null;
        int valuesIndex = 0;
        for (int i = 0; i < tableAttributes.length; ++i) {
            //找默认值
            for (String defaultValue : sqlElement.getDefaultValue()) {
                if (defaultValue.contains(tableAttributes[i])) {
                    value = defaultValue;
                    break;
                }
            }
            //若该字段有设置默认值
            if (value != null) {
                if (sqlElement.getSqlMethod() == SqlMethod.UPDATE) {
                    SqlUtil.addDefaultValue(sql, value);
                } else if (sqlElement.getSqlMethod() == SqlMethod.INSERT) {
                    value = value.replace(SqlConstant.SPACE, SqlConstant.EMPTY);
                    value = value.substring(value.lastIndexOf(SqlConstant.EQUALS) + 1);
                    SqlUtil.addValue(sql, value);
                }
                value = null;
            } else {
                if (sqlElement.getSqlMethod() == SqlMethod.UPDATE) {
                    sql.append(tableAttributes[i]);
                    sql.append(SqlConstant.SPACE_EQUALS);
                }
                SqlUtil.addValue(sql, values[valuesIndex++].trim());
            }
            if (i < tableAttributes.length - 1) {
                sql.append(SqlConstant.COMMA);
            }
        }
        return valuesIndex;
    }
    
    /**
     * 添加 where条件
     */
    private static void addWhere(StringBuilder sql, SqlElement sqlElement, String[] values, int valuesIndex) {
        String[] conditions = sqlElement.getSqlConditions();
        if (conditions.length > 0) {
            sql.append(SqlConstant.WHERE);
            for (int i = 0; i < conditions.length; ++i) {
                if (SqlUtil.isDefault(conditions[i])) {
                    SqlUtil.addDefaultValue(sql, conditions[i]);
                } else {
                    sql.append(conditions[i]);
                    sql.append(SqlConstant.SPACE_EQUALS);
                    SqlUtil.addValue(sql, values[valuesIndex++].trim());
                }
                if (i < conditions.length - 1) {
                    sql.append(SqlConstant.AND);
                }
            }
        }
    }
    
    /**
     * 添加不变的值
     */
    private static void addDefaultValue(StringBuilder sql, String value) {
        int i;
        for (i = value.length(); --i >= 0; ) {
            if (!Character.isLetter(value.charAt(i)) && !Character.isDigit(value.charAt(i)) && value.charAt(i) != SqlConstant.POINT) {
                break;
            }
        }
        sql.append(value, 0, i + 1);
        SqlUtil.addValue(sql, value.substring(i + 1));
    }
    
    /**
     * 添加值 -- 看是否需是sql语句和是否要加 '
     */
    private static void addValue(StringBuilder sql, String value) {
        if (value.toUpperCase().startsWith(SqlConstant.SELECT)) {
            sql.append(SqlConstant.LEFT);
            sql.append(value.trim());
            sql.append(SqlConstant.RIGHT);
        } else if (SqlUtil.isNumeric(value)) {
            sql.append(value);
        } else {
            sql.append(SqlConstant.SINGLE_QUOTATION);
            sql.append(value);
            sql.append(SqlConstant.SINGLE_QUOTATION);
        }
    }
    
    /**
     * 判断字符串是否为数字 -- 123，78.256等
     *
     * 只有字符串中只有数字和最多一个.且不在首尾时才返回true
     */
    private static boolean isNumeric(String value) {
        boolean contain = false;
        for (int i = value.length(); --i >= 0; ) {
            if (!Character.isDigit(value.charAt(i))) {
                if (value.charAt(i) == SqlConstant.POINT && !contain && i != 0 && i != value.length() - 1) {
                    contain = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }
    
    /**
     * 是否包含 =<>|
     */
    public static boolean isDefault(String condition) {
        return condition.contains(SqlConstant.EQUALS)
                || condition.contains(SqlConstant.GREATER)
                || condition.contains(SqlConstant.LESS)
                || condition.contains(SqlConstant.MARK);
    }
}
