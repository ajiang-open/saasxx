package com.saasxx.core.module.circle.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saasxx.core.module.circle.schema.PCircle;

/**
 * Created by lujijiang on 16/6/12.
 */
public interface CircleRepository extends JpaRepository<PCircle, String> {
}
