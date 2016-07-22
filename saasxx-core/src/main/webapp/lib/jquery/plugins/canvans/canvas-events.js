+function($){
	
	var ons = [];
	var offs = [];
	
	$.fn.extend({
		draw:function(fn){
			var $this = $(this);
			if(!$.isFunction(fn)){
				fn = $this.data("onDraw.handler");
				if(!fn){
					return $this;
				}
			}
			var event = $this.data("onDraw.event");
			var validTriggers = {};
			if(event){
				for(var name in event.triggers){
					validTriggers[name] = true;
				}
			}
			var eventData;
			$this[0].width = $this[0].width;
			var ctx = $this[0].getContext("2d");
			fn(ctx,function(triggers,data){
				if(triggers && event && event.position){
					if(ctx.isPointInPath(event.position.x,event.position.y)){
						eventData = data;
						triggers = triggers.split(/\s+/);
						$.each(triggers,function(){
							delete event.triggers[this];
						});
					}
				}
			},function(triggers){
				if(triggers && event && event.position){
					if(ctx.isPointInPath(event.position.x,event.position.y)){
						triggers = triggers.split(/\s+/);
						$.each(triggers,function(){
							if(this in validTriggers){
								event.triggers[this]=true;
							}
						});
					}
				}
			});
			if(event && $.isEmptyObject(event.triggers)){
				if($.isFunction(event.handler)){
					event.handler.call($this[0],event.event,event.position,eventData);
				}
			}
			return $this.data("onDraw.handler",fn);
		},
		onDraw:function(type,triggers,handler){
			var $this = $(this);
			var ctx = $this[0].getContext("2d");
			var offset = $this.offset();
			var types = type.split(/\s+/);
			type = "";
			for(var i =0;i<types.length;i++){
				type+=" "+types[i]+".onDraw";
			}
			$this.on(type,function(e){
				var position;
				if(e.type.indexOf("touch")!=-1){
					position = {
							x : e.originalEvent.touches[0].clientX - offset.left,
							y : e.originalEvent.touches[0].clientY - offset.top
					};
				}
				else {
					position = {
							x:e.clientX-offset.left,
							y:e.clientY-offset.top
					};
				}
				var event = {
						position:position,
						event:e,
						triggers:{},
						handler:handler
				};
				if(triggers){
					$.each(triggers.split(/\s+/),function(){
						event.triggers[this] = true;
					});
				}
				$this.data("onDraw.event",event);
				$this.draw($this.data("onDraw.handler"));
				$this.removeData("onDraw.event");
			});
			return $this;
		}
	});
}(jQuery);