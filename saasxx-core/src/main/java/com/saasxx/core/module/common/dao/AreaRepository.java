package com.saasxx.core.module.common.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saasxx.core.module.common.schema.PArea;

public interface AreaRepository extends JpaRepository<PArea, String> {

	PArea findByCode(String regionCode);

}
