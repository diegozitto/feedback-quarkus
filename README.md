# Sistema de Feedback de Cursos (Pos Tech - Fase 4)
Este projeto implementa uma plataforma de feedback serverless utilizando Quarkus e AWS Lambda.  
O sistema permite que estudantes avaliem aulas, armazenando os dados no DynamoDB e notificando administradores via SNS em caso de notas baixas.  

# Guia de Teste (Postman)
URL do Endpoint: POST - https://d9qwrm05fd.execute-api.sa-east-1.amazonaws.com/Prod/avaliacao/  

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
* Build: ./mvnw clean package -DskipTests

* Deploy: sam deploy
