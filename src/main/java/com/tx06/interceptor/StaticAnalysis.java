package com.tx06.interceptor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tx06.config.Constant;
import com.tx06.config.ApiDocProp;
import com.tx06.entity.Apidoc;
import com.tx06.request.SenderServiceImpl;
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

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteMapNullValue;

@Component
@Order(value = 100)
public class StaticAnalysis implements CommandLineRunner {
    private Log log = LogFactory.get(StaticAnalysis.class);
    private static LocalVariableTableParameterNameDiscoverer parameterNameDiscovere = new LocalVariableTableParameterNameDiscoverer();
    private String dbName;


    @Override
    public void run(String... args) throws Exception {
        try {
            init();
            tableCommentCheck();
            rsyncFieldComment();
            rsyncDict();
            start();
            log.debug("StaticAnalysis 执行完成");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void init() throws SQLException {
        log.debug("开始初始化");
        if(!StrUtil.isEmpty(MappingHandleBuilder.getProp().getServer().getBasePath())){
            Constant.BASE_PATH = MappingHandleBuilder.getProp().getServer().getBasePath();
        }

        String [] arr = MappingHandleBuilder.getJdbcTemplate().getDataSource().getConnection().getMetaData().getURL().split("\\?")[0].split("/");
        dbName = arr[arr.length-1];

    }

    private void tableCommentCheck() throws SQLException {

        log.debug("数据库表备注格式检查");
        String sql = "select t.`table_name`,t.`table_comment` from `information_schema`.`TABLES` t where t.`TABLE_SCHEMA` = '"+dbName+"' and table_comment not like '%|%'";
        List<Map<String,Object>> columns = MappingHandleBuilder.getJdbcTemplate().queryForList(sql);
        columns.forEach(r->{
            String tableName = (String) r.get("table_name");
            String tableComment = (String) r.get("table_comment");
            log.info(String.format("表%s 备注格式错误，期望controllerPath|tableComment，目前：%s", tableName, tableComment));
        });
    }

    private void rsyncFieldComment() throws SQLException {
        log.debug("同步数据库字段备注");
        String sql = "SELECT c.`COLUMN_NAME` AS field,c.`COLUMN_COMMENT` AS name FROM `information_schema`.`COLUMNS` c WHERE c.`TABLE_SCHEMA` = '" + dbName
                + "'  AND c.column_comment IS NOT NULL AND c.column_comment != ''  GROUP BY c.column_name";
        List<Map<String,Object>> columns = MappingHandleBuilder.getJdbcTemplate().queryForList(sql);
        columns.stream().forEach(r->{
            r.put("projectUuid",MappingHandleBuilder.getProp().server.getUuid());
            r.put("dataType","3");
        });
        SpringUtil.getBean(SenderServiceImpl.class).rsyncFieldComment(columns);
    }

    private void rsyncDict() throws SQLException {
        log.debug("同步数据库字段备注");
        String sql = MappingHandleBuilder.getProp().server.getDictSql();
        if(sql == null){
            return;
        }
        List<Map<String,Object>> columns = MappingHandleBuilder.getJdbcTemplate().queryForList(sql);
        columns.forEach(r->{
            r.put("u_project_uuid",MappingHandleBuilder.getProp().server.getUuid());
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

            if(restController == null || StrUtil.isEmpty(restController.value()) || StrUtil.isEmpty(info.getName()) || alreadLines.contains(url)){
                continue;
            }
            api.setProjectUuid(MappingHandleBuilder.getProp().server.getUuid());
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
        apidoc.setResponseExamples(JSON.toJSONString(newInstance( handlerMethod.getMethod().getReturnType())));
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
                        log.error(parameter.getType().getSimpleName() + "初始化失败,"+e.getMessage());
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
        apidoc.setParameterExamples(JSON.toJSONString(parameterExamples, WriteMapNullValue));
    }

    private JSONObject newInstance(Class c){
        try {
            return JSON.parseObject(JSON.toJSONString(c.newInstance(), WriteMapNullValue));
        } catch (Exception e) {
            log.error(c.getSimpleName() + "初始化失败,"+e.getMessage());
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
        apidoc.setFullTitle((controllerName+"/"+methodName));
    }

    /**
     * 设置contentType、method
     * */
    private void setMethodType(HandlerMethod handlerMethod,Apidoc apidoc){
        boolean hasRequestBody = hasRequestBody(handlerMethod);
        boolean hasFile = hasMultipartFile(handlerMethod);
        if(hasRequestBody){
            apidoc.setContentType(MediaType.APPLICATION_JSON_VALUE);
        }else if(hasFile){
            apidoc.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        }else{
            apidoc.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
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

}
