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

    private void validateResponse(HttpResponse<String> response) {
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
            validateResponse(response);
            return convertResponse(response.body(), responseType);
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
            validateResponse(response);
            return convertResponse(response.body(), responseType);
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
            validateResponse(response);
            return convertResponse(response.body(), responseType);
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
            validateResponse(response);
            return convertResponse(response.body(), responseType);
        } catch (RuntimeException | InterruptedException | IOException e) {
            throw new RuntimeException("Erro na chamada POST", e);
        }
    }
}