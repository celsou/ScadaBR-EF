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
<%@ include file="/WEB-INF/snippet/common.jsp" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:choose>
	<c:when test="${empty pointComponent.image}"><tag:img png="icon_comp" title="common.noImage"/></c:when>
	<c:otherwise>
		<c:set var="dynImg" value="${pointComponent.dynamicImageId}"/>
        <img src="graphics/${dynImg}/${dynImg}.svg"
			 width="${pointComponent.width}" height="${pointComponent.height}" onerror="this.src = this.src.replace('.svg','.png')"
			<c:if test="${!empty pointComponent.bkgdColorOverride}">style="background-color:${pointComponent.bkgdColorOverride};"</c:if>
		>
	</c:otherwise>
</c:choose>
<input type="hidden" id="dyn${pointComponent.id}" value='{"graphic":"${pointComponent.dynamicImageId}","value":"${proportion}"}'/>
<c:if test="${pointComponent.displayText}">
	<div class="displayText" style="position:absolute;left:${pointComponent.textX}px;top:${pointComponent.textY}px;">
		<%@ include file="/WEB-INF/snippet/basicContent.jsp" %>
	</div>
</c:if>
