$$(function(v,f,$rpc){
	var circle = v.circle = {};
	var find = f.find = function(){
		v.circles = $rpc.call("circleWebRpc.findCircles",circle);
	}
});