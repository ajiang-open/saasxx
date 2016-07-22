package com.saasxx.core.module.meetup.service;

import java.util.Date;
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
import com.saasxx.core.module.circle.dao.CircleRepository;
import com.saasxx.core.module.circle.dao.HobbyRepository;
import com.saasxx.core.module.circle.schema.PCircle;
import com.saasxx.core.module.common.dao.AreaRepository;
import com.saasxx.core.module.common.dao.FileRepository;
import com.saasxx.core.module.common.schema.PAddress;
import com.saasxx.core.module.common.schema.PFile;
import com.saasxx.core.module.common.schema.PText;
import com.saasxx.core.module.meetup.constant.MeetupStatus;
import com.saasxx.core.module.meetup.dao.MeetupRepository;
import com.saasxx.core.module.meetup.schema.PMeetup;
import com.saasxx.core.module.meetup.vo.VMeetup;
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
public class MeetupService {

	private static Log log = Logs.getLog();

	@PersistenceContext
	EntityManager em;

	@Autowired
	MeetupRepository meetupRepository;

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

	public Object saveMeetup(VMeetup vMeetup) {
		// 设置基本信息
		PMeetup pMeetup = new PMeetup();
		pMeetup.setTitle(vMeetup.getTitle());
		pMeetup.setKeywords(vMeetup.getKeywords());
		// 设置总部地址
		PAddress headquarter = new PAddress();
		headquarter.setAddress(vMeetup.getAddress().getAddress());
		headquarter.setZipCode(vMeetup.getAddress().getRegionCode());
		headquarter.setRegion(areaRepository.findByCode(vMeetup.getAddress().getRegionCode()));
		em.persist(headquarter);
		pMeetup.setAddress(headquarter);
		pMeetup.setTime(Lang.convert(vMeetup.getTime(), Date.class));
		// 设置描述信息
		PText description = new PText();
		description.setText(vMeetup.getDescription());
		description.setNext(description);
		em.persist(description);
		pMeetup.setDescription(description);
		// 设置照片集
		if (vMeetup.getAvatars() != null) {
			pMeetup.setAvatars(Lang.newList());
			for (String avatar : vMeetup.getAvatars()) {
				PFile file = new PFile();
				file.setPath(avatar);
				fileRepository.save(file);
				pMeetup.getAvatars().add(file);
			}
		}
		// 设置用户信息
		ShiroUser shiroUser = Shiros.currentUser();
		Assert.notNull(shiroUser, "用户尚未登录，请先登录");
		PUser pUser = userRepository.findByTel((String) shiroUser.getAttributes().get("tel"));
		Assert.notNull(pUser, "用户登录失效，请重新登录");
		pMeetup.setOrganizer(pUser);
		// 设置圈子状态
		pMeetup.setStatus(MeetupStatus.registered);
		// 设置圈子信息
		PCircle pCircle = circleRepository.findOne(vMeetup.getCircle().getId());
		Assert.notNull(pCircle, "圈子信息不正确，请检查");
		pMeetup.setCircle(pCircle);
		// 保存数据
		em.persist(pMeetup);
		vMeetup.setId(pMeetup.getId());
		return vMeetup;
	}

	public Object findMeetups(VMeetup example) {
		Finder finder = Finders.newFinder();
		PMeetup modelMeetup = finder.from(PMeetup.class);
		finder.select(modelMeetup);

		if (example.isOwn()) {
			ShiroUser shiroUser = Shiros.currentUser();
			Assert.notNull(shiroUser, "用户尚未登录，请先登录");
			String tel = (String) shiroUser.getAttributes().get("tel");
			PUser joinModelUser = finder.join(modelMeetup.getUsers()).inner();
			finder.on(joinModelUser).get(joinModelUser.getTel()).equal(tel);
		}

		finder.order(modelMeetup.getTime()).desc();

		List<PMeetup> pMeetups = (List<PMeetup>) finder.list(em);
		List<VMeetup> vMeetups = Lang.newList();
		for (PMeetup pMeetup : pMeetups) {
			VMeetup vMeetup = new VMeetup();
			vMeetups.add(vMeetup);
			toVMeetup(vMeetup, pMeetup);
		}
		return vMeetups;
	}

	private void toVMeetup(VMeetup vMeetup, PMeetup pCircle) {
		Beans.from(pCircle).excludes("avatars", "users", "hobbies").to(vMeetup);
		if (pCircle.getDescription() != null) {
			vMeetup.setDescription(pCircle.getDescription().toFullString());
		}
		vMeetup.setTime(Lang.toString(pCircle.getTime(), "yyyy-MM-dd HH:mm"));
		vMeetup.setBirthday(Lang.toString(pCircle.getDateCreated(), "yyyy-MM-dd"));
		// 设置照片
		List<String> avators = Lang.newList();
		for (PFile pFile : pCircle.getAvatars()) {
			avators.add(pFile.getPath());
		}
		vMeetup.setAvatars(avators);
		// 判断是否是属于自己的圈子
		ShiroUser shiroUser = Shiros.currentUser();
		if (shiroUser != null) {
			String tel = (String) shiroUser.getAttributes().get("tel");
			for (PUser pUser : pCircle.getUsers()) {
				if (pUser.getTel().equals(tel)) {
					vMeetup.setOwn(true);
					break;
				}
			}
		}
	}

	public Object joinMeetup(VMeetup vMeetup) {
		PMeetup pMeetup = meetupRepository.findOne(vMeetup.getId());
		Assert.notNull(pMeetup, "该圈子不存在，请检查后再操作");
		// 设置用户信息
		ShiroUser shiroUser = Shiros.currentUser();
		Assert.notNull(shiroUser, "用户尚未登录，请先登录");
		PUser pUser = userRepository.findByTel((String) shiroUser.getAttributes().get("tel"));
		Assert.notNull(pUser, "用户登录失效，请重新登录");
		if (pMeetup.getUsers() == null) {
			pMeetup.setUsers(Lang.newList());
		}
		pMeetup.getUsers().add(pUser);
		pUser.getMeetups().add(pMeetup);
		em.persist(pUser);
		em.persist(pMeetup);
		vMeetup.setOwn(true);
		return vMeetup;
	}

}
