$$(function(v,f,$rpc,$stateParams){
	// 圈子数据
	var meetup = v.meetup = {
			circle:{
				id:$stateParams.id
			},
			avatars:[],
			address:{}
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
			log.info(areas);
		});
	}
	// 初始化省份
	changeArea('000000',address.provinces);
	// 处理省市区结束
    // 处理文件上传
    var uploadComplete = f.uploadComplete = function(data,index){
    	if(typeof index === 'number'){
    		meetup.avatars[index] = data.paths[0];
    	}
    	else {
    		meetup.avatars.push(data.paths[0]);
    	}
    }
    // 处理圈子保存
    var saveMeetup = f.saveMeetup = function(){
    	$rpc.call("meetupWebRpc.saveMeetup",meetup).$then(function(json){
    		$alert("发布完成，回到主页",function(){
    			$location.path("/");
    		});
    	}).$fail(function(json){
    		$alert(json.error);
    	});
    }
});