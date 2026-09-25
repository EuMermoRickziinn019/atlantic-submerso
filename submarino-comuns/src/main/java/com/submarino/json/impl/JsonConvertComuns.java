package com.submarino.json.impl;

import com.submarino.json.JsonConvert;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;

public class JsonConvertComuns implements JsonConvert {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T jsonToObjetc(String json, Class<T> clazz){
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException jse) {
            throw new RuntimeException("Erro ao converter Json", jse);
        }
    }

    @Override
    public String objectToJson(Object object){
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException jse) {
            throw new RuntimeException("Erro ao converter Object", jse);
        }
    }
}
