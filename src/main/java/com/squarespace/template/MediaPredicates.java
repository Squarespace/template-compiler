package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;


public class MediaPredicates extends BaseRegistry<Predicate> { 

  static final Predicate IMAGE = new BasePredicate("image?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return false;
    }
  };
  
  static final Predicate GALLERY_META = new BasePredicate("gallery-meta?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return false;
    }
  };

  static final Predicate LOCATION = new BasePredicate("location?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode loc = ctx.node().get("location");
      for (String key : new String[] { "mapLat", "mapLng", "mapZoom" }) {
        if (loc.get(key).isMissingNode()) {
          return false;
        }
      }
      return true;
    }
  };
  
  static final Predicate MAIN_IMAGE = new BasePredicate("main-image?", false) {
    
    // TODO: implement
    
  };
  
  static final Predicate VIDEO = new BasePredicate("video?", false) {

    // TODO: implement

  };
  
  private static final String[] GALLERY_DESIGN_SELECT = new String[] { 
    "grid", "slideshow", "slider", "stacked" 
  };

  private static final String[] META_POSITION_SELECT = new String[] {
    "top", "top-left", "top-right", "center", "bottom", "bottom-left", "bottom-right"
  };
  
  private static final String[] ACTIVE_ALIGNMENT_SELECT = new String[] {
    "center", "left", "right"
  };
  
  private static final String[] GALLERY_BOOLEAN = new String[] {
    "autoplay", "auto-crop", "controls", "lightbox", "square-thumbs", "show-meta", 
    "show-meta-on-hover", "thumbnails"
  };
  
  
  static class GallerySelectPredicate extends BasePredicate {

    private String option;
    
    private String name;
    
    public GallerySelectPredicate(String option, String name) {
      super("gallery-" + option + "-" + name + "?", false);
      this.option = option;
      this.name = name;
    }
    
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node().path("options").path(option);;
      return !node.isMissingNode() ? name.equals(node.asText()) : false;
    }
  }
  
  static class GalleryBooleanPredicate extends BasePredicate {
    
    private String option;
    
    public GalleryBooleanPredicate(String option) {
      super("gallery-" + option + "?", false);
      this.option = option;
    }
    
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node().path("options").path(option);
      return !node.isMissingNode() ? node.asBoolean() : false;
    }
  }
  
  @Override
  public void registerTo(SymbolTable<StringView, Predicate> symbolTable) {
    for (String name : GALLERY_DESIGN_SELECT) {
      symbolTable.registerSymbol(new GallerySelectPredicate("design", name));
    }
    for (String name : META_POSITION_SELECT) {
      symbolTable.registerSymbol(new GallerySelectPredicate("meta-position", name));
    }
    for (String name : ACTIVE_ALIGNMENT_SELECT) {
      symbolTable.registerSymbol(new GallerySelectPredicate("active-alignment", name));
    }
    for (String option : GALLERY_BOOLEAN) {
      symbolTable.registerSymbol(new GalleryBooleanPredicate(option));
    }
  }
  
}
