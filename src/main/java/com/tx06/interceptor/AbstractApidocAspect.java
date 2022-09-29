package com.tx06.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tx06.config.Constant;
import com.tx06.config.Prop;
import com.tx06.entity.Apidoc;
import com.tx06.entity.ApidocFieldDict;
import com.tx06.request.Sender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.RequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.sql.Connection;
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
    private Map<String, List<Map>> fieldMap = new HashMap<String, List<Map>>();
    private List<Map<String, Object>> column = new ArrayList<>();
    private Apidoc apidoc = new Apidoc();
    protected Log log = LogFactory.getLog(AbstractApidocAspect.class);
    protected String webSiteBasePath;
    protected String webSiteUrl;
    public static JdbcTemplate jdbcTemplate;
    protected static String dbName;
    protected String requestMethod;
    protected Prop prop;
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
            run = getProp().getServer().getRun();
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

    /**
     * 3、初始化前置变量
     * */
    protected void initBefore() throws SQLException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        this.request = attributes.getRequest();
        this.method = methodSignature.getMethod();
        this.fieldMap = new HashMap<>();
        this.column = new ArrayList<>();
        this.webSiteBasePath = getBasePath(request);
        this.webSiteUrl = getUrl(request);

        if(jdbcTemplate == null){
            jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
            String [] arr = this.jdbcTemplate.getDataSource().getConnection().getMetaData().getURL().split("\\?")[0].split("/");
            dbName = arr[arr.length-1];
            String sql = "SELECT c.`TABLE_NAME` AS table_name,LOWER(REPLACE(c.`COLUMN_NAME`,'_','')) AS column_name,c.`COLUMN_TYPE` AS column_type,c.`COLUMN_COMMENT` AS column_comment FROM `information_schema`.`COLUMNS` c WHERE c.`TABLE_SCHEMA` = '" + dbName
                    + "' order by  c.`TABLE_NAME`";
            column = jdbcTemplate.queryForList(sql);
            if(!StrUtil.isEmpty(getProp().getServer().getBasePath())){
                Constant.BASE_PATH = getProp().getServer().getBasePath();
            }
        }

    }

    //内置
    public void sendApidoc(Object response) throws SQLException, IOException {
        if(this.webSiteUrl.contains("apidoc/add")){
            return;
        }
        String urlParam = request.getQueryString();
        initFieldMap();
        apidoc = new Apidoc();
        apidoc.setU_project_uuid(getProp().getServer().getUuid());
        apidoc.setTitle(title);
        apidoc.setFull_title(this.fullTitle);
        apidoc.setUrl(this.webSiteUrl);
        apidoc.setMethod(this.requestMethod);
        apidoc.setContent_type(request.getContentType() == null ? "application/x-www-form-urlencoded" : request.getContentType());
        apidoc.setUrl_parameter(urlParam);
        apidoc.setParameter_examples(getShortMap(request, method));
        if(response instanceof  String &&  response.toString().startsWith("[")){
            apidoc.setResponse_examples(lessenArray(JSONArray.parseObject(JSONArray.toJSONString(response, SerializerFeature.WriteMapNullValue))));
        }else if(response instanceof  String &&  response.toString().startsWith("{")){
            apidoc.setResponse_examples(lessenArray(JSONObject.parseObject(JSONObject.toJSONString(response, SerializerFeature.WriteMapNullValue))));
        }else{
            apidoc.setResponse_examples(lessenArray(JSONObject.parseObject(JSONObject.toJSONString(response, SerializerFeature.WriteMapNullValue))));
        }

        setParameterList(apidoc);
        setResponseList(apidoc);
        new Sender(apidoc).start();
    }

    public Prop getProp() {
        if(prop == null){
            prop = SpringUtil.getBean(Prop.class);
        }
        return prop;
    }

    //内置
    private void setParameterList(Apidoc apidoc) {
        if (!JSONUtil.isJson(apidoc.getParameter_examples())){
            return;
        }
        JSONObject json = JSONObject.parseObject(apidoc.getParameter_examples());
        setJSONObjectFieldMemo(json, "parameter", 0);
    }

    //内置
    private void setResponseList(Apidoc apidoc) {
        if (!JSONUtil.isJson(apidoc.getResponse_examples())) {
            return;
        }
        JSONObject json = JSONObject.parseObject(apidoc.getResponse_examples());
        setJSONObjectFieldMemo(json, "response", 0);
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
    private String getBasePath(HttpServletRequest request) {
        return request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length()) + "/";
    }

    ;

    //内置
    private void initFieldMap() {
        for (int i = 0; i < column.size(); i++) {
            Map col = column.get(i);
            String column_name = (String) col.get("column_name");
            if (!fieldMap.containsKey(column_name)) {
                fieldMap.put(column_name, new ArrayList<>());
            }
            fieldMap.get(column_name).add(col);
        }
    }

    ;

    //内置
    private void setJSONObjectFieldMemo(JSONObject json, String type, int level) {
        if (json == null) {
            return;
        }
        Iterator keySet = json.keySet().iterator();
        while (keySet.hasNext()) {
            Object key = keySet.next();
            if (!(key instanceof String)) {
                continue;
            }
            Object val = json.get(key);
            if (val instanceof JSONObject) {
                setJSONObjectFieldMemo((JSONObject) val, type, level + 1);
            } else if (val instanceof JSONArray) {
                setJSONArrayFieldMemo((JSONArray) val, type, level);
            } else {
                String lowerKey = ((String) key).toLowerCase().replaceAll("_", "");
                if (fieldMap.containsKey(lowerKey)) {
                    Map column = getColumn(json, lowerKey);
                    if (column == null) {
                        continue;
                    }
                    if (type.endsWith("response")) {
                        addApidocResponse((String) key, (String) column.get("column_comment"), (String) column.get("column_type"), level + key.toString());
                    } else if (type.endsWith("parameter")) {
                        addApidocParameter((String) key, (String) column.get("column_comment"), (String) column.get("column_type"), level + key.toString());
                    }
                } else {
                    if (type.endsWith("response")) {
                        addApidocResponse((String) key, "", "", level + key.toString());
                    } else if (type.endsWith("parameter")) {
                        addApidocParameter((String) key, "", "", level + key.toString());
                    }
                }
            }
        }
    }

    //内置(String)column.get("column_comment")    (String)column.get("column_type")
    private void addApidocResponse(String key, String field_name, String type, String full_key) {
        if ("isFromApidoc".equals(key)) {
            return;
        }
        ApidocFieldDict par = new ApidocFieldDict();
        par.setField(key);
        par.setType(type);
        par.setGlobal("2");
        par.setName(field_name);
        par.setUse_features("1");
        par.setData_type("2");
        par.setFull_key(full_key);
        par.setAuto_insert("1");
        apidoc.getApidocResponse().add(par);
    }

    //内置
    private void addApidocParameter(String key, String field_name, String type, String full_key) {
        if ("isFromApidoc".equals(key)) {
            return;
        }
        ApidocFieldDict par = new ApidocFieldDict();
        par.setField(key);
        par.setType(type);
        par.setGlobal("2");
        par.setName(field_name);
        par.setUse_features("1");
        par.setData_type("1");
        par.setAuto_insert("1");
        par.setFull_key(full_key);
        apidoc.getApidocParameter().add(par);

    }

    //内置
    private Map getColumn(JSONObject json, String lowerKey) {
        Iterator keySet = json.keySet().iterator();
        List<String> keyList = new ArrayList<>();
        while (keySet.hasNext()) {
            Object key = keySet.next();
            if (!(key instanceof String)) {
                continue;
            }
            String k = ((String) key).replaceAll("_", "").toLowerCase();
            keyList.add(k);
        }
        Map<String, List<Map>> tableColumn = new HashMap<>();
        int highestTotal = 0;
        String highestTable = null;
        for (int i = 0; i < column.size(); i++) {
            Map col = column.get(i);
            String table_name = (String) col.get("table_name");
            if (!tableColumn.containsKey(table_name)) {
                tableColumn.put(table_name, new ArrayList());
            }
            tableColumn.get(table_name).add(col);
            if (i == column.size() - 1 || !column.get(i + 1).get("table_name").equals(table_name)) {
                List cols = tableColumn.get(table_name);
                int total = 0;
                for (int j = 0; j < cols.size(); j++) {
                    Map col_ = (Map) cols.get(j);
                    if (keyList.contains(col_.get("column_name"))) {
                        total++;
                    }
                }
                if (total > highestTotal) {
                    highestTotal = total;
                    highestTable = table_name;
                }
            }
        }
        List cols = tableColumn.get(highestTable);
        for (int i = 0; i < cols.size(); i++) {
            if (((Map) cols.get(i)).get("column_name").equals(lowerKey)) {
                return (Map) cols.get(i);
            }
        }
        return null;
    }

    //内置
    private void setJSONArrayFieldMemo(JSONArray json, String type, int level) {
        if (json == null || json.size() < 1) {
            return;
        }
        Object val = json.get(0);
        if (val instanceof JSONObject) {
            setJSONObjectFieldMemo((JSONObject) val, type, level + 1);
        }
    }

    //内置
    public static String getShortMap(HttpServletRequest req, Method method) throws IOException {
        String methodStr = req.getMethod().toLowerCase();
        if ("get".equals(methodStr)) {
            return "";
        } else {
            if ("post".equals(methodStr) && req instanceof RequestWrapper) {
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

    public String getUrl(HttpServletRequest request) {
        String basepath = getBasePath(request);
        String url = request.getRequestURL().toString().substring(basepath.length());
        while (url.startsWith("/")) {
            url = url.substring(1);
        }
        return url;
    }

}
