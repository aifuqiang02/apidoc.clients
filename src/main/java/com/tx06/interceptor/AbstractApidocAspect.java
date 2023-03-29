package com.tx06.interceptor;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tx06.config.ApiDocProp;
import com.tx06.entity.Apidoc;
import com.tx06.request.SenderServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.RequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;


public abstract class AbstractApidocAspect {
    private Boolean run;
    protected RestController restController;
    protected HttpServletRequest request;
    protected Method method;
    protected ProceedingJoinPoint proceedingJoinPoint;
    protected String className = "";
    protected String fullTitle = "";
    protected String title = "";
    protected String u_project_uuid = "";
    private Apidoc apidoc = new Apidoc();
    protected Log log = LogFactory.getLog(AbstractApidocAspect.class);
    protected String webSiteBasePath;
    protected String webSiteUrl;
    public static JdbcTemplate jdbcTemplate;
    protected static String dbName;
    protected String requestMethod;
    protected ApiDocProp apiDocProp;
    /**
     * 1、判断接口文档是否更新
     *    配置文件run == true  运行。
     *            run  == false 不运行。
     *            run 不配置，linux 环境不运行，其他系统运行
     * */
    protected boolean isRun(){
        if(run != null){
            return run;
        }else{
            run = getApiDocProp().getServer().getRun();
        }
        return run;
    }

    protected abstract String getMethodName();

    /**
     * 2、设置接口名称
     * */
    public void setFullTitleName(){
        this.title = this.getMethodName();
        if(StrUtil.isEmpty(title)){
            this.fullTitle = null;
            return;
        }
        if(request.getMethod() == null){
            this.fullTitle = null;
            return;
        }
        this.restController = proceedingJoinPoint.getTarget().getClass().getAnnotation(RestController.class);
        if(this.restController == null){
            this.fullTitle = null;
            return;
        }
        String cv = this.restController.value();
        if(StrUtil.isEmpty(cv)){
            this.fullTitle = null;
            return;
        }
        this.fullTitle = (cv.endsWith("/") ? cv : cv + "/") + (title.startsWith("/") ? title.substring(1):title);
    }

    protected boolean checkCanSend(){
        if(this.fullTitle == null){
            return false;
        }
        if(this.request == null){
            return false;
        }
        return true;
    }

