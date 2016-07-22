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
	
	var base64ToBlob = function (base64Data, uploadType) {
	    uploadType = uploadType || '';
	    var sliceSize = 1024;
	    var byteCharacters = atob(base64Data);
	    var bytesLength = byteCharacters.length;
	    var slicesCount = Math.ceil(bytesLength / sliceSize);
	    var byteArrays = new Array(slicesCount);
	    for (var sliceIndex = 0; sliceIndex < slicesCount; ++sliceIndex) {
	        var begin = sliceIndex * sliceSize;
	        var end = Math.min(begin + sliceSize, bytesLength);
	        var bytes = new Array(end - begin);
	        for (var offset = begin, i = 0 ; offset < end; ++i, ++offset) {
	            bytes[i] = byteCharacters[offset].charCodeAt(0);
	        }
	        byteArrays[sliceIndex] = new Uint8Array(bytes);
	    }
	    return new Blob(byteArrays, { type: uploadType });
	}
	
	var module = ng.module(moduleName, []);
	
	// 文件指令
	module.directive('upload', function($injector,$parse,$ionicLoading) {
		return {
			restrict : 'E',
			scope:true,
			replace:true,
			template : '<div class="upload" ng-transclude></div>',
			transclude:true,
			link:function(scope, iElement, iAttrs, controller){
				// 设置变量
				scope.$attrs$ = iAttrs;
				// 设置样式
				if(iAttrs.style){
					$(iElement).attr(iAttrs.style);
				}
				// 设置好定位和布局
				var upload = $(iElement).children();
				if(upload.size()==0){
					upload = $(iElement).parent();
				}
				upload.each(function(){
					var $this = $(this);
					var file = $('<input type="file" style="display:none;">').insertAfter($this);
					file.css({
						position:"absolute",
						display : "block",
						opacity: 0,
						cursor: "pointer",
						"z-index":10,
						overflow: 'hidden',
						filter:"alpha(opacity=0)"
					});
					var loob = setInterval(function(){
						if($this.is(":hidden")){
							return;
						}
						file.attr("multiple",iAttrs.multiple)
						.width($this.outerWidth())
						.height($this.outerHeight())
						.offset($this.offset());
					}, 100);
					// 开始业务逻辑处理
					file.change(function(){
						var files = this.files;
						if(files.length==0){
							return;
						}
						// 处理选中文件的逻辑
						var change = iAttrs.change;
						if(change){
							change = $parse(change,null,true);
							if(typeof change === 'function'){
								change(scope,{
									files:files
								});
							}
						}
						var xhr = new XMLHttpRequest();
						var formData = new FormData();
						// 设置安全令牌
						var token = storage("$bearer-token$");
						token = token && token.indexOf(".")!=-1?token:"anonymous.anonymous";
						formData.append("$Authorization$","Bearer "+token);
						// 设置文件数据
						var filter = iAttrs.filter;
						if(filter){
							filter = $parse(filter,null,true);
						}
						// 设置图片转换质量
						var quality = parseFloat(iAttrs.quality);
						if(isNaN(quality)){
							quality = 0.9;
						}
						quality = quality<0.1?0.1:quality>1.0?1.0:quality;
						var fileArray = [];
						for(var i = 0;i<files.length;i++){
							// 过滤图片
							if(typeof filter === 'function'){
								if(!filter(scope,{
									file:files[i]
								})){
									continue;
								}
							}
							// 压缩图片
							if(files[i].type.indexOf("image/")!=-1){
								var width = parseInt(iAttrs.width);
								var height = parseInt(iAttrs.height);
								if(!isNaN(width) && !isNaN(height)){
									fileArray.push(files[i]);
									var image = new Image(width, height);
									image['name'] = files[i].name;
									image.onload = function(){
										var $this = this;
										var cavans = document.createElement("canvas");
										var ctx = cavans.getContext('2d');  
							            $(cavans).attr({width : width, height : height});
							            ctx.drawImage(image, 0, 0, width, height);
							            if(cavans.toBlob){
							            	cavans.toBlob(function(blob){
								            	formData.append('file', blob,$this['name']); // index 为第 n 个文件的索引
									            fileArray.pop();
									            if(fileArray.length==0){
									            	file.attr("disabled",true);
													xhr.send(formData);
												}
								            },"image/jpeg", quality);
							            }
							            else {
							            	var dataUrl = cavans.toDataURL("image/jpeg", quality);
							            	var base64 = dataUrl.substring(dataUrl.indexOf(",")+1);
							            	var blob = base64ToBlob(base64,"image/jpeg");
							            	formData.append('file', blob,$this['name']); // index 为第 n 个文件的索引
								            fileArray.pop();
								            if(fileArray.length==0){
								            	file.attr("disabled",true);
												xhr.send(formData);
											}
							            }
							            
									}
									var reader = new FileReader();
								    reader.onload = function (e) {
								    	image.src = e.target.result;
								    };
								    reader.readAsDataURL(files[i]);
									continue;
								}
							}
							formData.append('file', files[i]); // index 为第 n 个文件的索引
						}
						// 设置参数数据
						if(iAttrs.data){
							var data = typeof iAttrs.data === 'string'?eval('('+iAttrs.data+')'):iAttrs.data;
							formData.append("data",JSON.stringify(data));
						}
						//添加其它属性
						for(var name in iAttrs){
							if("|url|change|progress|done|error|filter|multiple|width|height|quality|data|".indexOf("|"+name+"|")==-1){
								var value = iAttrs[name];
								if(typeof value==='string'){
									formData.append(name,value);
								}
							}
						}
						// 开始提交文件数据
						$ionicLoading.show({
						      template: '{{$loading}}',
						      scope:scope
						});
						scope.$loading = "上传开始";
						xhr.open('post', iAttrs.url); // url 为提交的后台地址
						xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
						// 处理进度事件
						xhr.upload.addEventListener("progress", function(event){
							if(!event.lengthComputable){
								return;
							}
							var loaded = event.loaded;
							var total = event.total;
							scope.$apply(function(){
								scope.$loading = "上传"+(loaded*100/total).toFixed(2)+"%";
							});
							var progress = iAttrs.progress;
							if(progress){
								progress = $parse(progress,null,true);
								if(typeof progress === 'function'){
									progress(scope,{
										loaded:loaded,
										total:total
									});
								}
							}
						}, false); 
						// 处理完成事件
						var tempFiles = [];
						for(var i = 0;i<files.length;i++){
							tempFiles.push(files[i]);
						}
						xhr.addEventListener("load", function(event){
							$ionicLoading.hide();
							file.attr("disabled",false);
							var json = eval('(' + xhr.responseText + ')');
							if(json.status){
								var done = iAttrs.done;
								if(done){
									done = $parse(done,null,true);
									if(typeof done === 'function'){
										done(scope,{
											data:json.data,
											meta:function(fn,index){
												if($.isFunction(fn)){
													index = isNaN(index)?0:index;
													if((index<0 || index>=tempFiles.length)){
														alert('序号必须在0到'+tempFiles.length+'之间');
													}
													if(tempFiles[index].type.indexOf("image/")!=-1){
														var reader = new FileReader();
														reader.onload = function(e){
															var img = new Image();
															img.onload = function(){
																fn.call(this);
															};
															img.src = e.target.result;
														}
														reader.readAsDataURL(tempFiles[index]);
													}
												}
											}
										});
									}
								}
							}
							else {
								var error = iAttrs.error;
								if(error){
									error = $parse(error,null,true);
									if(typeof error === 'function'){
										error(scope,{
											xhr:xhr,
											event:event,
											error:json.error,
											errorType:json.errorType
										});
									}
								}
							}
						}, false); 
						// 处理失败事件
						xhr.addEventListener("error", function(event){
							$ionicLoading.hide();
							file.attr("disabled",false);
							var error = iAttrs.error;
							if(error){
								error = $parse(error,null,true);
								if(typeof error === 'function'){
									error(scope,{
										xhr:xhr,
										event:event
									});
								}
							}
						}, false);
						// 开始上传数据
						if(fileArray.length==0){
							file.attr("disabled",true);
							xhr.send(formData);
						}
						this.value = null;
						return false;
					});
				});
			}
		};
	});
	// 自动完成指令强化
	module.directive('autocomplete',
		function() {
			return {
				restrict : 'A',
				link : function link(scope, el, attrs) {
					if(typeof attrs.autocomplete === 'string' && attrs.autocomplete.trim().toLowerCase()==='off'){
						// password fields need one of the same type above it
						// (firefox)
						var type = el.attr('type') || 'text';
						// chrome tries to act smart by guessing on the name..
						// so replicate a shadow name
						var name = el.attr('name') || '';
						var shadowName = name + '_autocomplete_off_shadow';
						// trick the browsers to fill this innocent silhouette
						var shadowEl = angular.element('<input type="' + type
								+ '" name="' + shadowName
								+ '" style="display: none">');
						// insert before
						el.parent()[0].insertBefore(shadowEl[0], el[0]);
					}
				}
			};
		});
	
	// 解析文本
	module.directive('parseTxt',function($compile){
		return {
			restrict: 'A',
			scope: {
				content: '@',
				messagetype: '@',
				host: '@',
				imgheight: '@'
			},
			compile:function(telement,tAttr){
				return {
					post: function(scope,iElement,iAttr){
						var $this = angular.element(iElement);
						
						scope.$watch('content',function(){
							$this.empty();
							
							if(scope.messagetype == "img"){         // 图片
								var standardHeight = 120;           // 图片最高高度
								var actualHeight = 0;               // 最终图片设置高度
								
								if(scope.imgheight <= standardHeight){
									actualHeight = scope.imgheight;
								}else{
									actualHeight = standardHeight;
								}
								var html = $compile("<img zoom-img='"+scope.host + scope.content+"' height='" + actualHeight +  "' src='" + scope.host + scope.content + "?img:{height:" + actualHeight + "}' />")(scope)
								$this.append(html);
							}else if(scope.messagetype == "txt"){   // 纯文本
								var textMessage = Easemob.im.Helper.parseTextMessage(scope.content);
								var contents = textMessage.body;
								
								angular.forEach(contents,function(item,index){
									if(item.type == "emotion"){    // 表情
										$this.append('<img class="emotion-icon" style="width: 20px" src="' + item.data + '" />');
									}else if(item.type == "txt"){  // 纯文本
										$this.append('<span>' + item.data + '</span>');
									}
								});
							}
						});
					}
				}
			}
		}
	});
	
	//字符串转义为html
	module.filter('to_trusted', ['$sce', function ($sce) {
		return function (text) {
			return $sce.trustAsHtml(text);
		};
	}]);
	
	module.directive('zoomImg',function($timeout,$compile,$ionicSlideBoxDelegate){
		return {
			restrict: 'A',
			scope: {
				zoomImg: '@'
			},
			compile: function(tElement,tAttr){
				
				return {
					post: function(scope,iElement,iAttrs){
						var v = scope.v = {},
							d = scope.d = {},
							f = scope.f = {};
						var imgList,template,BgElement;

						scope.$watch('zoomImg',function(newVale){
							if(newVale != '' && newVale != null && newVale != 'undefined'){
								try {
									imgList = scope.zoomImg?scope.$eval(scope.zoomImg): null;
								} catch(e) {
									// console.log(e);
									imgList = scope.zoomImg;
								}

								if($.isArray(imgList)){
									v.imgList = imgList;
									template = '<div ng-click="f.hideImg()" class="zoomImgModal" style=""><ion-slide-box show-pager="true"><ion-slide ng-repeat="src in v.imgList"><ion-scroll zooming="true" overflow-scroll="false"scrollbar-x="false" scrollbar-y="false" min-zoom="1" min-zoom="3" direction="xy"ng-click="closeBigImg()"style="min-width: 320px; width: 100%; min-height: 568px; height: 100%; background-color: transparent;"><div style="width: 100%; height: 100%; min-width: 320px; min-height: 568px;background-size: 100%;background: url({{host}}{{src}}?img:{width:414,height:414}) no-repeat center;" /></ion-scroll></ion-slide></ion-slide-box></div>';
								}else{
									v.img = imgList;
									template = '<div ng-click="f.hideImg()" class="zoomImgModal" style=""><ion-scroll zooming="true" overflow-scroll="false"scrollbar-x="false" scrollbar-y="false" min-zoom="1" min-zoom="3" direction="xy"ng-click="closeBigImg()"style="min-width: 320px; width: 100%; min-height: 568px; height: 100%; background-color: transparent;"><div style="width: 100%; height: 100%; min-width: 320px; min-height: 568px;background-size: 100%;background: url({{host}}{{v.img}}?img:{width:414,height:414}) no-repeat center;" /></ion-scroll></div>';
								}

								BgElement = $compile(template)(scope);
								BgElement.appendTo('body');
							}
						});
						
						iElement.on('click',function(){
							BgElement.show();
							
						});

						f.hideImg = function(){
							BgElement.hide();
						};

						scope.$on('$destroy',function(){
							BgElement.remove();
						});
					}
				}
			},
			
		}
	});
	
}(angular, jQuery, "ionic-ui");