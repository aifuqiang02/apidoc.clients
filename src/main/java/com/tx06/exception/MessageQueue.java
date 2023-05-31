package com.tx06.exception;

import com.tx06.entity.TMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 艾付强
 */
public class MessageQueue {
    //队列大小
    static final int QUEUE_MAX_SIZE   = 1000;

    static BlockingQueue<TMessage> blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    /**
     * 私有的默认构造子，保证外界无法直接实例化
     */
    private MessageQueue(){};
    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例
     * 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder{
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private  static MessageQueue queue = new MessageQueue();
    }
    //单例队列
    public static MessageQueue getMessageQueue(){
        return SingletonHolder.queue;
    }


    //生产入队
    public  void  produce(TMessage message) {
        try {
            blockingQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //消费出队
    public  TMessage consume() throws InterruptedException {
        return blockingQueue.take();
    }
    // 获取队列大小
    public int size() {
        return blockingQueue.size();
    }
}
