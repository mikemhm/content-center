package com.itmuch.contentcenter.service.content;

import com.itmuch.contentcenter.dao.share.RocketmqTransactionLogMapper;
import com.itmuch.contentcenter.dao.share.ShareMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.dto.content.UserDTO;
import com.itmuch.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.itmuch.contentcenter.domain.entity.share.RocketmqTransactionLog;
import com.itmuch.contentcenter.domain.entity.share.Share;
import com.itmuch.contentcenter.domain.enums.AuditstatusEnum;
import com.itmuch.contentcenter.feignClient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {

    private final ShareMapper shareMapper;
    private final UserCenterFeignClient userCenterFeignClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    public ShareDTO findById(Integer id){
        Share share = shareMapper.selectByPrimaryKey(id);
        Integer userId = share.getUserId();
//        User user = this.restTemplate.getForObject("http://user-center/users/{userId}", User.class, userId);
        UserDTO userDTO = this.userCenterFeignClient.findById(userId);
        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share,shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            int a = ThreadLocalRandom.current().nextInt(5);
            System.out.println(a);
        }
    }


    public Share  auditById(Integer id, ShareAuditDTO shareAuditDTO) {
        //逻辑判断,id是否存在，状态是否待审核
        Share share = this.shareMapper.selectByPrimaryKey(id);
        if (share == null) {
            throw new IllegalArgumentException("参数非法，该分享不存在！");
        }
        if (!Objects.equals("NOT_YET",share.getAuditStatus())){
            throw new IllegalArgumentException("参数非法，该分享已审核");
        }

        //审核，通过/不通过
        auditByIdInDb(id,shareAuditDTO);
        //通过，加积分 mq
//        this.rocketMQTemplate.convertAndSend("add-bonus",
//                UserAddBonusMsgDTO.builder()
//                        .userId(share.getUserId())
//                        .bonus(50)
//                        .build());
        //发送半消息
        if (AuditstatusEnum.PASS.equals(shareAuditDTO.getAuditStatusEnumi())){
            String transactionId  = UUID.randomUUID().toString();
            rocketMQTemplate.sendMessageInTransaction("tx-add-bonus-group",
                    "add-bonus", MessageBuilder.withPayload(
                            UserAddBonusMsgDTO.builder()
                        .userId(share.getUserId())
                        .bonus(50)
                        .build()
                    )
                            //header 有妙用
                            .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                            .setHeader("share_id",id)
                            .build(),
                    // arg有大用处
                    shareAuditDTO);
        }else {
            this.auditByIdInDb(id,shareAuditDTO);
        }
        return share;
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdInDb(Integer id ,ShareAuditDTO shareAuditDTO) {
        Share share = Share.builder()
                .id(id)
                .auditStatus(shareAuditDTO.getAuditStatusEnumi().toString())
                .reason(shareAuditDTO.getReason())
                .build();
        share.setAuditStatus(shareAuditDTO.getAuditStatusEnumi().toString());
        this.shareMapper.updateByPrimaryKeySelective(share);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdWithRocketMqLog(Integer id,ShareAuditDTO auditDTO,String transactionId){
        this.auditByIdInDb(id,auditDTO);
        this.rocketmqTransactionLogMapper.insertSelective(RocketmqTransactionLog.builder()
                .transactionId(transactionId)
                .log("审核分享")
                .build());
    }
}
