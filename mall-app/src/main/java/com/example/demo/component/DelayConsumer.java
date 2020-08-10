package com.example.demo.component;

import com.example.demo.enums.OrderStatusEnum;
import com.example.demo.service.OrderMasterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = DelayMessage.TOPIC + "-group", topic = DelayMessage.TOPIC)
public class DelayConsumer implements RocketMQListener<DelayMessage> {

    @Autowired
    private OrderMasterService orderMasterService;

    @Transactional
    @Override
    public void onMessage(DelayMessage delayMessage) {
        Long id = delayMessage.getOrderId();
        try {
            // 未支付，取消订单
            if (orderMasterService.getById(id).getStatus().equals(OrderStatusEnum.TO_BE_PAID.getCode())) {
                orderMasterService.addStockMySQL(id); // MYSQL
                orderMasterService.addStockRedis(id); // REDIS
                orderMasterService.updateOrderStatus(id, OrderStatusEnum.CANCEL.getCode());
            }
            log.info("取消订单成功");
        } catch (Exception e) {
            log.info("取消订单失败");
        }
    }
}