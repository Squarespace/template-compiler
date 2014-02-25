package com.squarespace.template;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public abstract class TypeRef<T> {

  private final Type type;

  private final Class<?> clazz;
  
  public TypeRef() {
    Type superType = getClass().getGenericSuperclass();
    type = ((ParameterizedType)superType).getActualTypeArguments()[0];
    clazz = (Class<?>) type;
  }

  public Type type() {
    return type;
  }
  
  public Class<?> clazz() {
    return clazz;
  }
  
}