package com.saasxx.core.module.circle.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saasxx.core.module.circle.dao.HobbyRepository;
import com.saasxx.core.module.circle.schema.PHobby;
import com.saasxx.core.module.circle.vo.VHobby;
import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.Finders;
import com.saasxx.framework.dao.finder.facade.Finder;
import com.saasxx.framework.data.Beans;

/**
 * Created by lujijiang on 16/6/12.
 */
@Service
@Transactional
public class HobbyService {

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	HobbyRepository hobbyRepository;

	public List<VHobby> findTopHobbies() {
		List<VHobby> vHobbies = Lang.newList();
		Finder finder = Finders.newFinder();
		PHobby hobbyModel = finder.from(PHobby.class);
		finder.where(hobbyModel.getParent()).equal(hobbyModel);
		List<PHobby> pHobbies = (List<PHobby>) finder.list(entityManager);
		for (PHobby pHobby : pHobbies) {
			VHobby vHobby = new VHobby();
			Beans.from(pHobby).to(vHobby);
			vHobbies.add(vHobby);
		}
		return vHobbies;
	}
}
