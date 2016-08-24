package com.saasxx.core.module.common.web.webrpc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saasxx.core.module.common.service.AddressService;
import com.saasxx.core.module.common.vo.VArea;
import com.saasxx.framework.web.webrpc.annotation.WebRpc;

@Component
public class AddressWebRpc {
    @Autowired
    AddressService addressService;

    @WebRpc
    public List<VArea> findAreas(VArea vArea) {
        return addressService.findAreas(vArea);
    }

}
