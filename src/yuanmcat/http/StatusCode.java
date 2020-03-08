package yuanmcat.http;

/**
 * 状态码枚举
 *
 * @author wuyuan
 * @date 2019/12/30
 */
public enum StatusCode {
    /**
     * OK
     */
    OK(200, "OK"),
    /**
     * Bad Request
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * Forbidden
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * Not Found
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * Internal Server Error
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    
    private int code;
    private String phrase;
    
    StatusCode(int code, String phrase) {
        this.code = code;
        this.phrase = phrase;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getPhrase() {
        return phrase;
    }
    
    //根据状态码获取状态描述
    public static String queryPhrase(int code) {
        for (StatusCode statusCodeEnum : StatusCode.values()) {
            if (statusCodeEnum.getCode() == code) {
                return statusCodeEnum.getPhrase();
            }
        }
        return null;
    }
}
