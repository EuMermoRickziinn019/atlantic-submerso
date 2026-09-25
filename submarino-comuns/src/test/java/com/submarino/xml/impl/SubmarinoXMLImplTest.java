package com.submarino.xml.impl;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SubmarinoXMLImplTest {
    private final SubmarinoXMLImpl conversor = new SubmarinoXMLImpl();

    @XmlRootElement(name = "produto")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Produto {
        public String nome;
        public BigDecimal preco;
        public Produto() {}
        Produto(String nome, BigDecimal preco) { this.nome = nome; this.preco = preco; }
    }

    @Test
    void desserializaCamposXml() throws JAXBException {
        var produto = conversor.unmarshal(
                "<produto><nome>Peça naval</nome><preco>10.50</preco></produto>", Produto.class);
        assertEquals("Peça naval", produto.nome);
        assertEquals(new BigDecimal("10.50"), produto.preco);
    }

    @Test
    void escapaXmlEPreservaConteudoNoRoundTrip() throws JAXBException {
        var original = new Produto("Aço & <peça>", new BigDecimal("0.10"));
        String xml = conversor.marshal(original);
        assertTrue(xml.contains("&amp;"));
        assertTrue(xml.contains("&lt;peça&gt;"));
        var recuperado = conversor.unmarshal(xml, Produto.class);
        assertEquals(original.nome, recuperado.nome);
        assertEquals(original.preco, recuperado.preco);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "<produto>", "<outro/>"})
    void rejeitaDocumentoInvalidoOuRaizIncompativel(String xml) {
        assertThrows(JAXBException.class, () -> conversor.unmarshal(xml, Produto.class));
    }

    @Test
    void rejeitaObjetoSemMapeamentoXml() {
        assertThrows(JAXBException.class, () -> conversor.marshal(new Object()));
    }
}
