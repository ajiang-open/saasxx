$$(function(v,f,$rpc,$stateParams,$location){
	// 圈子数据
	var circle = v.circle = {
			id:$stateParams.id,
			users:[]
	};
	var invite = v.invite = {};
	// 邀请用户
	var inviteUser = f.inviteUser =function(){
		var user = $.extend({}, invite);
		circle.users.push(user);
		for(var key in invite){
			delete invite[key];
		}
	}
	var deleteUser = f.deleteUser = function(index){
		circle.users.splice(index, 1);
	}
	// 激活圈子
	var activeCircle = f.activeCircle = function(){
		$rpc.call("circleWebRpc.activeCircle",circle).$then(function(json){
			$alert("圈子新建成功，前往圈子主页",function(){
				$location.path("/circle/index");
			});
		}).$fail(function(json){
			$alert(json.error);
		});
	}
});