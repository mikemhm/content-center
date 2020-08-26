package com.itmuch.contentcenter.domain.dto.content;

import com.itmuch.contentcenter.domain.enums.AuditstatusEnum;
import lombok.Data;

@Data
public class ShareAuditDTO {
    /** 审核状态 */
    private AuditstatusEnum auditStatusEnumi;
    /** 说明 */
    private String reason;
}
