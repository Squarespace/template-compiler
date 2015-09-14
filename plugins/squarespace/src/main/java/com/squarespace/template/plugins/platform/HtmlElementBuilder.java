/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template.plugins.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.squarespace.template.plugins.PluginUtils;


/**
 * TEMPORARY PORT FROM V6
 * 
 * I'm going to eventually kill the need for this code by allowing formatters
 * to have private compiled partials. - phensley
 */
public class HtmlElementBuilder {
  
  private static final Set<String> SELF_CLOSING_TAGS = Collections.singleton("img");
  
  private final String tagName;
  
  private Map<String, String> attributes = new TreeMap<>();

  private String content;
  
  private List<HtmlElementBuilder> children = new ArrayList<>();
  
  public HtmlElementBuilder(String tagName) {
    this.tagName = tagName;
  }
  
  public void addClass(String className) {
    set("class", StringUtils.trimToEmpty(attributes.get("class")) + " " + className);
  }
  
  public void addStyle(String property, String value) {
    set("style", StringUtils.trimToEmpty(attributes.get("style")) + property + ":" + value + ";");
  }
  
  public void set(String attribute, Object value) {
    attributes.put(attribute, value.toString());
  }
  
  public void setContent(String content) {
    this.content = content;
  }
  
  public void appendChild(HtmlElementBuilder element) {
    this.children.add(element);
  }
  
  /**
   * Build the HTML.
   */
  public void render(StringBuilder buf) {
    boolean selfClosing = SELF_CLOSING_TAGS.contains(tagName);
    buf.append("<");
    buf.append(tagName);
    
    for (String attributeName : attributes.keySet()) {
      buf.append(" ").append(attributeName).append("=\"");
      PluginUtils.escapeHtmlAttribute(StringUtils.trimToEmpty(attributes.get(attributeName)), buf);
      buf.append("\"");
    }

    if (!selfClosing) {
      buf.append(">");        
    }
    
    if (!children.isEmpty()) {
      for (HtmlElementBuilder child : children) {
        child.render(buf);
      }
    } else if (content != null) {
      buf.append(content);
    }

    if (!selfClosing) {
      buf.append("</");
      buf.append(tagName);
      buf.append(">");        
    } else {
      buf.append(" />");
    }
  }
  
}
