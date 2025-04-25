# SIC/XE Simulator · Simulador SIC/XE
[![License: MIT](https://img.shields.io/badge/License-MIT-brightgreen.svg)](LICENSE) [![Java](https://img.shields.io/badge/Java-17%2B-red)](https://openjdk.org/) [![Gradle](https://img.shields.io/badge/Build-Gradle-blueviolet)](https://gradle.org/)

> **EN version below** • **Versão em português acima**

## Índice
- [Sobre o Projeto](#sobre-o-projeto)
- [Demonstração](#demonstra%C3%A7%C3%A3o)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Getting Started](#getting-started)
  - [Pré‑requisitos](#pré‑requisitos)
  - [Instalação](#instalação)
  - [Executando](#executando)
  - [Gerando JAR](#gerando-jar)
- [Conjunto de Instruções](#conjunto-de-instruções)
- [Roadmap](#roadmap)
- [Contribuindo](#contribuindo)
- [Histórico & Agradecimentos](#hist%C3%B3rico--agradecimentos)
- [Licença](#licença)
- [Contato](#contato)
- [English](#english)

## Sobre o Projeto
O **Simulador SIC/XE** é uma ferramenta educacional que implementa **montador**, **ligador**, **carregador** e **máquina virtual** da arquitetura [SIC/XE](https://en.wikipedia.org/wiki/SIC/XE) (Simplified Instructional Computer + Extra Equipment).
Ele foi concebido inicialmente como trabalho acadêmico colaborativo na Universidade Federal de Pelotas.
Desde 2025, o projeto segue mantido e expandido **individualmente por [Renan Pinho](https://github.com/pinhorenan)**.

**Objetivos:**
* Fornecer ao estudante um ambiente gráfico para experimentar assembly SIC/XE;
* Servir como base de estudos sobre montadores, ligadores e execução de código de baixo nível;
* Demonstrar boas práticas de engenharia de software em **Java 17 & JavaFX 20**.

---

## Demonstração
| Edição de código | Execução passo‑a‑passo |
| :---: | :---: |
| TODO |

---

## Funcionalidades
- **Processador de Macros** com suporte a macros aninhadas  
- **Montador completo** para SIC/XE com diretivas `START/END`, registros `H/D/R/T/M/E`, geração de `.obj` e `.meta`  
- **Ligador** multi‑módulo (relocável ou absoluto)  
- **Carregador** com relocação final opcional  
- **Máquina Virtual** com execução passo‑a‑passo, breakpoints e inspeção de registradores/memória  
- **Interface Gráfica em JavaFX** incluindo editor de código com destaque de sintaxe  
- **Logs estruturados** (`SLF4J`) e **testes unitários** (`JUnit 5`)

---

## Arquitetura
```
+---------------------------+
|       JavaFX GUI          |
+---------------------------+
          |
          v
+---------------------------+
|     Application Core      |
|  (Assembler/Linker/VM)    |
+---------------------------+
          |
          v
+---------------------------+
|        Hardware           |
|  (CPU, Memory, Devices)   |
+---------------------------+
```
> Cada camada expõe APIs bem‑definidas, facilitando testes e extensões. Consulte o diagrama completo em *TODO*).

---

## Getting Started

### Pré‑requisitos
* **Java 17** ou superior  
* **Git** 2.40+  
* Não é necessário ter Gradle instalado – o wrapper (`gradlew`) já acompanha o repositório.

### Instalação
```bash
git clone https://github.com/pinhorenan/simulador-sic-xe.git
cd simulador-sic-xe
```

### Executando
Linux / macOS:
```bash
./gradlew run
```
Windows PowerShell:
```powershell
.\gradlew.bat run
```

### Gerando JAR
```bash
./gradlew jar
java -jar build/libs/sicxe-simulator.jar
```

---

## Conjunto de Instruções
**Implementadas**
<details>
<summary>Formato 2</summary>

`ADDR, CLEAR, COMPR, DIVR, MULR, RMO, SHIFTL, SHIFTR, SUBR, TIXR`
</details>

<details>
<summary>Formato 3/4</summary>

`ADD, AND, COMP, DIV, J, JEQ, JGT, JLT, JSUB, LDA, LDB, LDCH, LDL, LDS, LDT, LDX, MUL, OR, RSUB, STA, STB, STCH, STL, STS, STT, STX, SUB, TIX`
</details>

**Pendentes**  
`FIX, FLOAT, NORM, HIO, SIO, TIO, ADDF, COMPF, DIVF, LDF, MULF, STF, SUBF, LPS, RD, SSK, STI, STSW, SVC, TD, WD`  
Chamadas a instruções não implementadas geram logs para facilitar contribuição.

---

## Roadmap
- [ ] Exportar log de execução em JSON
- [ ] Suporte completo a formato 1 e I/O
- [ ] Internacionalização (i18n) PT‑BR ↔ EN
- [ ] Integração contínua no GitHub Actions

---

## Contribuindo
1. *Fork* o projeto
2. Crie sua *feature branch*: `git checkout -b feature/minha-feature`
3. *Commit* suas alterações: `git commit -m 'feat: minha nova feature'`
4. *Push* para o *branch*: `git push origin feature/minha-feature`
5. Abra um *pull request*

---

## Histórico & Agradecimentos
Este projeto teve origem em **2024** como trabalho em equipe nas disciplinas de Organização e Arquitetura de Computadores da **UFPel**.  
Contribuíram na versão inicial:

| Nome | GitHub |
|---|---|
| Arthur Alves | @arthursa21 |
| Fabricio Bartz | @FabricioBartz |
| Gabriel Moura | @gbrimoura |
| Leonardo Braga | @braga0425 |
| Luis Eduardo | @LuisEduardoRasch |
| Renan Pinho | @pinhorenan |

Desde **2025‑04‑25** o desenvolvimento e manutenção passaram a ser conduzidos por **Renan Pinho**.  
A todos os co‑autores originais: **muito obrigado!**

---

## Licença
Distribuído sob a [licença MIT](LICENSE).

---

## Contato
Renan Pinho • [LinkedIn](https://www.linkedin.com/in/pinhorenan/) • rmdpinho@inf.ufpel.edu.br • pinhorenan@outlook.com

---

## English
<details>
<summary>Click to expand the English version</summary>

### SIC/XE Simulator
Educational assembler, linker, loader and virtual machine for the SIC/XE architecture, built with **Java 17 + JavaFX**.

*TODO*

</details>

