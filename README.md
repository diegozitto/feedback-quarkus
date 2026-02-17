# Sistema de Feedback de Cursos (Pos Tech - Fase 4)
Este projeto implementa uma plataforma de feedback serverless utilizando Quarkus e AWS Lambda.  
O sistema permite que estudantes avaliem aulas, armazenando os dados no DynamoDB e notificando administradores via SNS em caso de notas baixas. 

###### Acesse a [*Documentação técnica*](https://docs.google.com/document/d/1g8KcWhl_I4Be7GKzJ8gfy7qqwckk8KNf1qWoCbZxUbU/edit?usp=sharing) para mais detalhes. 

# Guia de Teste (Postman)
URL do Endpoint: POST - <API_URL>/avaliacao/  

Configuração no Postman:  
1. Method: POST  

2. Body: Selecione raw e o tipo JSON.  

3. Cabeçalho: Verifique se Content-Type é application/json.  

# Exemplos de Requisição:

Feedback Positivo (Baixa Prioridade):  
{  
"descricao": "Conteúdo excelente e muito bem detalhado.",  
"nota": 10  
}

Feedback Crítico (Alta Prioridade - Gera Alerta SNS):  
{  
"descricao": "Tive problemas para acessar os materiais da aula.",  
"nota": 3  
}

# Regras de Negócio e Priorização
O sistema classifica as avaliações automaticamente:  

* Prioridade ALTA (Nota 0 a 5): Salva no banco e dispara um e-mail de alerta imediato via SNS contendo a descrição e nota.

* Prioridade MÉDIA (Nota 6 ou 7): Apenas registro no banco de dados.

* Prioridade BAIXA (Nota 8 a 10): Apenas registro no banco de dados.

# Tecnologias e Arquitetura
Runtime: Java 21 com Quarkus

Infraestrutura: AWS SAM (Serverless Application Model)

Banco de Dados: Amazon DynamoDB (Tabela Feedbacks)

Mensageria: Amazon SNS

# Como executar o Deploy
* Build (Windows - PowerShell):

```powershell
\mvnw.cmd clean package -DskipTests
```

Observações sobre o artifact produzido
- O projeto Quarkus está configurado para empacotar o deploy como um ZIP usado pelo SAM. O `CodeUri` nas funções do `template.yaml` aponta para `target/function.zip`. Confirme que após o build exista o arquivo `target/function.zip` antes de executar `sam build`.

* Construir e empacotar com SAM e fazer deploy (exemplo):

```powershell
sam build
sam deploy --guided --parameter-overrides DemoEmail=seu-email@exemplo.com
```

Durante o `sam deploy --guided` você pode definir o parâmetro `DemoEmail` (usado para criar uma subscription de demonstração no SNS). Após o deploy, verifique seu e-mail e confirme a subscription para começar a receber notificações.

# Variáveis de Ambiente usadas nas funções
As funções definidas em `template.yaml` expõem as seguintes variáveis de ambiente úteis para o código:

- `TABLE_NAME` — nome da tabela DynamoDB (ex.: Feedbacks)
- `SNS_TOPIC_ARN` — ARN do tópico SNS de alertas críticos
- `QUARKUS_LAMBDA_HANDLER` — nome do handler Quarkus configurado via variável (ex.: `registrarFeedbackHandler` / `gerarRelatorioHandler`)

No código Quarkus, injete/recupere `TABLE_NAME` e `SNS_TOPIC_ARN` via configurações/variáveis de ambiente para apontar corretamente para os recursos provisionados.

# Outputs do Stack
O `template.yaml` exporta os seguintes outputs (úteis para a demo):

- `FeedbackApi` — URL do endpoint público (API Gateway)
- `FeedbackTableName` — nome da tabela DynamoDB
- `FeedbackTableArn` — ARN da tabela DynamoDB
- `CriticalAlertsTopicArn` — ARN do tópico SNS
- `DemoNotificationEmail` — email configurado no deploy para receber as notificações de demonstração

# Subscription SNS de demonstração
O template cria uma subscription do tipo `email` usando o parâmetro `DemoEmail`. Para que você consiga demonstrar o envio de alertas durante a gravação:

1. Informe seu e-mail no deploy (parâmetro `DemoEmail`).
2. Abra sua caixa de entrada e confirme a assinatura (clique no link enviado pelo SNS).
3. Ao enviar um feedback com nota entre 0 e 5, uma mensagem será publicada no tópico e enviada ao e-mail inscrito (após confirmação).

Se preferir não usar e-mail na demo, você pode alterar a Subscription para `protocol: https` e apontar para um endpoint alternativo, ou criar uma subscription `sms`/`lambda` conforme necessário.

# DLQ e Tracing
- O `template.yaml` provisiona uma fila SQS `ProcessFeedbackDLQ` e configura as funções com `DeadLetterQueue` apontando para essa fila. Isso garante que invocações que falharem repetidamente sejam encaminhadas para análise posterior.
- As funções têm `Tracing: Active` para permitir rastreamento (AWS X-Ray). Durante a demo você pode abrir o console do X-Ray para mostrar traces ou apenas abrir os logs no CloudWatch.

# Policies aplicadas
As políticas aplicadas às funções são geradas via SAM Policy Templates e servem para garantir o princípio do least privilege:

- `DynamoDBCrudPolicy` (usada em `ProcessFeedbackFunction`) — concede permissões de leitura/gravação somente para a tabela indicada (`Feedbacks`). Isso permite inserir e consultar itens sem dar permissões globais.
- `DynamoDBReadPolicy` (usada em `WeeklyReportFunction`) — concede permissões de leitura à tabela para leitura do histórico e geração de relatórios.
- `SNSPublishMessagePolicy` (usada em ambas as funções) — permite publicar mensagens no tópico SNS `CriticalAlerts`.

Na apresentação, comente que em ambientes de produção você poderia criar uma Role IAM explícita com ARNs restritos e políticas mais granulares (e.g., ações específicas de DynamoDB e condições de recurso).

# Testes e Demonstração (curl)
Exemplo de requisição com `curl` para enviar um feedback crítico:

```powershell
curl -X POST "<API_URL>/avaliacao" -H "Content-Type: application/json" -d '{"descricao":"Problemas de acesso","nota":3}'
```

# Referências
- `template.yaml` — arquivo SAM que descreve recursos: funções Lambda, tabela DynamoDB, tópico SNS, subscription, SQS DLQ
- `pom.xml` — dependências e plugins Quarkus
