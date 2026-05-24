# Escala Hospitalar

API REST para cadastro de profissionais de saúde e montagem de uma escala semanal de plantões.

O projeto foi construído com Spring Boot, JPA e validação Bean Validation, mantendo a regra de negócio no nível de serviço e expondo contratos simples por DTOs.

## Stack

- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- H2 em memória para desenvolvimento
- Driver PostgreSQL disponível para evolução para banco persistente
- Maven Wrapper

## Como executar

Pré-requisito: Java 17.

```bash
./mvnw spring-boot:run
```

A API sobe em:

```text
http://localhost:8080
```

O console H2 fica disponível em:

```text
http://localhost:8080/h2-console
```

Configuração padrão do H2:

```text
JDBC URL: jdbc:h2:mem:escala_hospitalar
User: sa
Password:
```

## Configuração

As configurações principais estão em `src/main/resources/application.properties`.

O banco padrão é H2 em memória, com `spring.jpa.hibernate.ddl-auto=create-drop`. Essa escolha facilita desenvolvimento e testes manuais, porque a aplicação cria e descarta o schema a cada execução.

O `pom.xml` também inclui o driver PostgreSQL em runtime. Isso indica uma preparação para trocar o banco em ambientes persistentes sem alterar a camada de domínio ou repositories, bastando ajustar as propriedades de datasource e estratégia de schema.

## Modelo de domínio

O domínio gira em torno de duas entidades:

- `Profissional`: representa médico, enfermeiro ou técnico, com nome, CRM/COREN, categoria e carga horária semanal contratada.
- `Plantao`: representa a alocação de um profissional em uma data e turno.

As categorias aceitas são:

```text
MEDICO
ENFERMEIRO
TECNICO
```

Os turnos aceitos são:

```text
MANHA: 07:00-13:00, 6h
TARDE: 13:00-19:00, 6h
NOITE: 19:00-07:00, 12h
```

## Endpoints

### Profissionais

Lista profissionais, opcionalmente filtrando por categoria:

```http
GET /api/profissionais
GET /api/profissionais?categoria=MEDICO
```

Cadastra um profissional:

```http
POST /api/profissionais
Content-Type: application/json

{
  "nome": "Ana Souza",
  "crm": "CRM12345",
  "categoria": "MEDICO",
  "cargaHorariaSemanal": 40
}
```

### Plantões

Cadastra um plantão:

```http
POST /api/plantoes
Content-Type: application/json

{
  "profissionalId": 1,
  "data": "2026-05-25",
  "turno": "MANHA"
}
```

Remove um plantão:

```http
DELETE /api/plantoes/{id}
```

Consulta a escala de uma semana, a partir da data informada:

```http
GET /api/plantoes/semana?dataInicio=2026-05-25
```

A resposta semanal traz o intervalo consultado, a lista de dias e uma linha por profissional com horas alocadas, indicador de limite atingido e plantões agrupados por data.

## Regras de negócio

As regras principais estão em `PlantaoService` e `ProfissionalService`.

- Não é permitido cadastrar dois profissionais com o mesmo CRM/COREN.
- Não é permitido criar plantão para profissional inexistente.
- Não é permitido cadastrar o mesmo profissional mais de uma vez na mesma data e turno.
- A soma dos plantões de um profissional na semana não pode ultrapassar sua carga horária semanal contratada.
- A semana usada para validar carga horária é calculada de segunda-feira a domingo a partir da data do plantão.

Além da validação em serviço, a entidade `Plantao` possui uma restrição única em banco para `profissional_id`, `data` e `turno`. Isso reforça a regra de duplicidade também no nível de persistência.

## Escolhas técnicas relevantes

### Separação em camadas

O projeto usa uma estrutura direta de camadas:

- `controller`: recebe requisições HTTP e retorna `ResponseEntity`.
- `service`: concentra regras de negócio e transações.
- `repository`: encapsula acesso a dados com Spring Data JPA.
- `entities`: define o modelo persistido.
- `dto`: define contratos de entrada e saída da API.
- `exceptions`: padroniza erros HTTP.

Essa separação mantém os controllers pequenos e evita que regra de negócio fique misturada com detalhes de HTTP ou persistência.

### DTOs com `record`

Os contratos da API usam `record`, reduzindo código repetitivo e deixando claro que requests e responses são objetos de transporte imutáveis.

As entidades JPA não são expostas diretamente nos endpoints. Em vez disso, respostas como `ProfissionalResponse`, `PlantaoResponse` e `EscalaSemanalResponse` controlam exatamente o formato retornado ao cliente.

### Validação declarativa

Os DTOs de entrada usam anotações como `@NotBlank`, `@NotNull`, `@Min` e `@Max`. Isso antecipa erros de entrada antes da execução das regras de negócio.

Erros de validação são tratados por `GlobalExceptionHandler`, retornando status `400` e uma lista de detalhes por campo.

### Transações no serviço

Os métodos de escrita usam `@Transactional`, enquanto consultas usam `@Transactional(readOnly = true)`.

Essa escolha deixa a fronteira transacional alinhada ao caso de uso, não ao controller. Também permite que validações e persistência de uma operação aconteçam dentro da mesma unidade de trabalho.

### Consulta semanal otimizada

A consulta semanal de plantões usa `JOIN FETCH` para carregar o profissional junto com cada plantão. Isso evita consultas adicionais ao montar a resposta semanal, especialmente porque o retorno precisa do nome e dados do profissional.

Depois da busca, os plantões são agrupados em memória por profissional e por data para montar uma estrutura adequada à tela de escala.

### Tratamento centralizado de erros

O projeto usa `@RestControllerAdvice` para converter exceções em respostas HTTP padronizadas:

- `404` para recurso não encontrado.
- `422` para violação de regra de negócio.
- `400` para dados inválidos.
- `500` para erros inesperados.

Todas as respostas de erro seguem o formato `ErroResponse`, com timestamp, status, erro, mensagem, path e detalhes.

### CORS aberto para API

`WebConfig` libera CORS para `/api/**`, permitindo chamadas de frontends em outros domínios durante desenvolvimento.

Como `allowedOriginPatterns("*")` está aberto, em produção o ideal é restringir as origens permitidas para os domínios reais do frontend.

## Testes

O projeto possui um teste inicial de contexto Spring:

```bash
./mvnw test
```

Esse teste valida se a aplicação consegue subir o contexto, mas ainda não cobre regras de negócio. Bons próximos testes seriam:

- cadastro de profissional com CRM duplicado;
- criação de plantão duplicado;
- bloqueio por carga horária semanal excedida;
- resposta da escala semanal agrupada por profissional e data.

## Observações para evolução

- Trocar H2 por PostgreSQL em ambiente persistente ajustando `spring.datasource.*` e `spring.jpa.hibernate.ddl-auto`.
- Adicionar migrations com Flyway ou Liquibase antes de produção.
- Restringir CORS por ambiente.
- Criar testes unitários para serviços e testes de integração para endpoints.
- Avaliar autenticação/autorização caso a API seja exposta fora de ambiente controlado.
