package com.submarino.specialArchives;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

public interface SubmarinoSpecialArchivesUtils {
    /** Fecha o arquivo. Não retém nem modifica a senha fornecida pelo chamador. */
    MaterialCriptografico carregar(Path arquivo, FormatoCriptografico formato,
                                  char[] senha)
            throws IOException, GeneralSecurityException;

    /** O chamador fecha o stream e limpa sua senha. A cópia interna é apagada. */
    MaterialCriptografico carregar(InputStream entrada, FormatoCriptografico formato,
                                  char[] senha)
            throws IOException, GeneralSecurityException;
}
