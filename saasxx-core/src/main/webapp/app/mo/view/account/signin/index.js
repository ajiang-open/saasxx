$$(function(v,f,$rpc,security,$back){
	// 声明用户对象
	var user = v.user = {};
	// 声明登录函数
	var signin = f.signin = function(){
		$rpc.call("signInWebRpc.signin",user).$then(function(json){
			security.login(json.jwt);
			$alert("登录成功",function(){
				security.back();
			});
		}).$fail(function(json){
			$alert(json.error);
		});
	}
});