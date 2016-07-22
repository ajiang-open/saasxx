+function(moduleName){
	angular.module(moduleName, [ 'ionic','config','ionic-ui','auto-router','ionic-validate','security','rpc'])
	.run(function($ionicPlatform,$rootScope,$rpc,host,$window,$location) {
		//极光推送end
		$ionicPlatform.ready(function() {
			// Hide the accessory bar by default (remove this to
			// show the
			// accessory bar above the keyboard
			// for form inputs)
			if (window.cordova && window.cordova.plugins
					&& window.cordova.plugins.Keyboard) {
				cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
				cordova.plugins.Keyboard.disableScroll(true);
			}
			if (window.StatusBar) {
				// org.apache.cordova.statusbar required
				StatusBar.styleLightContent();
			}
		});
		
	})
	.config(function($ionicConfigProvider, $urlRouterProvider,$autoRouterProvider,$rpcProvider,api,urls,version) {
		// 设置视图缓存数量
		$ionicConfigProvider.views.maxCache(100);
		// 配置导航栏的位置，统一设置在尾部
		$ionicConfigProvider.tabs.position("bottom");
		// 设置导航栏标题位置
		$ionicConfigProvider.navBar.alignTitle("center");
		// 安卓环境下禁止JS滚动
		if(!ionic.Platform.isIOS()){
			$ionicConfigProvider.scrolling.jsScrolling(false);
		}
		// 设置rpc
		$rpcProvider.url(api.webRpc);
		// 设置版本号，用以控制缓存
		$autoRouterProvider.version(version);
		// 设置路由控制器配置
		$autoRouterProvider.resolve({
			check : [ "security",function(security) {
				var checkUrl = api.security;
				var loginUrl = "/account/signin";
				return security.check(checkUrl, loginUrl,{
					"/account/index":true
				});
			} ]
		});
		$autoRouterProvider.config(urls);
		// 设置默认页面
		$urlRouterProvider.otherwise('/meetup/index');
	});
}("cmeetup");


