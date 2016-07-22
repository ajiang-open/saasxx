package com.saasxx.core.module.account.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.saasxx.core.module.account.service.UserService;
import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Captchas;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * Created by lujijiang on 16/6/10.
 */
@Controller
@RequestMapping("user")
public class UserController {

	private static Log log = Logs.getLog();

	public final static Map<String, String> CODE_MAP = Collections.synchronizedMap(Lang.newLRUMap(102400));
	@Autowired
	UserService userService;

	@RequestMapping("imageValidateCode")
	public void imageValidateCode(@RequestParam("key") String key, HttpServletResponse response) throws IOException {
		response.setContentType("image/jpeg");
		try (OutputStream os = response.getOutputStream()) {
			String code = Captchas.create().setChars("1234567890").setWidth(90).setHeight(30).setSize(4)
					.setInterferenceCount(32).generate(os);
			CODE_MAP.put(key, code);
		}
	}

	@RequestMapping("checkEmail.do")
	@ResponseBody
	public Object checkEmail(@RequestParam("value") String value) {
		log.info("The sign up email is {}", value);
		userService.checkSignUpEmail(value);
		return Lang.newMap();
	}
}
