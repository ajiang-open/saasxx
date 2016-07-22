package com.saasxx.core.module.account.web.webrpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saasxx.core.module.account.service.UserService;
import com.saasxx.core.module.account.vo.VUser;
import com.saasxx.framework.Lang;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.web.webrpc.annotation.WebRpc;

@Component
public class SignUpWebRpc {

	static Log log = Logs.getLog();

	@Autowired
	UserService userService;

	@WebRpc
	public Object step2(VUser vUser) {
		String id = userService.checkValidateCode(vUser);
		return Lang.newMap("id", id);
	}

	@WebRpc
	public Object signup(VUser vUser) {
		userService.signup(vUser);
		return Lang.newMap();
	}

}
