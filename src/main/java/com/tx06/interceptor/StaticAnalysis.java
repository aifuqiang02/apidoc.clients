package com.tx06.interceptor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tx06.config.ApiParamsType;
import com.tx06.config.BodyContentTypeEnum;
import com.tx06.config.Constant;
import com.tx06.config.RequestMethodEnum;
import com.tx06.entity.RequestParam;
import com.tx06.entity.*;
import com.tx06.handle.BaseRequestParamHandle;
import com.tx06.handle.RequestParamHandleFactory;
import com.tx06.request.SenderServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.MethodParameter;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteMapNullValue;

@Component
@Order(value = 100)
public class StaticAnalysis implements CommandLineRunner {
    private Log log = LogFactory.get(StaticAnalysis.class);
    private static StandardReflectionParameterNameDiscoverer parameterNameDiscovere = new StandardReflectionParameterNameDiscoverer();
    private String dbName;


    @Override
    public void run(String... args) throws Exception {
        try {
            init();
            //tableCommentCheck();
            //rsyncFieldComment();
            //rsyncDict();
            start();
            ApiChangeListener listener = new ApiChangeListener();
            listener.startListening();
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
        Assert.isFalse(StrUtil.isBlank(MappingHandleBuilder.getProp().getServer().getBasePackage())
                ,"tx06.server.basePackage 不能为空");
        if(StrUtil.isBlank(MappingHandleBuilder.getProp().getServer().getIgnoreField())){
            log.warn("tx06.server.ignoreField 为空");
        }
        Assert.isFalse(StrUtil.isBlank(MappingHandleBuilder.getProp().getServer().getToken()),
                "tx06.server.token 为空");

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
    }

    private String getDbName(){
        return "c:/" + MappingHandleBuilder.getProp().server.getUuid() + ".db";
    }

    public void start() throws InstantiationException, IllegalAccessException, IOException {
        log.error("缓存文件地址：", getDbName());
        log.debug("同步新增的接口");
        RequestMappingHandlerMapping mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
        // 拿到Handler适配器中的所有方法
        Map<RequestMappingInfo, HandlerMethod> methodMap = mapping.getHandlerMethods();
        if(!FileUtil.exist(getDbName())){
            FileUtil.touch(getDbName());
        }
        List<String> alreadLines = FileUtil.readLines(getDbName(),"utf-8");
        for (RequestMappingInfo info : methodMap.keySet()){
            HandlerMethod handlerMethod = methodMap.get(info);
            RestController restController = handlerMethod.getBeanType().getAnnotation(RestController.class);
            if(StrUtil.isBlank(info.getName()) || restController == null){
                continue;
            }
            Api api = buildApi( handlerMethod,info);
            String line = api.getUniqueIdentifier();
            if(alreadLines.contains(line)){
                continue;
            }else if(StrUtil.isBlank(api.getName())){
                continue;
            }
            SpringUtil.getBean(SenderServiceImpl.class).send(api, new Callback() {
                @Override
                public void onSuccess(Api apidoc) {
                    String line = apidoc.getUniqueIdentifier();
                    FileUtil.appendString(line +"\n",getDbName(),"utf-8");
                }

                @Override
                public void onFailure(Api apidoc, Exception exception) {
                    log.error("接口文档同步失败", exception);
                    log.error("apidoc", JSON.toJSONString(apidoc));
                }
            });
        }
    }

    public Api buildApi(HandlerMethod handlerMethod,RequestMappingInfo requestMappingInfo) throws IllegalAccessException, InstantiationException {
        Api api = new Api();
        api.setProjectUuid(MappingHandleBuilder.getProp().server.getUuid());
        setTitle( requestMappingInfo, api);
        setMethodType(handlerMethod,api);
        setContentType(handlerMethod,api);
        setUrl( requestMappingInfo, api);
        setParameters( handlerMethod, api);
        setRequestHeaders(api);
        return api;
    }

    private void setRequestHeaders(Api api) {
        List<RequestParam> headerParams = MappingHandleBuilder.getProp().getServer().getHeaderParams();
        RequestParams requestParams = api.getRequestParams();
        if(requestParams == null){
            requestParams = new RequestParams();
            api.setRequestParams(requestParams);
        }
        requestParams.setHeaderParams(headerParams);
    }


    private void setResponse(HandlerMethod handlerMethod,Apidoc apidoc) throws IllegalAccessException, InstantiationException {
        apidoc.setResponseExamples(JSON.toJSONString(newInstance( handlerMethod.getMethod().getReturnType())));
    }

    private void setParameters(HandlerMethod handlerMethod,Api api) throws IllegalAccessException, InstantiationException {
        String [] parameterNames = parameterNameDiscovere.getParameterNames(handlerMethod.getMethod());
        MethodParameter [] parameters = handlerMethod.getMethodParameters();
        RequestParams requestParams = new RequestParams();
        int methodType = api.getApiAttrInfo().getRequestMethod().intValue();
        for(int i=0;i<parameters.length;i++){
            MethodParameter methodParameter = parameters[i];
            Parameter parameter = methodParameter.getParameter();
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if(requestBody != null){
                try {
                    List<com.tx06.entity.RequestParam> responseParams = new ArrayList<>();
                    typeToRequestParams(responseParams,parameter.getType(),parameterNames[i],0);
                    requestParams.setBodyParams(responseParams);
                    api.setRequestParams(requestParams);
                }catch (Exception e){
                    log.error(parameter.getType().getSimpleName() + "初始化失败,"+e.getMessage());
                }
            }else if(methodType == RequestMethodEnum.GET.getValue()){
                List<com.tx06.entity.RequestParam> responseParams = new ArrayList<>();
                typeToRequestParams(responseParams,parameter.getType(),parameterNames[i],0);
                requestParams.setQueryParams(responseParams);
                api.setRequestParams(requestParams);
            }else{
                try {
                    List<com.tx06.entity.RequestParam> responseParams = requestParams.getBodyParams() == null ? new ArrayList<>() : requestParams.getBodyParams();
                    typeToRequestParams(responseParams,parameter.getType(),parameterNames[i],0);
                    requestParams.setBodyParams(responseParams);
                    api.setRequestParams(requestParams);
                }catch (Exception e){
                    log.error(parameter.getType().getSimpleName() + "初始化失败,"+e.getMessage());
                }
            }
        }
    }

    private void typeToRequestParams(List<com.tx06.entity.RequestParam> responseParams,Class<?> type,String fieldName,int index){
        if(index > 5){
            return;
        }
        if(type.isArray()){
            com.tx06.entity.RequestParam requestParam = createRequestParam(type, fieldName);
            // 获取泛型类型
            Type genericParameterType = type;
            if (genericParameterType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length > 0) {
                    Type listGenericType = typeArguments[0];

                    List<com.tx06.entity.RequestParam> childList = new ArrayList<>();
                    typeToRequestParams(childList, (Class<?>) listGenericType,null,index+1);
                    requestParam.setChildList(childList);
                }
            }
        }else{
            if(BeanUtils.isSimpleProperty(type)){//基本类型
                fieldToRequestParam(responseParams,type,fieldName,index);
            }else{
                BaseRequestParamHandle handle = RequestParamHandleFactory.getHandle(type.getName());
                if(handle != null){
                    handle.handleRequestParam(responseParams,type,fieldName,index);
                }else if(type.getName().contains(MappingHandleBuilder.getProp().getServer().getBasePackage())){
                    // 遍历对象属性
                    Field[] fields = type.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        fieldToRequestParam(responseParams,field.getType(),field.getName(),index);
                    }
                }
            }
        }
    }

    private void fieldToRequestParam(List<com.tx06.entity.RequestParam> responseParams,Class<?> fieldType,String fieldName,int index){
        if(fieldType.isArray()){
            // 获取泛型类型
            Type genericParameterType = fieldType;
            if (genericParameterType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length > 0) {
                    Type listGenericType = typeArguments[0];
                    com.tx06.entity.RequestParam requestParam = createRequestParam(fieldType, fieldName);
                    typeToRequestParams(requestParam.getChildList(), (Class<?>) listGenericType,null,index+1);
                }
            }
        }else if(BeanUtils.isSimpleProperty(fieldType) && !isIgnoreField(fieldName)){
            com.tx06.entity.RequestParam requestParam = createRequestParam(fieldType, fieldName);
            responseParams.add(requestParam);
        } else {
            com.tx06.entity.RequestParam requestParam = createRequestParam(fieldType, fieldName);
            typeToRequestParams(requestParam.getChildList(),fieldType,null,index+1);
        }
    }

    // 是否忽略字段
    private boolean isIgnoreField(String fieldName){
        if(fieldName == null){
            return true;
        }
        if("serialVersionUID".contains(fieldName)){
            return true;
        }
        List<String> ignoreFieldList = MappingHandleBuilder.getProp().getServer().getIgnoreFieldList();
        if(ignoreFieldList.contains(fieldName)){
            return true;
        }else{
            return false;
        }
    }



    public static com.tx06.entity.RequestParam createRequestParam(Class<?> type,String fieldName){
        com.tx06.entity.RequestParam requestParam = new com.tx06.entity.RequestParam();
        requestParam.setId(Long.valueOf(RandomUtil.randomNumbers(18)));
        requestParam.setParamType(ApiParamsType.getByType(type));
        requestParam.setName(fieldName);
        requestParam.setIsRequired(1);

        ParamAttr paramAttr = new ParamAttr();
        paramAttr.setId(Long.valueOf(RandomUtil.randomNumbers(18)));
        paramAttr.setApiParamId(requestParam.getId());
        requestParam.setParamAttr(paramAttr);
        return requestParam;
    }


    private JSONObject newInstance(Class c){
        try {
            return JSON.parseObject(JSON.toJSONString(c.newInstance(), WriteMapNullValue));
        } catch (Exception e) {
            log.error(c.getSimpleName() + "初始化失败,"+e.getMessage());
        }
        return new JSONObject();
    }

    /**
     * 设置url
     */
    private void setUrl(RequestMappingInfo info,Api api){
        //设置url
        Set<String> patterns = info.getPatternsCondition().getPatterns();
        String url = patterns.toArray(new String[patterns.size()])[0];
        api.setUri(url);
    }

    private void setTitle(RequestMappingInfo info,Api api){
        String requestMappingName = info.getName();
        api.setName(requestMappingName);
    }

    /**
     * 设置method
     * */
    private void setMethodType(HandlerMethod handlerMethod,Api api){
        boolean hasRequestBody = hasRequestBody(handlerMethod);
        String methodName = "GET";
        if(handlerMethod.getMethodAnnotation(RequestMapping.class) != null){
            RequestMapping requestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
            if(requestMapping.method().length > 0){
                methodName = requestMapping.method()[0].name();
            }else if(hasRequestBody){
                methodName = "POST";
            }else if(parameterIsBasicType(handlerMethod)){
                methodName = "GET";
            }else{
                methodName = "POST";
            }
        }else if(handlerMethod.getMethodAnnotation(PostMapping.class) != null){
            methodName = "POST";
        }else if(handlerMethod.getMethodAnnotation(GetMapping.class) != null){
            methodName = "GET";
        }else if(handlerMethod.getMethodAnnotation(PutMapping.class) != null){
            methodName = "PUT";
        }else if(handlerMethod.getMethodAnnotation(DeleteMapping.class) != null){
            methodName = "DELETE";
        }
        api.getApiAttrInfo().setRequestMethod(RequestMethodEnum.getByName(methodName));
    }

    /**
     * 设置contentType
     * */
    private void setContentType(HandlerMethod handlerMethod,Api api){
        boolean hasRequestBody = hasRequestBody(handlerMethod);
        boolean hasFile = hasMultipartFile(handlerMethod);
        BodyContentTypeEnum bodyContentTypeEnum = BodyContentTypeEnum.FROM_DATA;

        if(hasRequestBody){
            MethodParameter requestBodyParameter = getRequestBodyParameter( handlerMethod);
            if(requestBodyParameter.getParameter().getType().isArray()){
                bodyContentTypeEnum = BodyContentTypeEnum.JSON_ARRAY;
            }else{
                bodyContentTypeEnum = BodyContentTypeEnum.JSON_OBJECT;
            }
        }else if(hasFile){
            bodyContentTypeEnum = BodyContentTypeEnum.BINARY;
        }
        api.getApiAttrInfo().setContentType(bodyContentTypeEnum.getValue());
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

    /**
     * 请求参数是否全部是基本类型和String
     */
    private boolean parameterIsBasicType(HandlerMethod handlerMethod){
        MethodParameter [] parameters = handlerMethod.getMethodParameters();
        boolean isBasicType = true;
        for (int i=0;i<parameters.length;i++){
            MethodParameter methodParameter = parameters[i];
            if(!(ObjectUtil.isBasicType(methodParameter.getParameter().getType())
                    || "String".equals(methodParameter.getParameter().getType().getSimpleName()))){
                isBasicType = false;
                break;
            }
        }
        return isBasicType;
    }

    private MethodParameter getRequestBodyParameter(HandlerMethod handlerMethod){
        MethodParameter [] parameters = handlerMethod.getMethodParameters();
        for (int i=0;i<parameters.length;i++){
            MethodParameter methodParameter = parameters[i];
            RequestBody requestBody = methodParameter.getParameter().getAnnotation(RequestBody.class);
            if(requestBody != null){
                return methodParameter;
            }
        }
        return null;
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
