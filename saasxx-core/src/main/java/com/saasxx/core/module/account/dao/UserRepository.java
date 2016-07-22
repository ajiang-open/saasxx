package com.saasxx.core.module.account.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saasxx.core.module.account.schema.PUser;

/**
 * Created by lujijiang on 16/6/11.
 */
public interface UserRepository extends JpaRepository<PUser, String> {
	PUser findByTel(String tel);

	PUser findByEmail(String value);
}
