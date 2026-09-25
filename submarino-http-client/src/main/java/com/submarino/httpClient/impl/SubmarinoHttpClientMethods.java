package com.submarino.httpClient.impl;

import com.submarino.httpClient.SubmarinoHttpClient;
import com.submarino.json.JsonConvert;
import com.submarino.json.impl.JsonConvertComuns;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class SubmarinoHttpClientMethods<T> implements SubmarinoHttpClient<T> {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final JsonConvert jsonConvertComuns = new JsonConvertComuns();
    private final Class<T> targetClass;

    public SubmarinoHttpClientMethods(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    public T get(String url, Class<T> responseType, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        if (headers != null) {
            headers.forEach(builder::header);
        }
        try {
            HttpResponse<String> response =
                    this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return jsonConvertComuns.jsonToObjetc(response.body(), responseType);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Erro ao realizar requisição HTTP", e);
        }
    }

    public T post(String url, Class<T> responseType,Object reqBody, Map<String, String> headers) {
        try {
            String jsonPayload = jsonConvertComuns.objectToJson(reqBody);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

            if (headers != null) {
                headers.forEach(builder::header);
            }
            HttpResponse<String> response =
                    this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return jsonConvertComuns.jsonToObjetc(response.body(), responseType);
        } catch (RuntimeException | InterruptedException | IOException e) {
            throw new RuntimeException("Erro na chamada POST", e);
        }
    }

    public T delete(String url, Class<T> responseType, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();

        if (headers != null) {
            headers.forEach(builder::header);
        }
        try {
            HttpResponse<String> response =
                    this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return jsonConvertComuns.jsonToObjetc(response.body(), responseType);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Erro ao realizar requisição HTTP", e);
        }
    }

    public T put(String url, Class<T> responseType, Object reqBody, Map<String, String> headers) {
        try {
            String jsonPayload = jsonConvertComuns.objectToJson(reqBody);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload));

            if (headers != null) {
                headers.forEach(builder::header);
            }
            HttpResponse<String> response =
                    this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return jsonConvertComuns.jsonToObjetc(response.body(), responseType);
        } catch (RuntimeException | InterruptedException | IOException e) {
            throw new RuntimeException("Erro na chamada POST", e);
        }
    }
}