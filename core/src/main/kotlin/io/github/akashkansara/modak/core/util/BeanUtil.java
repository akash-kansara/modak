package io.github.akashkansara.modak.core.util;

import io.github.akashkansara.modak.core.beanmetadata.PropertyMetaData;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class BeanUtil {
  public BeanUtil() {
    // Default constructor
  }

  public Object getPropertyValue(Object bean, PropertyMetaData property)
      throws IllegalAccessException, InvocationTargetException {
    if (property.getField() != null) {
      return property.getField().get(bean);
    } else if (property.getReadMethod() != null) {
      return property.getReadMethod().invoke(bean);
    } else {
      return null;
    }
  }

  public boolean setPropertyValue(Object bean, PropertyMetaData property, Object newValue)
      throws IllegalAccessException, InvocationTargetException {
    Object oldValue = getPropertyValue(bean, property);
    if (property.getField() != null) {
      property.getField().set(bean, newValue);
    } else if (property.getWriteMethod() != null) {
      property.getWriteMethod().invoke(bean, newValue);
    }
    Object updatedValue = getPropertyValue(bean, property);
    return oldValue != updatedValue;
  }

  public Object getMapValue(Map<Object, Object> map, Object key) {
    return map.get(key);
  }

  public boolean setMapValue(Map<Object, Object> map, Object key, Object value) {
    Object oldValue = map.get(key);
    map.put(key, value);
    Object updatedValue = map.get(key);
    return oldValue != updatedValue;
  }

  public Object getArrayValue(Object[] array, int index) {
    return array[index];
  }

  public boolean setArrayValue(Object[] array, int index, Object value) {
    Object oldValue = array[index];
    array[index] = value;
    Object updatedValue = array[index];
    return oldValue != updatedValue;
  }

  public Object getListValue(List<Object> list, int index) {
    return list.get(index);
  }

  public boolean setListValue(List<Object> list, int index, Object value) {
    Object oldValue = list.get(index);
    list.set(index, value);
    Object updatedValue = list.get(index);
    return oldValue != updatedValue;
  }
}
