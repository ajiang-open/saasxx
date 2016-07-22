$$(function(v,f,$rpc,$interval,$location,$stateParams){
	// 声明用户对象
	var user = v.user = {
			id:$stateParams.id,
			gender:'female',
			avatars:[]
	};
	// 设置用户可选择的兴趣爱好
    $rpc.call("hobbyWebRpc.findTopHobbies").$then(function(json){
    	user.hobbies = json;
    });
    // 处理文件上传
    var uploadComplete = f.uploadComplete = function(data,index){
    	if(typeof index === 'number'){
    		user.avatars[index] = data.paths[0];
    	}
    	else {
    		user.avatars.push(data.paths[0]);
    	}
    }
    // 处理文件删除
    var deleteImage = f.deleteImage = function(index){
    	user.avatars.splice(index, 1);
    }
	// 声明注册函数
	var signup = f.signup = function(){
        $rpc.call("signUpWebRpc.signup",v.user).$then(function(json){
        	$alert("注册成功，请登录！",function(){
        		$location.path("/account/signin");
        	});
        }).$fail(function(json){
        	$alert(json.error);
        });
	}
	
});