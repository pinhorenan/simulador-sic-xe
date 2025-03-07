# Simulador SIC/XE

Este programa simula um sistema SIC/XE utilizando uma interface gráfica desenvolvida em Java. O simulador implementa gerenciamento de memória e registradores, além de uma interface visual para acompanhamento da execução do programa. O projeto utiliza Gradle para build e gerenciamento de dependências.

## Requisitos

- **JDK 21** ou superior

## Build e Execução

### 1. Clonando o Repositório

Clone o repositório para sua máquina:

```bash
git clone https://github.com/pinhorenan/SIC-XE-Simulator.git
cd SIC-XE-Simulator
```

### 2. Build e Execução com Gradle Wrapper

Para compilar e executar o simulador, utilize o Gradle Wrapper. Isso garantirá que todas as dependências (incluindo JavaFX) sejam resolvidas corretamente.

- **No Linux/Mac:**

  ```bash
  chmod +x gradlew
  ./gradlew run
  ```

- **No Windows:**

  ```bash
  gradlew.bat run
  ```

### 3. Gerando o JAR Executável

Se desejar gerar um JAR executável, utilize:

```bash
./gradlew jar
```

O JAR gerado estará na pasta `build/libs`. Para executá-lo, use:

```bash
java WIP
```

## Licença

Este projeto é licenciado sob a [MIT License](LICENSE).

