# 🍕 Pediu Patrão — Dashboard Administrativo

O **Pediu Patrão** é um sistema web para gestão de uma pizzaria/lanchonete, desenvolvido em Java com Spring Boot, Thymeleaf e MongoDB Atlas.

Esta entrega corresponde à **segunda etapa** do projeto e implementa um **Dashboard Administrativo** integrado ao sistema de pedidos. O objetivo é permitir que o responsável pelo estabelecimento acompanhe o desempenho das vendas, visualize a situação dos pedidos, identifique os produtos mais vendidos e analise tendências da operação.

---

## 🎯 Escopo da entrega

A solução foi desenvolvida para atender aos seguintes requisitos:

- **RF01 — Indicadores gerais de vendas**
- **RF02 — Análise de vendas**
- **RF03 — Situação dos pedidos**
- **RF04 — Produtos mais vendidos**
- **RF05 — Filtros de consulta**
- **RF06 — Integração com a API**
- **RF07 — Atualização e tratamento de dados**

A API foi projetada para fornecer os dados necessários aos indicadores, gráficos, rankings e filtros do dashboard.

---

## 📊 Dashboard administrativo

O dashboard está disponível em:

```text
/dashboard
```

O acesso é restrito aos perfis `ADMIN` e `GERENTE`.

### Indicadores apresentados

- Total de pedidos
- Pedidos válidos
- Pedidos cancelados
- Faturamento total
- Ticket médio
- Total de descontos
- Taxa de cancelamento
- Evolução de vendas por período
- Distribuição dos pedidos por status
- Ranking de produtos mais vendidos
- Receita gerada por produto

### Filtros disponíveis

- Data inicial
- Data final
- Status do pedido
- Quantidade de itens exibidos no ranking de produtos

---

## 📡 API do Dashboard

A API do dashboard está disponível sob o prefixo:

```text
/api/dashboard
```

Todos os endpoints exigem autenticação e são restritos aos perfis `ADMIN` ou `GERENTE`.

### Parâmetros

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `dataInicio` | `LocalDate` | Não | Data inicial do filtro, no formato `yyyy-MM-dd` |
| `dataFim` | `LocalDate` | Não | Data final do filtro, no formato `yyyy-MM-dd` |
| `status` | `String` | Não | Status do pedido; use `TODOS` para ignorar o filtro |
| `limite` | `int` | Não | Quantidade de produtos exibidos no ranking; padrão `5` |

### `GET /api/dashboard/indicadores`

Retorna os indicadores gerais de vendas.

**Exemplo de requisição:**

```http
GET /api/dashboard/indicadores?dataInicio=2026-11-01&dataFim=2026-11-30&status=TODOS
```

**Exemplo de resposta:**

```json
{
  "totalPedidos": 10,
  "pedidosValidos": 8,
  "pedidosCancelados": 2,
  "faturamentoTotal": 800.0,
  "ticketMedio": 100.0,
  "totalDescontos": 50.0,
  "taxaCancelamentoPercentual": 20.0
}
```

**Regras de cálculo:**

- Pedidos cancelados não entram no faturamento.
- O ticket médio é calculado dividindo o faturamento total pela quantidade de pedidos válidos.
- Descontos são considerados apenas em pedidos válidos.
- A taxa de cancelamento considera o total de pedidos do período filtrado.

### `GET /api/dashboard/vendas-periodo`

Retorna a evolução de pedidos e faturamento por dia.

**Exemplo de requisição:**

```http
GET /api/dashboard/vendas-periodo?dataInicio=2026-11-01&dataFim=2026-11-30&status=TODOS
```

**Exemplo de resposta:**

```json
[
  {
    "data": "01/11/2026",
    "quantidadePedidos": 3,
    "faturamento": 250.0
  },
  {
    "data": "02/11/2026",
    "quantidadePedidos": 5,
    "faturamento": 550.0
  }
]
```

**Regras de cálculo:**

- Pedidos cancelados não são considerados.
- Pedidos sem `dataHoraEntrada` são desconsiderados.
- Os resultados são agrupados por dia, em ordem cronológica.

### `GET /api/dashboard/status-pedidos`

Retorna a distribuição dos pedidos por status.

**Exemplo de requisição:**

