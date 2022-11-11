##哪些项目可以使用
```
  1、项目是springboot 项目。
     
```
##引用
```
1、pom.xml 中增加配置
<dependency>
  <groupId>com.tx06</groupId>
  <artifactId>apidoc.clients</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>

2、application.yml 增加以下配置
tx06: 
  server: 
    basePath: http://127.0.0.1/x-apidoc/   #可不配置,默认https://a.tx06.com/
    uuid: 9d43d986-0922-41c4-9d46-d5b1a3547983  #项目uuid , 在https://a.tx06.com/系统中创建
    dictPath: system/dict/type/dataDict  #字典接口地址  
    
3、使用@RestController("api接口") 注解，并增加中文说明  

4、方法映射使用以下3个，且增加name1
    @RequestMapping(value="/add",name="")   
    @GetMapping(value="/add",name="")   
    @PostMapping(value="/add",name="")
```

##其他文档
[环境部署](wiki/环境部署)