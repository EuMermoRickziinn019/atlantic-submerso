package com.submarino.json;

public interface JsonConvert {
    <T> T jsonToObjetc(String json, Class<T> clazz);
    String objectToJson(Object object);
}
