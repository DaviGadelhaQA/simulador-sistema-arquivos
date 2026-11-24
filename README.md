# Simulador de Sistema de Arquivos

## 1. Apresentação

Este projeto consiste no desenvolvimento de um simulador de sistema de arquivos utilizando a linguagem Java. O objetivo é reproduzir, de forma simplificada, operações básicas de gerenciamento de arquivos e diretórios, permitindo compreender conceitos relacionados aos sistemas operacionais.

O simulador oferece um Shell interativo para execução de comandos e utiliza Journaling para registrar operações e auxiliar na recuperação do estado do sistema.

## 2. Objetivos

- Simular operações básicas de um sistema de arquivos.
- Implementar criação, cópia, exclusão e renomeação de arquivos.
- Implementar criação, exclusão e renomeação de diretórios.
- Permitir a listagem do conteúdo dos diretórios.
- Disponibilizar um Shell interativo para execução dos comandos.
- Utilizar persistência em disco para preservar os dados.
- Implementar Journaling para registrar operações e permitir a recuperação do estado registrado.

## 3. Sistema de Arquivos

Um sistema de arquivos é responsável por organizar, armazenar, localizar e gerenciar arquivos e diretórios em um dispositivo de armazenamento.

Sua importância está na organização dos dados, no controle de acesso às informações e na possibilidade de recuperar e utilizar arquivos de maneira estruturada.

Neste projeto, foi desenvolvida uma estrutura hierárquica de diretórios, na qual cada diretório pode armazenar arquivos e outros diretórios.

## 4. Journaling

Journaling é uma técnica utilizada por sistemas de arquivos para registrar alterações antes ou durante sua aplicação, contribuindo para a integridade dos dados e a recuperação após interrupções inesperadas.

### 4.1. WAL (Write-Ahead Logging)

No modelo Write-Ahead Logging, as alterações são registradas em um log antes de serem efetivamente aplicadas aos dados principais. Isso permite utilizar o registro para recuperar operações após uma falha.

### 4.2. Log-Structured

No modelo Log-Structured, as alterações são organizadas em registros sequenciais, formando um histórico de operações que pode ser utilizado para reconstruir o estado dos dados.

### 4.3. Journaling implementado

Neste projeto, foi implementada uma abordagem simplificada baseada em snapshots do estado do sistema.

Cada operação de modificação gera um registro `BEGIN`, contendo um identificador, a descrição da operação e o estado serializado da estrutura. Em seguida, o estado é salvo no arquivo principal e a operação é confirmada com um registro `COMMIT`, associado ao mesmo identificador.

O estado é codificado em Base64 para armazenamento no arquivo de Journal.

Na inicialização, o simulador consulta o Journal e recupera o último estado registrado, restaurando a estrutura de diretórios e arquivos.

**Observação:** a implementação é didática e utiliza snapshots completos, não sendo uma implementação integral dos modelos WAL ou Log-Structured.

## 5. Arquitetura do Projeto

O projeto foi desenvolvido em Java e organizado em classes com responsabilidades específicas.

| Classe | Responsabilidade |
|---|---|
| `Main` | Inicializa o simulador e executa o Shell. |
| `Shell` | Interpreta os comandos digitados pelo usuário. |
| `FileSystemSimulator` | Gerencia arquivos, diretórios, operações e persistência. |
| `File` | Representa um arquivo e seu conteúdo. |
| `Directory` | Representa diretórios e mantém suas estruturas hierárquicas. |
| `Journal` | Registra as transações e recupera estados armazenados. |

### 5.1. Estruturas de dados

A classe `File` armazena o nome e o conteúdo de cada arquivo.

A classe `Directory` mantém duas estruturas principais:

- `Map<String, File>`: armazena os arquivos do diretório.
- `Map<String, Directory>`: armazena os subdiretórios.

Os diretórios também possuem uma referência ao diretório pai, permitindo navegar pela estrutura hierárquica.

### 5.2. Persistência

A estrutura de diretórios é serializada utilizando `ObjectOutputStream` e armazenada em:

`storage/filesystem.dat`

