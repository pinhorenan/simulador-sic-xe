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
git clone https://github.com/pinhorenan/SIC-XE-Simulator.git
cd SIC-XE-Simulator
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

- Montador SIC/XE completo:
  - Suporte às diretivas padrão: `START`, `END`, `WORD`, `BYTE`, `RESB`, `RESW`, `EXTDEF`, `EXTREF`
  - Geração de arquivos objeto estilo SIC/XE com registros `H/T/M/E/D/R`
- Ligador (Linker) multi-módulo:
  - Realiza linkagem absoluta ou relocável entre múltiplos módulos
  - Geração de arquivo objeto final (`.obj`) e arquivo binário serializado (`.meta`)
- Carregador com relocação final opcional:
  - Carrega programas objeto para memória virtual
  - Aplica realocação pendente (quando necessária)

## Conjunto de Instruções

### Instruções Suportadas

- **Formato 1**: No suportado
- **Formato 2**: ADDR, CLEAR, COMPR, DIVR, MULR, SUBR, TIXR, RMO
- **Formato 3/4**: ADD, AND, COMP, DIV, J, JEQ, JGT, JLT, JSUB, LDA, LDB, LDCH, LDL, LDS, LDT, LDX, MUL, OR, SUB, DIV, DIVF, J, JEQ, JGT, JLT, RSUB, SHIFTL, SHIFTR, STA, STB, STCH, STL, STS, STSW, STT, STX, SUB, TIX.

**Instruções Não implementadas:** Todas que não aparecem na listagem acima.

## Licença

Este projeto é licenciado sob a [MIT License](LICENSE).

