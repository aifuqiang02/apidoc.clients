package com.tx06.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "tx06")
@Component
@Data
public class ApiDocProp {
    public Server server = new Server();

    @Data
    public static class Server {
        private String run;
        private String basePath;
        private String dictSql;
        private String uuid;
        public boolean getRun() {
            if(StrUtil.isNotEmpty(run)){
                return run.equals("true");
            }else{
                return !SystemUtil.getOsInfo().isLinux();
            }
        }

        public String getBasePath() {
            if(basePath == null){
                this.basePath = Constant.BASE_PATH;
            }
            return basePath;
        }
    }

}
