package com.submarino.json.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonConvertComunsTest {
    private final JsonConvertComuns conversor = new JsonConvertComuns();

    public record Produto(String nome, BigDecimal preco, List<String> etiquetas) {}

    @Test
    void desserializaObjetoComDecimalELista() {
        var produto = conversor.jsonToObjetc("""
                {"nome":"Peça naval", "preco":123.45, "etiquetas":["aço","novo"]}
                """, Produto.class);
        assertEquals("Peça naval", produto.nome());
        assertEquals(new BigDecimal("123.45"), produto.preco());
        assertEquals(List.of("aço", "novo"), produto.etiquetas());
    }

    @Test
    void serializaERecuperaCaracteresEspeciaisSemPerderPrecisao() {
        var original = new Produto("Peça \"A\"\nSão Paulo \\", new BigDecimal("0.10"), List.of());
        String json = conversor.objectToJson(original);
        assertTrue(json.contains("\\\"A\\\""));
        assertTrue(json.contains("\\n"));
        assertEquals(original, conversor.jsonToObjetc(json, Produto.class));
    }

    @Test
    void converteArrayDeObjetos() {
        var produtos = conversor.jsonToObjetc(
                "[{\"nome\":\"A\",\"preco\":1,\"etiquetas\":[]}]", Produto[].class);
        assertEquals(1, produtos.length);
        assertEquals("A", produtos[0].nome());
    }

    @Test
    void preservaNullJson() {
        assertEquals("null", conversor.objectToJson(null));
        assertNull(conversor.jsonToObjetc("null", Produto.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{invalido", "", "{\"preco\":\"nao-numerico\"}"})
    void informaFalhaDeDesserializacaoComCausa(String json) {
        var erro = assertThrows(RuntimeException.class,
                () -> conversor.jsonToObjetc(json, Produto.class));
        assertInstanceOf(JsonProcessingException.class, erro.getCause());
    }

    @Test
    void preservaCausaQuandoGetterFalhaNaSerializacao() {
        var erro = assertThrows(RuntimeException.class,
                () -> conversor.objectToJson(new ObjetoInvalido()));
        assertInstanceOf(JsonProcessingException.class, erro.getCause());
    }

    public static class ObjetoInvalido {
        public String getValor() { throw new IllegalStateException("Falha de teste"); }
    }
}
