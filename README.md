# FreeToWear
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring MVC](https://img.shields.io/badge/Spring_MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![Cloudinary](https://img.shields.io/badge/Cloudinary-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white)

## Sumário
- [Estrutura da Aplicação](#estrutura-da-aplicação)
- [Dependências](#dependências)
- [Funcionalidades](#funcionalidades)
- [Próximas Versões](#próximas-versões)
- [Known Bugs](#known-bugs)
- [Licença](#licença)

---

## Estrutura da Aplicação
A aplicação segue arquitetura em camadas com separação entre Controller, Service e Repository:

Requisição HTTP → Controller → Service → Repository → MySQL

- **Controller** — recebe a requisição, valida os dados e delega para a camada de serviço
- **Service** — contém as regras de negócio, processa os DTOs e chama o repositório
- **Repository** — realiza o acesso ao banco de dados
- **DTO (Data Transfer Object)** — transporta dados entre as camadas, mantendo as entidades isoladas da camada HTTP

---

## Dependências

- [spring-boot-starter-web](https://spring.io/projects/spring-boot) `3.5.7`
- [spring-boot-starter-thymeleaf](https://www.thymeleaf.org) `3.5.7`
- [thymeleaf-extras-springsecurity6](https://github.com/thymeleaf/thymeleaf-extras-springsecurity) `3.5.7`
- [spring-boot-starter-security](https://spring.io/projects/spring-security) `3.5.7`
- [spring-boot-starter-validation](https://hibernate.org/validator) `3.5.7`
- [spring-session-jdbc](https://spring.io/projects/spring-session) `3.5.7`
- [spring-boot-starter-data-jpa](https://spring.io/projects/spring-data-jpa) `3.5.7`
- [mysql-connector-j](https://dev.mysql.com/downloads/connector/j) `9.4.0`
- [lombok](https://projectlombok.org) `1.18.34`
- [ulid-creator](https://github.com/f4b6a3/ulid-creator) `5.2.3`
- [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) `4.0.0`
- [cloudinary-http5](https://cloudinary.com/documentation/java_integration) `2.0.0`
- [cloudinary-taglib](https://cloudinary.com/documentation/java_integration) `2.0.0`

---

## Funcionalidades

- Cadastro e gerenciamento da conta do cliente
- Gerenciamento de endereços vinculados à conta
- Identificadores públicos seguros com ULID
- Autenticação de usuários com sessão HTTP e Spring Security
- Autorização por perfil de acesso
- Controle de sessão e persistência de login
- Criptografia de senhas com BCrypt
- Catálogo de produtos organizado por categorias
- Gerenciamento de produtos, categorias e cupons
- Carrinho de compras com gerenciamento de itens
- Fluxo de criação e finalização de pedidos
- Aplicação de cupons de desconto nos pedidos
- Seleção da forma de pagamento durante a compra
- Upload e gerenciamento de imagens dos produtos
- Upload de imagens personalizadas pelo cliente com URL assinada e protegida
  
---

## Próximas Versões
As funcionalidades abaixo ainda estão em desenvolvimento e serão adicionadas nas próximas versões:

- Validação de acesso aos recursos do sistema
- Proteção contra automações e abuso de requisições
- Limitação de requisições em endpoints sensíveis
- Centralização do tratamento de erros da aplicação
- Registro e monitoramento de eventos da aplicação
- Integração com serviços de pagamento
- Deploy da aplicação em ambiente de produção
- Implementação de testes automatizados
- Auditoria de ações críticas do sistema
- Melhorias gerais de segurança, desempenho e manutenção

---

## Known Bugs

- Algumas regras de validação ainda não foram aplicadas em todos os endpoints
- O fluxo de recuperação de senha ainda está em desenvolvimento
- Funcionalidades de autenticação e autorização ainda não foram finalizadas

>Este projeto está em desenvolvimento e possui finalidade educacional e de portfólio. Não é recomendado para uso em produção no estado atual.

---

## Licença

MIT License