```http
GET /api/dashboard/status-pedidos?dataInicio=2026-11-01&dataFim=2026-11-30
```

**Exemplo de resposta:**

```json
[
  {
    "status": "RECEBIDO",
    "quantidade": 2,
    "percentual": 20.0
  },
  {
    "status": "EM PREPARAÇÃO",
    "quantidade": 3,
    "percentual": 30.0
  },
  {
    "status": "PRONTO",
    "quantidade": 1,
    "percentual": 10.0
  },
  {
    "status": "SAIU PARA ENTREGA",
    "quantidade": 1,
    "percentual": 10.0
  },
  {
    "status": "RETIRADO",
    "quantidade": 1,
    "percentual": 10.0
  },
  {
    "status": "FINALIZADO",
    "quantidade": 1,
    "percentual": 10.0
  },
  {
    "status": "CANCELADO",
    "quantidade": 1,
    "percentual": 10.0
  }
]
```

**Regras de cálculo:**

- Todos os status padrão são retornados, mesmo quando não há pedidos naquele status.
- O percentual é calculado sobre o total de pedidos filtrados.
- Pedidos sem status definido não são considerados na contagem.

### `GET /api/dashboard/produtos-mais-vendidos`

Retorna o ranking de produtos mais vendidos.

**Exemplo de requisição:**

```http
GET /api/dashboard/produtos-mais-vendidos?dataInicio=2026-11-01&dataFim=2026-11-30&status=TODOS&limite=5
```

**Exemplo de resposta:**

```json
[
  {
    "produto": "Pizza Calabresa",
    "quantidadeVendida": 12,
    "receitaGerada": 600.0
  },
  {
    "produto": "Pizza Margherita",
    "quantidadeVendida": 8,
    "receitaGerada": 400.0
  }
]
```

**Regras de cálculo:**

- Pedidos cancelados não são considerados.
- A quantidade vendida é calculada a partir dos itens do pedido.
- A receita gerada é calculada por `quantidade × precoUnitario`.
- O parâmetro `limite` define quantos produtos serão exibidos; o valor padrão é `5`.

### Possíveis erros

| Código HTTP | Situação | Causa |
|---|---|---|
| `401 Unauthorized` | Usuário não autenticado | O usuário não está logado |
| `403 Forbidden` | Acesso negado | O usuário não possui perfil `ADMIN` ou `GERENTE` |
| `400 Bad Request` | Parâmetros inválidos | A data inicial é posterior à data final |
| `500 Internal Server Error` | Falha inesperada | Erro de processamento ou conexão com o banco |

---

## 📡 API de Pedidos

A API de pedidos é responsável pelo registro e acompanhamento dos pedidos que alimentam o dashboard.

### `POST /api/pedidos`

Cria um pedido com um ou mais itens.

**Acesso:** `ATENDENTE` ou `GERENTE`.

**Exemplo de requisição:**

```http
POST /api/pedidos
Content-Type: application/json
```

```json
{
  "clienteId": "João",
  "itens": [
    {
      "sabor": "Pizza Calabresa",
      "quantidade": 2
    },
    {
      "sabor": "Coca-Cola",
      "quantidade": 1
    }
  ]
}
```

**Regras de negócio:**

- O pedido deve conter pelo menos um item.
- A quantidade de cada item deve ser maior que zero.
- O preço unitário é obtido do catálogo oficial de produtos.
- O valor total é calculado no servidor.
- O desconto inicial é sempre `0.0`.
- O status inicial é `RECEBIDO`.

**Exemplo de resposta:**

```json
{
  "id": "672f123abc456def78901234",
  "clienteId": "João",
  "itens": [
    {
      "sabor": "Pizza Calabresa",
      "quantidade": 2,
      "precoUnitario": 50.0
    },
    {
      "sabor": "Coca-Cola",
      "quantidade": 1,
      "precoUnitario": 8.0
    }
  ],
  "valorTotal": 108.0,
  "desconto": 0.0,
  "status": "RECEBIDO"
}
```

### `GET /api/pedidos`

Retorna a lista de pedidos cadastrados.

**Acesso:** `ADMIN`, `GERENTE` ou `ATENDENTE`.

### `PUT /api/pedidos/{id}/status`

Atualiza o status do pedido conforme a máquina de estados.

