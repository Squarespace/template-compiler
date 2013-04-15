package com.squarespace.template;


/**
 * A predicate is a function that returns either true of false.
 */
class CorePredicates {

  // TBD: from template-helpers.js

  // collectionNameTypeEquals?
  // has-multiple?
  // main-image?
  // child-images?
  // location?
  // excerpt?
  // comments?
  // collection?
  // collection-page?
  // passthrough?
  // event?
  // same-day?
  // external-link?
  // folder?
  // singular?
  // plural?
  // disqus?
  // serviceNameEmail?
  // debug?
  // calendar-view?
  // has-variants?
  // varied-prices?
  // on-sale?
  // sold-out?
  
  // PREFIX-FORMATTERS
  // content string templates
  // promoted block types
  
  // index?
  // redirect?
  // clickable?

  // gallery options

  // gallery-meta?
  
  // handlebars helpers?
  
  
  
  static final Predicate PLURAL = new BasePredicate("plural?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().asLong() > 1;
    }
  };

  static final Predicate SINGULAR = new BasePredicate("singular?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().asLong() == 1;
    }
  };

  static final Predicate COLLECTION_TYPE_NAME_EQUALS = new BasePredicate("collectionTypeNameEquals?", true) {

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
