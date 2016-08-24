package com.saasxx.core.module.common.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saasxx.core.module.common.schema.PFile;

/**
 * Created by lujijiang on 16/6/11.
 */
public interface FileRepository extends JpaRepository<PFile, String> {
}
