# Geospatial - Avaliação Java SCCON

API REST em Spring Boot para cadastro de pessoas em memoria, com calculo de idade e salario.

## Tecnologias

- Java 21
- Spring Boot 3.2.3
- Maven
- Bean Validation
- springdoc-openapi
- JUnit 5 e Mockito

## Como executar

### Pre-requisitos

- Java 17 ou superior
- Maven 3.x

Se usar `sdkman`:

```bash
sdk use java 21.0.4-tem
```

### Subindo a aplicacao

```bash
mvn spring-boot:run
```

Para rodar em outra porta sem alterar o padrao do projeto:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

### Testes

```bash
mvn clean test
```

### Gerando o jar

```bash
mvn clean package
java -jar target/geospatial-1.0.0.jar
```

## Documentacao da API

Com a aplicacao em execucao:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Endpoints

As datas usam o formato `dd/MM/yyyy`.

### `GET /person`

Lista todas as pessoas em ordem alfabetica por nome.

### `GET /person/{id}`

Busca uma pessoa pelo `id`.

### `POST /person`

Cria uma pessoa.

Exemplo:

```json
{
  "name": "Maria Oliveira",
  "birthDate": "15/08/1995",
  "admissionDate": "01/03/2019"
}
```

Regras:

- `id` e opcional; se nao for enviado, e gerado automaticamente
- `birthDate` deve estar no passado
- `admissionDate` nao pode estar no futuro
- `admissionDate` nao pode ser anterior a `birthDate`

### `PUT /person/{id}`

Substitui todos os dados da pessoa informada. Todos os campos sao obrigatorios.

### `PATCH /person/{id}`

Atualiza parcialmente uma pessoa.

Exemplos:

```json
{ "name": "Jose Renomeado" }
```

```json
{ "admissionDate": "01/01/2022" }
```

Regras:

- pelo menos um campo deve ser enviado
- `name`, quando enviado, nao pode ser vazio
- `birthDate`, quando enviada, deve estar no passado
- `admissionDate`, quando enviada, nao pode estar no futuro
- a combinacao final das datas continua sendo validada

### `DELETE /person/{id}`

Remove uma pessoa pelo `id`.

### `GET /person/{id}/age?output={days|months|years}`

Calcula a idade da pessoa em dias, meses ou anos.

### `GET /person/{id}/salary?output={full|min}`

Calcula o salario atual em valor total ou em quantidade de salarios minimos.

## Regras de negocio

- os dados ficam em memoria usando `ConcurrentHashMap`
- ids automaticos sao gerados com `AtomicInteger`
- a idade e calculada com `ChronoUnit` sobre a data atual
- o salario parte de `1558.00`, com reajuste anual de `18%` e bonus fixo de `500.00`
- o arredondamento final usa `RoundingMode.CEILING`

## Validacoes e erros

O projeto usa Bean Validation para entrada e `@RestControllerAdvice` para padronizar respostas de erro.

Exemplos de situacoes tratadas:

- campo obrigatorio ausente
- datas invalidas ou inconsistentes
- `output` fora dos valores aceitos
- JSON malformado
- data em formato invalido

Exemplo de erro de validacao:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "violations": [
    {
      "field": "name",
      "rejectedValue": null,
      "message": "Name is required"
    }
  ]
}
```

## Estrutura

```text
controller   -> endpoints HTTP
service      -> regras de negocio
repository   -> armazenamento em memoria
calculator   -> calculos de idade e salario
dto          -> entrada e saida da API
handler      -> tratamento global de erros
exception    -> excecoes de dominio
```

## Dados iniciais

Ao subir a aplicacao, o repositorio em memoria e carregado com:

- Ana Costa
- Carlos Mendes
- Jose da Silva
