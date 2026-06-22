# 🌊 APS - Sistema de Monitoramento Ambiental & Chat em Tempo Real (Rio Tietê)

Este projeto consiste em uma aplicação desktop corporativa baseada na arquitetura **Cliente-Servidor**, projetada para a comunicação interna de fiscais ambientais e o registro de ocorrências de poluição no Rio Tietê.

## 🚀 Tecnologias e Ferramentas Utilizadas
* **Linguagem:** Java 17
* **Interface Gráfica:** Java Swing (Interface customizada com o tema *Nimbus LookAndFeel*)
* **Comunicação em Rede:** Sockets TCP/IP (Protocolo customizado via pacotes de texto estruturados)
* **Concorrência:** Multi-threading (Gerenciamento assíncrono de múltiplos clientes conectados simultaneamente)
* **Banco de Dados:** SQLite (Persistência local leve e eficiente)
* **Gerenciamento de Dependências:** Maven
* **Processamento de Linguagem Natural (NLP):** Biblioteca `sensitive-word` integrada ao servidor para filtragem e moderação automática de termos sensíveis.

## 🧠 Destaques Técnicos da Engenharia do Software
* **Padrão de Projeto Singleton:** Aplicado na conexão com o banco de dados para garantir que apenas uma instância de gravação gerencie as transações de dados de forma síncrona.
* **Segurança e Criptografia:** Implementação de algoritmo **SHA-256** para mascaramento e armazenamento seguro de credenciais na base de dados.
* **Blindagem de Infraestrutura:** Uso de **PreparedStatements** com consultas parametrizadas para mitigar riscos de vulnerabilidades críticas como *SQL Injection*.
* **Interface Gráfica Responsiva:** Atualizações dinâmicas de tabelas e logs executadas diretamente na **Event Dispatch Thread (EDT)** do Java, evitando o congelamento ou travamento da aplicação para o usuário final.

## 👥 Desenvolvedores (Integrantes da APS)
* Cleiton (Interface Gráfica, Demonstração Prática e Persistência)
* Enzo (Arquitetura de Rede, Infraestrutura e Multi-threading)
* Vitor (Protocolo de Comunicação, Banco de Dados e Segurança)