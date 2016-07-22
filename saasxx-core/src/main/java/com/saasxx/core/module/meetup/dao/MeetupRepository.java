package com.saasxx.core.module.meetup.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saasxx.core.module.meetup.schema.PMeetup;

/**
 * Created by lujijiang on 16/6/12.
 */
public interface MeetupRepository extends JpaRepository<PMeetup, String> {
}
