package com.itmuch.contentcenter.controller;

import com.itmuch.contentcenter.dao.share.ShareMapper;
import com.itmuch.contentcenter.domain.entity.share.Share;
import com.itmuch.contentcenter.rocketmq.MySource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestController {

    private final ShareMapper shareMapper;
    private final DiscoveryClient discoveryClient;
    private final Source source;
    private final MySource mySource;

    @GetMapping("/test")
    public List<Share> testInsert(){
        Share share = new Share();
        share.setTitle("标题");
        share.setCreateTime(new Date());
        share.setUpdateTime(new Date());
        share.setCover("xxx");
        share.setAuthor("大明");
        share.setBuyCount(2);
        this.shareMapper.insertSelective(share);
        List<Share> shares = this.shareMapper.selectAll();
        return shares;
    }

    @GetMapping("/test2")
    public Object test2(){
    //        List<ServiceInstance> instances = this.discoveryClient.getInstances("user-center");
        List<String> services = this.discoveryClient.getServices();
        return services;
    }


    @GetMapping("/getName")
    public String getName(){
        this.source.output()
                .send(MessageBuilder.withPayload("我是消息").build());
        return "success";
    }

    @GetMapping("/getName2")
    public String getName2(){
        this.mySource.output()
                .send(MessageBuilder.withPayload("我是消息content,mySource").build());
        return "success2";
    }
}