**Acesso:** `ATENDENTE` ou `GERENTE`.

**Exemplo:**

```http
PUT /api/pedidos/{id}/status?status=EM%20PREPARA%C3%87%C3%83O
```

### `PUT /api/pedidos/{id}/desconto`

Aplica desconto ao pedido.

**Acesso:** `ATENDENTE` ou `GERENTE`; a autorização de gerente é exigida quando o usuário logado não é `GERENTE`.

**Exemplo:**

```http
PUT /api/pedidos/{id}/desconto?percentual=10&usernameGerente=gerente&senhaGerente=senha
```

### `PUT /api/pedidos/{id}/cancelar`

Cancela o pedido com justificativa obrigatória.

**Acesso:** `ATENDENTE` ou `GERENTE`; a autorização de gerente é exigida quando o usuário logado não é `GERENTE`.

**Exemplo:**

```http
PUT /api/pedidos/{id}/cancelar?justificativa=Produto%20indispon%C3%ADvel&usernameGerente=gerente&senhaGerente=senha
```

---

## 🧾 Regras de negócio

### Pedidos

- Um pedido deve conter pelo menos um item.
- A quantidade de cada item deve ser maior que zero.
- O preço do item é obtido obrigatoriamente do catálogo de produtos cadastrado no banco.
- O preço enviado pelo cliente não é aceito como valor oficial.
- O desconto inicia sempre em `0.0` na criação do pedido.
- O valor total é calculado no servidor.
- O status inicial do pedido é `RECEBIDO`.

### Fluxo de status

```text
RECEBIDO
  → EM PREPARAÇÃO
  → PRONTO
  → SAIU PARA ENTREGA ou RETIRADO
  → FINALIZADO
```

Transições fora dessa sequência são bloqueadas pelo backend.

### Descontos

- Apenas o perfil `GERENTE` pode aplicar descontos.
- O desconto máximo permitido é de 20%.
- Não é possível aplicar desconto em pedidos que já saíram para entrega, foram retirados, finalizados ou cancelados.
- A aplicação do desconto é registrada na auditoria.

### Cancelamentos

- Apenas o perfil `GERENTE` pode autorizar cancelamentos.
- É obrigatório informar uma justificativa.
- Não é possível cancelar pedidos que já saíram para entrega, foram retirados, finalizados ou cancelados.
- O cancelamento é registrado na auditoria.

### Produtos

- Somente o perfil `GERENTE` pode cadastrar, editar ou excluir produtos.
- O nome e o tipo do produto são obrigatórios.
- O preço deve ser maior que zero.
- Não é permitido cadastrar dois produtos com o mesmo nome.
- Produtos ativos compõem o catálogo disponível para pedidos.
- O dashboard utiliza os itens dos pedidos para calcular o ranking de produtos mais vendidos.

---

## 🔐 Segurança e perfis de acesso

O sistema utiliza autenticação baseada em formulário e controle de acesso baseado em papéis — RBAC (*Role-Based Access Control*).

| Perfil | Principais permissões |
|---|---|
| `ADMIN` | Gerencia usuários, configurações e possui acesso ao dashboard |
| `GERENTE` | Acessa o dashboard, gerencia produtos, aplica descontos e autoriza cancelamentos |
| `ATENDENTE` | Registra e acompanha pedidos, consulta clientes e produtos |

| Rota | Acesso |
|---|---|
| `/api/usuarios/**` | `ADMIN` |
| `/usuarios/**` | `ADMIN` |
| `/configuracoes/**` | `ADMIN` |
| `/produtos/**` | `GERENTE` |
| `/api/dashboard/**` | `ADMIN` ou `GERENTE` |
| `/api/produtos` — `GET` | Usuário autenticado |
| `/api/produtos` — `POST`, `PUT`, `DELETE` | `GERENTE` |

A listagem de produtos permanece acessível a usuários autenticados porque é utilizada na tela de pedidos. A criação, edição e exclusão de produtos são restritas ao perfil `GERENTE`.

---

## ⚙️ Decisões de projeto

