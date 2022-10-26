package com.tx06.interceptor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tx06.config.Constant;
import com.tx06.config.Prop;
import com.tx06.entity.Apidoc;
import com.tx06.request.SenderServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.util.*;

@Component
@Order(value = 100)
public class StaticAnalysis extends AbstractApidocAspect implements CommandLineRunner {
    private Log log = LogFactory.getLog(StaticAnalysis.class);
    private static LocalVariableTableParameterNameDiscoverer parameterNameDiscovere = new LocalVariableTableParameterNameDiscoverer();

    @Override
    public void run(String... args) throws Exception {
        init();
        rsycnFieldComment();
        rsyncDict();
        start();
        log.debug("StaticAnalysis 执行完成");
    }

    private void init() throws SQLException {
        log.debug("开始初始化");
        this.u_project_uuid = SpringUtil.getBean(Prop.class).getServer().getUuid();
        if(!StrUtil.isEmpty(getProp().getServer().getBasePath())){
            Constant.BASE_PATH = getProp().getServer().getBasePath();
        }
        AbstractApidocAspect.jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
    }

    private void rsycnFieldComment() throws SQLException {
        log.debug("同步数据库字段备注");
        String [] arr = AbstractApidocAspect.jdbcTemplate.getDataSource().getConnection().getMetaData().getURL().split("\\?")[0].split("/");
        dbName = arr[arr.length-1];
        String sql = "SELECT c.`COLUMN_NAME` AS field,c.`COLUMN_COMMENT` AS name FROM `information_schema`.`COLUMNS` c WHERE c.`TABLE_SCHEMA` = '" + dbName
                + "'  AND c.column_comment IS NOT NULL AND c.column_comment != ''  GROUP BY c.column_name";
        List<Map<String,Object>> columns = jdbcTemplate.queryForList(sql);
        columns.stream().forEach(r->{
            r.put("u_project_uuid",this.u_project_uuid);
            r.put("data_type","3");
        });
        SpringUtil.getBean(SenderServiceImpl.class).rsycnFieldComment(columns);
    }

    private void rsyncDict() throws SQLException {
        log.debug("同步数据库字段备注");
        String sql = getProp().server.getDictSql();
        if(sql == null)return;
        List<Map<String,Object>> columns = jdbcTemplate.queryForList(sql);
        columns.stream().forEach(r->{
            r.put("u_project_uuid",this.u_project_uuid);
        });
        SpringUtil.getBean(SenderServiceImpl.class).rsyncDict(columns);
    }


    public void start() throws InstantiationException, IllegalAccessException, IOException {
        log.debug("同步新增的接口");
        RequestMappingHandlerMapping mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
        // 拿到Handler适配器中的所有方法
        Map<RequestMappingInfo, HandlerMethod> methodMap = mapping.getHandlerMethods();
        if(!FileUtil.exist("urls.db")){
            FileUtil.touch("urls.db");
        }
        List<String> alreadLines = FileUtil.readLines("urls.db","utf-8");
        for (RequestMappingInfo info : methodMap.keySet()){
            Apidoc api = new Apidoc();
            HandlerMethod handlerMethod = methodMap.get(info);
            RestController restController = handlerMethod.getBeanType().getAnnotation(RestController.class);
            Set<String> patterns = info.getPatternsCondition().getPatterns();
            String url = patterns.toArray(new String[patterns.size()])[0];

            if(restController == null || StrUtil.isEmpty(restController.value()) || StrUtil.isEmpty(info.getName()) || alreadLines.contains(url))continue;
            api.setU_project_uuid(getProp().getServer().getUuid());
            api.setConfirmed("2");
            setMethodType(handlerMethod,api);
            setUrlTitle( handlerMethod, info, api);
            setParameters( handlerMethod, api);
            setResponse( handlerMethod, api);

            SpringUtil.getBean(SenderServiceImpl.class).send(api);
            FileUtil.appendString(url+"\n","urls.db","utf-8");
        }
    }


    private void setResponse(HandlerMethod handlerMethod,Apidoc apidoc) throws IllegalAccessException, InstantiationException {
        apidoc.setResponse_examples(JSON.toJSONString(newInstance( handlerMethod.getMethod().getReturnType())));
    }

