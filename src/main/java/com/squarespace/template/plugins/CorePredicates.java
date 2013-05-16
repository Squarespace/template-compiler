package com.squarespace.template.plugins;

import static com.squarespace.template.GeneralUtils.isTruthy;

import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.BaseRegistry;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.Patterns;
import com.squarespace.template.Predicate;
import com.squarespace.v6.utils.enums.CollectionType;
import com.squarespace.v6.utils.enums.RecordType;


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
  
  public static final Predicate DEBUG = new BasePredicate("debug?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.resolve("debug"));
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
  
  public static final Predicate EXTERNAL_LINK = new BasePredicate("external-link?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.node().path("externalLink"));
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
  
  public static final Predicate HAS_MULTIPLE = new BasePredicate("has-multiple?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().size() > 1;
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
  
  public static final Predicate PLURAL = new BasePredicate("plural?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().asLong() > 1;
    }
  };
  
  public static final Predicate SERVICE_NAME_EMAIL = new BasePredicate("serviceNameEmail?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().path("serviceName").asText().equals("email");
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
  
  public static final Predicate SINGULAR = new BasePredicate("singular?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().asLong() == 1;
    }
  };

  
}