- O cálculo de valores, descontos e transições de status é realizado exclusivamente no backend, evitando manipulação de dados pelo cliente.
- O preço dos itens do pedido é obtido do cadastro oficial de produtos, impedindo que o frontend envie valores arbitrários.
- Pedidos cancelados são excluídos dos indicadores de faturamento, ticket médio e ranking de produtos.
- O dashboard consome uma API REST própria, separando a camada de apresentação da camada de dados e regras de negócio.
- Os filtros de período e status são aplicados no backend antes do cálculo dos indicadores.
- O parâmetro `limite` é aplicado no backend para limitar a quantidade de produtos retornados no ranking.
- Valores nulos são tratados para evitar falhas nos cálculos de faturamento, descontos, ticket médio e ranking.
- A auditoria registra operações sensíveis, como criação de pedidos, alterações de status, descontos e cancelamentos.

---

## ✅ Requisitos funcionais implementados

### Etapa 1

- **RF01 — Controle de usuários e permissões:** perfis `ADMIN`, `GERENTE` e `ATENDENTE`, com bloqueio de acesso no backend e controle visual no frontend.
- **RF02 — Evolução do cadastro de pedidos:** pedidos com múltiplos itens e cálculo do valor total no servidor.
- **RF03 — Controle de transições de status:** máquina de estados para impedir mudanças inválidas no fluxo do pedido.
- **RF04 — Cancelamentos e descontos exclusivos:** restrição das ações ao perfil `GERENTE`, com limite de 20% para descontos.
- **RF05 — Auditoria de dados sensíveis:** registro de operações relevantes, incluindo usuário, perfil, data/hora, valores anteriores e novos valores.

### Etapa 2

- **RF01 — Indicadores gerais de vendas:** total de pedidos, pedidos válidos, cancelados, faturamento, ticket médio, descontos e taxa de cancelamento.
- **RF02 — Análise de vendas:** série temporal de quantidade de pedidos e faturamento por dia.
- **RF03 — Situação dos pedidos:** distribuição dos pedidos entre os status do fluxo operacional.
- **RF04 — Produtos mais vendidos:** ranking por quantidade vendida e receita gerada.
- **RF05 — Filtros de consulta:** filtros por data inicial, data final e status do pedido.
- **RF06 — Integração com a API:** consumo dos endpoints REST pelo dashboard.
- **RF07 — Atualização e tratamento de dados:** tratamento de valores nulos, pedidos cancelados, itens inválidos e ausência de dados.

---

## ⚙️ Requisitos não funcionais

- **Segurança:** autenticação, autorização por papéis e proteção de rotas e métodos sensíveis.
- **Integridade de dados:** validação de regras de negócio e cálculo de valores no backend.
- **Rastreabilidade:** auditoria de operações críticas.
- **Usabilidade:** interface administrativa com sidebar, filtros, indicadores e gráficos.
- **Manutenibilidade:** separação entre controllers, services, repositories e entidades.
- **Persistência:** uso do MongoDB Atlas como banco de dados NoSQL.
- **Testabilidade:** testes unitários com JUnit 5 e Mockito.
- **Portabilidade:** execução local por meio do Maven Wrapper.

---

## 🧪 Evidências de testes

As evidências dos testes e da validação funcional estão disponíveis na pasta:

```text
docs/evidencias/
```

### Testes unitários

Os testes unitários do `PedidoServiceTest` validam:

- Cálculo do valor total com base no preço oficial do catálogo.
- Ignorância de preço e desconto enviados pelo cliente.
- Forçagem do desconto inicial para `0.0`.
- Bloqueio de transições inválidas de status.
- Aplicação de desconto válido de até 20%.
- Recálculo do valor total após desconto.
- Rejeição de desconto superior a 20%.

Comando executado:

```powershell
.\mvnw.cmd clean test
```

Resultado obtido:

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Validação funcional

Foram realizados testes manuais para validar:

- Criação de pedido com múltiplos itens.
- Cálculo do valor total pelo backend.
- Bloqueio de transições inválidas de status.
- Aplicação de desconto por perfil autorizado.
- Cancelamento com justificativa obrigatória.
- Geração dos indicadores do dashboard.
- Geração do gráfico de vendas por período.
- Distribuição dos pedidos por status.
- Ranking de produtos mais vendidos.
- Persistência e consulta dos dados no MongoDB Atlas.

---

## 📝 Changelog

### [Etapa 2] — Dashboard Administrativo e API

