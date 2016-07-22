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
	// 分页指令
	module.directive('pageUi',function($parse){
		var pageFun = function(oPage){
			var oPage = {
				allPage: oPage.total,
				listPage: oPage.size,
				nowPage: oPage.index,
			},
			pageNums = [],
			at = 0;
		if(oPage.allPage < oPage.listPage){
			for (var i = 1; i <= oPage.allPage; i++) {
				if(i == oPage.nowPage){
					at = i;
				}
				pageNums[i-1] = i;
			}
		}else{
			for(var i = 1; i <= oPage.listPage; i++){

				if(oPage.nowPage < (oPage.listPage + 1 ) / 2){
					if(i == oPage.nowPage){
						at = i;
					}
					pageNums[i-1] = i;
				}else
				if(oPage.allPage - oPage.nowPage < Math.floor((oPage.listPage + 1) / 2)){
					if((oPage.listPage - i) == (oPage.allPage - oPage.nowPage)){
						at = i;
					}
					pageNums[i-1] = (oPage.allPage - oPage.listPage + i);
				}else{
					if(i == Math.floor((oPage.listPage + 1) / 2)){
						at = i - 1;
					}
					pageNums[i-1] = (oPage.nowPage - Math.floor((oPage.listPage) / 2) + i);
				}
			}
		}
		return {
			pageNums: pageNums,
			at: at
		};
	}
	return {
			restrict: 'E',
			replace: true,
			scope: {
				index: '@',
				total: '@',
				size: '='
			},
			template: '<div class="gc-page-ui"><ul>'
				+'<li ng-show="index > size/2 && total > size"><span ng-click="$go$(1)">1</span></li>'
				+'<li ng-show="index > 0" class="gc-page-ui-prev"><span ng-click="$go$(index)"></span></li>'
				+'<li ng-class="{\'on\': $index == at}" ng-hide="pageNums.length == 1" ng-repeat="pageNum in pageNums"><span ng-click="$go$(pageNum)">{{pageNum}}</span></li>'
				+'<li ng-show="total - index > 1 "  class="gc-page-ui-next"><span ng-click="$go$((index - 0) + 2)"></span></li>'
				+'<li ng-show="total - index > ((size - 0) + 1)/2 && total > size"><span ng-click="$go$(total)">{{total}}</span></li>'
				/*+'<li ng-hide="pageNums.length <= 1"><span>共{{total}}页</span></li>'*/
				+'</ul></div>',
			link: function(scope,element,attrs){
				var oPage = pageFun({
					index: scope.index || 1,
					total: scope.total || 10,
					size: scope.size || 7
				});

				 var goFun = scope.$go$ = function(page){
					var go = $parse(attrs.go);
						go(scope.$parent,{page: page});

						oPage = pageFun({
							index: page,
							total: scope.total || 10,
							size: scope.size || 7
						});
				}
				scope.$watch('index',function(){
					oPage = pageFun({
						index: scope.index || 1,
						total: scope.total || 10,
						size: scope.size || 7
					});
					scope.pageNums = oPage.pageNums;
					scope.at = oPage.at;
				});
				scope.$watch('total',function(){
					oPage = pageFun({
						index: scope.index || 1,
						total: scope.total || 10,
						size: scope.size || 7
					});
					scope.pageNums = oPage.pageNums;
					scope.at = oPage.at;
				});
				scope.$watch('size',function(){
					oPage = pageFun({
						index: scope.index || 1,
						total: scope.total || 10,
						size: scope.size || 7
					});
					scope.pageNums = oPage.pageNums;
					scope.at = oPage.at;
				});
				var oBody = angular.element(document.getElementsByTagName('body')[0]);
				oBody.on('keydown',function(e){
					//39
					//37
					if(e.keyCode == 39){
						if(scope.index > scope.total - 2){
							return;
						}
						goFun((scope.index - 0) + 2);
					}else
					if(e.keyCode == 37){
						if(scope.index <= 0){
							return;
						}
						goFun(scope.index);
					}
				});
			}
		}
	});
	// 分页指令
	module.directive('page', function($injector,$parse) {
		return {
			restrict : 'E',
			scope:true,
			replace:true,
			template : '<ul class="pagination page">'
				+'<li ng-if="$attrs$.start>0"><a href="javascript:void(0);" data-page="0" ng-click="$go$(0)" aria-label="First"><span aria-hidden="true">1</span></a></li>'
				+'<li ng-if="$attrs$.start>1"><a href="javascript:void(0);" title="上{{$attrs$.halfSize*2}}页" ng-click="$go$($attrs$.number-$attrs$.halfSize*2)" aria-label="PrevSize"><span aria-hidden="true">«</span></a></li>'
				+'<li ng-if="$attrs$.start>1"><a href="javascript:void(0);" title="上一页" ng-click="$go$($attrs$.number-1)" aria-label="Prev"><span aria-hidden="true">‹</span></a></li>'
				+'<li ng-repeat="$page$ in $pages$"><a href="javascript:void(0);" data-page="{{$page$}}" ng-class="$attrs$.number==$page$?\'current-page\':\'\'" ng-click="$go$($page$)" aria-label="{{$page$}}"><span aria-hidden="true">{{$page$+1}}</span></a></li>'
				+'<li ng-if="$attrs$.end<$attrs$.total-1"><a href="javascript:void(0);" title="下一页" ng-click="$go$($attrs$.number+1)" aria-label="Next"><span aria-hidden="true">›</span></a></li>'
				+'<li ng-if="$attrs$.end<$attrs$.total-1"><a href="javascript:void(0);" title="下{{$attrs$.halfSize*2}}页" ng-click="$go$($attrs$.number+$attrs$.halfSize*2)" aria-label="NextSize"><span aria-hidden="true">»</span></a></li>'
				+'<li ng-if="$attrs$.end<$attrs$.total"><a href="javascript:void(0);" data-page="{{$attrs$.total-1}}" ng-class="$attrs$.number==$attrs$.total?\'current-page\':\'\'" ng-click="$go$($attrs$.total-1)" aria-label="End"><span aria-hidden="true">{{$attrs$.total}}</span></a></li>'
				+'</ul>',
			link:function(scope, iElement, iAttrs, controller){
				scope.$pages$ = [];
				scope.$attrs$ = iAttrs;
				scope.$go$ = function(number){
					var go = iAttrs.go;
					if(go){
						go = $parse(go,null,true);
					}
					if(typeof go === 'function'){
						var total = parseInt(scope.$attrs$.total);
						if(!isNaN(total) && total>0){
							if(number<0){
								number = 0;
							}
							else if(number>=total){
								number = total-1;
							}
							go(scope,{number:number,page:number+1});
						}
					}
				};
				var reset = function(){
					var total = parseInt(scope.$attrs$.total);
					if(!isNaN(total) && total>0){
						var number = parseInt(scope.$attrs$.number);
						number = isNaN(number)?0:number>=total?total-1:number;
						scope.$attrs$.number = number;
						var halfSize = parseInt(scope.$attrs$.halfSize);
						halfSize = isNaN(halfSize)?5:halfSize;
						scope.$attrs$.halfSize = halfSize;
						scope.$pages$ = [];
						var start = number-halfSize;
						if(start<0){
							start = 0;
						}
						var end = start+halfSize*2+1;
						if(end>total){
							end = total;
							start = end - halfSize*2-1;
							if(start<0){
								start = 0;
							}
						}
						for(var i = start;i<end;i++){
							scope.$pages$.push(i);
						}
						scope.$attrs$.start = start;
						scope.$attrs$.end = end;
					}
					else {
						scope.$pages$ = [];
						scope.$attrs$.total = 0;
					}
				}
				scope.$watch("$attrs$.total",function(newValue,oldValue){
					reset();
				});
				scope.$watch("$attrs$.number",function(newValue,oldValue){
					reset();
				});
				scope.$watch("$attrs$.index",function(newValue,oldValue){
					var index = parseInt(newValue);
					if(!isNaN(index)){
						scope.$attrs$.number = index-1;
					}
					reset();
				});
				scope.$watch("$attrs$.size",function(newValue,oldValue){
					var size = parseInt(newValue);
					if(!isNaN(size)){
						size = size>0?size:0;
						scope.$attrs$.halfSize = Math.ceil(size/2);
					}
					reset();
				});
				// 支持键盘左右方向键翻页操作
				$(document).keydown(function(event) {
					var number = parseInt(scope.$attrs$.number);
					number = isNaN(number) ? 0 : number;
					var total = parseInt(scope.$attrs$.total);
					if (event.keyCode == 37) {
						// 左方向键
						number = number-1<0?0:number-1;
					} else if (event.keyCode == 39) {
						// 又方向键
						number = number+1>=total?total-1:number+1;
					}
					else {
						return true;
					}
					// 点击页码动作
					$("a[data-page='"+number+"']:not(:hidden)",iElement).click();
				});
			}
		};
	});
	// 文件指令
	module.directive('upload', function($injector,$parse) {
		return {
			restrict : 'E',
			scope:true,
			replace:true,
			template : '<div class="upload" ng-transclude></div>',
			transclude:true,
			link:function(scope, iElement, iAttrs, controller){
				// 设置变量
				scope.$attrs$ = iAttrs;
				// 设置好定位和布局
				var upload = $(iElement).children();
				upload.each(function(){
					var $this = $(this);
					var file = $('<input type="file" style="display:none;">').insertAfter($this);
					file.css({
						position:"absolute",
						display : "block",
						opacity: 0,
						cursor: "pointer",
						"z-index":10,
						filter:"alpha(opacity=0)"
					});
					var loob = setInterval(function(){
						if($this.is(":hidden")){
							return;
						}
						file.attr("multiple",iAttrs.multiple)
						.width($this.width())
						.height($this.height())
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
						xhr.open('post', iAttrs.url); // url 为提交的后台地址
						xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
						// 处理进度事件
						xhr.upload.addEventListener("progress", function(event){
							var progress = iAttrs.progress;
							if(progress){
								progress = $parse(progress,null,true);
								if(typeof progress === 'function'){
									var loaded = event.loaded;
									var total = event.total;
									progress(scope,{
										loaded:loaded,
										total:total
									});
								}
							}
						}, false);
						// 处理完成事件
						xhr.addEventListener("load", function(event){
							file.attr("disabled",false);
							var json = eval('(' + xhr.responseText + ')');
							if(json.status){
								var done = iAttrs.done;
								if(done){
									done = $parse(done,null,true);
									if(typeof done === 'function'){
										done(scope,{
											data:json.data
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
	// 文件指令 修改
	module.directive('upLoad', function($injector,$parse) {
		return {
			restrict : 'E',
			scope:true,
			replace:true,
			template : '<div class="upload" ng-transclude></div>',
			transclude:true,
			link:function(scope, iElement, iAttrs, controller){
				// 设置变量
				scope.$attrs$ = iAttrs;
				// 设置好定位和布局
				var htmlFile = '<input type="file" style="height: 100%; position: absolute; top: 0; bottom: 0; left: 0; right: 0; opacity: 0;">';

				$(iElement)
				.css({
					'position': 'absolute',
					'top': 0,
					'bottom': 0,
					'left': 0,
					'right': 0,
					'cursor': 'pointer',
					'overflow': 'hidden'
				})
				.parent().css('position','relative');
				var file = $(htmlFile).appendTo(iElement);
				var $this = $(iElement);
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
						xhr.open('post', iAttrs.url); // url 为提交的后台地址
						xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
						// 处理进度事件
						xhr.upload.addEventListener("progress", function(event){
							var progress = iAttrs.progress;
							if(progress){
								progress = $parse(progress,null,true);
								if(typeof progress === 'function'){
									var loaded = event.loaded;
									var total = event.total;
									progress(scope,{
										loaded:loaded,
										total:total
									});
								}
							}
						}, false);
						// 处理完成事件
						xhr.addEventListener("load", function(event){
							file.attr("disabled",false);
							var json = eval('(' + xhr.responseText + ')');
							if(json.status){
								var done = iAttrs.done;
								if(done){
									done = $parse(done,null,true);
									if(typeof done === 'function'){
										done(scope,{
											data:json.data
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
	//监控ng-repeat渲染完成
	module.directive('onFinishRenderFilters', function ($timeout){
	    return {
	        restrict: 'A',
	        scope: true,
	        link: function(scope, element, attr) {
	            if (scope.$last === true) {
	                $timeout(function() {
	                    scope.$emit('ngRepeatFinished');
	                });
	            }
	        }
	    };
	});
	//时间
	module.directive('dateTime',function($parse){
			return {
				restrict: 'E',
				replace: true,
				template: '<input type="text" readonly class="form-control" />',
				scope: true,
				link: function(scope,element,attrs){
					var id = Math.floor(Math.random()*10) + new Date().getTime();
						element.attr('id',id);
						element.parent().addClass('datep');
						scope.$attrs$ = attrs;
						if(attrs.value){
							scope.$watch("$attrs$.value",function(newValue,oldValue){
								element.val(newValue);
							});
						}
					var fun = attrs.fun;
						fun = $parse(fun);

					var format = attrs.format?attrs.format:'YYYY-MM-DD hh:mm:ss',           //时间格式
						isinitVal = attrs.isinitval?scope.$eval(attrs.isinitval):false,
						isTime = attrs.istime?scope.$eval(attrs.istime):false,
						isClear = attrs.isclear?scope.$eval(attrs.isclear):true,
						festival = attrs.festival?scope.$eval(attrs.festival):false,        //节假日
						zIndex = attrs.zindex?attrs.zindex:1900,
						marks = attrs.marks?attrs.marks:null,
						minDate = attrs.mindate?attrs.mindate:jeDate.now(0),
						maxDate = attrs.maxdate?attrs.maxdate:"2099-12-31 23:59:59";

						if(scope.$eval(attrs.ismindate)){                                   //最小时间的限制
							minDate = '1977-01-01';
						}

					var model = $parse(attrs.model);

					 var oJe = jeDate({
						dateCell: '#'+id,
						format: format,
						isinitVal:isinitVal,
						isTime:isTime, //isClear:false,
						minDate: minDate,
						isClear: isClear,
						festival: festival,
						marks: marks,
						zIndex: zIndex,
						maxDate: maxDate,
						okfun:function(v){
							if(typeof attrs.model == 'string'){
								model.assign(scope,v)
							}
							fun(scope,{
								t: v
							});
						},
						choosefun: function(v){
							if(typeof attrs.model == 'string'){
								model.assign(scope,v)
							}
							fun(scope,{
								t: v
							});
						},
						clearfun:function(v){
							if(typeof attrs.model == 'string'){
								model.assign(scope,null)
							}
							fun(scope,{
								t: v
							});
						}
					});

					 element.on('blur',function(){
							oJe = null;
					});
				}
			}
	});
	module.directive('addressArea', ['$rpc','$parse',function ($rpc,$parse) {
		var style = '.addressAreaStyle{text-align: center; z-index: 100; position: absolute; border-left: 1px solid #ddd}';
			style += '.addressAreaStyle>div{top: 0; position: absolute; width: 100px; height: 148px; overflow-y: scroll; border: 1px solid #ddd; margin-left: -1px; background-color: #f0f0f0;}';
			style += '.addressAreaStyle>div>ul{border-right: 1px solid #ddd;}';
			style += '.addressAreaStyle>div>ul>li{padding: 4px; cursor: pointer;}';
			style += '.addressAreaStyle>div>ul>li:hover{color: #999;}';
			style += '.addressAreaStyle>div>ul>li.active{background-color: #ddd;}';
		var html = '<div style="display: inline-block; position: relative;" ng-click="f.clickInputArea($event)"><style type="text/css">';
			html += style+'</style><input readonly class="form-control" ng-model="v.fullNmae"><i ng-if="v.isremove" ng-click="f.remove($event)" class="glyphicon glyphicon-remove" style="color: #999; position: absolute; z-index: 2; right: 0; top: 0; padding: 10px;"></i><div class="addressAreaStyle" ng-if="v.areaShow" style="">';
			html += '<div style="left: 0;" ng-if="v.Area.provinces"><ul style=""><li ng-class="{\'active\': d.id == v.initArea.provinceId}" ng-click="f.clickSelect($event,d,\'citys\')" ng-repeat="d in v.Area.provinces" style="">{{d.name}}</li></ul></div>';
			html += '<div style="left: 100px;" ng-if="v.Area.citys"><ul style=""><li ng-class="{\'active\': d.id == v.initArea.cityId}" ng-click="f.clickSelect($event,d,\'regions\')" ng-repeat="d in v.Area.citys" style="">{{d.name}}</li></ul></div>';
			html += '<div style="left: 200px;" ng-if="v.Area.regions"><ul style=""><li ng-class="{\'active\': d.id == v.initArea.regionId}" ng-click="f.clickSelect($event,d)" ng-repeat="d in v.Area.regions" style="">{{d.name}}</li></ul></div>';
			html += '</div></div>';
		return {
			restrict: 'E',
			scope: true,
			transclude: true,
			template: html,
			replace: true,
			link: function(scope,iElement,iAttrs){
				var ofs = iElement.offset();
					iElement.css({
						position: 'relative'
					});
				var model = $parse(iAttrs.model),
					done = $parse(iAttrs.done),
					initval = iAttrs.initval ? scope.$eval(iAttrs.initval) : true,           //是否显示默认地区
					value = iAttrs.initid ? scope.$eval('('+iAttrs.initid+')'): null,        //加载指定默认地址
					body = angular.element(document.querySelector('body'));

				var v = scope.v = {},
					d = scope.d = {};
				//全称
				v.fullNmae = '';

				v.isremove = iAttrs.isremove ? scope.$eval(iAttrs.isremove) : false;   //是否显示清空按钮
				v.areaShow = false;
				//放置前台显示数据
				v.Area = {};
				//放置初始地址
				v.initArea = {};
				//放置选中的地域
				var sA = d.selectArea = {};
				scope.f = {
					clickSelect : function($event,obj,n){
						angular.element($event.target).addClass('active').siblings().removeClass('active');

						$rpc.call('areaWebRpc.findRootOrChildrens',obj.id)
						.$then(function(data){
							switch (n) {
								case 'citys':
									sA.provinces = angular.copy(obj);

									v.fullNmae = sA.provinces.name;

									v.initArea.provinceId = sA.provinces.id;
									v.initArea.province = sA.provinces.name;
									delete sA.citys
									delete sA.regions
									delete v.Area.citys
									delete v.Area.regions
									delete v.initArea.regionId
									delete v.initArea.region
									break;
								case 'regions':
									sA.citys = angular.copy(obj);
									if($.isEmptyObject(sA.provinces)){
										v.fullNmae = angular.copy(v.initArea.province) + sA.citys.name;
									}else{
										v.initArea.cityId = sA.citys.id;
										v.initArea.city = sA.citys.name;

										v.fullNmae = sA.provinces.name + sA.citys.name;
									}
									delete sA.regions
									delete v.Area.regions
									break;
								default:
									sA.regions = angular.copy(obj);
									if($.isEmptyObject(sA.provinces) || !sA.provinces|| $.isEmptyObject(sA.citys)){

										//判断是否存在市的时候
										if($.isEmptyObject(sA.citys)){
											//全称
											v.fullNmae = v.initArea.province + v.initArea.city + sA.regions.name;
											v.initArea.regionId = sA.regions.id;
											v.initArea.region = sA.regions.name;
										}else{

											//全称
											v.fullNmae = v.initArea.province + sA.citys.name + sA.regions.name;

											v.initArea.cityId = sA.citys.id;
											v.initArea.city = sA.citys.name;
											v.initArea.regionId = sA.regions.id;
											v.initArea.region = sA.regions.name;
										}
									}else{
										v.initArea.regionId = sA.regions.id;
										v.initArea.region = sA.regions.name;
										v.fullNmae = sA.provinces.name + sA.citys.name + sA.regions.name;
									}
									break;
							}
							// console.log(sA);
							//当有数据的时候
							if(data){
								v.Area[n] = angular.copy(data);
							}else{
								if(iAttrs.model){
									model.assign(scope,v.fullNmae);
								}
								v.initArea.fullName = angular.copy(v.fullNmae);
								//iElement.find('input').val(v.fullNmae);
								done(scope,{d:v.initArea});
								v.areaShow = false;
							}

						})
						$event.stopPropagation();
						return false;
					},
					clickInputArea: function($event){
						v.areaShow = true;
						$event.stopPropagation();
						return false;
					},
					hideArea: function($event){
						v.areaShow = false;
 					},
 					remove: function($event){
 						v.fullNmae = '';
 						done(scope,null);
 						v.areaShow = false;
 						$event.stopPropagation();
						return false;
 					}
				};

				body.bind('click',function(){
					scope.$apply(scope.f.hideArea)
				});

				scope.$watch(iAttrs.initid,function(){
					$rpc.calls(function(){
						$rpc.call('areaWebRpc.initArea',value)
						.$then(function(data){
							if(data){
								v.Area.provinces = angular.copy(data.provinces);
								v.Area.citys = angular.copy(data.citys);
								v.Area.regions = angular.copy(data.regions);
								v.initArea = angular.copy(data.defaultArea);
								if(initval){
									v.fullNmae = angular.copy(v.initArea.fullName);

									if(iAttrs.model){
										model.assign(scope,v.fullNmae);
									}

									done(scope,{d:v.initArea});
								}
							}
						},function(err){
							console.error(err);
						});

					});
				});
			}
		};
	}])
	//图片加载缓慢是出现
	module.directive('img',function(){
		return {
			restrict: 'E',
			scope: true,
			compile: function(){
			return {
				pre: function(scope,element,attrs){
					 var img = new Image(),
				 	  $this = $(element).attr('src','app/theme/images/publics/catLoading.gif');
			 	  	  $(img).attr('src',attrs.src);
					 $(img).on('error',function(e){
					 	 //$this.attr('src','app/theme/images/publics/default.png');
					 	$this.attr('src','app/theme/images/publics/catLoading.gif');
					 });
					  $(img).on('load',function(){
					  	$this.attr('src',attrs.src);
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
// 手机遮罩
module.filter('cover_phone', [function() {
	return function(num) {
		return num.slice(0, 3) + '****' + num.slice(-5, -1);
	}
}])

//解析文本
module.directive('parseTxt',function(){
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

							$this.append("<img height='" + actualHeight +  "' src='" + scope.host + scope.content + "?img:{height:" + actualHeight + "}' />");
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

}(angular, jQuery, "bootstrap-ui");
