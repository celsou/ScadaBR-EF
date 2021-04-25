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
<tag:page>

	<style>
		@font-face {
			font-family: Nunito;
			src: local("Nunito-Regular.ttf"),
				 url("resources/fonts/Nunito/Nunito-Regular.ttf");
		}
	
		.container { width: 100%; box-sizing: border-box; padding: 5%;}
		.centerLink { text-align: center; margin: 20px auto auto auto;}
		
		.greenBg { background-color: #39B54A; color: #FFFFFF; }
		.blackBg { background-color: #414042; color: #FFFFFF; }
		.whiteBg { background-color: #FFFFFF; color: #414042; }
		
		.blackLink { color: #414042 !important; transition: 0.1s linear;}
		.whiteLink { color: #FFFFFF !important; transition: 0.1s linear;}
		.blackLink:hover { color: #5D5C5E !important; }
		.whiteLink:hover { color: #E9E9E9 !important; }
		
		.center { text-align: center; }
		.justify { text-align: justify; text-indent: 2em; }
		.bigText { font-size: 120px !important; }
		 
		#help { font-family: 'Nunito', Arial, Helvetica, sans-serif; overflow: hidden; }
		#help p, #help h1 { margin: 0px; }
		#help h1 { font-size: 46px; text-align: center; }
		#help a, #help a:hover, #help a:visited { font-size: 20px; font-family: 'Nunito', Arial, Helvetica, sans-serif; }
		
		#help p  { font-size: 30px; }
		#help p.comment  { font-size: 15px; font-style: italic; color: #AAA8AD; margin-top: 30px; }
	</style>

	<div id="help">
		<c:set var="filepath">/WEB-INF/dox/<fmt:message key="dox.dir"/>/help.html</c:set>
		<jsp:include page="${filepath}"/>
	</div>
</tag:page>