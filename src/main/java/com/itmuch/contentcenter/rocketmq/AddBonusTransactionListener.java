package com.itmuch.contentcenter.rocketmq;

import com.itmuch.contentcenter.dao.share.RocketmqTransactionLogMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.entity.share.RocketmqTransactionLog;
import com.itmuch.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

@RocketMQTransactionListener(txProducerGroup = "tx-add-bonus-group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddBonusTransactionListener implements RocketMQLocalTransactionListener {
    private final ShareService shareService;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;


    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        MessageHeaders headers = msg.getHeaders();
        String transactionId = headers.get(RocketMQHeaders.TRANSACTION_ID).toString();
        Integer shareId = Integer.valueOf(headers.get("share_id").toString());
        try {
            this.shareService.auditByIdWithRocketMqLog(shareId, (ShareAuditDTO) arg,transactionId);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            e.printStackTrace();
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        MessageHeaders headers = msg.getHeaders();
        String transactionId = headers.get(RocketMQHeaders.TRANSACTION_ID).toString();
        RocketmqTransactionLog transactionLog = rocketmqTransactionLogMapper.selectOne(RocketmqTransactionLog.builder()
                .transactionId(transactionId)
                .build());
        if (transactionLog != null) {
            return RocketMQLocalTransactionState.COMMIT;
        }else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}
