$$(function(v,f,$rpc,$interval,$location){
	// 声明用户对象
	var user = v.user = {};
    // 处理图片验证码事宜
    var key = v.key = $.id();
    var changeImageValidateCode = f.changeImageValidateCode = function () {
        key = v.key = $.id();
    }
    // 处理短信验证码事宜
    var sendSmsValidateCode = f.sendSmsValidateCode = function () {
    	log.info("sendSmsValidateCode");
        $rpc.call("userWebRpc.sendSmsValidateCode",user,{
            key:key,
            imageValidateCode:v.imageValidateCode+''
        }).$then(function (json) {
            v.smsClock = 60;
            var p = $interval(function () {
                if(v.smsClock<=0){
                    $interval.cancel(p);
                }
                else {
                    v.smsClock--;
                }
            },1000);
        }).$fail(function (json) {
            log.error(json);
            alert(json.error);
        });
    }
	// 声明注册函数
	var signup = f.signup = function(){
        $rpc.call("signUpWebRpc.step2",user).$then(function (json) {
            $location.path("/account/signup/step2/"+json.id);
        }).$fail(function (json) {
            log.error(json);
            alert(json.error);
        });
	}
});