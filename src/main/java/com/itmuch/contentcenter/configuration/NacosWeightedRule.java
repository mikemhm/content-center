package com.itmuch.contentcenter.configuration;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServer;

@Slf4j
public class NacosWeightedRule extends AbstractLoadBalancerRule {
    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        //读取配置文件，并初始化
    }

    @Override
    public Server choose(Object o) {
        try {
            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
            //想要请求微服务的名称
            String name = loadBalancer.getName();
            //实现负载均衡算法
            //通过nacos 拿到服务发现的相关api
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            //nacos 自动通过基于权重的负载均衡算法，选择一个实例
            Instance instance = namingService.selectOneHealthyInstance(name);
            log.info("使用的实例是：{}",instance);
            return new NacosServer(instance);

        } catch (NacosException e) {
            e.printStackTrace();
        }
        return null;
    }
}
