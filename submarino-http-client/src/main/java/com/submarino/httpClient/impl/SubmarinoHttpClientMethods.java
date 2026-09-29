package com.submarino.httpClient.impl;

import com.submarino.httpClient.SubmarinoHttpClient;
import com.submarino.httpClient.SubmarinoTypeResponse;
import com.submarino.json.JsonConvert;
import com.submarino.json.impl.JsonConvertComuns;
import com.submarino.xml.SubmarinoXML;
import com.submarino.xml.impl.SubmarinoXMLImpl;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class SubmarinoHttpClientMethods<T> implements SubmarinoHttpClient<T> {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final JsonConvert jsonConvertComuns = new JsonConvertComuns();
    private final SubmarinoXML xmlConvertComuns = new SubmarinoXMLImpl();
    private final SubmarinoTypeResponse submarinoTypeResponse;

    public SubmarinoHttpClientMethods(SubmarinoTypeResponse typeResponse) {
        this.submarinoTypeResponse = typeResponse;
    }

    public T convertResponse(String body, Class<T> responseType) {
        if(this.submarinoTypeResponse.equals(SubmarinoTypeResponse.JSON)) {
            return jsonConvertComuns.jsonToObjetc(body, responseType);
        } else {
            try {
                return xmlConvertComuns.unmarshal(body, responseType);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void validateResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        throw new RuntimeException(
                "Erro HTTP " +
                statusCode +
                ": " +
                response.body()
        );
    }

    public T send(HttpRequest.Builder builder, Class<T> responseType, Map<String, String> headers) {
        if(builder != null) {
            if (headers != null) {
                headers.forEach(builder::header);
            }
            try {
                HttpResponse<String> response =
                        this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                validateResponse(response);
                return convertResponse(response.body(), responseType);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Erro ao realizar requisição HTTP", e);
            }
        }
        return null;
    }

    public T get(String url, Class<T> responseType, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();
        return send(builder, responseType, headers);
    }

    public T post(String url, Class<T> responseType,Object reqBody, Map<String, String> headers) {
        String jsonPayload = jsonConvertComuns.objectToJson(reqBody);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));
            return send(builder, responseType, headers);
    }

    public T delete(String url, Class<T> responseType, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();
        return send(builder, responseType, headers);
    }

    public T put(String url, Class<T> responseType, Object reqBody, Map<String, String> headers) {
        String jsonPayload = jsonConvertComuns.objectToJson(reqBody);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload));
        return send(builder, responseType, headers);
    }
}