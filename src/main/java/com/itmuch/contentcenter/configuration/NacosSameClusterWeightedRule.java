package com.itmuch.contentcenter.configuration;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServer;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class NacosSameClusterWeightedRule extends AbstractLoadBalancerRule {
    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object key) {
        try {
            //拿到配置文件中的集群名称 Bj
            String clusterName = nacosDiscoveryProperties.getClusterName();
            Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();

            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
            //想要请求微服务的名称
            String name = loadBalancer.getName();
            //实现负载均衡算法
            //通过nacos 拿到服务发现的相关api
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            //1、找到指定服务的所有实例 A
            List<Instance> instances = namingService.selectInstances(name, true);
            List<Instance> collect = instances.stream().filter(instance -> Objects.equals(instance.getMetadata().get("bate"), metadata.get("bate"))).collect(Collectors.toList());
            //2、过滤出相同集群下的所有实例 B
            List<Instance> sameClusterInstances = collect.stream().filter(instance -> Objects.equals(instance.getClusterName(), clusterName)).collect(Collectors.toList());
            //3、如果B是空，则用 A
            List<Instance> instancesToBeChosen = new ArrayList<>();
            if (CollectionUtils.isEmpty(sameClusterInstances)){
                instancesToBeChosen = instances;
                log.warn("发生跨集群调用，name={};clusterName={};instances{}",
                        name,clusterName,instances);
            }else {
                instancesToBeChosen = sameClusterInstances;
            }
            //4、基于权重的负载均衡算法 ，返回一个实例
            Instance instance = ExtendBalancer.getHostByRandomWeight2(instancesToBeChosen);
            log.info("选择的实例是：port={},instance{}",instance.getPort(),instance);
            return new NacosServer(instance);
        } catch (NacosException e) {
            log.error("发生异常：{}",e);
        }
        return null;
    }
}

//通过nacos负载均衡源码，使用别人的代码
class ExtendBalancer extends Balancer {
    public static Instance getHostByRandomWeight2(List<Instance> hosts) {
        return getHostByRandomWeight(hosts);
    }
}
