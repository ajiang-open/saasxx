package com.saasxx.core.module.security.service;

import java.util.Collection;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saasxx.core.module.account.constant.UserStatus;
import com.saasxx.core.module.account.dao.UserRepository;
import com.saasxx.core.module.account.schema.PUser;
import com.saasxx.framework.Lang;
import com.saasxx.framework.security.shiro.ShiroService;
import com.saasxx.framework.security.shiro.ShiroUser;

@Service
@Transactional
public class ShiroServiceImpl implements ShiroService {

	@Autowired
	UserRepository userRepository;

	@Override
	public ShiroUser findUser(AuthenticationToken token) {
		if (token instanceof UsernamePasswordToken) {
			UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
			PUser pUser = userRepository.findByTel(usernamePasswordToken.getUsername());
			if (pUser == null) {
				throw Lang.newException("用户%s不存在", usernamePasswordToken.getUsername());
			}
			if (!UserStatus.activated.equals(pUser.getStatus())) {
				throw Lang.newException("用户%s的状态不正确", usernamePasswordToken.getUsername());
			}
			String username = pUser.getRealName();
			Map<String, Object> attributes = Lang.newMap();
			attributes.put("tel", pUser.getTel());
			attributes.put("email", pUser.getEmail());
			attributes.put("gender", pUser.getGender());
			attributes.put("realName", pUser.getRealName());
			return findUser(username, attributes);
		}
		return null;
	}

	@Override
	public ShiroUser findUser(final String username, final Map<String, Object> attributes) {
		return new ShiroUser() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3669437954275565742L;

			{
				setUsername(username);
				setAttributes(attributes);
			}

			@Override
			public boolean isSuperAdmin() {
				return false;
			}
		};
	}

	@Override
	public Collection<String> findRoles(ShiroUser shiroUser) {
		return Lang.newList();
	}

	@Override
	public Collection<String> findPermissions(ShiroUser shiroUser) {
		return Lang.newList();
	}

}
