package com.itmuch.contentcenter.feignClient;

import com.itmuch.contentcenter.domain.dto.content.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-center")
public interface TestUserCenterFeignClient {
    @GetMapping("/a")
    public UserDTO getUser(UserDTO userDTO);

}
