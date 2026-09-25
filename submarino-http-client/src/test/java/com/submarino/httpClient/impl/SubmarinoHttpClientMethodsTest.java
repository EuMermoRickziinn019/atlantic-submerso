package com.submarino.httpClient.impl;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Testes do cliente contra servidor HTTP local efêmero, sem serviços externos. */
@Timeout(10)
class SubmarinoHttpClientMethodsTest {
    private MockWebServer servidor;
    private final SubmarinoHttpClientMethods<Resposta> cliente =
            new SubmarinoHttpClientMethods<>(Resposta.class);

    public record Resposta(String mensagem) {}

    @BeforeEach
    void iniciarServidor() throws Exception {
        servidor = new MockWebServer();
        servidor.start();
    }

    @AfterEach
    void fecharServidor() throws Exception {
        servidor.shutdown();
    }

    private Resposta executar(String metodo, String url, Map<String, String> headers) {
        var corpo = Map.of("nome", "Peça & aço");
        return switch (metodo) {
            case "GET" -> cliente.get(url, Resposta.class, headers);
            case "POST" -> cliente.post(url, Resposta.class, corpo, headers);
            case "PUT" -> cliente.put(url, Resposta.class, corpo, headers);
            case "DELETE" -> cliente.delete(url, Resposta.class, headers);
            default -> throw new IllegalArgumentException(metodo);
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE"})
    void enviaMetodoCabecalhosCorpoERetornaObjeto(String metodo) throws Exception {
        servidor.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"mensagem\":\"ação concluída\"}"));
        var resposta = executar(metodo, servidor.url("/itens?pagina=2").toString(),
                Map.of("X-Teste", "submarino", "Content-Type", "application/json"));
        assertEquals("ação concluída", resposta.mensagem());
        var requisicao = servidor.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(requisicao);
        assertEquals(metodo, requisicao.getMethod());
        assertEquals("/itens?pagina=2", requisicao.getPath());
        assertEquals("submarino", requisicao.getHeader("X-Teste"));
        if (metodo.equals("POST") || metodo.equals("PUT")) {
            assertEquals("{\"nome\":\"Peça & aço\"}", requisicao.getBody().readUtf8());
        } else {
            assertEquals(0L, requisicao.getBodySize());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE"})
    void aceitaCabecalhosNulos(String metodo) {
        servidor.enqueue(new MockResponse().setBody("{\"mensagem\":\"ok\"}"));
        assertEquals("ok", executar(metodo, servidor.url("/").toString(), null).mensagem());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE"})
    void propagaFalhaDeRespostaJsonInvalida(String metodo) {
        servidor.enqueue(new MockResponse().setBody("isto nao e json"));
        var erro = assertThrows(RuntimeException.class,
                () -> executar(metodo, servidor.url("/").toString(), Map.of()));
        assertNotNull(erro.getCause());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE"})
    void rejeitaUrlInvalidaSemEnviarRequisicao(String metodo) {
        assertThrows(RuntimeException.class, () -> executar(metodo, "http://[invalida", null));
        assertEquals(0, servidor.getRequestCount());
    }
}
