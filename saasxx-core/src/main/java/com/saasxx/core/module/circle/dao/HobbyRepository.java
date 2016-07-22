package com.saasxx.core.module.circle.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saasxx.core.module.circle.schema.PHobby;

/**
 * Created by lujijiang on 16/6/12.
 */
public interface HobbyRepository extends JpaRepository<PHobby, String> {
}
