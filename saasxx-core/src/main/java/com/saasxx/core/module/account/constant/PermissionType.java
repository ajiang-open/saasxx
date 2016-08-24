package com.saasxx.core.module.account.constant;

public enum PermissionType {
    /**
     * 用于控制菜单显示与否的权限
     */
    menu,
    /**
     * 用于控制URL的权限
     */
    url,
    /**
     * 用于控制Angular JS的hash路径
     */
    path,
    /**
     * Shiro的注解权限
     */
    anno;
}
