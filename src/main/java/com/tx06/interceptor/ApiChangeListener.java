package com.tx06.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ApiChangeListener {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;
    private Future<?> future;

    private static List<String> apiList = new ArrayList<>();

    public void startListening() {
        // 提交监听任务到线程池
        Future<?> future = executor.submit(() -> {
            while (running) {
                try {
                    // 这里放置你的监听逻辑
                    if(apiList.isEmpty()){
                        apiList = getApiList();
                        TimeUnit.SECONDS.sleep(1);
                        continue;
                    }
                    List<String> newApiList = getApiList();
                    if(apiIsChange(newApiList)){
                        apiList = newApiList;
                        TimeUnit.SECONDS.sleep(1);
                        StaticAnalysis staticAnalysis = SpringUtil.getBean(StaticAnalysis.class);
                        staticAnalysis.start();
                        continue;
                    }
                    // 模拟工作，比如每隔一秒检查一次任务状态
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    // 当线程被中断时退出循环
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    System.out.println("监听线程被中断");
                    break;
                }
            }
            System.out.println("监听已停止");
        });

        // 保存 Future 对象以便可以取消任务
        this.future = future;
    }

    private boolean apiIsChange(List<String> newApiList){

        for (String newApi : newApiList) {
            if(!apiList.contains(newApi)){
                return true;
            }
        }
        return false;
    }

    private List<String> getApiList() {
        // 这里放置你的监听逻辑
        RequestMappingHandlerMapping mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
        // 拿到Handler适配器中的所有方法
        Map<RequestMappingInfo, HandlerMethod> methodMap = mapping.getHandlerMethods();
        List apiList = new ArrayList<>();
        for (RequestMappingInfo info : methodMap.keySet()) {
            HandlerMethod handlerMethod = methodMap.get(info);
            RestController restController = handlerMethod.getBeanType().getAnnotation(RestController.class);
            if (StrUtil.isBlank(info.getName()) || restController == null) {
                continue;
            }
            Set<String> patterns = info.getPatternsCondition().getPatterns();
            String url = patterns.toArray(new String[patterns.size()])[0];
            apiList.add(info.getName() + url);
        }
        return apiList;
    }

    public void stopListening() {
        // 设置运行标志为 false 并尝试中断线程
        running = false;
        future.cancel(true); // 尝试停止执行中的任务

        // 关闭线程池
        executor.shutdownNow();

        // 清理资源或进行其他必要的收尾工作
        System.out.println("容器销毁，线程已关闭");
    }

    public static void main(String[] args) throws InterruptedException {
        ApiChangeListener listener = new ApiChangeListener();
        listener.startListening();

        // 模拟容器运行一段时间后销毁
        TimeUnit.SECONDS.sleep(5);

        // 模拟容器销毁
        listener.stopListening();
    }
}
