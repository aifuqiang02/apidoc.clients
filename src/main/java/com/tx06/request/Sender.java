package com.tx06.request;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tx06.config.Constant;
import com.tx06.entity.Apidoc;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

import static com.tx06.config.Constant.VERSION;


public class Sender extends Thread{
    private Log log = LogFactory.getLog(Sender.class);
    private Apidoc apidoc;

    public Sender(Apidoc apidoc) {
        this.apidoc = apidoc;
    }

    @Override
    public void run() {
        try {
            String param = JSON.toJSONString(apidoc);

          String str = HttpUtil.post(Constant.BASE_PATH + "/apidoc/add?version="+VERSION,param);
          JSONObject rs = JSON.parseObject(str);
          if(!rs.containsKey("code") || rs.getInteger("code").intValue()!=200){
              log.error("apidoc 接口请求失败：" + rs.getString("msg"));
          }
        }catch (Exception e){
            log.error("apidoc 接口请求失败：" + e.getMessage());
        }
    }
}
