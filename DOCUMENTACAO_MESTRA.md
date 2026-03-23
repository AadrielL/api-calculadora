# ⚡ Ecossistema Elétrico Inteligente (NBR 5410) - Documentação Mestra

Este documento detalha a arquitetura, o fluxo de dados e a jornada do usuário no sistema distribuído de gestão e cálculo para eletricistas.

---

## 🏗️ 1. Contexto Geral do Ecossistema
O sistema é composto por três APIs Java/Spring Boot que trabalham em conjunto para isolar responsabilidades e garantir escalabilidade:

* **API Calculadora (Porta 8081):** O cérebro comercial. Gerencia orçamentos, dashboard de métricas, configurações de preços e o histórico com sistema de Soft Delete.
* **API Materiais (Porta 8082):** O cérebro técnico. Contém a `LevantamentoEngine` que processa as cargas elétricas e gera snapshots técnicos imutáveis baseados na NBR 5410.
* **API Auth (Porta 8083):** O cérebro de segurança. Centraliza a autenticação JWT e injeta o `tenantId` para garantir o isolamento Multitenancy (um eletricista nunca vê os dados de outro).

---

## 🗄️ 2. Fluxo de Dados e Banco (Relacionamentos)
O banco de dados PostgreSQL utiliza o campo `tenant_id` em todas as tabelas críticas para filtragem dinâmica via `TenantContext`.

* **Profissional (Auth):** Tabela mestre que gera o `tenant_id`.
* **Orçamentos:** Cada registro pertence a um `tenant_id`. Possui estados como `ACEITO`, `PENDENTE` ou `EXCLUIDO` (Soft Delete).
* **Snapshot de Materiais:** Relacionamento 1:1 com o Orçamento. Salva a lista de materiais calculada no momento da venda, evitando que atualizações de preços ou normas alterem orçamentos já enviados.
* **Configurações:** Tabela de chave-valor por `tenant_id` que define o custo do $m^2$, diárias e itens personalizados.

---

## 👤 3. Jornada do Cliente (Do Quiz ao Orçamento)
O fluxo foi projetado para ser 100% digital e rápido:

1.  **Entrada (Angular/Javascript):** O cliente acessa a URL única do eletricista e preenche o quiz (áreas da casa, pontos de luz, ar-condicionado).
2.  **Processamento Comercial (8081):** A API Calculadora recebe o quiz, identifica o profissional pelo `tenantId` e calcula o valor da mão de obra.
3.  **Processamento Técnico (8082):** Em paralelo, os dados são enviados para a Engine de Materiais, que aplica as regras da NBR 5410 para definir cabos, disjuntores e tubulações.
4.  **Finalização:** O sistema consolida Mão de Obra + Materiais em um Dashboard que o cliente pode visualizar e o eletricista pode gerir em tempo real.

---

## 📡 4. Tabela de Endpoints Principais

| Serviço | Método | Endpoint | Função |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/auth/login` | Gera Token JWT e TenantId. |
| **Calculadora** | `POST` | `/v1/orcamentos/gerar-quiz` | Inicia novo orçamento. |
| **Calculadora** | `GET` | `/api/v1/dashboard/stats` | Alimenta gráficos de performance. |
| **Materiais** | `POST` | `/v1/levantamento/calcular` | Executa Engine NBR 5410. |
| **Histórico** | `DELETE` | `/v1/historico/{id}` | Move orçamento para a lixeira. |

---

> **Nota Técnica:** Todas as APIs estão configuradas com **CORS** para permitir o consumo seguro via **Javascript/Angular**, suportando Headers customizados para o tráfego do Token JWT.