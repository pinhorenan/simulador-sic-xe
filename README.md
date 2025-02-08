# Simulador SIC/XE
Este programa simula um sistema SIC/XE em Java. Ele possui as funções de construção de memória, registradores, um analisador sintático, um console e um interpretador.

Para executar o simulador, digite 'analisar_arq (arquivo com extensão)'.  
Depois digite 'iniciar' para atribuir endereços a cada instrução.  
Depois digite 'prox' para avançar pelas instruções.

Você também pode:  
Digitar 'visualizar_reg (qualquer registrador)' a qualquer momento para ver o valor nesse registrador específico.  
Digitar 'visualizar_mem (endereço)' para ver os dados armazenados em um local de memória específico.  
Digitar 'alterar_mem (endereço) (valor do byte)' para alterar o valor do byte em um endereço de memória específico.  
Digitar 'alterar_reg (registrador) (valor hexadecimal de seis dígitos)' para alterar o valor em um registrador específico.  
Digitar 'parar' para interromper o programa.  
Digitar 'creditos' para ver os créditos do programa.  
Digitar 'carregar_arq' para importar uma memória existente de um arquivo de texto.  
Digitar 'salvar_arq' para exportar a memória existente para um arquivo de texto.  
Digitar 'exit' para sair da simulação.

Você também pode digitar 'help' para ver a lista de comandos.

## Créditos
Arthur Alves - Organização, discussão, ajustes.  
Fabricio Bartz - Organização, discussão, ajustes.  
Gabriel Moura - Construção, definição e ajustes dos registradores e memória.  
Leonardo Braga - Ajustes nas flags de operações.  
Luis Eduardo Rasch - Construção e ajuste do console, leitura e analise dos arquivos, e testes.  
Renan Pinho - Construção das instruções e simulador, ajustes em todo o código e transpiler.
