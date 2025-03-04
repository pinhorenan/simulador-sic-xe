# Simulador SIC/XE

Este programa simula um sistema SIC/XE utilizando uma interface gráfica desenvolvida em Java. O simulador implementa gerenciamento de memória e registradores, além de uma interface visual para acompanhamento da execução do programa. O projeto utiliza Gradle para build e gerenciamento de dependências.

## Requisitos

- **JDK 21** ou superior
- **Gradle 8+** (não é necessário instalá-lo separadamente, pois o projeto usa o Gradle Wrapper)

## Build e Execução

### 1. Clonando o Repositório

Clone o repositório para sua máquina:

```bash
git clone https://github.com/seu-usuario/SIC-XE-Simulator.git
cd SIC-XE-Simulator
```

### 2. Build e Execução com Gradle Wrapper

Para compilar e executar o simulador, utilize o Gradle Wrapper. Isso garantirá que todas as dependências (incluindo JavaFX) sejam resolvidas corretamente.

- **No Linux/Mac:**

  ```bash
  ./gradlew run
  ```

- **No Windows:**

  ```bash
  gradlew.bat run
  ```

O comando acima compila o projeto e inicia a aplicação, executando a função `main` definida no seu projeto.

### 3. Gerando o JAR Executável

Se desejar gerar um JAR executável, utilize:

```bash
./gradlew jar
```

O JAR gerado estará na pasta `build/libs`. Para executá-lo, use:

```bash
java --module-path "caminho/para/javafx/lib" --add-modules javafx.controls,javafx.fxml,javafx.media -jar build/libs/SIC-XE-Simulator.jar
```

> **Observação:** Se você configurou o `applicationDefaultJvmArgs` no `build.gradle`, o JAR pode já estar preparado para rodar sem especificar o `--module-path`.

## Estrutura do Projeto

```
SIC-XE-Simulator/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── sicxesimulator/...
│   │   └── resources/
│   │       └── darkmode.css
│   └── test/
│       └── java/...
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── .gitignore
├── README.md
└── (outros arquivos de configuração)
```

## Licença

Este projeto é licenciado sob a [MIT License](LICENSE).

