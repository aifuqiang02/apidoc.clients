package com.tx06.request;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tx06.config.ApiDocProp;
import com.tx06.entity.Apidoc;
import com.tx06.entity.ExceptionLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.tx06.config.Constant.VERSION;

@EnableAsync
@Component
public class SenderServiceImpl {
    private Log log = LogFactory.getLog(SenderServiceImpl.class);

    @Async("apidocTaskExecutor")
    public void send(Apidoc apidoc){
        try {
            String param = JSON.toJSONString(apidoc);

            String str = HttpUtil.post(SpringUtil.getBean(ApiDocProp.class).getServer().getBasePath() + "/apiDoc/add?version="+VERSION,param);
            JSONObject rs = JSON.parseObject(str);
            if(!rs.containsKey("code") || rs.getInteger("code").intValue()!=200){
                log.error("apidoc 接口请求失败：" + rs.getString("msg"));
            }
        }catch (Exception e){
            log.error("apidoc 接口请求失败：" + e.getMessage());
        }
    }

    public void rsycnFieldComment(List<Map<String,Object>> list){
        try {
            String param = JSON.toJSONString(list);

            String str = HttpUtil.post(SpringUtil.getBean(ApiDocProp.class).getServer().getBasePath() + "/apiDoc/rsycnFieldComment?version="+VERSION,param);
            JSONObject rs = JSON.parseObject(str);
            if(!rs.containsKey("code") || rs.getInteger("code").intValue()!=200){
                log.error("apidoc 接口请求失败：" + rs.getString("msg"));
            }
        }catch (Exception e){
            log.error("apidoc 接口请求失败：" + e.getMessage());
        }
    }


    public void sendExceptionLog(ExceptionLog exceptionLog){
        try {
            String param = JSON.toJSONString(exceptionLog);

            String str = HttpUtil.post(SpringUtil.getBean(ApiDocProp.class).getServer().getBasePath() + "/project/exceptionLog/doSave?version="+VERSION,param);
            JSONObject rs = JSON.parseObject(str);
            if(!rs.containsKey("code") || rs.getInteger("code").intValue()!=200){
                log.error("apidoc 接口请求失败：" + rs.getString("msg"));
            }
        }catch (Exception e){
            log.error("apidoc 接口请求失败：" + e.getMessage());
        }
    }

    public void rsyncDict(List<Map<String,Object>> list) {
        try {
            String param = JSON.toJSONString(list);

            String str = HttpUtil.post(SpringUtil.getBean(ApiDocProp.class).getServer().getBasePath() + "/apiDoc/rsyncDict?version="+VERSION,param);
            JSONObject rs = JSON.parseObject(str);
            if(!rs.containsKey("code") || rs.getInteger("code").intValue()!=200){
                log.error("apidoc 接口请求失败：" + rs.getString("msg"));
            }
        }catch (Exception e){
            log.error("apidoc 接口请求失败：" + e.getMessage());
        }
    }
}
