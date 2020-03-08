package yuanmcat.exception;

/**
 * 初始化执行顺序异常
 *
 * @author wuyuan
 * @date 2019/12/31
 */
public class RunnerException extends RuntimeException {
    private String msg;
    
    public RunnerException(String msg) {
        this.msg = msg;
    }
    
    public String getMsg() {
        return msg;
    }
}
