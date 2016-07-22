$$(function(v,f,$rpc,$stateParams,$location){
	// 圈子数据
	var circle = v.circle = {
			name:$stateParams.name,
			avatars:[],
			headquarter:{}
	};
	// 处理省市区开始
	// 地址数据
	var address = v.address = {
			provinces:[],
			cities:[],
			regions:[]
	}
	// 更改地区通用方法
	var changeArea = f.changeArea = function(){
		var args = Array.prototype.slice.call(arguments);
		var code = args[0];
		var areas = args[1];
		// 清空其它数组
		if(args.length>2){
			for(var i = 2;i<args.length;i++){
				var arg = args[i];
				if($.isArray(arg)){
					arg.length=0;
				}
			}
		}
		$rpc.call("addressWebRpc.findAreas",{
			code:code
		}).$then(function(json){
			areas.length = 0;
			areas.push.apply(areas, json);
		});
	}
	// 初始化省份
	changeArea('000000',address.provinces);
	// 处理省市区结束
	// 设置可选择的兴趣爱好
    $rpc.call("hobbyWebRpc.findTopHobbies").$then(function(json){
    	circle.hobbies = json;
    });
    // 处理文件上传
    var uploadComplete = f.uploadComplete = function(data,index){
    	if(typeof index === 'number'){
    		circle.avatars[index] = data.paths[0];
    	}
    	else {
    		circle.avatars.push(data.paths[0]);
    	}
    }
    // 处理圈子保存
    var saveCircle = f.saveCircle = function(){
    	$rpc.call("circleWebRpc.saveCircle",circle).$then(function(json){
    		$alert("注册完成，还差激活",function(){
    			$location.path("/circle/add/step3/"+json.id);
    		});
    	}).$fail(function(json){
    		$alert(json.error);
    	});
    }
});