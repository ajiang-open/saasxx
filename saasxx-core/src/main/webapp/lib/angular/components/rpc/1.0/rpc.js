/**
 * 智能服务，可智能调度和整合页面的各个服务为一体，避免多次无谓的请求
 * 
 * @author lujijiang
 */
+function(ng, $, moduleName) {
	'use strict';
	var module = ng.module(moduleName, []);
	module.provider("$rpc",function(){
		
		var url;
		
		this.url = function(_url){
			url = _url;
		}
		
		this.$get = function($http,$q){
			
			var transactionIndex = 1;
			
			var transaction = false;
			
			var calls = [];
			
			var cacheDatas = {};
			
			var done = function(){
				if(!url){
					throw new Error("The url for $rpc is undefined");
				}
				
				if(calls.length==0){
					return this;
				}
				
				var deferreds = {};
				var promises = {};
				var rpcNames = {};
				var postCalls = [];
				var callMap = {};
				
				var handleResult = function(e,key){
					if(key){
						cacheDatas[key] = {
								time:new Date().getTime(),
								result:e
						};
					}
					var name = rpcNames[e.id];
					var deferred = deferreds[e.id];
					var promise = promises[e.id];
					deferred.notify(e);
					if(e.errorType){
						deferred.reject({
							errorType:e.errorType,
							error:e.error
						});
					}
					else {
						deferred.resolve(e.result);
						if($.isPlainObject(e.result)){
							for(var key in e.result){
								promise[key] = e.result[key];
							}
						}
						else if($.isArray(e.result)){
							for(var i = 0;i<e.result.length;i++){
								promise.push(e.result[i]);
							}
						}
						else {
							log.info("$rpc("+name+")'s result is primitive type,use $then or $success function to deal with");
							promise["$"] = e.result;
						}
					}
				}
				
				$.each(calls,function(i,call){
					var id = "id"+(JSON.stringify({
						name:call.name,
						args:call.args
					}).hashCode()+10000000000);
					
					callMap[id] = call;
					rpcNames[id] = call.name;
					deferreds[id] = call.deferred;
					promises[id] = call.promise;
					
					var cacheData = cacheDatas[id];
					if(cacheData && new Date().getTime()-cacheData.time<call.cache*1000){
						var result = cacheData.result;
						handleResult(result);
					}
					else {
						postCalls.push({
							id:id,
							name:call.name,
							args:call.args
						});
					}
				});
				
				if(postCalls.length>0){
					$http.post(url, postCalls)
					// 处理成功后的情况
					.success(function(data, status, headers, config) {
						$.each(data,function(_,e){
							var call = callMap[e.id];
							handleResult(e,e.id);
						});
					})
					// 处理失败时的情况
					.error(function(data, status, headers, config) {
						$.each(deferreds,function(i,deferred){
							var data = {
								errorType:true,
								error:"网络或服务器错误，状态："+status+"，地址："+url
							};
							deferred.notify(data);
							deferred.reject(data);
						});
					});
				}
				calls = [];
				return this;
			};
			
			function Rpc(_cache){
				
				this._cache = !isNaN(_cache)?_cache:0;
				
				this.cache = function(_cache){
					return new Rpc(_cache);
				}
				
				this.calls = function(fn){
					transaction = transactionIndex++;
					if($.isFunction(fn) && fn.length==1){
						fn(function(){
							transaction = false;
							if(!transaction){
								done();
							}
						});
					}
					else {
						fn();
						transaction = false;
						if(!transaction){
							done();
						}
					}
				}
				
				this.call = function(){
					var args = Array.prototype.slice.call(arguments);
					if(args.length<1){
						throw new Error("At least one argument for $rpc call ");
					}
					var deferred = $q.defer();
					var name = args[0];
					args = args.slice(1);
					// 将所有的参数转换为JSON字符串
					for(var i = 0;i<args.length;i++){
						args[i] = JSON.stringify(args[i]);
					}
					var promise = deferred.promise;
					var $promise = [];
					$promise.$promise = promise;
					$promise.$then = function(successCallback, errorCallback, notifyCallback){
						promise.then(successCallback, errorCallback, notifyCallback);
						return this;
					};
					$promise.$success = function(successCallback){
						promise.then(successCallback);
						return this;
					};
					$promise.$fail = function(errorCallback){
						promise.then(null,errorCallback);
						return this;
					};
					calls.push({
						name:name,
						args:args,
						deferred:deferred,
						promise:$promise,
						cache:this._cache
					});
					if(!transaction){
						done();
					}
					return $promise;
				}
			}
			
			return new Rpc(0);
		}
	});
}(angular, jQuery, "rpc");