package com.itmuch.contentcenter.controller.content;

import com.itmuch.contentcenter.auth.CheckAuthorization;
import com.itmuch.contentcenter.auth.CheckLogin;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.entity.share.Share;
import com.itmuch.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shares")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareAdminController {

    private final ShareService shareService;

    @GetMapping("/audit/{id}")
    @CheckLogin
    @CheckAuthorization("admin")
    public ShareDTO findById(@PathVariable Integer id){
        return this.shareService.findById(id);
    }

    @PutMapping("/audit/{id}")
    public Share auditById(@PathVariable Integer id,@RequestBody ShareAuditDTO shareAuditDTO){
        Share share = this.shareService.auditById(id, shareAuditDTO);
        return share;
    }
}
