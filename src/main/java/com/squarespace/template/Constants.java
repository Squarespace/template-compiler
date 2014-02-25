package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;


public class Constants {

  public static final StringView EMPTY_STRING_VIEW = new StringView("");
  
  public static final String[] EMPTY_ARRAY_OF_STRING = new String[] { };
  
  public static final Arguments EMPTY_ARGUMENTS = new Arguments();

  public static final String NULL_PLACEHOLDER = "???";

  public static final String[] TIMEZONE_KEY = new String[] { "website", "timeZone" };

  public static final JsonNode MISSING_NODE = MissingNode.getInstance();
 
}
