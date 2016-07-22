package com.saasxx.core.module.common.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saasxx.core.module.common.dao.AreaRepository;
import com.saasxx.core.module.common.schema.PArea;
import com.saasxx.core.module.common.vo.VArea;
import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.Finders;
import com.saasxx.framework.dao.finder.facade.Finder;

@Service
@Transactional
public class AddressService {

	@PersistenceContext
	EntityManager em;

	@Autowired
	AreaRepository areaRepository;

	public List<VArea> findAreas(VArea vArea) {
		Finder finder = Finders.newFinder();
		PArea modelArea = finder.from(PArea.class);
		if (vArea.getCode() == null) {
			finder.where(modelArea.getParent()).equal(modelArea);
		} else {
			finder.where(modelArea.getParent().getCode()).equal(vArea.getCode());
			finder.where(modelArea.getParent()).notEqual(modelArea);
			finder.where(modelArea.getDisabled()).equal(false);
		}
		finder.order(modelArea.getCode()).asc();
		List<PArea> pAreas = (List<PArea>) finder.list(em, true);
		List<VArea> vAreas = Lang.newList();
		for (PArea pArea : pAreas) {
			VArea area = new VArea();
			area.setCode(pArea.getCode());
			area.setName(pArea.getName());
			vAreas.add(area);
		}
		return vAreas;
	}

}
