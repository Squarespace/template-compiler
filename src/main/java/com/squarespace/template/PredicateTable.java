package com.squarespace.template;

import com.fasterxml.jackson.core.type.TypeReference;


/**
 * Symbol table holding all active predicates.  Uses reflection to discover and 
 * statically register predicate instances by container class, since they are numerous.
 */
public class PredicateTable extends SymbolTable<StringView, Predicate> {

  private static final int NUM_BUCKETS = 128;
  
  private static final TypeReference<Predicate> TYPE_REF = new TypeReference<Predicate>() { };
  
  public PredicateTable() {
    super(TYPE_REF, NUM_BUCKETS);
  }
  
  public PredicateTable(int numBuckets) {
    super(TYPE_REF, numBuckets);
  }

  /**
   * Callback to cast the Predicate and store it in the table.
   */
  @Override
  void registerSymbol(Object impl) {
    Predicate predicate = (Predicate) impl;
    put(new StringView(predicate.getIdentifier()), predicate);
  }

}
