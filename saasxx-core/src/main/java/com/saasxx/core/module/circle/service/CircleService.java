package com.saasxx.core.module.circle.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.saasxx.core.module.account.dao.UserRepository;
import com.saasxx.core.module.account.schema.PUser;
import com.saasxx.core.module.account.service.UserService;
import com.saasxx.core.module.account.vo.VUser;
import com.saasxx.core.module.circle.constant.CircleStatus;
import com.saasxx.core.module.circle.dao.CircleRepository;
import com.saasxx.core.module.circle.dao.HobbyRepository;
import com.saasxx.core.module.circle.schema.PCircle;
import com.saasxx.core.module.circle.schema.PHobby;
import com.saasxx.core.module.circle.vo.VCircle;
import com.saasxx.core.module.circle.vo.VHobby;
import com.saasxx.core.module.common.dao.AreaRepository;
import com.saasxx.core.module.common.dao.FileRepository;
import com.saasxx.core.module.common.schema.PAddress;
import com.saasxx.core.module.common.schema.PFile;
import com.saasxx.core.module.common.schema.PText;
import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.Finders;
import com.saasxx.framework.dao.finder.facade.Finder;
import com.saasxx.framework.data.Beans;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.security.shiro.ShiroUser;
import com.saasxx.framework.security.shiro.Shiros;

@Service
@Transactional
public class CircleService {

	private static Log log = Logs.getLog();

	@PersistenceContext
	EntityManager em;
	@Autowired
	UserRepository userRepository;
	@Autowired
	CircleRepository circleRepository;
	@Autowired
	FileRepository fileRepository;
	@Autowired
	HobbyRepository hobbyRepository;
	@Autowired
	AreaRepository areaRepository;
	@Autowired
	UserService userService;

	public Object saveCircle(VCircle vCircle) {
		// 设置基本信息
		PCircle pCircle = new PCircle();
		pCircle.setName(vCircle.getName());
		pCircle.setKeywords(vCircle.getKeywords());
		// 设置总部地址
		PAddress headquarter = new PAddress();
		headquarter.setAddress(vCircle.getHeadquarter().getAddress());
		headquarter.setZipCode(vCircle.getHeadquarter().getRegionCode());
		headquarter.setRegion(areaRepository.findByCode(vCircle.getHeadquarter().getRegionCode()));
		em.persist(headquarter);
		pCircle.setHeadquarter(headquarter);
		// 设置描述信息
		PText description = new PText();
		description.setText(vCircle.getDescription());
		description.setNext(description);
		em.persist(description);
		pCircle.setDescription(description);
		// 设置照片集
		if (vCircle.getAvatars() != null) {
			pCircle.setAvatars(Lang.newList());
			for (String avatar : vCircle.getAvatars()) {
				PFile file = new PFile();
				file.setPath(avatar);
				fileRepository.save(file);
				pCircle.getAvatars().add(file);
			}
		}
		// 设置个人喜好
		if (vCircle.getHobbies() != null) {
			pCircle.setHobbies(Lang.newList());
			for (VHobby vHobby : vCircle.getHobbies()) {
				if (vHobby.isValue()) {
					PHobby pHobby = hobbyRepository.findOne(vHobby.getId());
					pCircle.getHobbies().add(pHobby);
				}
			}
		}
		// 设置用户信息
		ShiroUser shiroUser = Shiros.currentUser();
		Assert.notNull(shiroUser, "用户尚未登录，请先登录");
		PUser pUser = userRepository.findByTel((String) shiroUser.getAttributes().get("tel"));
		Assert.notNull(pUser, "用户登录失效，请重新登录");
		pCircle.setAdmin(pUser);
		pCircle.setCreator(pUser);
		// 设置圈子状态
		pCircle.setStatus(CircleStatus.registered);
		// 保存数据
		em.persist(pCircle);
		vCircle.setId(pCircle.getId());
		return vCircle;
	}

	/**
	 * 激活圈子
	 * 
	 * @param vCircle
	 * @return
	 */
	public Object activeCircle(VCircle vCircle) {
		PCircle pCircle = circleRepository.findOne(vCircle.getId());
		Assert.notNull(pCircle, "圈子不存在，请检查后重试");
		if (pCircle.getUsers() == null) {
			pCircle.setUsers(Lang.newList());
		}
		for (VUser vUser : vCircle.getUsers()) {
			PUser pUser = userRepository.findByTel(vUser.getTel());
			if (pUser == null) {
				userService.saveUser(vUser);
				pUser = userRepository.findByTel(vUser.getTel());
			}
			pCircle.getUsers().add(pUser);
		}
		pCircle.setStatus(CircleStatus.activated);
		em.persist(pCircle);
		return vCircle;
	}

	public List<VCircle> findCircles(VCircle example) {
		Finder finder = Finders.newFinder();
		PCircle modelCircle = finder.from(PCircle.class);
		finder.select(modelCircle);

		if (example.isOwn()) {
			ShiroUser shiroUser = Shiros.currentUser();
			Assert.notNull(shiroUser, "用户尚未登录，请先登录");
			String tel = (String) shiroUser.getAttributes().get("tel");
			PUser joinModelUser = finder.join(modelCircle.getUsers()).inner();
			finder.on(joinModelUser).get(joinModelUser.getTel()).equal(tel);
		}

		List<PCircle> pCircles = (List<PCircle>) finder.list(em);
		List<VCircle> vCircles = Lang.newList();
		for (PCircle pCircle : pCircles) {
			VCircle vCircle = new VCircle();
			vCircles.add(vCircle);
			toVCircle(vCircle, pCircle);
		}
		return vCircles;
	}

	private void toVCircle(VCircle vCircle, PCircle pCircle) {
		Beans.from(pCircle).excludes("avatars", "users", "hobbies").to(vCircle);
		if (pCircle.getDescription() != null) {
			vCircle.setDescription(pCircle.getDescription().toFullString());
		}
		vCircle.setBirthday(Lang.toString(pCircle.getDateCreated(), "yyyy-MM-dd"));
		// 设置照片
		List<String> avators = Lang.newList();
		for (PFile pFile : pCircle.getAvatars()) {
			avators.add(pFile.getPath());
		}
		vCircle.setAvatars(avators);
		// 判断是否是属于自己的圈子
		ShiroUser shiroUser = Shiros.currentUser();
		if (shiroUser != null) {
			String tel = (String) shiroUser.getAttributes().get("tel");
			for (PUser pUser : pCircle.getUsers()) {
				if (pUser.getTel().equals(tel)) {
					vCircle.setOwn(true);
					break;
				}
			}
		}
	}

	public Object joinCircle(VCircle vCircle) {
		PCircle pCircle = circleRepository.findOne(vCircle.getId());
		Assert.notNull(pCircle, "该圈子不存在，请检查后再操作");
		// 设置用户信息
		ShiroUser shiroUser = Shiros.currentUser();
		Assert.notNull(shiroUser, "用户尚未登录，请先登录");
		PUser pUser = userRepository.findByTel((String) shiroUser.getAttributes().get("tel"));
		Assert.notNull(pUser, "用户登录失效，请重新登录");
		if (pCircle.getUsers() == null) {
			pCircle.setUsers(Lang.newList());
		}
		pCircle.getUsers().add(pUser);
		pUser.getCircles().add(pCircle);
		em.persist(pUser);
		em.persist(pCircle);
		vCircle.setOwn(true);
		return vCircle;
	}

}
