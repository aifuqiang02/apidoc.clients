package com.tx06.request;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tx06.config.ApiDocProp;
import com.tx06.entity.*;
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
    private final Log log = LogFactory.getLog(SenderServiceImpl.class);

    @Async("apidocTaskExecutor")
    public void send(Api apidoc, Callback callback){
        try {
            ApiDocProp.Server server = SpringUtil.getBean(ApiDocProp.class).getServer();
            ApiBatchAddVO apiBatchAddVO = new ApiBatchAddVO();
            apiBatchAddVO.setApi(apidoc);
            apiBatchAddVO.setProjectUuid(apidoc.getProjectUuid());
            String param = JSON.toJSONString(apiBatchAddVO);
            HttpRequest httpRequest = HttpUtil.createPost(server.getBasePath() + "/api/api?version="+VERSION);
            httpRequest.addHeaders(Map.of("Content-Type","application/json;charset=UTF-8"));
            httpRequest.addHeaders(Map.of("Authorization",server.getToken()));
            httpRequest.body(param);
            String str = httpRequest.execute().body();
            JSONObject rs = JSON.parseObject(str);
            if(!rs.containsKey("code") || rs.getInteger("code")!=200){
                log.error("apidoc 接口请求失败：" + rs.getString("msg"));
                callback.onFailure(apidoc, new Exception(rs.getString("msg")));
            }else{
                callback.onSuccess(apidoc);
            }
        }catch (Exception e){
            callback.onFailure(apidoc, e);
            log.error("apidoc 接口请求失败：" + e.getMessage());
        }
    }

    public void rsyncFieldComment(List<Map<String,Object>> list){
        try {
            String param = JSON.toJSONString(list);

            String str = HttpUtil.post(SpringUtil.getBean(ApiDocProp.class).getServer().getBasePath() + "/apiDoc/rsycnFieldComment?version="+VERSION,param);
            JSONObject rs = JSON.parseObject(str);
            if(!rs.containsKey("code") || rs.getInteger("code")!=200){
                log.error("apidoc 接口请求失败：" + rs.getString("msg"));
            }
        }catch (Exception e){
            log.error("apidoc 接口请求失败：" + e.getMessage());
        }
    }

    public void sendMessage(TMessage message){
        try {
            String param = JSON.toJSONString(message);
            String str = HttpUtil.post(SpringUtil.getBean(ApiDocProp.class).getServer().getBasePath() + "/business/message/doSave?version="+VERSION,param);
            JSONObject rs = JSON.parseObject(str);
            if(!rs.containsKey("code") || rs.getInteger("code")!=200){
                log.error("接口请求失败：" + rs.getString("msg"));
            }
        }catch (Exception e){
            log.error("接口请求失败：" + e.getMessage());
        }
    }

    public void rsyncDict(List<Map<String,Object>> list) {
        try {
            String param = JSON.toJSONString(list);

            String str = HttpUtil.post(SpringUtil.getBean(ApiDocProp.class).getServer().getBasePath() + "/apiDoc/rsyncDict?version="+VERSION,param);
            JSONObject rs = JSON.parseObject(str);
            if(!rs.containsKey("code") || rs.getInteger("code")!=200){
                log.error("apidoc 接口请求失败：" + rs.getString("msg"));
            }
        }catch (Exception e){
            log.error("apidoc 接口请求失败：" + e.getMessage());
        }
    }
}
