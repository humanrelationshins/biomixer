///<reference path="headers/require.d.ts" />

///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="GraphView" />

import GraphView = require('./GraphView');


export function nodeTooltipLambda(outerThis: GraphView.GraphView<any, any>){
    return function(d){
        var me = this,
        meData = d,
        leaveDelayTimer = null,
        visible = false,
        tipsyId = undefined;
        
        // TODO This creates a timer per popup, which is sort of silly. Figure out another way.
        var leaveMissedTimer = undefined;
        function missedEventTimer() {
            leaveMissedTimer = setTimeout(missedEventTimer, 1000);
            // The hover check doesn't work when we are over children it seems, and the tipsy has plenty of children...
            if($("#"+me.id+":hover").length != 0 && $(tipsyId+":hover").length != 0){
                console.log("Not in thing "+me.id+" and tipsyId "+tipsyId);
                leave();
            }
        }
        missedEventTimer();
        
        function leave() {
            // We add a 100 ms timeout to give the user a little time
            // moving the cursor to/from the tipsy object
            leaveDelayTimer = setTimeout(function () {
                $(me).tipsy('hide');
                visible = false;
            }, 100);
        }
    
        function enter() {
            if(outerThis.dragging){
                return;
            }
            $(me).tipsy({
                html: true,
                fade: true,
                // offset: parseInt($(me).attr("r")), // works better without this!
                offset: 15, // need this for the gravity south-east cases. It makes it quite far for the other cases though...
                fallback: "Fetching data...",
                title: function() {
                  // var d = this.__data__, c = d.i; //colors(d.i);
                  // return 'Hi there! My color is <span style="color:' + c + '">' + c + '</span>';
                  return outerThis.createNodePopupTable(me, meData);
                },
                trigger: 'manual',
                gravity: function() {
                    var location = "";
                    
                    if($(me).offset().top > ($(document).scrollTop() + $(window).height() / 2)){
                        location += "s";
                    } else {
                        location += "n";
                    }
                    
                    if($(me).offset().left > ($(document).scrollLeft() + $(window).width() / 2)){
                        location += "e";
                    } else {
                        location += "w";
                    }
                    // console.log("Location "+location);
                    return location;
                },
            });
            
            if (visible) {
                clearTimeout(leaveDelayTimer);
            } else {
                $(me).tipsy('show');
                // The .tipsy object is destroyed every time it is hidden,
                // so we need to add our listener every time its shown
                var tipsy = $(me).tipsy("tip");
                outerThis.lastDisplayedTipsy = tipsy;
                tipsy.nodeIdentifier;
                outerThis.lastDisplayedTipsyData = meData;
                outerThis.lastDisplayedTipsySvg = me;
                tipsyId = $(me).attr("id"+"_tipsy");
                tipsy.attr("id", tipsyId);
                
                // For the tipsy specific listeners, change opacity.
                tipsy.mouseenter(function(){tipsy.css("opacity",1.0); enter(); }).mouseleave(function(){tipsy.css("opacity",0.8); leave();});
                tipsy.mouseover(function(){
                    tipsy.css("opacity",1.0);
                    clearTimeout(leaveMissedTimer);
                });
                visible = true;
            }
        }
        
        $(this).hover(enter, leave);
        $(this).mouseover(function(){
            clearTimeout(leaveMissedTimer);
        });
        
        // TODO Use a timer, poll style, to prevent cases where mouse events are missed by browser.
        // That happens commonly. We'll want to hide stale open tipsy panels when this happens.
        // d3.timer(function(){}, -4 * 1000 * 60 * 60, +new Date(2012, 09, 29));
    }
}