package com.saasxx.core.module.account.web.webrpc;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saasxx.core.module.account.service.UserService;
import com.saasxx.core.module.account.vo.VUser;
import com.saasxx.framework.Lang;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.security.shiro.Shiros;
import com.saasxx.framework.security.shiro.jwt.JWTShiroRealm;
import com.saasxx.framework.web.webrpc.annotation.WebRpc;

@Component
public class SignInWebRpc {

    static Log log = Logs.getLog();

    @Autowired
    UserService userService;

    @Autowired
    JWTShiroRealm jwtShiroRealm;

    @WebRpc
    public Object signin(VUser vUser) {
        // 进行登录
        userService.signin(vUser);
        // 回写JWT用户会话令牌信息
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        String jwt = jwtShiroRealm.createJWT(Shiros.currentUser().getUsername(), Shiros.currentUser().getAttributes(),
                calendar.getTime());
        return Lang.newMap("jwt", jwt);
    }

}
