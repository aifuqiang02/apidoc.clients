package com.tx06.exception;

import com.tx06.entity.ExceptionLog;
import com.tx06.request.SenderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 消费队列
 * 创建者 科帮网
 * 创建时间    2017年8月4日
 */
@Component
public class ExceptionConsumeQueue {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionConsumeQueue.class);

    @Autowired
    SenderServiceImpl senderService;

    @PostConstruct
    public void startThread() {
        ExecutorService e = Executors.newFixedThreadPool(2);// 两个大小的固定线程池
        e.submit(new PollException(senderService));
        e.submit(new PollException(senderService));
    }

    class PollException implements Runnable {
        SenderServiceImpl senderService;

        public PollException(SenderServiceImpl senderService) {
            this.senderService = senderService;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    ExceptionLog exceptionLog = ExceptionQueue.getExceptionQueue().consume();
                    if (exceptionLog != null) {
                        logger.info("剩余待发送异常总数:{}",ExceptionQueue.getExceptionQueue().size());
                        //可以设置延时 以及重复校验等等操作
                        senderService.sendExceptionLog(exceptionLog);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @PreDestroy
    public void stopThread() {
        logger.info("destroy");
    }
}