O registro das operações é armazenado em:

`storage/journal.log`

Os arquivos são criados automaticamente durante a inicialização e utilização do simulador.

## 6. Funcionalidades implementadas

O simulador disponibiliza as seguintes operações:

| Operação | Comando | Descrição |
|---|---|---|
| Criar diretório | `mkdir` | Cria um diretório. |
| Criar arquivo | `create` | Cria um arquivo com conteúdo. |
| Copiar arquivo | `copy` | Copia um arquivo para outro diretório. |
| Apagar arquivo | `rm` | Remove um arquivo. |
| Renomear arquivo | `rename` | Altera o nome de um arquivo. |
| Apagar diretório | `rmdir` | Remove um diretório vazio. |
| Renomear diretório | `mvdir` | Altera o nome de um diretório. |
| Listar conteúdo | `ls` | Exibe arquivos e subdiretórios. |
| Ajuda | `help` | Exibe os comandos disponíveis. |
| Encerrar | `exit` | Finaliza o Shell. |

## 7. Instalação e execução

### 7.1. Pré-requisitos

- Java Development Kit (JDK) 21 ou superior.
- Terminal de comandos.
- Git (opcional, para clonar o repositório).

### 7.2. Clonar o projeto

```bash
git clone https://github.com/DaviGadelhaQA/simulador-sistema-arquivos.git
cd simulador-sistema-arquivos
```

### 7.3. Compilar

Na raiz do projeto, execute:

```bash
javac -d out src/simulador/*.java
```

### 7.4. Executar

```bash
java -cp out simulador.Main
```

Após a inicialização, o Shell ficará disponível para receber comandos.

## 8. Exemplos de utilização

Criar um diretório:

```bash
mkdir /documentos
```

Criar um arquivo:

```bash
create /documentos/relatorio.txt "Conteudo do relatorio"
```

Listar o conteúdo:

```bash
ls /documentos
```

Copiar um arquivo:

```bash
copy /documentos/relatorio.txt /backup
```

Renomear um arquivo:

```bash
rename /documentos/relatorio.txt trabalho.txt
```

Apagar um arquivo:

```bash
rm /documentos/trabalho.txt
```

Encerrar o simulador:

```bash
exit
```

## 9. Testes e resultados

Durante o desenvolvimento, foram realizados testes manuais para verificar as operações e a persistência dos dados.

Os resultados obtidos foram:

- Criação, cópia, exclusão e renomeação de arquivos executadas com sucesso.
- Criação, exclusão e renomeação de diretórios executadas com sucesso.
- Listagem dos conteúdos dos diretórios funcionando corretamente.
- Execução dos comandos pelo Shell interativo.
- Registro de operações com identificadores de transação.
- Associação dos registros `BEGIN` e `COMMIT` pelo mesmo identificador.
- Recuperação do estado do sistema após o encerramento e uma nova inicialização.

A recuperação foi validada verificando a permanência dos diretórios e arquivos previamente criados.

## 10. Limitações

O projeto possui finalidade acadêmica e implementa uma versão simplificada de um sistema de arquivos.

O Journaling utiliza snapshots completos da estrutura, não implementando todos os mecanismos de recuperação e gerenciamento de falhas encontrados em sistemas de arquivos reais.

A recuperação de falhas durante uma operação não foi submetida a testes específicos.

## 11. Tecnologias utilizadas

- Java 21
- Programação Orientada a Objetos
- Serialização de objetos
- Estruturas de dados `Map`
- API de arquivos do Java (`java.nio.file`)
- Git e GitHub

## 12. Repositório

**GitHub:** [simulador-sistema-arquivos](https://github.com/DaviGadelhaQA/simulador-sistema-arquivos)

## 13. Conclusão

O desenvolvimento do simulador permitiu aplicar conceitos de sistemas operacionais relacionados à organização de arquivos, gerenciamento de diretórios, persistência de dados e Journaling.

A implementação do Shell interativo tornou possível executar operações por meio de comandos, enquanto o mecanismo de persistência e recuperação permitiu preservar a estrutura criada entre diferentes execuções do programa.