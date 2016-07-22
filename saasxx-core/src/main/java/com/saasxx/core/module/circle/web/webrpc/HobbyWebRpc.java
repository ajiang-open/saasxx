package com.saasxx.core.module.circle.web.webrpc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saasxx.core.module.circle.service.HobbyService;
import com.saasxx.core.module.circle.vo.VHobby;
import com.saasxx.framework.web.webrpc.annotation.WebRpc;

/**
 * Created by lujijiang on 16/6/12.
 */
@Component
public class HobbyWebRpc {
	@Autowired
	private HobbyService hobbyService;

	@WebRpc
	public List<VHobby> findTopHobbies() {
		return hobbyService.findTopHobbies();
	}

}
