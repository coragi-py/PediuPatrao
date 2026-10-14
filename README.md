# 🍕 Sistema PediuPatrão - Evolução e Segurança

Este repositório contém a evolução da aplicação **PediuPatrão**, um sistema de gestão para pizzarias. O projeto foi aprimorado para incluir controle rigoroso de acesso (RBAC), gestão de fluxo de pedidos, cálculos automatizados de valores no backend e um sistema completo de auditoria persistida em banco de dados NoSQL.

## 🛠 Tecnologias Utilizadas
* **Java 21**
* **Spring Boot (3.4.4)**
* **Spring Security** (Autenticação e Proteção de Rotas/Métodos)
* **MongoDB Atlas** (Persistência de Dados e Auditoria)
* **Thymeleaf** + `thymeleaf-extras-springsecurity6` (Renderização de Front-End)
* **Maven** (Gerenciamento de Dependências)

---

## Configuração de Segurança e Variáveis de Ambiente (.env)

Para garantir as boas práticas de engenharia de software e a segurança das credenciais, este projeto não expõe senhas diretamente no código-fonte (`application.properties`). Utilizamos o padrão de variáveis de ambiente.

Para rodar o projeto em sua máquina ou testar a conexão, siga estes passos:

1. Na raiz do projeto (na mesma pasta onde se encontra o arquivo `pom.xml`), crie um arquivo chamado exatamente **`.env`**.
2. Cole o conteúdo abaixo dentro do arquivo `.env`, substituindo as informações pelas credenciais reais do seu MongoDB Atlas:

   ```env
   MONGO_URI=mongodb+srv://<seu-usuario>:<sua-senha>@<seu-cluster>.mongodb.net/?retryWrites=true&w=majority&appName=PediuPatrao
   SPRING_SECURITY_PASSWORD=<sua-senha-segura>

---

## Instruções de Execução

Para rodar o projeto localmente em sua máquina, siga os passos abaixo:

1. **Pré-requisitos:** Certifique-se de ter o **JDK 21** instalado e configurado, além de uma IDE de sua preferência (NetBeans, IntelliJ IDEA, Eclipse ou VS Code).
2. **Configuração do Banco de Dados:**
   * Crie um cluster gratuito no [MongoDB Atlas](https://www.mongodb.com/cloud/atlas).
   * Vá em **Network Access** e adicione o IP `0.0.0.0/0` para permitir conexões externas.
   * Vá em **Database Access** e crie um usuário/senha.
3. **Configuração do Projeto:**
   * Abra o arquivo `src/main/resources/application.properties`.
   * Substitua a URI de conexão pela string do seu cluster, incluindo o usuário e a senha criados:
     `spring.data.mongodb.uri=mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/?retryWrites=true&w=majority`
4. **Construção e Execução:**
   * Na sua IDE (ou via terminal), execute o comando Maven `Clean and Build` (ou `mvn clean install`) para baixar todas as dependências.
   * Inicie o projeto rodando a classe principal `PizzariaPedidosApplication.java`.
   * Acesse a aplicação no navegador através de `http://localhost:8080`.

---

## Requisitos Implementados

* **RF01 - Controle de Usuários e Permissões:** Implementação de 3 perfis distintos (`ADMIN`, `GERENTE`, `ATENDENTE`). O acesso foi bloqueado nativamente no backend via `@PreAuthorize`, além do controle visual no HTML. A gestão de contas é exclusiva do `ADMIN`.
* **RF02 - Evolução do Cadastro de Pedidos:** Reestruturação da entidade `Pedido` para receber múltiplos itens (`ItemPedido`). O cálculo do valor final é realizado estritamente no servidor, ignorando valores arbitrários do client-side.
* **RF03 - Controle de Transições de Status:** Criação de uma máquina de estados rigorosa para os pedidos (`RECEBIDO` ➔ `EM PREPARAÇÃO` ➔ `PRONTO` ➔ `SAIU PARA ENTREGA` / `RETIRADO` ➔ `FINALIZADO`). Impedimento estrutural de transições inválidas.
* **RF04 - Cancelamentos e Descontos Exclusivos:** Ações restritas ao `GERENTE`. O desconto é limitado a 20% do valor total. Implementação da autorização em tempo de execução ("Overriding"), permitindo que um Atendente solicite a credencial do Gerente sem precisar deslogar do sistema.
* **RF05 - Auditoria de Dados Sensíveis:** Criação de entidade e serviço autônomo para auditar operações. Registra `data/hora`, `usuário`, `perfil`, `valores antigos` e `novos valores` para ações como inclusões, descontos, cancelamentos e alterações cadastrais de clientes.

---

## Principais Alterações Realizadas no Projeto Base

1. **Correção de Brechas de Segurança (`SecurityConfig` e `UsuarioDetailsService`):**
   * Habilitação da anotação `@EnableMethodSecurity` para garantir bloqueios nas APIs.
   * Correção na injeção de Roles: o sistema original atribuía "USER" para todos de forma estática; agora ele consulta o banco de dados dinamicamente.
   * Fechamento da rota `/api/usuarios` que antes permitia cadastros sem autenticação (permitAll).
2. **Implementação do Thymeleaf Security Extras (`pom.xml`):**
   * Adição da dependência `thymeleaf-extras-springsecurity6` para controlar a exibição condicional de menus (`sec:authorize`) com base no perfil do usuário logado.
3. **Evolução do Entidade `Pedido` e criação de `ItemPedido`:**
   * Inserção dos atributos de controle temporal (`dataHoraEntrada`, `dataHoraSaida`), responsáveis (`responsavelRegistro`, `responsavelSaida`) e de cálculo (`valorTotal`, `desconto`).
4. **Criação do Módulo de Auditoria Isolado:**
   * Injeção do `AuditoriaService` diretamente no `PedidoService` e `ClienteService` para capturar os dados do contexto de segurança do Spring e salvar logs automaticamente no MongoDB de forma desacoplada.
5. **Integração Front-End e CSRF:**
   * Substituição de métodos vulneráveis no HTML e adição de scripts assíncronos (`fetch`) que carregam ativamente o token CSRF (`_csrf.token`) gerado pelo Spring, garantindo comunicações POST/PUT seguras entre as telas e a API.

---