    private void setParameters(HandlerMethod handlerMethod,Apidoc apidoc) throws IllegalAccessException, InstantiationException {
        String [] parameterNames = parameterNameDiscovere.getParameterNames(handlerMethod.getMethod());
        MethodParameter [] parameters = handlerMethod.getMethodParameters();
        boolean hasRequestBody = hasRequestBody(handlerMethod);
        Map parameterExamples = new HashMap();
        if(hasRequestBody){
            for(int i=0;i<parameters.length;i++){
                MethodParameter methodParameter = parameters[i];
                Parameter parameter = methodParameter.getParameter();
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                if(requestBody != null){
                    try {
                        parameterExamples.putAll(newInstance(parameter.getType()));
                    }catch (Exception e){
                        LogFactory.getLog("setParameters:").error(parameter.getType().getSimpleName() + "初始化失败,"+e.getMessage());
                    }
                }else{
                    if(BeanUtils.isSimpleProperty(parameter.getType())){//基本类型
                        parameterExamples.put(parameterNames[i],"");
                    }else{
                        parameterExamples.putAll(newInstance(parameter.getType()));
                    }
                }
            }
        }else {
            for(int i=0;i<parameters.length;i++){
                MethodParameter methodParameter = parameters[i];
                Parameter parameter = methodParameter.getParameter();
                if(BeanUtils.isSimpleProperty(parameter.getType())){//基本类型
                    parameterExamples.put(parameterNames[i],"");
                }else{
                    parameterExamples.putAll(newInstance(parameter.getType()));
                }
            }
        }
        apidoc.setParameter_examples(JSON.toJSONString(parameterExamples, SerializerFeature.WriteMapNullValue));
    }

    private JSONObject newInstance(Class c){
        try {
            return JSON.parseObject(JSON.toJSONString(c.newInstance(), SerializerFeature.WriteMapNullValue));
        } catch (Exception e) {
            LogFactory.getLog("setParameters:").error(c.getSimpleName() + "初始化失败,"+e.getMessage());
        }
        return new JSONObject();
    }

    private void setUrlTitle(HandlerMethod handlerMethod,RequestMappingInfo info,Apidoc apidoc){
        //设置url
        Set<String> patterns = info.getPatternsCondition().getPatterns();
        apidoc.setUrl(patterns.toArray(new String[patterns.size()])[0]);

        RestController restController = handlerMethod.getBeanType().getAnnotation(RestController.class);
        if(restController == null || StrUtil.isEmpty(restController.value())){
            return;
        }
        String controllerName = restController.value();
        String methodName = info.getName();
        apidoc.setTitle(methodName);
        apidoc.setFull_title((controllerName+"/"+methodName));
    }

    /**
     * 设置contentType、method
     * */
    private void setMethodType(HandlerMethod handlerMethod,Apidoc apidoc){
        boolean hasRequestBody = hasRequestBody(handlerMethod);
        boolean hasFile = hasMultipartFile(handlerMethod);
        if(hasRequestBody){
            apidoc.setContent_type(MediaType.APPLICATION_JSON_VALUE);
        }else if(hasFile){
            apidoc.setContent_type(MediaType.MULTIPART_FORM_DATA_VALUE);
        }else{
            apidoc.setContent_type(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        }
        if(handlerMethod.getMethodAnnotation(RequestMapping.class) != null){
            apidoc.setMethod(hasRequestBody ? "POST" : "GET");
        }else if(handlerMethod.getMethodAnnotation(PostMapping.class) != null){
            apidoc.setMethod("POST");
        }else if(handlerMethod.getMethodAnnotation(GetMapping.class) != null){
            apidoc.setMethod("GET");
        }else if(handlerMethod.getMethodAnnotation(PutMapping.class) != null){
            apidoc.setMethod("PUT");
        }else if(handlerMethod.getMethodAnnotation(DeleteMapping.class) != null){
            apidoc.setMethod("DELETE");
        }
    }

    private boolean hasRequestBody(HandlerMethod handlerMethod){
        MethodParameter [] parameters = handlerMethod.getMethodParameters();
        boolean hasRequestBody = false;
        for (int i=0;i<parameters.length;i++){
            MethodParameter methodParameter = parameters[i];
            RequestBody requestBody = methodParameter.getParameter().getAnnotation(RequestBody.class);
            if(requestBody != null){
                hasRequestBody = true;
                break;
            }
        }
        return hasRequestBody;
    }

    private boolean hasMultipartFile(HandlerMethod handlerMethod){
        MethodParameter[] parameters = handlerMethod.getMethodParameters();
        boolean has = false;
        for (int i=0;i<parameters.length;i++){
            MethodParameter methodParameter = parameters[i];
            has = methodParameter.getParameter().getType().getSimpleName().contains("MultipartFile");
            if(has){
                break;
            }
        }
        return has;
    }

    @Override
    protected String getMethodName() {
        return null;
    }
}
