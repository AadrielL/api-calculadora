# ⚡ Ecossistema Elétrico Inteligente (NBR 5410) - Documentação Mestra

Este documento detalha a arquitetura, o fluxo de dados e a jornada do usuário no sistema distribuído de gestão e cálculo para eletricistas profissionais.

---

## 🏗️ 1. Contexto Geral do Ecossistema
O sistema é composto por três APIs **Java / Spring Boot 3** que trabalham em conjunto para isolar responsabilidades e garantir escalabilidade:

* **API Calculadora (Porta 8081):** O cérebro comercial. Gerencia orçamentos, dashboard de métricas, configurações de preços e o histórico com sistema de **Soft Delete** (Lixeira).
* **API Materiais (Porta 8082):** O cérebro técnico. Contém a `LevantamentoEngine` que processa as cargas elétricas e gera **snapshots técnicos imutáveis** baseados na **NBR 5410**.
* **API Auth (Porta 8083):** O cérebro de segurança. Centraliza a autenticação **JWT** e injeta o `tenantId` para garantir o isolamento **Multitenancy** (um eletricista nunca vê os dados de outro).

---

## 🗄️ 2. Fluxo de Dados e Banco (Relacionamentos)
O banco de dados **PostgreSQL** utiliza o campo `tenant_id` em todas as tabelas críticas para filtragem dinâmica via `TenantContext`.

* **Profissional (Auth):** Tabela mestre que gera o identificador único (`tenant_id`).
* **Orçamentos:** Cada registro pertence a um profissional. Possui estados como `ACEITO`, `PENDENTE` ou `EXCLUIDO`.
* **Snapshot de Materiais:** Relacionamento **1:1** com o Orçamento. Salva a lista de materiais exata do momento da venda, protegendo o profissional contra alterações futuras de preços ou normas.
* **Configurações:** Tabela de chave-valor que define custos de $m^2$, diárias e itens personalizados.

---

## 👤 3. Jornada do Cliente (User Flow)
O fluxo foi projetado para ser 100% digital, garantindo autonomia estratégica ao profissional:

1. **Entrada (Angular / Javascript):** O cliente acessa a URL única do profissional e preenche o **Quiz Técnico** (áreas da casa, pontos de luz, tomadas de uso específico).
2. **Processamento Comercial (8081):** A API Calculadora identifica o profissional, aplica as taxas configuradas e calcula o valor da mão de obra.
3. **Processamento Técnico (8082):** A Engine de Materiais processa os dados sob a norma **NBR 5410** para definir o dimensionamento de cabos, disjuntores e infraestrutura.
4. **Visualização Controlada (Dashboard):**
    * **Cliente:** Visualiza apenas o **valor total da mão de obra** e o escopo do serviço.
    * **Eletricista:** Tem acesso exclusivo à **lista detalhada de materiais** e decide se deseja liberar o relatório técnico ou utilizá-lo apenas para logística própria.
5. **Conversão:** O profissional monitora o status pelo dashboard e gerencia o fechamento do contrato.

---

## 📡 4. Tabela de Endpoints Principais

| Serviço | Método | Endpoint | Função |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/auth/login` | Gera Token JWT e TenantId. |
| **Calculadora** | `POST` | `/v1/orcamentos/gerar-quiz` | Inicia novo fluxo de orçamento. |
| **Calculadora** | `GET` | `/api/v1/dashboard/stats` | Alimenta gráficos de performance. |
| **Materiais** | `POST` | `/v1/levantamento/calcular` | Executa Engine técnica NBR 5410. |
| **Histórico** | `DELETE` | `/v1/historico/{id}` | Move orçamento para a lixeira (Soft Delete). |

---

> **Nota Técnica:** Todas as APIs estão configuradas com **CORS** para permitir o consumo seguro via **Javascript (Angular/React)**, suportando Headers customizados para o tráfego do Token JWT e isolamento de contexto.