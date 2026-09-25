package com.submarino.xml;

import jakarta.xml.bind.JAXBException;

public interface SubmarinoXML {
    <T> T unmarshal(String xml, Class<T> clazz) throws JAXBException;
    String marshal(Object obj) throws JAXBException;
}
