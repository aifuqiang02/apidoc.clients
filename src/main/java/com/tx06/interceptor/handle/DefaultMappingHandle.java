package com.tx06.interceptor.handle;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tx06.config.ApiDocProp;
import com.tx06.entity.Apidoc;
import com.tx06.request.SenderServiceImpl;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class DefaultMappingHandle implements MappingHandle{
  public ProceedingJoinPoint pjp;
  public Method method;
  public Annotation requestMapping;
  public RestController restController;
  public Object response;
  public ApiDocProp prop;

  public HttpServletRequest request;
  public Apidoc apidoc = new Apidoc();

  @Override
  public String getMappingValue() {
    return null;
  }

  @Override
  public void initMethodTitle() {
  }

  @Override
  public void initFullTitle() {
    String classTitle = this.restController.value();
    apidoc.setFullTitle((classTitle.endsWith("/") ? classTitle : classTitle + "/") + (apidoc.getTitle().startsWith("/") ? apidoc.getTitle().substring(1):apidoc.getTitle()));
  }

  @Override
  public void initMethodType() {
  }

  @Override
  public void initUrl() {
     apidoc.setUrl(getUrl());
  }

  @Override
  public void initParameter() throws IOException {
    apidoc.setParameterExamples(getShortMap());
  }

  @Override
  public void initResponse() {
    if (response instanceof String && response.toString().startsWith("[")) {
      apidoc.setResponseExamples(lessenArray(JSONArray.parseObject(JSONArray.toJSONString(response, SerializerFeature.WriteMapNullValue))));
    } else if (response instanceof String && response.toString().startsWith("{")) {
      apidoc.setResponseExamples(lessenArray(JSONObject.parseObject(JSONObject.toJSONString(response, SerializerFeature.WriteMapNullValue))));
    } else {
      apidoc.setResponseExamples(lessenArray(JSONObject.parseObject(JSONObject.toJSONString(response, SerializerFeature.WriteMapNullValue))));
    }
  }

  @Override
  public void initApiDoc() {

  }

  @Override
  public Object sendApi() throws Throwable {
    Object response = pjp.proceed();

    initMethodTitle();
    initFullTitle();
    initMethodType();
    initUrl();
    initParameter();
    initResponse();
    initApiDoc();

    apidoc.setProjectUuid(prop.getServer().getUuid());
    apidoc.setContentType(request.getContentType() == null ? "application/x-www-form-urlencoded" : request.getContentType());
    apidoc.setConfirmed("1");
    SpringUtil.getBean(SenderServiceImpl.class).send(apidoc);
    return response;
  }


  private String getUrl() {
    String val = this.getMappingValue();

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

  //内置
  private static String getBasePath(HttpServletRequest request) {
    return request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length()) + "/";
  };


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
  public String getShortMap() throws IOException {
    String methodStr = request.getMethod().toLowerCase();
    if ("get".equals(methodStr)) {
      return "";
    } else {
      if ("post".equals(methodStr) && request instanceof ServletRequestWrapper) {
        String requestParam = getBodyString(request);
        return requestParam;
      }else if("post".equals(methodStr) && request.getClass().getName().contains("RepeatedlyRequestWrapper") &&  this.apidoc.getContentType()!=null && this.apidoc.getContentType().toLowerCase().contains("application/json")){
        String requestParam = getBodyString(request);
        return requestParam;
      } else {
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, String[]> tmp = request.getParameterMap();
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


}
