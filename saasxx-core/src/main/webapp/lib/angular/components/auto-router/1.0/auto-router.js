/**
 * 自动路由插件，基于ui-router实现动态载入控制器
 * */
+function(ng,$,moduleName){
	'use strict';
	var ui = null;
	
	var version;
	
	var fixUrl = function(url){
		var v = undefined;
		if(version){
			if(typeof version === 'string'){
				v = version;
			}
			else if(typeof version === 'function'){
				v = version.call(window);
			}
		}
		if(!v){
			return url;
		}
		if(url.indexOf("?")!=-1){
			url += "&"+v;
		}
		else {
			url += "?"+v;
		}
		return url;
	}
	
	var includeJS = function() {
		var args = Array.prototype.slice
		.call(arguments);
		if(args.length==0){
			return;
		}
		var url = args.shift();
		if($.isFunction(url)){
			url();
			return;
		}
		$.ajax({
			url:fixUrl(url),
			dataType: "script",
			cache: true,
			crossDomain:true,
			success:function(){
				if(args.length>0){
					includeJS.apply(window,args);
				}
			}
		});
	}
	
	var $params;
	
	+function(){
		// 解析URL中的参数
		var urlParams = {};
		var point = location.href.lastIndexOf("?");
		if(point!=-1){
			var queryString = location.href.substring(point+1);
			var paramStrings = queryString.split("&");
			for(var i = 0;i<paramStrings.length;i++){
				var paramString = paramStrings[i];
				var p = paramString.indexOf("=");
				p = p==-1?paramString.length:p;
				var name = decodeURI(paramString.substring(0,p));
				var value = decodeURI(paramString.substring(p+1));

				var oldValue = urlParams[name];
				if(oldValue){
					if($.isArray(oldValue)){
						oldValue.push(value);
						value = oldValue;
					}
					else {
						value = [oldValue,value];
					}
				}
				urlParams[name] = value;
			}
		}
		// 获取会话参数
		var params;
		if(typeof localStorage.$params !== 'string'){
			params = {};
		}
		else{
			params = JSON.parse(localStorage.$params);
		}
		// 合并URL参数
		for(var name in urlParams){
			params[name] = urlParams[name];
		}
		localStorage.$params = JSON.stringify(params);
		// 参数访问方法
		$params = function(){
			var oldValue;
			var args = Array.prototype.slice
					.call(arguments);
			if(args.length>0){
				oldValue = params[args[0]];
				if(args.length>=2){
					params[args[0]] = args[1];
					localStorage.$params = JSON.stringify(params);
				}
			}
			else {
				alert("Illegal arguments for call $params,at least one or more arguments");
			}
			return oldValue;
		}
	}();
	
	var controller = function($injector, $rootScope, $scope, $location,
			$stateParams,$state,$rpc){
		// 注入$alert选项
		window.$alert = function(content,fn){
			if($injector.has("$ionicPopup")){
				content = content?content:title;
				var $ionicPopup = $injector.get("$ionicPopup");
				$ionicPopup.alert({
					title: "提示",
					template: content
				}).then(fn);
			}
			else {
				alert(content);
				fn();
			}
		}
		var beans = {
			$scope:$scope,
			// 注入当前控制器作用域
			v : $scope.v = {},
			f : $scope.f = {},
			// 注入当前屏幕高宽
			$width:$scope.$width = screen.width,
			$height:$scope.$height = screen.height,
			// super scope
			$super : $scope.$parent,
			// 注入当前位置信息
			$location : $location,
			// 注入当前路由状态
			$state:$state,
			// 注入当前路由参数
			$stateParams : $stateParams,
			// 注入本地存储参数
			$params:$params,
			// 返回函数
			$back:$scope.$back = function(){
				if($injector.has("$ionicHistory")){
					var $ionicHistory = $injector.get("$ionicHistory");
					if($ionicHistory.backView() && $ionicHistory.backView().stateId!=$ionicHistory.currentView().stateId){
						$ionicHistory.goBack();
					}
					else {
						location.href="#/";
					}
				}
				else {
					history.back();
				}
			}
		};
		$scope["$beans"] = beans;
		
		// 处理动态载入的JS
		window['$$'] = function() {
			
			if($injector.has("$ionicHistory")){
				var $ionicLoading = $injector.get("$ionicLoading");
				$ionicLoading.hide();
			}
			
			var args = Array.prototype.slice
					.call(arguments);
			$scope.$apply(function($scope) {
				var fn = args[args.length - 1];
				var inject = args.slice(0,
						args.length - 1);
				if(inject.length > 0){
					fn.$inject = inject;
				}
				var beans = $scope.$beans;
				$injector.invoke(fn, $scope, beans);
			});
		};

		// 引入顶、中、尾的逻辑
		var jsUrls = [];
		var fetchJsUrl = function(jsUrl){
			var p = jsUrl.indexOf("?");
			if(p){
				jsUrl = jsUrl.substring(0,p);
			}
			jsUrl = jsUrl.substring(0,jsUrl.lastIndexOf("."))+".js";
			return jsUrl;
		}
		var searchJS = function(current){
			if(current.self.templateUrl){
				var jsUrl = fetchJsUrl(current.self.templateUrl);
				jsUrls.push(jsUrl);
			}
			else if(current.self.views){
				for(var _name in current.self.views){
					var view = current.self.views[_name];
					if(view && view.templateUrl){
						var jsUrl = fetchJsUrl(view.templateUrl);
						jsUrls.push(jsUrl);
					}
				}
			}
			if(current.parent){
				searchJS(current.parent);
			}
		}
		searchJS($state.$current);
		jsUrls = jsUrls.reverse();
		if($injector.has("$ionicHistory")){
			var $ionicLoading = $injector.get("$ionicLoading");
			$scope.$on('$ionicView.enter', function(e){
				$ionicLoading.show({
				      template: '加载中...'
				});
				$rpc.calls(function(done){
					jsUrls.push(done);
					includeJS.apply(window, jsUrls);
				});
			});
		}
		else {
			$rpc.calls(function(done){
				jsUrls.push(done);
				includeJS.apply(window, jsUrls);
			});
		}
	}
	
	var module = ng.module(moduleName, ["ui.router"]);
	module.provider("$autoRouter",function($stateProvider,$urlRouterProvider){
		
		var resolve;
		
		var configStates = function(prefixPath,config,parentState,viewName,parentUrl){
			
			viewName = viewName?viewName:"$";
			
			for(var name in config){
				
				var path = prefixPath;
				
				if(name.indexOf("$")==0){
					continue;
				}
				
				var childView;
				var value = config[name];
				var state;
				var url;
				if(name.indexOf("/")==0){
					state = parentState;
					url = name;
				}
				else {
					childView = viewName+"_"+name;
					state = parentState?parentState+"_"+name:name;
					url = "/"+name;
					path+=url;
				}
				
				if(parentUrl){
					url = parentUrl+url;
				}
				
				if(typeof value==='string'){
					var views = {};
					var templateUrl = value;
					views[viewName] = {
							templateUrl:fixUrl(templateUrl),
							controller:controller
					};
					$stateProvider.state(state,{
						url: url,
						views:views,
						resolve : resolve,
						path:path
					});
				}
				else if($.isPlainObject(value)){
					var templateUrl = value["$super"];
					if(templateUrl){
						$stateProvider.state(state,{
							url: url,
							templateUrl:fixUrl(templateUrl),
							"abstract":true
						});
						configStates(path,value,state+".",childView);
					}
					else {
						configStates(path,value,state,viewName,url);
					}
				}
			}
		}
		
		this.ui = function(v){
			ui = v;
		}
		
		this.config = function(config){
			configStates("",config);
		}
		
		this.resolve = function(op){
			resolve = op;
		}
		
		this.version = function(op){
			version = op;
		}
		
		this.$get = function($http,$q){
			
			return {
				
			};
		}
	});
	
}(angular,jQuery,"auto-router");