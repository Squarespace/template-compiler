package com.squarespace.template.plugins.platform.enums;


public enum BackgroundSource implements PlatformEnum {

  UNDEFINED(-1, "undefined"),
  UPLOAD(1, "upload"),
  INSTAGRAM(2, "instagram"),
  VIDEO(3, "video"),
  NONE(4, "none");

  private final int code;
  private final String name;
  
  private BackgroundSource(int code, String name) {
    this.code = code;
    this.name = name;
  }
  
  @Override
  public int code() {
    return code;
  }

  @Override
  public String stringValue() {
    return name;
  }

}
