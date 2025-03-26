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
  - Geração de arquivos objeto (`.obj`) estilo SIC/XE com registros `H/T/M/E/D/R`
  - Geração de arquivo binário serializado (`.meta`) para retenção de metadados
- Ligador (Linker) multi-módulo:
  - Realiza linkagem absoluta ou relocável entre múltiplos módulos
  - Geração de arquivo objeto e binário na mesma estrutura do montador
- Carregador com relocação final opcional:
  - Carrega programas objeto para memória virtual
  - Aplica realocação pendente (quando necessária)

## Conjunto de Instruções

### Instruções Suportadas
Em geral, não foram implementadas instruções que lidam com input/output e instruções de ponto flutuante.

- **Formato 1**: Não há suporte à instruções de formato 1.
- **Formato 2**: ADDR, CLEAR, COMPR, DIVR, MULR, RMO, SHIFTL, SHIFRT, SUBR, TIXR.
- **Formato 3/4**: ADD, AND, COMP, DIV, J, JEQ, JGT, JLT, JSUB, LDA, LDB, LDCH, LDL, LDS, LDT, LDX, MUL, OR, RSUB, STA, STB, STCH, STL, STS, STT, STX, SUB, TIX.

- **Instruções Não implementadas:** ADDF, COMPF, DIVF, FIX, FLOAT, HIO, LPS, MULF, NORM, RD, SIO, SSK, STF, STI, STSW, SUBF, SVC, TD, TIO, WD.

## Licença

Este projeto é licenciado sob a [MIT License](LICENSE).