    /**
     * 3、初始化前置变量
     * */
    protected void initBefore() throws SQLException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        this.request = attributes.getRequest();
        this.method = methodSignature.getMethod();
        this.webSiteUrl = getUrl(this.method,request);
    }

    //内置
    public void sendApidoc(Object response) throws SQLException, IOException {
        try {
            if (this.webSiteUrl.contains("apidoc/add") || request.getMethod() == null) {
                return;
            }
            String urlParam = request.getQueryString();
            apidoc = new Apidoc();
            apidoc.setProjectUuid(getApiDocProp().getServer().getUuid());
            apidoc.setTitle(title);
            apidoc.setFullTitle(this.fullTitle);
            apidoc.setUrl(this.webSiteUrl);
            apidoc.setMethod(this.requestMethod);
            apidoc.setContentType(request.getContentType() == null ? "application/x-www-form-urlencoded" : request.getContentType());
            apidoc.setUrlParameter(urlParam);
            apidoc.setParameterExamples(getShortMap(request, method));
            apidoc.setConfirmed("1");
            if (response instanceof String && response.toString().startsWith("[")) {
                apidoc.setResponseExamples(lessenArray(JSONArray.parseObject(JSONArray.toJSONString(response, SerializerFeature.WriteMapNullValue))));
            } else if (response instanceof String && response.toString().startsWith("{")) {
                apidoc.setResponseExamples(lessenArray(JSONObject.parseObject(JSONObject.toJSONString(response, SerializerFeature.WriteMapNullValue))));
            } else {
                apidoc.setResponseExamples(lessenArray(JSONObject.parseObject(JSONObject.toJSONString(response, SerializerFeature.WriteMapNullValue))));
            }

            SpringUtil.getBean(SenderServiceImpl.class).send(apidoc);
        }catch (Exception e){
            log.error(ExceptionUtil.stacktraceToString(e));
        }
    }

    public ApiDocProp getApiDocProp() {
        if(apiDocProp == null){
            apiDocProp = SpringUtil.getBean(ApiDocProp.class);
        }
        return apiDocProp;
    }

    //内置
    private String lessenArray(JSONObject jsonObj) {
        Iterator it = jsonObj.keySet().iterator();
        while (it.hasNext()) {
            Object k = it.next();
            if (!(k instanceof String)) {
                continue;
            }
            Object val = jsonObj.get(k);
            if (val instanceof JSONArray && val != null && ((JSONArray) val).size() > 1) {
                JSONArray arr = new JSONArray();
                arr.add(((JSONArray) val).get(0));
                jsonObj.put((String) k, arr);
            } else if (val instanceof JSONObject) {
                lessenArray((JSONObject) val);
            } else {
                jsonObj.put((String) k, val);
            }
        }
        return JSONObject.toJSONString(jsonObj, SerializerFeature.WriteMapNullValue);
    }

    //内置
    public static String getBasePath(HttpServletRequest request) {
        return request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length()) + "/";
    };

    //内置
    public String getShortMap(HttpServletRequest req, Method method) throws IOException {
        String methodStr = req.getMethod().toLowerCase();
        if ("get".equals(methodStr)) {
            return "";
        } else {
            if ("post".equals(methodStr) && req instanceof RequestWrapper) {
                String requestParam = getBodyString(req);
                return requestParam;
            }else if("post".equals(methodStr) && req.getClass().getName().contains("RepeatedlyRequestWrapper") &&  this.apidoc.getContentType()!=null && this.apidoc.getContentType().toLowerCase().contains("application/json")){
                String requestParam = getBodyString(req);
                return requestParam;
            } else {
                Map<String, Object> params = new HashMap<String, Object>();
                Map<String, String[]> tmp = req.getParameterMap();
                if (tmp != null) {
                    for (String key : tmp.keySet()) {
                        if ("_".equals(key) || "callback".equals(key)) {
                            continue;
                        }
                        String[] values = tmp.get(key);
                        if (values.length == 1 && values[0].trim().length() > 10000) {
                            continue;
                        }
                        params.put(key, values.length == 1 ? values[0].trim() : values);
                    }
                }
                return JSON.toJSONString(params, SerializerFeature.WriteMapNullValue);
            }
        }
    }

    //内置
    private static String getBodyString(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString().trim();
    }

    public String getUrl(Method method,HttpServletRequest request) {
        Annotation[] annotations = method.getAnnotations();
        String val = "";
        for(int i=0;i<annotations.length;i++){
            Annotation annotation = annotations[i];
            try {
                if(annotation instanceof GetMapping){
                    val = ((GetMapping)annotation).value()[0];
                }else if(annotation instanceof PostMapping){
                    val = ((PostMapping)annotation).value()[0];
                }else if(annotation instanceof RequestMapping){
                    val = ((RequestMapping)annotation).value()[0];
                }else if(annotation instanceof DeleteMapping){
                    val = ((DeleteMapping)annotation).value()[0];
                }else if(annotation instanceof PutMapping){
                    val = ((PutMapping)annotation).value()[0];
                }
            }catch (Exception e){
            }
        }
        String url = "";
        if(!val.contains("{")){
            String basepath = getBasePath(request);
            url = request.getRequestURL().toString().substring(basepath.length());
        }else {
            RequestMappingHandlerMapping mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
            // 拿到Handler适配器中的所有方法
            Map<RequestMappingInfo, HandlerMethod> methodMap = mapping.getHandlerMethods();
            for (RequestMappingInfo info : methodMap.keySet()) {
                Method handlerMethod = methodMap.get(info).getMethod();
                if (handlerMethod.equals(method)) {
                    Set<String> patterns = info.getPatternsCondition().getPatterns();
                    url = patterns.toArray(new String[patterns.size()])[0];
                    break;
                }
            }
        }
        while (url.startsWith("/")) {
            url = url.substring(1);
        }
        return url;
    }

}
