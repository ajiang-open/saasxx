package com.saasxx.core.module.account.web.webrpc;

import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saasxx.core.module.account.service.UserService;
import com.saasxx.core.module.account.vo.ImageValidateVo;
import com.saasxx.core.module.account.vo.VUser;
import com.saasxx.core.module.account.web.controller.UserController;
import com.saasxx.framework.Lang;
import com.saasxx.framework.web.webrpc.annotation.WebRpc;

import java.util.Random;

@Component
public class UserWebRpc {

    static Log log = Logs.getLog();
    @Autowired
    UserService userService;

    @WebRpc
    public Object sendSmsValidateCode(VUser vUser, ImageValidateVo imageValidateVo) {
        if (!Lang.equals(imageValidateVo.getImageValidateCode(), UserController.CODE_MAP.get(imageValidateVo.getKey()))) {
            throw Lang.newException("图片验证码不正确");
        }
        vUser.setValidateCode(generateCode());
        userService.saveUser(vUser);
        return Lang.newMap();
    }

    private String generateCode() {
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            Random random = new Random();
            codeBuilder.append(random.nextInt(10));
        }
        return codeBuilder.toString();
    }
}
