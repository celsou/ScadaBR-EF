<%--
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="com.serotonin.mango.vo.UserComment"%>
<%@page import="com.serotonin.mango.rt.event.type.EventType"%>
<%@page import="com.serotonin.mango.rt.event.AlarmLevels"%>
<%@page import="com.serotonin.mango.web.dwr.EventsDwr"%>

<tag:page dwr="EventsDwr">
  
  <div style="display:none;">
	  <%@ include file="/WEB-INF/jsp/include/userComment.jsp" %>
  </div>
  
  <link rel="stylesheet" href="resources/flatpickr/flatpickr.min.css">
  <script src="resources/flatpickr/flatpickr.min.js"></script>
  <script src="resources/flatpickr/l10ns/allLocales.min.js"></script>
  
  <style>
     html {scroll-behavior: smooth;}
    .section {width: 99%; transition: 0.25s ease-in-out; overflow: hidden; padding: 5px;}
    .section > * {transition: 0.25s ease-in-out;}
    .titlePadding {padding: 3px !important;}
    .bold {font-weight:bold;}
    
    #pendingAlarms table {width: 100%; border:2px solid transparent}
    #searchResults table {width: 100%; border:2px solid transparent;}
    
    #searchForm {width: 70%; table-layout: fixed; margin: 20px auto; padding: 5px;}
    #searchForm input[type="text"], input[type="number"] {width: 100%; box-sizing: border-box;}
    #searchForm td {padding: 5px; vertical-align: top;}
    #searchForm td span {display: block;}
    
    #searchBtn {width: 90px; height: 35px; font-size: 12px; display: block; margin: auto;}
  </style>
  
  <script type="text/javascript">
    // Tell the log poll that we're interested in monitoring pending alarms.
	 mango.longPoll.pollRequest.pendingAlarms = true;
  	
    function updatePendingAlarmsContent(content) {
        hide("hourglass");
        
        $set("pendingAlarms", content);
        if (content) {
            show("ackAllDiv");
            hide("noAlarms");
        } else {
            $set("pendingAlarms", "");
            hide("ackAllDiv");
            show("noAlarms");
        }
        updateAlarmsCount();
    }
        
    function updateAlarmsCount() {
	    if (document.querySelector("#alarms #rowsCount")) {
	    	rows = document.querySelector("#alarms #rowsCount").value;
	    	$("alarmsCount").innerHTML = " - " + rows;
	    } else {
	    	$("alarmsCount").innerHTML = "";
    	}
    }
    
    function silenceAll() {
    	MiscDwr.silenceAll(function(result) {
    		var silenced = result.data.silenced;
    		for (var i=0; i<silenced.length; i++)
    			setSilenced(silenced[i], true);
    	});
    }
	
	var searchOptions = {};
	
	function updateSearchOptions() {
		searchOptions.keywords = $("keywords").value.replace(/[;,]/g, " ");;
		searchOptions.fromNone = !($("enableStartDate").checked);
		searchOptions.toNone = !($("enableEndDate").checked);
		searchOptions.startDate = new Date($("startDate").value);
		searchOptions.endDate = new Date($("endDate").value);
		searchOptions.eventId = $("eventId").value;
		searchOptions.eventStatus = arrayFromCheckboxes($("eventStatus"));
		searchOptions.alarmLevels = arrayFromCheckboxes($("alarmLevel"));
		searchOptions.eventSourceTypes = arrayFromCheckboxes($("eventSourceType"));
	}
	
	function doSearch(page, date) {
       	updateSearchOptions();
		setDisabled("searchBtn", true);
        $set("searchMessage", " - <fmt:message key="events.search.searching"/>");
        
        EventsDwr.search(searchOptions.eventId, searchOptions.eventSourceTypes, searchOptions.eventStatus, 
						 searchOptions.alarmLevels,  searchOptions.keywords, /*dateRangeType*/ 3, /*relativeDateType*/ 0,
						 /*previousPeriodCount*/ 0, /*previousPeriodType*/ 0, /*pastPeriodCount*/ 0, /*pastPeriodType*/ 0,
						 
						 /*Start date configuration:*/
						 searchOptions.fromNone, 
						 searchOptions.startDate.getFullYear(), (searchOptions.startDate.getMonth() + 1) ,
						 searchOptions.startDate.getDate(), searchOptions.startDate.getHours(), searchOptions.startDate.getMinutes(), 
						 searchOptions.startDate.getSeconds(),
						 /*End date configuration:*/
						 searchOptions.toNone,
						 searchOptions.endDate.getFullYear(), (searchOptions.endDate.getMonth() + 1),
						 searchOptions.endDate.getDate(), searchOptions.endDate.getHours(),
						 searchOptions.endDate.getMinutes(), searchOptions.endDate.getSeconds(),
						 /* Other options */
						 page, 0, updateSearchResults);
    }
	
	function updateSearchResults(results) {
		$set("searchResults", results.data.content);
        setDisabled("searchBtn", false);
        $set("searchMessage", " - " + results.data.resultCount);
        toggleDisplay("search", true);
        window.setTimeout(scrollToResults, 100);
	}
	
	function scrollToResults() {
		// Scroll screen to the search results
        if (window.pageYOffset !== undefined) {
			var windowY = window.pageYOffset;
			var tableY = $("searchResults").getBoundingClientRect().top;
			window.scroll(0, (windowY + tableY));
		} else {
			window.scroll(0, 0);
		}
	}
	
	function arrayFromCheckboxes(element) {
		var arr = new Array();
		var inputs = element.querySelectorAll("input[type='checkbox'");
		
		for (var i = 0; i < inputs.length; i++) {
			if (inputs[i].checked)
				arr.push(inputs[i].value);
		}
				
		return arr;
	}
	
	function disableCalendarInput(checkbox) {
		if (checkbox.checked) {
			checkbox.parentElement.parentElement.querySelector(".form-control").disabled = false;
			checkbox.parentElement.parentElement.querySelector(".form-control").style.backgroundColor = "";
			checkbox.parentElement.parentElement.querySelector(".disabledText").style.visibility = "hidden";
		} else {
			checkbox.parentElement.parentElement.querySelector(".form-control").disabled = true;
			checkbox.parentElement.parentElement.querySelector(".form-control").style.backgroundColor = "#DDDDDD";
			checkbox.parentElement.parentElement.querySelector(".disabledText").style.visibility = "";
		}
	}
	
	function toggleDisplay(elementId, forceShow) {
		var element = document.getElementById(elementId);
		var parent = element.parentElement;
		var titleElement = parent.querySelector(".smallTitle");
		if (parent.style.maxHeight == "")
			parent.style.maxHeight = parent.clientHeight + "px";
		if (forceShow || parent.clientHeight <= 40) {
			var newSize = element.clientHeight + parent.clientHeight;
			parent.style.maxHeight = newSize + "px";
			element.style.visibility = "";
		} else {
			parent.style.maxHeight = titleElement.clientHeight + "px";
			element.style.visibility = "hidden";
		}
	}
	
	function toggleArrowIcon(element) {
		if (element.src.includes("arrow_up"))
			element.src = "images/icon_arrow_down.png";
		else
			element.src = "images/icon_arrow_up.png";
	}

     dojo.addOnLoad(function() {
         var locale = (navigator.language || navigator.userLanguage).replace(/-.*/, "");
         if (flatpickr.l10ns[locale])
			flatpickr.localize(flatpickr.l10ns[locale]);
         flatpickr("#startDate", { enableTime: true, altInput: true, defaultDate: new Date(Date.now() - 86400000) });
         flatpickr("#endDate", { enableTime: true, altInput: true, defaultDate: Date.now() });
         
         disableCalendarInput($("enableStartDate"));
         disableCalendarInput($("enableEndDate"));
     });
     
  </script>
  
  <!-- Pending alarms section -->
  <div class="borderDiv section marB" style="float:left;">
    <div class="smallTitle titlePadding" style="float:left;">
      <tag:img png="flag_white" title="events.alarms"/>
      <fmt:message key="events.pending"/>
      <span id="alarmsCount"></span>
      <tag:help id="pendingAlarms"/>
      <img src="images/icon_arrow_up.png" class="ptr" onclick="toggleArrowIcon(this); toggleDisplay('alarms');">
    </div>
    <div id="alarms" style="clear:both;">
		<div id="ackAllDiv" class="titlePadding" style="display:none;float:right;">
		  <fmt:message key="events.acknowledgeAll"/>
		  <tag:img png="tick" onclick="MiscDwr.acknowledgeAllPendingEvents()" title="events.acknowledgeAll"/>&nbsp;
		  <fmt:message key="events.silenceAll"/>
		  <tag:img png="sound_mute" onclick="silenceAll()" title="events.silenceAll"/><br/>
		</div>    
		<div id="pendingAlarms"></div>
		<div id="noAlarms" style="display:none;padding:6px;text-align:left;">
		  <b><fmt:message key="events.emptyList"/></b>
		</div>
    </div>
    <div id="hourglass" style="padding:6px;text-align:center;"><tag:img png="hourglass"/></div>
  </div>
  
  <!-- Events search section -->
  <div class="borderDiv section marB" style="clear:left;float:left;max-height:25px;">
	
    <div class="smallTitle titlePadding" style="float: left;">
		<tag:img png="magnifier" title="events.search"/>
		<fmt:message key="events.search"/>
		<span id="searchMessage"></span>
		<tag:help id="eventsSearch"/>
		<img src="images/icon_arrow_down.png" class="ptr" onclick="toggleArrowIcon(this); toggleDisplay('search');">
    </div>
    
	<div id="search" style="clear:both;visibility:hidden;">
		<!-- Search form -->
		<table id="searchForm" class="borderDiv">
			<tr>
				<td colspan="3">
					<span class="bold"><fmt:message key="events.search.keywords"/>:</span>
					<input id="keywords" type="text">
				</td>
			</tr>
			<tr>
				<td>
					<span class="bold"> <input id="enableStartDate" type="checkbox" checked="true" onchange="disableCalendarInput(this);"> <fmt:message key="events.search.startDate"/>: </span>
					<input id="startDate" type="text" readonly="readonly">
					<span class="disabledText formError" style="text-align:center;visibility:hidden;"><fmt:message key="events.search.fromBeginning"/></span>
				</td>
				<td>
					<span class="bold"> <input id="enableEndDate" type="checkbox" checked="true" onchange="disableCalendarInput(this);"> <fmt:message key="events.search.endDate"/>: </span>
					<input id="endDate" type="text" readonly="readonly">
					<span class="disabledText formError" style="text-align:center;visibility:hidden;"><fmt:message key="events.search.toEnd"/></span>
				</td>
				<td>
					<span class="bold"> <fmt:message key="events.id"/>: <input type="checkbox" style="width:0px;visibility:hidden;"> </span>
					<input id="eventId" type="number" min="0" step="1">
				</td>
			</tr>
			
			<tr>
				<td id="eventSourceType">
					<span class="bold"><fmt:message key="events.search.type"/>:</span>
					<span> <input type="checkbox" value="<c:out value="<%= EventType.EventSources.DATA_POINT %>"/>" > <fmt:message key="eventHandlers.pointEventDetector"/></span>
					<span> <input type="checkbox" value="<c:out value="<%= EventType.EventSources.SCHEDULED %>"/>" > <fmt:message key="scheduledEvents.ses"/></span>
					<span> <input type="checkbox" value="<c:out value="<%= EventType.EventSources.COMPOUND %>"/>" > <fmt:message key="compoundDetectors.compoundEventDetectors"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= EventType.EventSources.DATA_SOURCE %>"/>" > <fmt:message key="eventHandlers.dataSourceEvents"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= EventType.EventSources.PUBLISHER %>"/>" > <fmt:message key="eventHandlers.publisherEvents"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= EventType.EventSources.MAINTENANCE %>"/>" > <fmt:message key="eventHandlers.maintenanceEvents"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= EventType.EventSources.SYSTEM %>"/>" > <fmt:message key="eventHandlers.systemEvents"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= EventType.EventSources.AUDIT %>"/>" > <fmt:message key="eventHandlers.auditEvents"/> </span>
				</td>
				<td id="alarmLevel">
					<span class="bold"><fmt:message key="common.alarmLevel"/>:</span>
					<span> <input type="checkbox" value="<c:out value="<%= AlarmLevels.NONE %>"/>" > <fmt:message key="common.alarmLevel.none"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= AlarmLevels.INFORMATION %>"/>" > <fmt:message key="common.alarmLevel.info"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= AlarmLevels.URGENT %>"/>" > <fmt:message key="common.alarmLevel.urgent"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= AlarmLevels.CRITICAL %>"/>" > <fmt:message key="common.alarmLevel.critical"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= AlarmLevels.LIFE_SAFETY %>"/>" > <fmt:message key="common.alarmLevel.lifeSafety"/> </span>
				</td>
				<td id="eventStatus">
					<span class="bold"><fmt:message key="common.status"/>:</span>
					<span> <input type="checkbox" value="<c:out value="<%= EventsDwr.STATUS_ACTIVE %>"/>" > <fmt:message key="common.active"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= EventsDwr.STATUS_RTN %>"/>" > <fmt:message key="event.rtn.rtn"/> </span>
					<span> <input type="checkbox" value="<c:out value="<%= EventsDwr.STATUS_NORTN %>"/>" > <fmt:message key="common.nortn"/> </span>
				</td>
			</tr>
			<tr>
				<td colspan="3">
					<input id="searchBtn" type="button" value="<fmt:message key="events.search.search"/>" onclick="doSearch(0, 0, true);">
				</td>
			</tr>
		</table>
		<!-- Search Results -->
		<div id="searchResults"></div>
	</div>
  </div>
  
</tag:page>
