package com.tx06.exception;

import com.tx06.entity.TMessage;
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
public class MessageConsumeQueue {
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumeQueue.class);

    @Autowired
    SenderServiceImpl senderService;

    @PostConstruct
    public void startThread() {
        ExecutorService e = Executors.newFixedThreadPool(2);// 两个大小的固定线程池
        e.submit(new PollMessage(senderService));
        e.submit(new PollMessage(senderService));
    }

    static class PollMessage implements Runnable {
        SenderServiceImpl senderService;

        public PollMessage(SenderServiceImpl senderService) {
            this.senderService = senderService;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    TMessage message = MessageQueue.getMessageQueue().consume();
                    if (message != null) {
                        logger.info("剩余待发送异常总数:{}", MessageQueue.getMessageQueue().size());
                        //可以设置延时 以及重复校验等等操作
                        senderService.sendMessage(message);
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