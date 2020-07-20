package com.itmuch.contentcenter.service.content;

import com.itmuch.contentcenter.dao.share.ShareMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.dto.content.UserDTO;
import com.itmuch.contentcenter.domain.entity.share.Share;
import com.itmuch.contentcenter.feignClient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {

    private final ShareMapper shareMapper;
    private final UserCenterFeignClient userCenterFeignClient;

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
}
