package com.saasxx.core.module.account.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saasxx.core.module.account.schema.PPreUser;

/**
 * Created by lujijiang on 16/6/11.
 */
public interface PreUserRepository extends JpaRepository<PPreUser, String> {

}
