/*
 * 专用于创建各种工具类型的jQuery方法
 * @author lujijiang
 * **/
+function($) {
	
	'use strict';
	
	$.extend({ 
		/**
		 * 预防XSS攻击，不能保证所有的漏洞都能避免，最好的方式还是需要后台处理
		 * */
		preventXSS:function(html){
			var div = $("<div/>").html(html);
			$("script",div).each(function(){
				$(this).remove();
			});
			$("*",div).each(function(){
				for(var key in this){
					var value = this[key];
					if(!value){
						continue;
					}
					if(typeof value ==='string'){
						value = value.trim();
					}
					if(key=='href' || key=='src'){
						if(/^javascript:/i.test(value)){
							$(this).removeAttr(key);
						}
					}
					else if(key.indexOf("on")==0){
						$(this).removeAttr(key);
					}
				}
			});
			return div.html();
		},
		/**
		 * 打开新页面
		 */
		open:function(op){
			op.data = $.toData(op.data);
			op.target = $.type(op.target)==='string'?op.target:'_blank';
			op.type = $.type(op.type)==='string'?op.type:'post';
			var form = $('#ui_open_form');
			if(!form.is('form')){
				form = $('<form/>').attr('id','ui_open_form').appendTo(document.body);
			}
			form.attr('action',op.url).attr('method',op.type).attr('target',op.target).empty();
			$.each(op.data,function(i,e){
				$('<input type="hidden" name="'+e.name+'"/>').val(e.value).appendTo(form);
			});
			form.submit();
		},
		/**
		 * 生成随机ID
		 */
		id:function(){
			return "id_" + ("" + Math.random()).replace( /\D/g, "" );
		},
		/**
		 * 根据传入的对象执行指定路径的值，如果发生错误或者类型不对，则返回默认值
		 */
		eval:function(obj,path,defaultValue,type){
			var value;
			try {
				value = eval('obj.'+path);
			} catch (e) {}
			if($.type(type)!=='string'){
				return value===undefined?defaultValue:value;
			}
			return $.type(value)===type?value:defaultValue;
		},
		/**
		 * 重构jQuery的getJSON
		 */
		getJSON: function( url, data, callback ) {
			callback = $.isFunction(data)?data:callback;
			return jQuery.get( url, $.toData(data), function(json){
				if($.isFunction(callback)){
					callback($.toJSON(json));
				}
			}, "text" );
		},
		/**
		 * 类getJSON，以POST方式提交
		 */
		postJSON: function( url, data, callback ) {
			callback = $.isFunction(data)?data:callback;
			return jQuery.post( url, $.toData(data), function(json){
				if($.isFunction(callback)){
					callback($.toJSON(json));
				}
			}, "text" );
		},
		/**
		 * 深度克隆数据对象
		 */
		deepClone:function(obj,hashMap){
			if(!hashMap){
				hashMap = {};
			}
			var newObj = hashMap[obj];
			if(newObj){
				// 防止死循环
				return newObj;
			}
			if($.isArray(obj)){
				var array = [];
				$.each(obj,function(i,e){
					array.push($.deepClone(e,hashMap));
				});
				newObj = array;
			}
			if($.isPlainObject(obj)){
				var object = {};
				for(var key in obj){
					object[key] = $.deepClone(obj[key],hashMap);
				}
				newObj = object;
			}
			if(newObj){
				hashMap[obj] = newObj;
				return newObj;
			}
			return obj;
		},
		/**
		 * 将对象转换为标准json对象，包括字符串、非标准的fastjson循环引用对象等
		 */
		toJSON:function(json){
			var root = typeof json === 'string'?eval('('+json+')'):json;
			var map = {};
			map['$'] = root;
			var fixFastJSON = function(obj){
				if($.isPlainObject(obj)||$.isArray(obj)){
					map['@'] = obj;
					map['../'] = obj;
					for(var k in obj){
						var v = obj[k];
						if($.isPlainObject(v)){
							var p = v['$ref'];
							if(p){
								if(p.indexOf('..')!=-1){
									p = p.replace(/(..)(\/)?/g,"['../']");
									v = eval('(map'+p+')');
									obj[k] = v;
									continue;
								}
								else if (p==='@') {
									v = eval("(map['@'])");
									obj[k] = v;
									continue;
								}
								else {
									v = eval('(map.'+p+')');
									obj[k] = v;
									continue;
								}
							}
							v['../'] = obj;
						}
						if(k!='../'){
							fixFastJSON(v);
						}
					}
				}
			};
			fixFastJSON(root);
			return root;
		},
		/**
		 * 合并数据
		 */
		toData:function(){
			var data = [];
			
			for (var i = 0; i < arguments.length; i++) {
				var argument = arguments[i];
				if($.isArray(argument)){
					$.each(argument,function(i,e){
						if(e.name!=undefined&&e.value!=undefined){
							data.push({
								name : e.name,
								value : e.value
							});
						}
						else if($.isPlainObject(e)){
							data = data.concat($.toData(e));
						}
					});
				}
				else if($.isPlainObject(argument)){
					for(var name in argument){
						var value = argument[name];
						if(!($.type(value)=='string'||$.type(value)=='number'||$.type(value)=='boolean')){
							continue;
						}
						data.push({
							name : name,
							value : value
						});
					}
				}
				else if(!$.isFunction(argument) && $(argument).is('form')){
					data = data.concat($(argument).serializeArray());
				}
			}
			return data;
		},
		/**
		 * 用于多事件聚合触发机制，当第二个参数是函数时，注册方法；当第二个参数是字符串时，开始按条件执行方法
		 * 
		 */
		call:function(keys,arg){
			if($.type(keys)!='string'){
				alert("$.call方法的第一个参数必须是字符串");
				return;
			}
			var mapData = $(window).data("ui-call:mapData");
			if(!$.isPlainObject(mapData)){
				mapData = {};
				$(window).data("ui-call:mapData",mapData);
			}
			var fnData = $(window).data("ui-call:fnData");
			if(!$.isPlainObject(fnData)){
				fnData = {};
				$(window).data("ui-call:fnData",fnData);
			}
			
			if($.isFunction(arg)){
				var keyArray = keys.split(/\s+/);
				var map = {};
				$.each(keyArray,function(i,key){
					map[key] = true;
				});
				fnData[keys] = arg;
				mapData[keys]=map;
			}
			else if ($.type(arg)==='string') {
				var fn = fnData[keys];
				if(!fn){
					return;
				}
				var map = mapData[keys];
				if(!map){
					return;
				}
				delete map[arg];
				if($.isEmptyObject(map)){
					fn();
				}
			}
			else {
				alert("$.call方法的第二个参数必须是函数或者字符串");
				return;
			}
		}
	});
	// 用于重载val方法
	var val = $.fn.val;
	
	$.fn.extend({
		/**
		 * 重载val方法，兼容IE的placeholder模拟
		 */
		val : function() {
			var isPlaceholder = $(this).data('ui-input.placeholder');
			if (arguments.length==0) {
				if (isPlaceholder) {
					return "";
				}
				var value = val.call($(this));
				return value;
			}
			else {
				if(arguments[0]==''&&isPlaceholder){
					return $(this);
				}
				return val.call($(this), arguments[0]);
			}
		},
		placeholder:function(){
			$(this).each(function() {
				var input = $(this); 
				// IE添加placeholder模拟
				if (!Modernizr.input.placeholder){
					if(input.is("input[type=text],input[type=email],input[type=url],input[type=number],input[type=range],input[type=search],textarea")){
						var placeholder = input.attr("placeholder");
						if(placeholder){
							if(input.data('ui-input.placeholder.init')){
								return input;
							}
							var addPlaceholder = function() {
								if(!$(this).val()){
									if($(this).attr('readonly')){
										return;
									}
									if($(this).attr('disabled')){
										return;
									}
									$(this)
									.css('color','#909090')
									.data('ui-input.placeholder',true)
									.val(placeholder);
								}
							}
							var removePlaceholder = function() {
								if($(this).data('ui-input.placeholder')){
									$(this).css('color', '#000000')
									.data('ui-input.placeholder',false)
									.val('');
								}
							}
							addPlaceholder.call(input);
							input.bind('blur',addPlaceholder)
							.bind('focus',removePlaceholder);
							// 查找所有的包含自身的form并当form提交时删除自身的placeholder数据
							$(document.body).on("submit","form",function(){
								if($(input,this).size()>0){
									removePlaceholder.call(input);
								}
							});
							input.data('ui-input.placeholder.init',true);
						}
					}
				}
			});
			return $(this);
		},
		/**
		 * 用于radio和checkbox的选中和选不中操作
		 */
		checked:function(f){
			if(f){
				$(this).attr('checked',true);
				$(this).prop('checked',true);
			}
			else {
				$(this).attr('checked',false);
				$(this).prop('checked',false);
			}
		},
		/**
		 * 将一个页面元素转换为html
		 */
		outerHtml:function(arg){
			var ret;

		    // If no items in the collection, return
		    if (!this.length)
		        return typeof arg == "undefined" ? this : null;
		    // Getter overload (no argument passed)
		    if (!arg) {
		        return this[0].outerHTML || 
		            (ret = this.wrap('<div>').parent().html(), this.unwrap(), ret);
		    }
		    // Setter overload
		    $.each(this, function (i, el) {
		        var fnRet, 
		            pass = el,
		            inOrOut = el.outerHTML ? "outerHTML" : "innerHTML";

		        if (!el.outerHTML)
		            el = $(el).wrap('<div>').parent()[0];

		        if (jQuery.isFunction(arg)) { 
		            if ((fnRet = arg.call(pass, i, el[inOrOut])) !== false)
		                el[inOrOut] = fnRet;
		        }
		        else
		            el[inOrOut] = arg;

		        if (!el.outerHTML)
		            $(el).children().unwrap();
		    });

		    return this;
		},
		/**
		 * form元素使用AJAX提交，此方法会寻找元素内的所有输入控件的数据进行提交，并允许自行追加要提交的数据
		 */
		ajaxSubmit:function(url, data, callback){
			if($.isFunction(data)){
				callback = data;
			}
			var form = $(this).is('form')?$(this):$(this).find('form');
			if(form.size()>0){
				form.submit(function() {
					var submits = $(this).find("[type='submit']:enabled");
					submits.attr("disabled",true).postJSON(url,$.toData(form.serializeArray(),data),function(json){
						$(this).attr("disabled",false);
						if($.isFunction(callback)){
							callback.call(form,json);
						}
					});
					return false;
				});
			}
		},
		/**
		 * 以get的方式请求数据，同时相关元素disabled掉
		 */
		getJSON:function( url, data, callback ){
			if($.isFunction(data)){
				callback = data;
			}
			var $this = $(this);
			$this.attr('disabled',true);
			$.getJSON(url,data,function(json){
				$this.attr('disabled',false);
				if($.isFunction(callback)){
					callback.call($this,json);
				}
			});
		},
		/**
		 * 以post的方式请求数据，同时相关元素disabled掉
		 */
		postJSON:function( url, data, callback ){
			if($.isFunction(data)){
				callback = data;
			}
			var $this = $(this);
			$this.attr('disabled',true);
			$.postJSON(url,data,function(json){
				$this.attr('disabled',false);
				if($.isFunction(callback)){
					callback.call($this,json);
				}
			});
		},
		error : function(message) {
			var agents = $(this).data('ui-validateError-agents');
			if (agents) {
				agents._validateError(message);
			} else {
				var destroy = $(this).data("ui-validate.error-destroy");
				if (!destroy) {
					destroy = function() {
						$(this).tooltip('destroy').removeData("bs.tooltip")
								.next(".tooltip").remove();
					}
					$(this).data("ui-validate.error-destroy", destroy);
				}
				$(this).tooltip('destroy').removeData("bs.tooltip").next(
						".tooltip").remove();
				$(this).tooltip({
					title : message,
					placement : 'bottom'
				}).off("mouseout", destroy).on("mouseout", destroy).one(
						'shown.bs.tooltip', function() {
							$(this).focus();
						}).tooltip('show');
			}
			return $(this);
		}
	});
	
	// 为所有的控件添加valuechange事件
	$(function(){
		var valuechange = function(e){
			if(e.type==='propertychange' && e.originalEvent.propertyName.toLowerCase () !== "value"){
				return;
			}
			if(e.type==='click'){
				if(this.attachEvent) {
					// 用于监听IE9的剪切事件
					var $this = $(this);
					var oncut = $this.data("valuechange.fn.oncut");
					if(!$.isFunction(oncut)){
						oncut = function() {
							setTimeout(function() {
								$this.trigger('input');
							},100);
						};
						$this.data("valuechange.fn.oncut",oncut);
					}
					this.detachEvent("oncut",oncut);
					this.attachEvent("oncut",oncut);
				}
				return;
			}
			var oldValue = $(this).data("valuechange.value");
			var value = $(this).val();
			if(oldValue!=value){
				$(this).data("valuechange.value",value);
				$(this).trigger("valuechange");
			}
		}
		
		var events = "click.valuechange input.valuechange propertychange.valuechange keyup.valuechange";
		$(document.body).off(events).on(events,"input,select,textarea",valuechange);
	});
	
	
	// 初始化
	$(function() {
		// 让没有placeholder的浏览器也能模拟placeholder功能
		if (!Modernizr.input.placeholder){
			$("input[type='text'],textarea").placeholder();
		}
	});
}(jQuery);