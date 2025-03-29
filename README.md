# Simulador SIC/XE

Este projeto é um simulador acadêmico da arquitetura SIC/XE (Simplified Instructional Computer with Extra Equipment), desenvolvido com Java e JavaFX. O programa permite escrever código assembly SIC/XE, montar programas em linguagem simbólica, realizar ligação (linkagem) de múltiplos módulos e carregá-los na memória virtual, simulando sua execução.

O simulador conta com uma interface gráfica intuitiva que permite ao usuário:
- Editar, montar e visualizar o código-fonte assembly.
- Visualizar e editar arquivos objeto gerados (.obj).
- Realizar a ligação entre múltiplos módulos objeto, com suporte à relocação absoluta ou relocável.
- Carregar programas gerados na máquina virtual SIC/XE simulada.
- Acompanhar o estado dos registradores, memória e execução passo-a-passo.

## Pré-requisitos

- Java 17 ou superior
- Gradle (wrapper incluso no projeto)

## Como Usar

### 1. Clonando o Repositório

Clone o repositório e acesse a pasta do projeto:

```bash
git clone https://github.com/pinhorenan/Simulador_SIC-XE.git
cd Simulador_SIC-XE
```

### 2. Compilando e Executando com Gradle Wrapper

Utilize o Gradle Wrapper incluso no projeto para compilar e executar o simulador:

- **Linux/Mac:**

```bash
./gradlew run
```

- **Windows:**

```cmd
gradlew.bat run
```

## Gerando e Executando o JAR

Se preferir gerar um JAR executável:

```bash
./gradlew jar
```

O JAR gerado estará localizado em `build/libs`. Para executá-lo, utilize:

```bash
java -jar build/libs/sicxesimulator.jar
```

## Funcionalidades Suportadas

- Processador de Macros:
  - Realiza um pre-processamento do código antes da montagem, expandindo qualquer macro definida
  - Suporte à macros aninhadas
- Montador SIC/XE completo:
  - Suporte às diretivas padrão: `START`, `END`, `WORD`, `BYTE`, `RESB`, `RESW`, `EXTDEF`, `EXTREF`
  - Geração de arquivos objeto (`.obj`) estilo SIC/XE com registros `H/D/R/T/M/E`
  - Geração de arquivo binário serializado (`.meta`) para retenção de metadados
- Ligador (Linker) multi-módulo:
  - Realiza linkagem absoluta ou relocável entre múltiplos módulos
  - Geração de arquivo objeto e binário na mesma estrutura do montador
- Carregador com relocação final opcional:
  - Carrega programas objeto para memória virtual
  - Aplica realocação pendente (quando necessária)

## Conjunto de Instruções do SIC/XE

Este simulador implementa parcialmente o conjunto de instruções SIC/XE conforme a especificação oficial, com algumas limitações de escopo educacional:

### Instruções Implementadas

- **Formato 2 (registradores):**  
  `ADDR`, `CLEAR`, `COMPR`, `DIVR`, `MULR`, `RMO`, `SHIFTL`, `SHIFTR`, `SUBR`, `TIXR`

- **Formato 3/4 (memória e imediato):**  
  `ADD`, `AND`, `COMP`, `DIV`, `J`, `JEQ`, `JGT`, `JLT`, `JSUB`, `LDA`, `LDB`, `LDCH`, `LDL`, `LDS`, `LDT`, `LDX`, `MUL`, `OR`, `RSUB`, `STA`, `STB`, `STCH`, `STL`, `STS`, `STT`, `STX`, `SUB`, `TIX`

### Instruções NÃO Implementadas (Presentes como STUB)

Por restrições do projeto, não há suporte para:

- **Formato 1 e Instruções especiais:**  
  `FIX`, `FLOAT`, `NORM`, `HIO`, `SIO`, `TIO`

- **Instruções de ponto flutuante:**  
  `ADDF`, `COMPF`, `DIVF`, `LDF`, `MULF`, `STF`, `SUBF`

- **Controle do sistema e operações I/O:**  
  `LPS`, `RD`, `SSK`, `STI`, `STSW`, `SVC`, `TD`, `WD`

Essas instruções possuem placeholders (stubs) que geram logs informativos indicando sua ausência de implementação.

## 📚 Documentação das Principais Classes

| Classe                                                                                       | Descrição                                      |
|----------------------------------------------------------------------------------------------|------------------------------------------------|
| [`ControlUnit`](src/main/java/sicxesimulator/hardware/cpu/ControlUnit.java)                  | Busca, decodifica e despacha instruções.       |
| [`InstructionDecoder`](src/main/java/sicxesimulator/hardware/cpu/InstructionDecoder.java)    | Decodifica instruções SIC/XE.                  |
| [`ExecutionUnit`](src/main/java/sicxesimulator/hardware/cpu/ExecutionUnit.java)              | Executa instruções do conjunto SIC/XE.         |
| [`MacroProcessor`](src/main/java/sicxesimulator/software/macroprocessor/MacroProcessor.java) | Expande macros definidas no código fonte.      |
| [`Assembler`](src/main/java/sicxesimulator/software/assembler/Assembler.java)                | Coordena as fases da montagem do programa.     |
| [`Linker`](src/main/java/sicxesimulator/software/linker/Linker.java)                         | Realiza a ligação de módulos objeto.           |
| [`Loader`](src/main/java/sicxesimulator/software/loader/Loader.java)                         | Realiza a carga de arquivos `.obj` na memória. |

## Licença

Este projeto é licenciado sob a [MIT License](LICENSE).

