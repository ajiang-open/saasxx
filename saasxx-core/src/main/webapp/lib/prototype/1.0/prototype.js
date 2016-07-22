+function(){
	'use strict';
	// 模拟console对象
	window.console = window.console || {};
	window.console.log = window.console.log || function(msg){
		window.status = msg;
	};
	window.console.info = window.console.info || function(msg){
		window.status = 'info:'+msg;
	};
	window.console.debug = window.console.debug || function(msg){
		window.status = 'debug:'+msg;
	};
	window.console.error = window.console.error || function(msg){
		window.status = 'error:'+msg;
	};
	window.console.warn = window.console.warn || function(msg){
		window.status = 'warn:'+msg;
	};
	// 将console对象指向log对象
	window['log'] = window.console;
	
	// 扩充Array的forEach方法
	Array.prototype.forEach = function(fn){
		if(this){
			for(var i = 0;i<this.length;i++){
				fn.call(this,this[i]);
			}
		}
	}
	
	// 扩充String的方法
	String.prototype.hashCode = function() {
		var hash = 0, i, chr, len;
		if (this.length === 0)
			return hash;
		for (i = 0, len = this.length; i < len; i++) {
			chr = this.charCodeAt(i);
			hash = ((hash << 5) - hash) + chr;
			hash |= 0; // Convert to 32bit integer
		}
		return hash;
	}
	
	String.prototype.trim = function() {
		return $.trim(this);
	}
	

	String.prototype.escapeXml = function() {
		return $('<div/>').text(this).html();
	}
	
	String.prototype.unescapeXml = function() {
		return $('<div/>').html(this).text();
	}
	
	String.prototype.startsWith = function(prefix) {
		return (this.substr(0, prefix.length) === prefix);
	}

	String.prototype.endsWith = function(suffix) {
		return (this.substr(this.length - suffix.length) === suffix);
	}

	String.prototype.contains = function(txt) {
		return (this.indexOf(txt) >= 0);
	}
	
	String.prototype.format = function(){
		if (arguments.length == 0)
	        return this;
	    var s = this;
	    for ( var i = 0; i < arguments.length; i++) {
	        s = s.replace(new RegExp('\{\s*' + i + '\s*\}', 'gm'), arguments[i]);
	    }
	    return s;
	}
	
	// 扩充Date方法
	Date.prototype.format = function (fmt) {
		var o = {
			"M+" : this.getMonth() + 1, // 月份
			"d+" : this.getDate(), // 日
			"h+" : this.getHours() % 12 == 0 ? 12 : this.getHours() % 12, // 小时
			"H+" : this.getHours(), // 小时
			"m+" : this.getMinutes(), // 分
			"s+" : this.getSeconds(), // 秒
			"q+" : Math.floor((this.getMonth() + 3) / 3), // 季度
			"S" : this.getMilliseconds()// 毫秒
		};
		var week = {
			"0" : "/u65e5",
			"1" : "/u4e00",
			"2" : "/u4e8c",
			"3" : "/u4e09",
			"4" : "/u56db",
			"5" : "/u4e94",
			"6" : "/u516d"
		};
		if (/(y+)/.test(fmt)) {
			fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
					.substr(4 - RegExp.$1.length));
		}
		if (/(E+)/.test(fmt)) {
			fmt = fmt
					.replace(
							RegExp.$1,
							((RegExp.$1.length > 1) ? (RegExp.$1.length > 2 ? "/u661f/u671f"
									: "/u5468")
									: "")
									+ week[this.getDay() + ""]);
		}
		for ( var k in o) {
			if (new RegExp("(" + k + ")").test(fmt)) {
				fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
						: (("00" + o[k]).substr(("" + o[k]).length)));
			}
		}
		return fmt; 
	}
}();