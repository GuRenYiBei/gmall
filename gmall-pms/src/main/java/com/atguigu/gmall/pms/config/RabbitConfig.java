package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/15/21:00
 * @Description:
 *
 ******************************************/
@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        this.rabbitTemplate.setConfirmCallback((correlationData,ack,cause)->{
            if (!ack) {
                log.error("消息没有到达交换机" + cause);
            }
        });
        this.rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey)->{
            log.error("消息没有到达交换机，交换机{}，路由键{}，消息内容{}",exchange,routingKey,new String(message.getBody()));
        });
    }
}