#### Adicionado

- Dashboard administrativo acessível pela rota `/dashboard`.
- Restrição de acesso ao dashboard para os perfis `ADMIN` e `GERENTE`.
- Endpoint `/api/dashboard/indicadores` para indicadores gerais de vendas.
- Endpoint `/api/dashboard/vendas-periodo` para análise temporal de vendas.
- Endpoint `/api/dashboard/status-pedidos` para distribuição dos pedidos por status.
- Endpoint `/api/dashboard/produtos-mais-vendidos` para ranking de produtos.
- Indicador de **ticket médio** no dashboard.
- Filtros por data inicial, data final e status do pedido.
- Filtro de limite para o ranking de produtos mais vendidos.
- Tratamento de pedidos cancelados nos indicadores e rankings.
- Tratamento de valores nulos em `valorTotal`, `desconto`, `dataHoraEntrada`, `quantidade` e `precoUnitario`.
- Tela de dashboard com consumo da API por JavaScript.
- Botão **Dashboard** na sidebar.
- Cadastro, edição, listagem e exclusão de produtos.
- Restrição das operações de produto ao perfil `GERENTE`.
- Endpoint `/api/produtos/ativos` para consulta do catálogo ativo.
- Funcionalidade de adição de múltiplos itens ao mesmo pedido antes da confirmação.
- Lista temporária de itens no modal de criação de pedido.
- Remoção individual de itens antes da confirmação do pedido.
- Cálculo provisório do total do pedido no frontend.
- Envio de todos os itens no array `itens` para o endpoint de criação de pedido.

#### Alterado

- `SecurityConfig` atualizado para proteger a rota `/produtos/**` com o papel `GERENTE`.
- `ProdutoController` atualizado para aplicar `@PreAuthorize("hasRole('GERENTE')")` nas operações de escrita.
- `ProdutoService` atualizado com validações de nome, tipo, preço e duplicidade.
- `DashboardService` atualizado para calcular ticket médio, filtrar pedidos e tratar valores nulos.
- `DashboardController` atualizado para receber filtros e retornar dados estruturados para o frontend.

#### Corrigido

- Correção de tipos genéricos incompletos em `DashboardController` e `DashboardService`.
- Correção do fechamento de blocos e estrutura sintática do `PedidoServiceTest`.
- Correção de incompatibilidade entre JDK 26 e a versão do Byte Buddy utilizada pelo Mockito, com execução dos testes utilizando JDK 21.
- Correção do botão “Confirmar Pedido”, que não acionava a requisição à API por referenciar um campo inexistente no formulário.
- Correção da limitação que impedia o registro de mais de um tipo de produto no mesmo pedido.
- Correção da sobrescrita de produtos no MongoDB causada pelo envio de identificador vazio no formulário de cadastro.

### [Etapa 1] — Evolução e segurança do sistema

#### Adicionado

- Controle de acesso baseado em papéis.
- Perfis `ADMIN`, `GERENTE` e `ATENDENTE`.
- Auditoria persistida no MongoDB.
- Entidade `ItemPedido`.
- Máquina de estados para pedidos.
- Regras de desconto e cancelamento.
- Integração do Thymeleaf com Spring Security.

#### Alterado

- Entidade `Pedido` reestruturada para suportar múltiplos itens.
- Cálculo de valores transferido para o backend.
- Rotas de usuários e configurações protegidas.

#### Corrigido

- Atribuição dinâmica de papéis aos usuários.
- Fechamento de rotas públicas sensíveis.
- Proteção de comunicações com token CSRF.

---

## 🔀 Git Flow

O projeto utiliza Git Flow para organizar o desenvolvimento.

```text
main
└── develop
    └── feature/dashboard-api
```

- `main`: código estável e validado.
- `develop`: integração das funcionalidades em desenvolvimento.
- `feature/dashboard-api`: implementação da API, dashboard, filtros, testes e integração com o MongoDB Atlas.

Comandos utilizados:

```bash
git checkout develop
git checkout -b feature/dashboard-administrativo

git add .
git commit -m "feat: implementa dashboard administrativo e API de indicadores"

git push origin feature/dashboard-administrativo
```

Após a validação, a branch é integrada à `develop` por meio de pull request.