package com.saasxx.core.module.meetup.web.webrpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saasxx.core.module.meetup.service.MeetupService;
import com.saasxx.core.module.meetup.vo.VMeetup;
import com.saasxx.framework.web.webrpc.annotation.WebRpc;

@Component
public class MeetupWebRpc {

	@Autowired
	MeetupService meetupService;

	@WebRpc
	public Object saveMeetup(VMeetup vMeetup) {
		return meetupService.saveMeetup(vMeetup);
	}

	@WebRpc
	public Object findMeetups(VMeetup vMeetup) {
		return meetupService.findMeetups(vMeetup);
	}

	@WebRpc
	public Object joinMeetup(VMeetup vMeetup) {
		return meetupService.joinMeetup(vMeetup);
	}
}
