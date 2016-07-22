$$(function(v,f,$rpc){
	// 查询样例
	v.example = {};
	// 查询操作
	f.setOwn = function(own){
		v.example.own = own;
		v.meetups = $rpc.call("meetupWebRpc.findMeetups",v.example);
	}
	f.setOwn(false);
	// 添加圈子活动
	f.joinMeetup = function(index){
		$rpc.call("meetupWebRpc.joinMeetup",v.meetups[index]).$then(function(json){
			$alert("加入活动成功",function(){
				v.meetups[index] = json;
			});
		}).$fail(function(json){
			$alert(json.error);
		});
	}
});