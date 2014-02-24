package com.squarespace.template;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;


/**
 * Symbol table holding all active predicates.  Uses reflection to discover and 
 * statically register predicate instances by container class, since they are numerous.
 */
public class PredicateTable extends SymbolTable<StringView, Predicate> {

  private static final int NUM_BUCKETS = 128;
  
  private static final TypeReference<Predicate> TYPE_REF = new TypeReference<Predicate>() { };

  private final List<String> symbolList = new ArrayList<>();
  
  public PredicateTable() {
    super(TYPE_REF, NUM_BUCKETS);
  }
  
  public PredicateTable(int numBuckets) {
    super(TYPE_REF, numBuckets);
  }

  public String[] getSymbols() {
    return symbolList.toArray(Constants.EMPTY_ARRAY_OF_STRING);
  }
  
  /**
   * Callback to cast the Predicate and store it in the table.
   */
  @Override
  public void registerSymbol(Object impl) {
    Predicate predicate = (Predicate) impl;
    put(new StringView(predicate.getIdentifier()), predicate);
    symbolList.add(predicate.getIdentifier());
  }

}
