package yuanmcat.sql;

/**
 * 每个应用一个sql语句容器 SqlContainer，
 * SqlContainer容器中存放的一条sql对应一个SqlElement对象
 *
 * @author wuyuan
 * @date 2020/1/3
 */
public class SqlElement {
    //sql中的字段名
    private String[] tableAttributes = {};
    //sql条件中的字段名(有的已经包含条件了)
    private String[] sqlConditions = {};
    //sql类型 -- 增删改查
    private SqlMethod sqlMethod = null;
    //sql中的默认值
    private String[] defaultValue = {};
    //表名
    private String tableName = null;
    //若为已确定的sql则直接获取constantSql即可，不需要重复拼接
    private String constantSql = null;
    
    /**
     * 是否为已确定的sql
     */
    public boolean isConstantSql() {
        for (String condition : this.sqlConditions) {
            if (!SqlUtil.isDefault(condition)) {
                return false;
            }
        }
        for (String value : this.defaultValue) {
            if (!SqlUtil.isDefault(value)) {
                return false;
            }
        }
        return true;
    }
    
    public SqlElement() {
    }
    
    public SqlElement(String... tableAttributes) {
        this.tableAttributes = tableAttributes;
    }
    
    public String[] getTableAttributes() {
        return tableAttributes;
    }
    
    public String[] getSqlConditions() {
        return sqlConditions;
    }
    
    public void setSqlConditions(String[] sqlConditions) {
        this.sqlConditions = sqlConditions;
    }
    
    public SqlMethod getSqlMethod() {
        return sqlMethod;
    }
    
    public void setSqlMethod(SqlMethod sqlMethod) {
        this.sqlMethod = sqlMethod;
    }
    
    public String getConstantSql() {
        return constantSql;
    }
    
    public void setConstantSql(String constantSql) {
        this.constantSql = constantSql;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String[] getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String[] defaultValue) {
        this.defaultValue = defaultValue;
    }
}
