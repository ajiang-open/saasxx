package com.saasxx.core.module.circle.web.webrpc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saasxx.core.module.circle.service.CircleService;
import com.saasxx.core.module.circle.vo.VCircle;
import com.saasxx.framework.web.webrpc.annotation.WebRpc;

@Component
public class CircleWebRpc {

	@Autowired
	CircleService circleService;

	@WebRpc
	public List<VCircle> findCircles(VCircle vCircle) {
		return circleService.findCircles(vCircle);
	}

	@WebRpc
	public Object saveCircle(VCircle vCircle) {
		return circleService.saveCircle(vCircle);
	}

	@WebRpc
	public Object activeCircle(VCircle vCircle) {
		return circleService.activeCircle(vCircle);
	}

	@WebRpc
	public Object joinCircle(VCircle vCircle) {
		return circleService.joinCircle(vCircle);
	}
}
