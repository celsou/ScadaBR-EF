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
<style>
    #fakeEditorPopup input[type="number"],
    #fakeEditorPopup input[type="text"],
    #fakeEditorPopup select {
        width: 200px;
        box-sizing: border-box;
    }
    #fakeEditorPreviewImgs {
        max-width: 420px;
        padding: 4px;
        overflow: auto;
        white-space:nowrap;
    }
</style>
<div id="fakeEditorPopup" style="display:none;left:0px;top:0px;" class="windowDiv">
    <table width="100%">
      <tr>
        <td>
          <tag:img png="plugin_edit" title="pão de forma" style="display:inline;"/>
          <span><fmt:message key="graphic.staticImage"/></span>
        </td>
        <td align="right">
          <tag:img png="save" onclick="fakeEditor.save()" title="common.save" style="display:inline;"/>&nbsp;
          <tag:img png="cross" onclick="fakeEditor.close()" title="common.close" style="display:inline;"/>
        </td>
      </tr>
    </table>
    <table id="fakeEditor">
      <tr>
        <td>
          <input id="overrideHeightEnable" type="checkbox" onchange="fakeEditor.updateControls();">
          <label><fmt:message key="viewEdit.graphic.overrideHeight"/></label>
        </td>
        <td>
          <input id="overrideHeight" type="number" step="1" min="1" disabled onkeyup="this.reportValidity();">
        </td>
      </tr>
      <tr>
        <td>      
          <input id="overrideWidthEnable" type="checkbox" onchange="fakeEditor.updateControls();">
          <label><fmt:message key="viewEdit.graphic.overrideWidth"/></label>
        </td>
        <td>
          <input id="overrideWidth" type="number" step="1" min="1" disabled onkeyup="this.reportValidity();">
        </td>
      </tr>
      <tr>
        <td>
          <input id="customPathEnable" type="checkbox" onchange="fakeEditor.updateControls();">
          <label><fmt:message key="viewEdit.graphic.customPath"/></label>
        </td>
        <td>
          <input id="customPath" type="text" style="visibility: hidden;">
        </td>
      </tr>
      <tr id="fakeEditorSetSelector">
        <td>
          <span class="formLabelRequired"><fmt:message key="viewEdit.graphic.imageSet"/></span>
        </td>
        <td>
          <select id="staticImageSets" onchange="fakeEditor.displayImages($get(this));">
            <option></option>
            <c:forEach items="${imageSets}" var="imageSet">
              <option value="${imageSet.id}">${imageSet.name} (${imageSet.imageCount} <fmt:message key="viewEdit.graphic.images"/>)</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </table>
    <div id="fakeEditorPreviewImgs"></div>
    <span id="fakeEditorErrors" class="formError" style="display: none;"></span>
