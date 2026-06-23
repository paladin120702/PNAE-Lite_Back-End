# PNAE Lite — Backend

API REST do **PNAE Lite** (Sistema Simplificado de Gestão de Merenda Escolar).
Responsável pela modelagem do domínio, regras de negócio e persistência.

## Stack

- **Java 17** + **Spring Boot 3.2.5**
- **Spring Web** (REST), **Spring Data JPA** (Hibernate), **Bean Validation**
- **H2** (banco em memória) com console habilitado
- **JUnit 5** + **Mockito** (testes)

## Arquitetura (DDD)

O código é organizado por **domínio** da regra de negócio:

```
com.pnaelite
├── cardapio/                 # Bounded context: Cardápio
│   ├── domain/               #   Cardapio, ItemCardapio (associativa) + repositório
│   ├── application/          #   CardapioService + DTOs
│   └── web/                  #   CardapioController (GET /cardapios)
├── estoque/                  # Bounded context: Estoque / Consumo
│   ├── domain/               #   Escola, Ingrediente, Estoque + repositórios
│   ├── application/          #   ConsumoService (regra de desconto) + DTOs
│   └── web/                  #   ConsumoController (POST /consumo)
└── shared/
    ├── config/               #   WebConfig (CORS), DataSeeder (dados de exemplo)
    └── exception/            #   Exceções de domínio + GlobalExceptionHandler
```

### Modelo de domínio

| Entidade | Descrição | Relacionamentos |
|---|---|---|
| `Escola` | Escola que consome a merenda | — |
| `Ingrediente` | Insumo (nome + unidade de medida) | — |
| `Estoque` | Quantidade de um ingrediente numa escola | `@ManyToOne` → `Escola`, `Ingrediente` (único por par) |
| `Cardapio` | Cardápio servido | `@OneToMany` → `ItemCardapio` |
| `ItemCardapio` | **Entidade associativa** Cardápio↔Ingrediente | `@ManyToOne` → `Cardapio`, `Ingrediente` + `quantidadePorAluno` |

## Regra de negócio crítica

No `POST /consumo`, o `ConsumoService` (anotado com `@Transactional`):

1. Para cada ingrediente do cardápio, calcula `quantidadePorAluno × quantidadeAlunos`.
2. Verifica o estoque da escola.
3. Se **qualquer** ingrediente não tiver estoque suficiente, lança
   `EstoqueInsuficienteException` → o JPA faz **rollback** de todos os descontos da
   transação (nada é persistido) e a API responde **HTTP 422**.

## Endpoints

| Método | Rota | Descrição | Respostas |
|---|---|---|---|
| `GET` | `/cardapios` | Lista cardápios com seus itens | `200` |
| `POST` | `/consumo` | Registra consumo e desconta o estoque | `200`, `400` (validação), `404` (não encontrado), `422` (estoque insuficiente) |

### Exemplo — `POST /consumo`

```bash
curl -X POST http://localhost:8080/consumo \
  -H 'Content-Type: application/json' \
  -d '{"escolaId":1,"cardapioId":1,"quantidadeAlunos":50}'
```

Sucesso (`200`):
```json
{
  "mensagem": "Consumo registrado com sucesso.",
  "cardapio": "Arroz, feijao e carne",
  "quantidadeAlunos": 50,
  "descontos": [
    { "ingrediente": "Arroz", "unidadeMedida": "g", "quantidadeDescontada": 4000.0, "quantidadeRestante": 6000.0 }
  ]
}
```

Estoque insuficiente (`422`):
```json
{
  "timestamp": "2026-06-22T16:08:41Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Estoque insuficiente de 'Arroz': necessario 8000000.000 g, disponivel 6000.000 g.",
  "details": { "ingrediente": "Arroz", "unidadeMedida": "g", "necessario": 8000000.0, "disponivel": 6000.0 }
}
```

## Banco de dados (H2 em memória)

- **JDBC URL:** `jdbc:h2:mem:pnaedb`  ·  **Usuário:** `sa`  ·  **Senha:** *(vazia)*
- **Console:** http://localhost:8080/h2-console
- Os dados de exemplo (1 escola, 7 ingredientes, estoque e 3 cardápios) são criados
  na inicialização pelo `DataSeeder`.
  > Como o banco é **em memória**, os dados são recriados a cada start da aplicação.

## Como rodar

### Local (Maven)

```bash
cd backend
mvn spring-boot:run          # API em http://localhost:8080
```

### Testes

```bash
mvn test
```

- `ConsumoServiceTest` — unitário (Mockito): desconto, falta de estoque, recursos inexistentes.
- `ConsumoIntegrationTest` — integração (`@SpringBootTest` + H2 real): persistência e **rollback** efetivo.

O perfil `test` (`application-test.properties`) usa um H2 isolado (`pnaedb-test`,
`ddl-auto=create-drop`) e desativa o `DataSeeder` (`@Profile("!test")`).

### Docker

O `docker-compose.yml` mora **neste repositório** (backend) e sobe backend + frontend
juntos. Como o frontend está em outro repositório, ele é referenciado como **pasta irmã**
(clonada lado a lado):

```
workspace/
├── backend/    (este repo, contém o docker-compose.yml)
└── frontend/   (repo do frontend, clonado ao lado)
```

```bash
# a partir da pasta do backend (sobe backend + frontend)
docker compose up --build        # API em :8080, UI em :5173

# se o diretório do frontend tiver outro nome:
FRONTEND_PATH=../pnae-frontend docker compose up --build

# apenas o backend (sem o frontend)
docker build -t pnae-backend .
docker run -p 8080:8080 pnae-backend
```

O H2 permanece **em memória** dentro do container — nenhum serviço de banco externo
é necessário.

## Configuração

Principais propriedades em `src/main/resources/application.properties`:

| Propriedade | Valor | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta HTTP |
| `spring.datasource.url` | `jdbc:h2:mem:pnaedb` | H2 em memória |
| `spring.h2.console.enabled` | `true` | Habilita o console do H2 |
| `spring.jpa.hibernate.ddl-auto` | `update` | Geração do schema pelo Hibernate |

O CORS é liberado para `http://localhost:5173` (frontend) em `shared/config/WebConfig.java`.
