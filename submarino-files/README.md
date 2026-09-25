# Certificados e chaves

Requer Java 23. Os leitores aceitam `Path` ou `InputStream`. A variante `Path`
fecha o arquivo; na variante `InputStream`, o chamador fecha o stream.

Formatos: PKCS12 (.pfx/.p12), JKS, certificados X.509 DER/PEM e PEM com
certificados, chaves públicas, PKCS8 e pares de chaves OpenSSL tradicionais.
PEM criptografado é interpretado pelo Bouncy Castle. Não há detecção pela extensão.
Blocos não suportados, como CSR, são rejeitados. Chaves DER avulsas não são suportadas.

## Senhas e seleção da chave

```java
var leitor = new SubmarinoSpecialArchivesUtilsImpl();
char[] senhaArquivo = obterSenhaArquivo();
char[] senhaChave = obterSenhaChave();
try {
    var material = (MaterialCriptografico.Repositorio) leitor.carregar(
            Path.of("empresa.pfx"), FormatoCriptografico.PKCS12, senhaArquivo);
    var extrator = new ExtratorCredencial();
    var aliases = extrator.listarAliases(material.keyStore());
    // A aplicação escolhe explicitamente o alias; pode haver várias identidades.
    var credencial = extrator.extrair(material.keyStore(), "empresa", senhaChave);
    new ValidadorCertificado().validarParaAssinatura(credencial, Instant.now());
} finally {
    Arrays.fill(senhaArquivo, '\0');
    Arrays.fill(senhaChave, '\0');
}
```

`obterSenhaArquivo/Chave` são funções da aplicação consumidora. A biblioteca não
abre prompts, não guarda senhas e não altera os arrays do chamador. Suas cópias
internas são limpas em `finally`; isso não garante apagar cópias internas da JVM
ou dos providers. Não converta senhas em `String`, nem registre material privado.
Para senha vazia, use `new char[0]`. `null` é rejeitado em PKCS12/JKS e em PEM
criptografado. A senha da chave pode diferir da senha do repositório.
Uma falha de autenticação também pode indicar corrupção; não se presume que
toda falha de leitura seja senha incorreta. Não há tentativas automáticas.

## PEM

```java
var material = (MaterialCriptografico.Pem) leitor.carregar(
        Path.of("empresa.pem"), FormatoCriptografico.PEM, senha);
```

As listas de certificados e chaves não são associadas automaticamente. Selecione
a chave e organize a cadeia com o certificado titular primeiro, depois construa
`new CredencialAssinatura(chave, cadeia)` e valide a correspondência.

## A3 / PKCS#11

Crie um arquivo de configuração local confiável, por exemplo:

```text
name = SubmarinoToken
library = C:/caminho/do/fabricante/pkcs11.dll
slotListIndex = 0
```

```java
char[] pin = obterPin();
try (var sessao = SessaoA3.abrir(Path.of("token.cfg"), pin)) {
    var extrator = new ExtratorCredencial();
    var aliases = extrator.listarAliases(sessao.repositorio());
    var credencial = extrator.extrair(sessao.repositorio(), "alias-selecionado", null);
    new ValidadorCertificado().validarParaAssinatura(credencial, Instant.now());
    // Use a chave enquanto a sessão está aberta. Ela não é exportada do token.
} finally {
    Arrays.fill(pin, '\0');
}
```

O arquivo depende do driver, arquitetura e slot do dispositivo. Não receba essa
configuração de uploads: ela define uma biblioteca nativa a carregar.
Não compartilhe sessões simultâneas do mesmo token; logout pode afetar outras
sessões do dispositivo. Fechar a sessão faz logout, sem registrar um provider
global. É necessária homologação com o token real; não foi testado hardware.
Dispositivos com PIN-pad ou autenticação específica do fabricante podem exigir
uma integração própria além desta API.

## Validações e uso fiscal

- `validarParaAssinatura`: validade temporal, titular não-CA, permissão de assinatura
  e prova de correspondência entre chave privada e certificado. Executa uma
  assinatura de desafio aleatório (no A3, essa operação usa o token).
- `validarCnpjTitular`: comparação exata do CNPJ esperado com o OID ICP-Brasil
  `2.16.76.1.3.3`. Não usa texto do CN, não valida dígitos verificadores e não
  presume autorizações de matriz/filial, procuração ou uso de e-CPF.
- `validarConfianca`: constrói a cadeia PKIX a partir de raízes confiáveis
  fornecidas explicitamente e verifica revogação usando LCRs. Falha quando
  não há prova suficiente; não usa soft-fail ou fallback OCSP.

```java
var validador = new ValidadorCertificado();
validador.validarCnpjTitular(credencial.cadeia().getFirst(), cnpjEsperado);
var resultado = validador.validarConfianca(
        credencial, raizesConfiaveis, lcrsAtuais, Instant.now());
```

Para ICP-Brasil, obtenha raízes oficiais por um canal confiável. Nunca transforme
automaticamente um certificado enviado pelo usuário em raiz de confiança.
A aplicação fornece e atualiza as LCRs; a biblioteca não implementa download/cache.
O provider PKIX pode usar fontes de revogação adicionais conforme a configuração
da JVM. Estas verificações não equivalem à homologação NF-e/NFS-e/CT-e: políticas
de certificado, finalidade, CPF, regras do emitente e do autorizador continuam
sendo responsabilidades da integração fiscal.

Referências:
- [Bouncy Castle PEMParser](https://downloads.bouncycastle.org/java/docs/bcpkix-jdk18on-javadoc/org/bouncycastle/openssl/PEMParser.html)
- [Java AuthProvider](https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/security/AuthProvider.html)
- [Java PKIXParameters](https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/security/cert/PKIXParameters.html)
- [ITI: OIDs ICP-Brasil](https://www.gov.br/iti/pt-br/central-de-conteudo/doc-icp-04-01-versao-3-0-formatado-pdf)

## Verificação

Na raiz do Submarino: `mvn -pl submarino-files -am verify`.
Os testes geram chaves, certificados e LCRs sintéticos em memória e no diretório
temporário de teste; não precisam de certificados pessoais ou acesso à rede.
