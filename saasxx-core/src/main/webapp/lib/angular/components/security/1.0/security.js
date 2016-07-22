+function(ng, $, moduleName) {
	'use strict';
	
	var storage = function(){
		var args = Array.prototype.slice.call(arguments);
		if(args.length==1){
			return localStorage?localStorage[args[0]]:$.cookie(args[0]);
		}
		if(args.length==2){
			var oldValue = storage(args[0]);
			if(localStorage){
				localStorage[args[0]] = args[1];
			}
			else {
				$.cookie(args[0],args[1],{ expires: 365 });
			}
			return oldValue;
		}
	}
	
	var module = ng.module(moduleName, []);
	// 定义安全服务
	module.service("security", [
			"$q",
			"$http",
			"$location",
			'$rootScope',
			"$injector","$window",
			function($q, $http, $location, $rootScope,$injector,$window) {

				var backPath;

				var $locationChangeSuccess;

				this.login = function(jwt) {
					storage("$bearer-token$",jwt);
					storage("$deny-paths$",null);
				}

				this.go = function(path){
					backPath = $location.path();
					$location.path(path);
				}

				this.back = function(path) {
					try{
						var $ionicHistory = $injector.get("$ionicHistory");
						
						$ionicHistory.nextViewOptions({
						    disableAnimate: true,
						    historyRoot: true
						});
					}catch(e){log.info(e)}
					if (backPath) {
						$location.path(backPath);
					} else {
						$location.path("/");
					}
				}
				
				this.logout = function(){
					storage("$bearer-token$",null);
					storage("$deny-paths$",null);
				}
				
				/**
				 * 检查是否具有权限
				 */
				this.check = function(checkUrl, loginUrl, urls) {
					var deferred = $q.defer();
					var path = $rootScope.$toPath;
					var url = location.href.substring(0, location.href
							.indexOf("#"));
					if (path == loginUrl) {
						deferred.resolve();
						return deferred.promise;
					}

					backPath = location.href.substring(location.href
							.indexOf("#") + 1,location.href.length);
					
					var goLogin = function(){
						if(typeof loginUrl==='string'){
							$location.path(loginUrl);
						}
						else {
							$location.path("/");
							if(typeof loginUrl === 'function'){
								loginUrl.call(window);
							}
						}
						if ($locationChangeSuccess) {
							// 解除$locationChangeSuccess事件的绑定
							$locationChangeSuccess();
						}
						$locationChangeSuccess = $rootScope.$on(
								'$locationChangeSuccess', function() {
							if (loginUrl == $rootScope.$toPath) {
								var message = path
										+ "权限检查失败，转到登录页,登录信息删除";
								log.warn(message);
								deferred.resolve(message);
							}
						});
					}
					
					if(urls[path]){
						var denyPaths = storage("$deny-paths$");
						denyPaths = denyPaths ? eval("("+denyPaths+")"):denyPaths;
						if(denyPaths){
							if(denyPaths[path]){
								goLogin();
							}
							else {
								var message = path + "权限检查通过";
								log.debug(message);
								deferred.resolve(message);
							}
						}
						else {
							$http.post(checkUrl)
							// 处理成功后的情况
							.success(
									function(json, status, headers, config) {
										if (json.status && json.data) {
											denyPaths = json.data;
//											storage("$deny-paths$",JSON.stringify(denyPaths));
											if(denyPaths[path]){
												goLogin();
											}
											else {
												var message = path + "权限检查通过";
												log.debug(message);
												deferred.resolve(message);
											}
										}
										else {
											goLogin();
										}
									})
							// 处理失败时的情况
							.error(function(data, status, headers, config) {
								deferred.reject("error:status="+status+",data="+data);
							});
						}
					}
					else {
						var message = path + "权限检查通过";
						log.debug(message);
						deferred.resolve(message);
					}
					return deferred.promise;
				}
			} ]);

	module.config(function($httpProvider) {
		$httpProvider.interceptors.push(function(){
			return {
				request:function(config) {
					var token = storage("$bearer-token$");
					token = token && token.indexOf(".")!=-1?token:"anonymous.anonymous";
					config.headers["Authorization"]="Bearer "+token;
					return config;
				},
				response:function(response) {
					var jwt = response.headers("WWW-Authenticate");
					if(jwt){
						storage("$bearer-token$",jwt);
					}
					return response;
				}
			};
		});
	});
	
	
	module.run(function($rootScope) {
		$rootScope.$on('$stateChangeStart', function(event, toState,
				toStateParams) {
			$rootScope.$toPath = toState.path.replace(/:[a-zA-Z]+/g, function(s) {
				s = s.substring(1);
				return toStateParams[s];
			});
		});
	});
	
}(angular, jQuery, "security");