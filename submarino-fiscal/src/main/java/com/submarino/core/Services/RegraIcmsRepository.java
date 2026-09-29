package com.submarino.core.Services;

import com.submarino.models.records.RegraIcms;

import java.util.ArrayList;
import java.util.List;

public class RegraIcmsRepository {
    private final List<RegraIcms> regras = new ArrayList<>();

    public void adicionar(RegraIcms regra) {
        regras.add(regra);
    }

    public List<RegraIcms> buscarTodas() {
        return List.copyOf(regras);
    }
}
