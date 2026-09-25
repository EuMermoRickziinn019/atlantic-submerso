package com.submarino.httpClient;

import java.net.http.HttpResponse;
import java.util.Map;

public interface SubmarinoHttpClient<T> {
    T get(String url, Class<T> responseType, Map<String, String> headers);
    T post(String url, Class<T> responseType, Object reqBody, Map<String, String> headers);
    T delete(String url, Class<T> responseType, Map<String, String> headers);
    T put(String url, Class<T> responseType, Object reqBody, Map<String, String> headers);
}

