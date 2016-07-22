+function(ng, $, moduleName) {
	'use strict';
	
	$.fn.extend({
		showIonicErrorInfo:function(message,arg){
			var item = $(this).closest(".item");
			var key = moduleName+".errorDivs";
			var errorDivs = item.data(key);
			var errorDiv;
			if(!errorDivs || !errorDivs.is(".form-errors")){
				errorDivs = $('<div class="item form-errors"/>').insertAfter(item);
				errorDiv = $('<div class="form-error"/>').appendTo(errorDivs);
				item.data(key,errorDivs);
			}
			else {
				errorDiv = errorDivs.find(".form-error");
			}
			if(message){
				var title = "";
				var label = item.find(".input-label");
				if(label.is(".input-label")){
					title = label.text();
				}
				else {
					var input = item.find("input");
					if(input.is("input")){
						title = input.attr("title");
					}
				}
				message = message.format(title,arg);
				errorDiv.text(message);
			}
			else {
				errorDivs.remove();
				item.removeData(key);
			}
		}
	});
	
	
	var module = ng.module(moduleName, []);
	// 通过ajax验证
	module.directive("remote", [ '$http', function($http) {
		return {
			require : 'ngModel',
			link : function(scope, ele, attrs, ctrl) {
				if (!ctrl) {
					return;
				}
				scope.$watch(attrs.ngModel, function(newValue,oldValue) {
					var value = newValue;
					if(value === undefined){
						return;
					}
					if (typeof value === 'string') {
						value = value.trim();
						if(value == ''){
							return;
						}
					}
					

					$http({
						url : attrs.remote,
						method : 'POST',
						params : {
							value:value
						},
						headers : {
							'Content-Type' : 'application/x-www-form-urlencoded'
						}
					})
					.then(function successCallback(response) {
						if(response.status==200){
							var json = response.data;
							if (!json.status) {
								ctrl.$error['message'] = json.error;
								ctrl.$setValidity('valid', false);
							} else {
								delete ctrl.$error['message'];
								ctrl.$setValidity('valid', true);
							}
							ele.trigger("change.validate");
						}
						else {
							ctrl.$setValidity('valid', false);
							ctrl.$error['message'] = "status:"+response.status+",statusText:"+response.statusText;
							ele.trigger("change.validate");
						}
					},
					function errorCallback(response) {
						ctrl.$setValidity('valid', false);
						ctrl.$error['message'] = response;
						ele.trigger("change.validate");
					});
				});
			}
		}
	} ]);
	// 验证消息显示
	module.directive("validate", [ function() {
		return {
			require : 'ngModel',
			link : function(scope, ele, attrs, ctrl) {
				if (!ctrl) {
					return;
				}
				ele = $(ele);
				var showError = function() {
					if (ctrl.$error.required) {
						var message = attrs.mRequired;
						message = message?message:"{0}不能为空";
						$(ele).showIonicErrorInfo(message);
					} else if (ctrl.$error.email) {
						var message = attrs.mEmail;
						message = message?message:"{0}必须是电子邮箱格式";
						$(ele).showIonicErrorInfo(message);
					} else if (ctrl.$error.max) {
						var max = attrs.ngMax ? attrs.ngMax
								: attrs.max;
						var message = attrs.mMax;
						message = message?message:"{0}长度不能大于{1}";
						$(ele).showIonicErrorInfo(message,max);
					} else if (ctrl.$error.maxlength) {
						var maxlength = attrs.ngMaxlength ? attrs.ngMaxlength
								: attrs.maxlength;
						var message = attrs.mMaxlength;
						message = message?message:"{0}长度不能大于{1}位";
						$(ele).showIonicErrorInfo(message,maxlength);
					} else if (ctrl.$error.min) {
						var min = attrs.ngMin ? attrs.ngMin
								: attrs.min;
						var message = attrs.mMin;
						message = message?message:"{0}长度不能小于{1}";
						$(ele).showIonicErrorInfo(message,min);
					} else if (ctrl.$error.minlength) {
						var minlength = attrs.ngMinlength ? attrs.ngMinlength
								: attrs.minlength;
						var message = attrs.mMinlength;
						message = message?message:"{0}长度不能小于{1}位";
						$(ele).showIonicErrorInfo(message,minlength);
					} else if (ctrl.$error.number) {
						var message = attrs.mNumber;
						message = message?message:"{0}必须是数字";
						$(ele).showIonicErrorInfo(message);
					} else if (ctrl.$error.pattern) {
						var pattern = attrs.ngPattern ? attrs.ngPattern
								: attrs.pattern;
						var message = attrs.mPattern;
						message = message?message:"{0}必须是满足正则表达式{1}";
						$(ele).showIonicErrorInfo(message,pattern);
					} else if (ctrl.$error.url) {
						$(ele).showIonicErrorInfo("{0}必须是URL链接");
					} else if (ctrl.$error.date) {
						$(ele).showIonicErrorInfo("{0}必须是日期");
					} else if (ctrl.$error.datetimelocal) {
						$(ele).showIonicErrorInfo("{0}必须是日期时间，精确到分");
					} else if (ctrl.$error.time) {
						$(ele).showIonicErrorInfo("{0}必须是时间");
					} else if (ctrl.$error.week) {
						$(ele).showIonicErrorInfo("{0}必须是星期");
					} else if (ctrl.$error.month) {
						$(ele).showIonicErrorInfo("{0}必须是月份");
					} else if (ctrl.$error.valid) {
						$(ele).showIonicErrorInfo(ctrl.$error.message);
					} else {
						$(ele).showIonicErrorInfo('');
					}
				}
				var form = ele.closest('form');
				var submits = form.find("[type='submit']");
				var item = ele.closest(".item");
				var inputEvents = "input.validate change.validate";
				ele.off(inputEvents).on(inputEvents,function(){
					item.toggleClass('has-error',
							ctrl.$dirty && ctrl.$invalid);
					submits.attr("disabled",ctrl.$dirty && ctrl.$invalid);
					if (ctrl.$dirty && ctrl.$invalid) {
						showError();
					} else {
						$(ele).showIonicErrorInfo('');
					}
				});
				
				form.attr("novalidate",true).on("submit.validate",
						function(event) {
							item.toggleClass('has-error',
									ctrl.$invalid);
							submits.attr("disabled", ctrl.$invalid);
							if (ctrl.$invalid) {
								// 只要有一项验证不通过，则禁止时间冒泡提交
								event.stopImmediatePropagation();
								showError();
							} else {
								$(ele).showIonicErrorInfo('');
							}
							return ctrl.$valid;
						});
				// 重新排列当前绑定的submit事件顺序，按校验与非校验分组，将校验的事件放在前面
				var submitEventMap = $._data(form[0], 'events');
				var submitEvents = submitEventMap.submit;
				var prevSubmitEvents = [];
				var afterSubmitEvents = [];
				for(var i = 0;i<submitEvents.length;i++){
					var submitEvent = submitEvents[i];
					if(submitEvent.namespace=="validate"){
						prevSubmitEvents.push(submitEvent);
					}
					else {
						afterSubmitEvents.push(submitEvent);
					}
				}
				submitEvents = prevSubmitEvents.concat(afterSubmitEvents);
				// 解除所有的submit
				form.off("submit");
				// 重新按新顺序绑定所有的submit事件
				for(var i = 0;i<submitEvents.length;i++){
					var submitEvent = submitEvents[i];
					form.on(submitEvent.namespace?submitEvent.type+"."+submitEvent.namespace:submitEvent.type,submitEvent.handler);
				}
			}
		}
	} ]);
}(angular, jQuery, "ionic-validate");