package yuanmcat.http;


import yuanmcat.GetStatic;
import yuanmcat.constant.HttpConstant;
import yuanmcat.http.json.JSONParser;
import yuanmcat.http.json.model.JsonObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 获取返回内容类型工具类
 *
 * @author wuyuan
 * @date 2019/12/30
 */
public class ContentTypeUtils {
    
    private static JsonObject jsonObject;
    
    static {
        JSONParser jsonParser = new JSONParser();
        try {
            jsonObject = (JsonObject) jsonParser.fromJSON(readFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取返回数据类型
     */
    private static String readFile() {
        try {
            InputStream is = GetStatic.class.getResourceAsStream(HttpConstant.CONTENT_TYPE);
            BufferedInputStream bis = new BufferedInputStream(is);
            
            StringBuilder sb = new StringBuilder();
            byte[] by = new byte[HttpConstant.DEFAULT_BUFFER_SIZE * 2];
            int len = -1;
            while ((len = bis.read(by)) != -1) {
                sb.append(new String(by, 0, len));
            }
            bis.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getContentType(String ext) {
        return (String) jsonObject.get(ext);
    }
}
