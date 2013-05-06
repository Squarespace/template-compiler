package com.squarespace.template;


/**
 * A predicate is a function that returns either true of false.
 */
public class CorePredicates extends BaseRegistry<Predicate> {

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