</div>
  
  <script type="text/javascript">
    function FakeEditor() {
        this.componentId = null;
        this.component = null;
        this.selectedImage = -1;
        this.currentImageSetId = null;
        this.imageSets = <sst:convert obj="${imageSets}"/>;
        this.customHeight = null;
        this.customWidth = null;
        this.customSource = null;
        
        this.open = function(compId, reload) {
            hide("fakeEditorPopup");
            fakeEditor.componentId = compId;
            
            if (!reload && $("iconifyCB").checked) {
              ViewDwr.getViewComponent(compId, function(resp) {
                if (resp.content != "<!--FAKE COMPONENT-->")
                    $("c" + compId + "Content").innerHTML = resp.content;
                fakeEditor.open(compId, true);
              });
              return;
            }
			
            var img = $("c" + compId + "Content").querySelector("img");
            
            
            if (img.style.height.replace("px", "") != "") {
                $("overrideHeightEnable").checked = true;
                $("overrideHeight").disabled = false;
                $("overrideHeight").value = img.style.height.replace("px", "");
            }
            
            if (img.style.width.replace("px", "") != "") {
                $("overrideWidthEnable").checked = true;
                $("overrideWidth").disabled = false;
                $("overrideWidth").value = img.style.width.replace("px", "");
            }
            
            var src = decodeURIComponent(img.src.replace(this.getAbsolutePath(), ""));

            for (var i = 0; i < this.imageSets.length; i++) {
                var filenames = this.imageSets[i].imageFilenames;
                if (filenames.indexOf(src) != -1) {
                    $("staticImageSets").selectedIndex = i + 1;
                    this.displayImages($get("staticImageSets"));
                    this.setImage(filenames.indexOf(src));
                }
            }
            
            if (!this.currentImageSetId && src != "images/plugin.png" && src != "undefined") {
                $("customPathEnable").checked = true;
                $("customPath").style.visibility = "visible";
                $("customPath").value = src;
                $("fakeEditorSetSelector").style.display = "none";
                $("fakeEditorPreviewImgs").style.display = "none";
            }

            positionEditor(compId, "fakeEditorPopup");
            
            show('fakeEditor');
            show("fakeEditorPopup");
        };
        
        this.close = function() {
            this.selectedImage = -1;
            this.currentImageSetId = null;

            $("overrideHeightEnable").checked = false;
            $("overrideWidthEnable").checked = false;
            $("customPathEnable").checked = false;
            $("overrideHeight").disabled = true;
            $("overrideWidth").disabled = true;
            $("overrideHeight").value = "";
            $("overrideWidth").value = "";
            $("customPath").value = "";
            
            $("customPath").style.visibility = "hidden";
            $("staticImageSets").selectedIndex = 0;
            $("fakeEditorPreviewImgs").innerHTML = "";
            $("fakeEditorErrors").innerHTML = "";
            
            show("fakeEditorSetSelector");
            show("fakeEditorPreviewImgs")
            
            hide("fakeEditorErrors");
            hide("fakeEditorPopup");
        };
        
        this.save = function() {
            var imageSrc = "";
            var html = "<!--FAKE COMPONENT-->";

            // Get custom image size
            html += "<img style='";
            if ($("overrideHeightEnable").checked)
                html += "height: " + $("overrideHeight").value + "px;";
            if ($("overrideWidthEnable").checked)
                html += "width: " + $("overrideWidth").value + "px;";
            html += "' ";
            
            // Get image source
            html += "src='";
            if ($("customPathEnable").checked) {
                imageSrc = $("customPath").value;
                if (!imageSrc.length) {
					$("fakeEditorErrors").innerHTML = "<fmt:message key="viewEdit.graphic.missingDefault"/>";
					show("fakeEditorErrors");
                    return;
				}
            } else {
				try {
					if (this.selectedImage == -1) throw "No image selected!";
					var imageSet = this.findImageSet(this.currentImageSetId);
					imageSrc = imageSet.imageFilenames[this.selectedImage];
                } catch (e) {
					console.warn(e);
					$("fakeEditorErrors").innerHTML = "<fmt:message key="viewEdit.graphic.missingDefault"/>";
					show("fakeEditorErrors");
                    return;
                }
            }
            
            html += imageSrc + "' >";
            
            ViewDwr.saveHtmlComponent(fakeEditor.componentId, html, function() {
                updateHtmlComponentContent("c"+ fakeEditor.componentId, html);
                fakeEditor.close();
            });
        };
        
        this.findImageSet = function(id) {
            for (var i = 0; i < fakeEditor.imageSets.length; i++) {
                if (fakeEditor.imageSets[i].id == id)
                    return fakeEditor.imageSets[i];
            }
            return null;
        };

        this.setImage = function(imageId) {
            var image;
            // Clear previously selected image
            if (fakeEditor.selectedImage != -1 && fakeEditor.currentImageSetId)
                $("imageSet"+ fakeEditor.selectedImage).border = "0";
            // Select new image
            fakeEditor.selectedImage = imageId;
            if (fakeEditor.selectedImage != -1 && fakeEditor.currentImageSetId)
                $("imageSet"+ fakeEditor.selectedImage).border = "2";
        };

        this.displayImages = function(imageSetId) {
            fakeEditor.setImage(-1);            
            fakeEditor.currentImageSetId = imageSetId;
            
            var imageSet = fakeEditor.findImageSet(imageSetId);
            if (imageSet)
                $set("fakeEditorPreviewImgs", fakeEditor.createImageList(imageSet));
            else
                $set("fakeEditorPreviewImgs");
        };

        this.createImageList = function(imageSet) {
            var html = "";
            for (var i = 0; i < imageSet.imageFilenames.length; i++) {
                html += "<img ";
                html +=     "class='ptr' onclick='fakeEditor.setImage("+ i +")' ";
                html +=     "id='imageSet"+ i +"' src='"+ imageSet.imageFilenames[i] +"' ";
                html +=     "style='display:inline; max-height: 150px;' border='0' ";
                html += ">&nbsp;";
            }
            return html;
        };

        this.updateControls = function() {
            $("overrideHeight").disabled = !$("overrideHeightEnable").checked;
            $("overrideWidth").disabled = !$("overrideWidthEnable").checked;
            if ($("customPathEnable").checked) {
                $("customPath").style.visibility = "visible";
                $("fakeEditorSetSelector").style.display = "none";
                $("fakeEditorPreviewImgs").style.display = "none";
            } else {
                $("customPath").style.visibility = "hidden";
                $("fakeEditorSetSelector").style.display = "";
                $("fakeEditorPreviewImgs").style.display = "";
            }
        };

        this.getAbsolutePath = function() {
            var loc = window.location;
            var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
            return loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
        };
    }
        
    var fakeEditor = new FakeEditor();
  </script>
