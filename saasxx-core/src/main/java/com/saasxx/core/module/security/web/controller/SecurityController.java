package com.saasxx.core.module.security.web.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saasxx.framework.Lang;
import com.saasxx.framework.security.shiro.Shiros;

/**
 * 安全控制器
 *
 * @author lujijiang
 */
@RestController
@RequestMapping("/security")
public class SecurityController {
    /**
     * 安全检查
     *
     * @return
     */
    @RequestMapping("/check")
    public Object check() {
        Map<String, Boolean> map = Lang.newMap();
        map.put("/account/index", Shiros.currentUser() == null);
        return map;
    }
}
