# Sistema de Feedback de Cursos (Quarkus + AWS Lambda)
Este projeto é uma API Serverless desenvolvida com Quarkus e implantada na AWS usando SAM (Serverless Application Model).  
O sistema recebe avaliações de cursos, armazena no DynamoDB e dispara notificações via SNS para casos críticos.

# Endpoint de Teste
O endpoint principal para envio de feedbacks é: POST  
[https://d9qwrm05fd.execute-api.sa-east-1.amazonaws.com/Prod/avaliacao/](https://d9qwrm05fd.execute-api.sa-east-1.amazonaws.com/Prod/avaliacao/)

# Padrão das Requisições (Postman)
Para testar a API, configure uma nova requisição no Postman com os seguintes detalhes:  

Método: POST  

Headers: Content-Type: application/json  

Body (raw JSON):  

{  
  "descricao": "O conteúdo de Quarkus com AWS está excelente!",  
  "nota": 8  
}

# Regras de Prioridade (Urgência)
O sistema classifica automaticamente a urgência do feedback com base na nota enviada:

0 a 5	ALTA	Salva no banco e envia e-mail de alerta ao coordenador.  
6 a 7	MEDIA	Apenas salva no banco de dados.  
8 a 10	BAIXA	Apenas salva no banco de dados.

# Tecnologias Utilizadas
Java 21  

Quarkus (Framework Java nativo para nuvem)  

AWS Lambda (Processamento Serverless)  

Amazon DynamoDB (Banco de dados NoSQL)  

Amazon SNS (Serviço de notificações)  

AWS SAM (Infraestrutura como Código)  

# Como executar o projeto localmente

Build do projeto:  
./mvnw clean package -DskipTests  

Deploy na AWS:  
sam deploy  
