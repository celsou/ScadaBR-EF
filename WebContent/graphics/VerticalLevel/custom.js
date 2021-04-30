mango.view.graphic.dynamic["VerticalLevel"] = function (elementId, percentage, width, height) {
	
	// Create the "bar div" if not exists
	if(!document.querySelector("#c" + elementId + "Static div.bars")) {
		var div = document.createElement("div");
		
		div.style = "position: absolute; bottom: 2px; left: 2px; height: 95px; width: 16px; transition: 0.4s ease-in-out;";
		div.classList.add("bars");
		document.querySelector("#c" + elementId + "Static").appendChild(div);
	}
	
	// Create style tag if not exists
	if (!document.querySelector("#c" + elementId + "Static style")) {
		var style = document.createElement("style");
		// Customize simple point renderer
		style.innerHTML += "#c" + elementId + "Content .displayText { left: 10px !important; transform: translate(-50%, 0); }";
		document.querySelector("#c" + elementId + "Static").appendChild(style);
	}
	
	// Animate bar using CSS
	document.querySelector("#c" + elementId + "Static div.bars").style.maxHeight = (percentage * 95) + "px";
	if (percentage < 0.75)
		document.querySelector("#c" + elementId + "Static div.bars").style.backgroundColor = "#008000";
	else if (percentage < 0.9)
		document.querySelector("#c" + elementId + "Static div.bars").style.backgroundColor = "#E5E500";
	else
		document.querySelector("#c" + elementId + "Static div.bars").style.backgroundColor = "#E50000";
}
