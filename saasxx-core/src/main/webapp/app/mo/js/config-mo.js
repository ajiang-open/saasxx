+function(ng, $, moduleName) {
    'use strict';
    var module = ng.module(moduleName, []);
    var host = "";//本地使用
    // 定义版本号
    module.constant("version",function(){
    	var scripts = document.getElementsByTagName("script");
    	var script = scripts[scripts.length-1];
    	var point = script.src.indexOf("=");
    	if(point){
    		return script.src.substring(point);
    	}
    	return new Date().getTime();
    });
    // 定义全局host变量
    module.constant("host",host);
    // 定义API的地址服务
    module.constant('api', {
    	webRpc:host + 'web-rpc',
        // 注册登录相关的URL
        sign:host + 'sign',
        // 安全相关的地址
        security:host + 'security/check'
    });
    // 定义移动端的页面映射
    module.constant('urls',{
    	meetup:{
        	$super:'app/mo/view/meetup/super.html',
        	index:'app/mo/view/meetup/index/index.html',
        },
        circle:{
        	$super:'app/mo/view/circle/super.html',
        	index:'app/mo/view/circle/index/index.html',
        	add:{
        		step1:"app/mo/view/circle/add/step1/index.html",
        		step2:{
        			":name":"app/mo/view/circle/add/step2/index.html"
        		},
        		step3:{
        			":id":"app/mo/view/circle/add/step3/index.html"
        		}
        	},
        	meetup:{
        		add:{
            		step1:{
            			":id":"app/mo/view/meetup/add/step1/index.html"
            		},
            	}
        	}
        },
        account:{
        	$super:'app/mo/view/account/super.html',
        	index:'app/mo/view/account/index/index.html',
        	signin:'app/mo/view/account/signin/index.html',
        	signup:{
        		step1:'app/mo/view/account/signup/step1/index.html',
				step2:{
					":id":'app/mo/view/account/signup/step2/index.html'
				},
				step3:'app/mo/view/account/signup/step3/index.html'
        	}
        }
    });
}(angular, jQuery, "config");
