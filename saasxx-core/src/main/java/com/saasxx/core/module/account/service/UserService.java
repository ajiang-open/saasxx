package com.saasxx.core.module.account.service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.saasxx.core.module.account.constant.UserGender;
import com.saasxx.core.module.account.constant.UserStatus;
import com.saasxx.core.module.account.dao.PreUserRepository;
import com.saasxx.core.module.account.dao.UserRepository;
import com.saasxx.core.module.account.schema.PPreUser;
import com.saasxx.core.module.account.schema.PUser;
import com.saasxx.core.module.account.vo.VPreUser;
import com.saasxx.core.module.account.vo.VUser;
import com.saasxx.core.module.common.dao.FileRepository;
import com.saasxx.core.module.common.schema.PFile;
import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Beans;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.security.Passwords;

/**
 * Created by lujijiang on 16/6/11.
 */
@Service
@Transactional
public class UserService {
	/**
	 * 短信验证码过期时间限制30分钟
	 */
	public static final int SMS_VALIDATE_TIME_LIMIT = 30 * 60 * 1000;

	private static Log log = Logs.getLog();

	@Autowired
	UserRepository userRepository;
	@Autowired
	PreUserRepository preUserRepository;
	@Autowired
	FileRepository fileRepository;

	public void saveUser(VUser vUser) {
		PUser pUser = userRepository.findByTel(vUser.getTel());
		if (pUser == null) {
			pUser = new PUser();
			pUser.setTel(vUser.getTel());
			// 设置临时值
			pUser.setEmail(UUID.randomUUID().toString().concat("@saasxx.com"));
			pUser.setGender(UserGender.male);
			pUser.setStatus(UserStatus.registered);
		}
		pUser.setValidateCode(vUser.getValidateCode());
		pUser.setValidateCodeCreatedTime(new Date());
		userRepository.save(pUser);
		vUser.setId(pUser.getId());
	}

	public String checkValidateCode(VUser vUser) {
		PUser pUser = userRepository.findByTel(vUser.getTel());
		if (pUser == null) {
			throw Lang.newException("用户%s不存在", vUser.getTel());
		}
		if (pUser.getValidateCode() == null) {
			throw Lang.newException("短信验证码无效");
		}
		if (!Lang.equals(vUser.getValidateCode(), pUser.getValidateCode())) {
			throw Lang.newException("短信验证码不正确");
		}
		if (System.currentTimeMillis() - pUser.getValidateCodeCreatedTime().getTime() > SMS_VALIDATE_TIME_LIMIT) {
			throw Lang.newException("短信验证码已过期");
		}
		return pUser.getId();
	}

	/**
	 * 检查注册邮箱是否可用
	 * 
	 * @param value
	 */
	public void checkSignUpEmail(String value) {
		PUser pUser = userRepository.findByEmail(value);
		if (pUser != null && UserStatus.activated.equals(pUser.getStatus())) {
			throw Lang.newException("该邮箱已被注册");
		}
	}

	/**
	 * 注册用户
	 * 
	 * @param vUser
	 * @throws UnsupportedEncodingException
	 */
	public void signup(VUser vUser) {
		PUser pUser = userRepository.findOne(vUser.getId());
		Assert.notNull(pUser, "用户不存在，请重新注册");
		Beans.from(vUser).excludes("tel", "avatars", "hobbies", "password").to(pUser);
		// 设置状态
		pUser.setStatus(UserStatus.activated);
		// 加密密码
		String salt = Passwords.generateSalt();
		pUser.setSalt(salt);
		String encryptPassword = Passwords.encryptPassword(vUser.getPassword(), salt);
		pUser.setPassword(encryptPassword);
		// 设置照片集
		if (vUser.getAvatars() != null) {
			pUser.setAvatars(Lang.newList());
			for (String avatar : vUser.getAvatars()) {
				PFile file = new PFile();
				file.setPath(avatar);
				fileRepository.save(file);
				pUser.getAvatars().add(file);
			}
		}
		userRepository.save(pUser);
	}

	/**
	 * 登录方法
	 * 
	 * @param vUser
	 * @throws UnsupportedEncodingException
	 */
	public void signin(VUser vUser) {
		PUser pUser = userRepository.findByTel(vUser.getTel());
		if (pUser == null) {
			throw Lang.newException("用户%s不存在", vUser.getTel());
		}
		if (!UserStatus.activated.equals(pUser.getStatus())) {
			throw Lang.newException("用户%s的状态不正确", vUser.getTel());
		}
		if (!Lang.equals(Passwords.encryptPassword(vUser.getPassword(), pUser.getSalt()), pUser.getPassword())) {
			throw Lang.newException("用户%s的密码不正确", vUser.getTel());
		}
		// 登录逻辑
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername(pUser.getTel());
		token.setPassword(vUser.getPassword().toCharArray());
		SecurityUtils.getSubject().login(token);
	}

	public void signupPreUser(VPreUser preUser) {
		PUser pUser = userRepository.findByEmail(preUser.getEmail());
		if (pUser == null) {
			pUser = new PUser();
		}
		pUser.setEmail(preUser.getEmail());
		pUser.setTel(preUser.getTel());
		pUser.setRealName(preUser.getRealName());
		// 设置临时值
		pUser.setGender(UserGender.male);
		pUser.setStatus(UserStatus.registered);
		userRepository.save(pUser);
		// 保存预设电话
		PPreUser pPreUser = new PPreUser();
		pPreUser.setUser(pUser);
		pPreUser.setAdvice(preUser.getAdvice());
		preUserRepository.save(pPreUser);
	}

}
