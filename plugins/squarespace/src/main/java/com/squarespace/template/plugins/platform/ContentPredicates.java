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

import static com.squarespace.template.GeneralUtils.isTruthy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.Patterns;
import com.squarespace.template.Predicate;
import com.squarespace.template.PredicateRegistry;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.plugins.PluginDateUtils;
import com.squarespace.template.plugins.PluginUtils;
import com.squarespace.template.plugins.platform.enums.CollectionType;
import com.squarespace.template.plugins.platform.enums.FolderBehavior;
import com.squarespace.template.plugins.platform.enums.RecordType;


public class ContentPredicates implements PredicateRegistry {

  @Override
  public void registerPredicates(SymbolTable<StringView, Predicate> table) {
    table.add(CALENDAR_VIEW);
    table.add(CHILD_IMAGES);
    table.add(CLICKABLE);
    table.add(COLLECTION);
    table.add(COLLECTION_PAGE);
    table.add(COLLECTION_TEMPLATE_PAGE);
    table.add(COLLECTION_TYPE_NAME_EQUALS);
    table.add(EXCERPT);
    table.add(EXTERNAL_LINK);
    table.add(FOLDER);
    table.add(GALLERY_META);
    table.add(HAS_MULTIPLE);
    table.add(INDEX);
    table.add(LOCATION);
    table.add(MAIN_IMAGE);
    table.add(PASSTHROUGH);
    table.add(REDIRECT);
    table.add(SAME_DAY);
    table.add(SERVICE_NAME_EMAIL);
    table.add(SHOW_PAST_EVENTS);
    
    for (String name : GALLERY_DESIGN_SELECT) {
      table.add(new GallerySelectPredicate("design", name));
    }
    for (String name : META_POSITION_SELECT) {
      table.add(new GallerySelectPredicate("meta-position", name));
    }
    for (String name : ACTIVE_ALIGNMENT_SELECT) {
      table.add(new GallerySelectPredicate("active-alignment", name));
    }
    for (String option : GALLERY_BOOLEAN) {
      table.add(new GalleryBooleanPredicate(option));
    }

    for (RecordType type : RecordType.values()) {
      // Skip types with special handling (see below)
      if (PROMOTED_RECORD_PREDICATES.contains(type)) {
        continue;
      }
      Predicate impl = new RecordTypePredicate(type.stringValue() + "?", type);
      table.add(impl);
    }

    // Register predicates for types with an extra promotedBlock test.
    table.add(new PromotedRecordTypePredicate("external-video?", RecordType.VIDEO, "video"));
    table.add(new PromotedRecordTypePredicate("video?", RecordType.VIDEO, "video"));
    table.add(new PromotedRecordTypePredicate("image?", RecordType.IMAGE, "image"));
    table.add(new PromotedRecordTypePredicate("quote?", RecordType.QUOTE, "quote"));
    table.add(new PromotedRecordTypePredicate("link?", RecordType.LINK, "link"));
    table.add(new PromotedRecordTypePredicate("gallery?", RecordType.GALLERY, "gallery"));

    // Register all of the promoted-only predicates
    for (String blockType : PROMOTED_BLOCK_TYPES) {
      String identifier = "promoted" + blockType.toUpperCase() + "?";
      table.add(new PromotedBlockTypePredicate(identifier, blockType));
    }
  }
  
