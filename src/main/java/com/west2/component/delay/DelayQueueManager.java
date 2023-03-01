package com.west2.component.delay;

import com.west2.entity.vo.LiveCutVO;
import com.west2.service.OrderReplayService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;

/**
 * @author
 * @Time 2020/11/23 10:59
 * @description: DelayQueueManager延时任务管理类
 */

@Slf4j
@Component
public class DelayQueueManager implements CommandLineRunner {

    @Autowired
    private OrderReplayService orderReplayService;

    private DelayQueue<DelayTask> delayQueue = new DelayQueue<>();



    /**
     * 加入到延时队列中
     * @param task
     */
    public void put(DelayTask task) {
        log.info("加入延时任务：{}", task);
        delayQueue.put(task);
    }

    /**
     * 取消延时任务
     * @param task
     * @return
     */
    public boolean remove(DelayTask task) {
        log.info("取消延时任务：{}", task);
        return delayQueue.remove(task);
    }

    /**
     * 取消延时任务
     * @param taskid
     * @return
     */
    public boolean remove(String taskid) {
        return remove(new DelayTask(new LiveLeapTask(taskid), 0));
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化延时队列");
        Executors.newSingleThreadExecutor().execute(new Thread(this::excuteThread));
    }

    /**
     * 延时任务执行线程
     */
    private void excuteThread() {
        while (true) {
            try {
                DelayTask task = delayQueue.take();
                processTask(task);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * 内部执行延时任务
     * @param task
     */
    private void processTask(DelayTask task) {
        log.info("执行延时任务：{}", task);
        //根据task中的data自定义数据来处理相关逻辑，例 if (task.getData() instanceof XXX) {}
        //rabbitProducer.sendDemoQueue(task.getData());
        LiveLeapTask data = task.getData();
        if (data != null) {
            LiveCutVO vo = new LiveCutVO();
            vo.setOrderId(data.getOrderId())
                .setTitle(data.getTitle())
                    .setType(data.getType());
            orderReplayService.saveCuts(vo);
        }

    }




}

