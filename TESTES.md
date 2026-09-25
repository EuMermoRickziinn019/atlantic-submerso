# Testes do Submarino

Requer JDK 23 e Maven. Execute na raiz do projeto:

```shell
mvn test
```

Para executar apenas um módulo e suas dependências:

```shell
mvn -pl submarino-comuns -am test
mvn -pl submarino-http-client -am test
mvn -pl submarino-files -am test
mvn -pl submarino-fiscal -am test
```

JUnit Jupiter e Maven Surefire têm suas versões centralizadas no POM pai.
Relatórios XML e texto ficam em `target/surefire-reports` de cada módulo.

Para gerar todos os JARs, execute `mvn clean package` na raiz ou os goals
`clean` e `package` do projeto raiz no painel Maven do IntelliJ.
Os JARs são gerados em `submarino/target/`. Classes compiladas e relatórios
continuam nas pastas `target` individuais dos módulos. Execute `clean` na raiz
para também remover JARs de versões anteriores da pasta compartilhada.

| Módulo | Cenários |
| --- | --- |
| submarino-comuns | JSON: objetos, arrays, precisão decimal, caracteres especiais, null e falhas de conversão. XML: leitura, escrita, escape, documento inválido e mapeamento incompatível. |
| submarino-http-client | GET, POST, PUT e DELETE: método, caminho, parâmetros, cabeçalhos, corpo, resposta, cabeçalhos nulos, JSON inválido e URL inválida. |
| submarino-files | PKCS12/JKS/PEM/X509, senhas, aliases, validade, assinatura, CNPJ, confiança e revogação; validações locais da configuração A3. |
| submarino-fiscal | JUnit configurado; `NfeCalculator` ainda está vazia, sem regras fiscais para testar. |

Os testes HTTP são testes de componente com MockWebServer em porta local efêmera,
sem acesso a serviços externos. Os testes criptográficos geram material sintético
e usam uma data fixa. Nenhum certificado pessoal, banco de dados ou token real é
necessário. A3 com hardware continua dependendo de validação no dispositivo.

Não há teste de cálculo fiscal enquanto o módulo fiscal não tiver implementação.
O POM raiz apenas agrega módulos; a classe de exemplo fora deles não faz parte
da compilação dos subprojetos.
