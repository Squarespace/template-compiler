package com.squarespace.template.plugins;

import static com.squarespace.template.GeneralUtils.isTruthy;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.BaseRegistry;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.Patterns;
import com.squarespace.template.Predicate;
import com.squarespace.v6.utils.enums.CollectionType;
import com.squarespace.v6.utils.enums.RecordType;


/**
 * A predicate is a function that returns either true of false.
 */
public class CorePredicates extends BaseRegistry<Predicate> {

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
        CollectionType collType = CollectionType.valueOf(type.asInt());
        return isTruthy(collection) && CollectionType.COLLECTION_TYPE_PAGE.equals(collType);
      }
      return false;
    }
  };
  
  public static final Predicate EXCERPT = new BasePredicate("excerpt?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode excerpt = ctx.node().path("excerpt");
      String text = "";
      if (!excerpt.isMissingNode()) {
        JsonNode html = excerpt.path("html");
        if (!html.isMissingNode()) {
          text = html.asText();
        }
      } else {
        text = excerpt.asText();
      }
      text = PluginUtils.removeTags(text);
      text = Patterns.WHITESPACE_NBSP.matcher(text).replaceAll("");
      return text.length() > 0;
    }
  };
  
  public static final Predicate EVENT = new BasePredicate("event?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node().path("recordType");
      if (!node.isMissingNode()) {
        RecordType type = RecordType.valueOf(node.asInt());
        return RecordType.EVENT.equals(type);
      }
      return false;
    }
  };
  
  public static final Predicate FOLDER = new BasePredicate("folder?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.node().path("collection").path("folder"));
    }
  };
  
  public static final Predicate PASSTHROUGH = new BasePredicate("passthrough?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode pass = ctx.node().path("passthrough");
      String sourceUrl = ctx.node().path("sourceUrl").asText();
      return !pass.isMissingNode() && !sourceUrl.equals("");
    }
  };
  
  public static final Predicate PLURAL = new BasePredicate("plural?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().asLong() > 1;
    }
  };

  public static final Predicate SINGULAR = new BasePredicate("singular?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().asLong() == 1;
    }
  };

  public static final Predicate COLLECTION_TYPE_NAME_EQUALS = new BasePredicate("collectionTypeNameEquals?", true) {

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.exactly(1);
    }
    
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      
      return false;
    }
  };
  
}