  public static final Predicate CALENDAR_VIEW = new BasePredicate("calendar-view?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode view = ctx.resolve("calendarView");
      return view.asBoolean();
    }
  };


  public static final Predicate CHILD_IMAGES = new BasePredicate("child-images?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode items = ctx.node().path("items");
      if (items.size() > 0) {
        JsonNode first = items.path(0);
        if (isTruthy(first.path("mainImageId")) || isTruthy(first.path("systemDataId"))) {
          return true;
        }
      }
      return false;
    }
  };


  public static final Predicate CLICKABLE = new BasePredicate("clickable?", false) {

    private final int indexType = FolderBehavior.INDEX.code();

    private final int redirectType = FolderBehavior.REDIRECT.code();

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.resolve("folderBehavior");
      if (node.isMissingNode()) {
        return true;
      }
      int type = node.asInt();
      return type == indexType || type == redirectType;
    }

  };

  public static final Predicate COLLECTION = new BasePredicate("collection?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.node().path("collection"));
    }
  };

  public static final Predicate COLLECTION_PAGE = new BasePredicate("collection-page?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode collection = ctx.node().path("collection");
      JsonNode type = collection.path("type");
      if (!type.isMissingNode()) {
        CollectionType collType = CollectionType.fromCode(type.asInt());
        return isTruthy(collection) && CollectionType.COLLECTION_TYPE_PAGE.equals(collType);
      }
      return false;
    }
  };

  public static final Predicate COLLECTION_TEMPLATE_PAGE = new BasePredicate("collection-template-page?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode collection = ctx.node().path("collection");

      if (collection.isMissingNode()) {
        JsonNode type = ctx.node().path("type");
        if (!type.isMissingNode()) {
          CollectionType collType = CollectionType.fromCode(type.asInt());
          return isTruthy(type) && CollectionType.TEMPLATE_PAGE.equals(collType);
        }
      } else {
        JsonNode type = collection.path("type");
        if (!type.isMissingNode()) {
          CollectionType collType = CollectionType.fromCode(type.asInt());
          return isTruthy(collection) && CollectionType.TEMPLATE_PAGE.equals(collType);
        }
      }

      return false;
    }
  };


  public static final Predicate COLLECTION_TYPE_NAME_EQUALS = new BasePredicate("collectionTypeNameEquals?", true) {

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.exactly(1);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.resolve("typeName").asText().equals(args.first());
    }
  };


  public static final Predicate EXCERPT = new BasePredicate("excerpt?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode excerpt = ctx.node().path("excerpt");
      JsonNode html = excerpt.path("html");
      String text = "";
      if (html.isTextual()) {
        text = html.asText();
      } else if (excerpt.isTextual()) {
        text = excerpt.asText();
      }
      text = PluginUtils.removeTags(text);
      text = Patterns.WHITESPACE_NBSP.matcher(text).replaceAll("");
      return text.length() > 0;
    }
  };


  public static final Predicate EXTERNAL_LINK = new BasePredicate("external-link?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.node().path("externalLink"));
    }
  };


  public static final Predicate FOLDER = new BasePredicate("folder?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.node().path("collection").path("folder"));
    }
  };


  static final Predicate GALLERY_META = new BasePredicate("gallery-meta?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode options = ctx.resolve("options");
      return isTruthy(options.path("controls")) || isTruthy(options.path("indicators"));
    }
  };


  public static final Predicate HAS_MULTIPLE = new BasePredicate("has-multiple?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().size() > 1;
    }
  };


  public static final Predicate INDEX = new BasePredicate("index?", false) {
    private final int indexType = FolderBehavior.INDEX.code();

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode collection = ctx.node().path("collection");
      if (!collection.isObject()) {
        return false;
      }
      JsonNode folder = collection.path("folder");
      JsonNode folderBehavior = collection.path("folderBehavior");
      return isTruthy(folder) && folderBehavior.isNumber() && folderBehavior.asInt() == indexType;
    }
  };


  static final Predicate LOCATION = new BasePredicate("location?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode loc = ctx.node().path("location");
      for (String key : new String[] { "mapLat", "mapLng" }) {
        if (loc.path(key).isMissingNode()) {
          return false;
        }
      }
      return true;
    }
  };


  static final Predicate MAIN_IMAGE = new BasePredicate("main-image?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      return isTruthy(node.path("mainImageId")) || isTruthy(node.path("systemDataId"));
    }
  };


  public static final Predicate PASSTHROUGH = new BasePredicate("passthrough?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode pass = ctx.node().path("passthrough");
      String sourceUrl = ctx.node().path("sourceUrl").asText();
      return isTruthy(pass) && !sourceUrl.equals("");
    }
  };


  public static final Predicate REDIRECT = new BasePredicate("redirect?", false) {

    private final int redirectType = FolderBehavior.REDIRECT.code();

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().path("folderBehavior").asInt() == redirectType;
    }
  };


  public static final Predicate SAME_DAY = new BasePredicate("same-day?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      JsonNode tzNode = ctx.resolve(Constants.TIMEZONE_KEY);
      String tzName = "UTC";
      if (tzNode.isMissingNode()) {
        tzName = DateTimeZone.getDefault().getID();
      } else {
        tzName = tzNode.asText();
      }
      long instant1 = node.path("startDate").asLong();
      long instant2 = node.path("endDate").asLong();
      return PluginDateUtils.sameDay(instant1, instant2, tzName);
    }
  };


  public static final Predicate SERVICE_NAME_EMAIL = new BasePredicate("serviceNameEmail?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().path("serviceName").asText().equals("email");
    }
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

    private final String option;

    private final String name;

    public GallerySelectPredicate(String option, String name) {
      super("gallery-" + option + "-" + name + "?", false);
      this.option = option;
      this.name = name;
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.resolve("options").path(option);;
      return !node.isMissingNode() ? name.equals(node.asText()) : false;
    }
  }

  static class GalleryBooleanPredicate extends BasePredicate {

    private final String option;

    public GalleryBooleanPredicate(String option) {
      super("gallery-" + option + "?", false);
      this.option = option;
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.resolve("options").path(option);
      return !node.isMissingNode() ? node.asBoolean() : false;
    }
  }

  // Summary Block
  public static final Predicate SHOW_PAST_EVENTS = new BasePredicate("show-past-events?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode view = ctx.node();
      return view.path("showPastOrUpcomingEvents").asText().equals("past");
    }
  };
  

  private static class RecordTypePredicate extends BasePredicate {

    private final int recordType;

    public RecordTypePredicate(String identifier, RecordType type) {
      super(identifier, false);
      this.recordType = type.code();
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().path("recordType").asInt() == recordType;
    }
  }

  private static class PromotedRecordTypePredicate extends BasePredicate {

    private final int recordType;

    private final String promotedBlockType;

    public PromotedRecordTypePredicate(String identifier, RecordType type, String promotedBlockType) {
      super(identifier, false);
      this.recordType = type.code();
      this.promotedBlockType = promotedBlockType;
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      return node.path("recordType").asInt() == recordType
          || node.path("promotedBlockType").asText().equals(promotedBlockType);
    }
  }

  private static class PromotedBlockTypePredicate extends BasePredicate {

    private final String promotedBlockType;

    public PromotedBlockTypePredicate(String identifier, String promotedBlockType) {
      super(identifier, false);
      this.promotedBlockType = promotedBlockType;
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().path("promotedBlockType").asText().equals(promotedBlockType);
    }
  }

  // This list of record type checks are special-cased to also perform a promotedBlockType check.
  // Just going by how the JavaScript predicates are defined.
  private static final Set<RecordType> PROMOTED_RECORD_PREDICATES = new HashSet<>(Arrays.asList(
    RecordType.VIDEO,
    RecordType.IMAGE,
    RecordType.QUOTE,
    RecordType.LINK,
    RecordType.GALLERY
  ));

  private static final String[] PROMOTED_BLOCK_TYPES = new String[] {
    "map","embed","image","code","quote","twitter","link","video","foursquare","instagram","form"
  };

}
