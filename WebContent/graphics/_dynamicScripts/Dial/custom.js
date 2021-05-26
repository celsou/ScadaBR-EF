mango.view.graphic.dynamic["Dial"] = function (elementId, percentage, width, height) {
	
	// Create pointer image if not exists
	if(!document.querySelector("#c" + elementId + "Static img.dial-pointer")) {
		var img = document.createElement("img");
		
		img.src = "graphics/_dynamicScripts/Dial/pointer.svg";
		img.onerror = "this.src = this.src.replace('.svg','.png');";
		img.style = "position: absolute; top: 0px; left: 0px;";
		img.classList.add("dial-pointer");
		document.querySelector("#c" + elementId + "Static").appendChild(img);
	}
	
	// Create style tag if not exists
	if (!document.querySelector("#c" + elementId + "Static style")) {
		var style = document.createElement("style");
		// Customize simple point renderer
		style.innerHTML  = "#c" + elementId + "Content .displayText { left: 50% !important; transform: translate(-50%, 0); }";
		style.innerHTML += "#c" + elementId + "Content .displayText { top: " + 100 + "px !important; }";
		style.innerHTML += "#c" + elementId + "Content .simpleRenderer { border: none; background-color: #FFFFFFAA; }";
		style.innerHTML += "#c" + elementId + "Content .simpleRenderer { font-size: " + 18 + "px !important; }";
		// Enable CSS animations
		style.innerHTML += "#c" + elementId + " img.dial-pointer { transition: 0.4s ease-in-out; }";
		document.querySelector("#c" + elementId + "Static").appendChild(style);
	}
	
	// Rotate pointer image using CSS
	var degOffset = -120;
	var degree = (240 * percentage) + degOffset;
	document.querySelector("#c" + elementId + "Static img.dial-pointer").style.transform = "rotate(" + degree + "deg)";
	document.querySelector("#c" + elementId + "Static img.dial-pointer").style.MozTransform = "rotate(" + degree + "deg)";
	document.querySelector("#c" + elementId + "Static img.dial-pointer").style.webkitTransform = "rotate(" + degree + "deg)";
}
