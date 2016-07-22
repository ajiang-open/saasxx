$$(function(v,f,$rpc){
	// 查询样例
	v.example = {};
	// 查询操作
	f.setOwn = function(own){
		v.example.own = own;
		v.circles = $rpc.call("circleWebRpc.findCircles",v.example);
	}
	f.setOwn(false);
	// 添加圈子活动
	f.joinCircle = function(index){
		$rpc.call("circleWebRpc.joinCircle",v.circles[index]).$then(function(json){
			$alert("加入圈子成功",function(){
				v.circles[index] = json;
			});
		}).$fail(function(json){
			$alert(json.error);
		});
	}
});