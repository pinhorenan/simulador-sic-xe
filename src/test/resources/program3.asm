* Este é um programa de exemplo com comentários
START 3000
       LDA VALUE1      ; Carrega o valor de VALUE1 em A
       SUB VALUE2      ; Subtrai o valor de VALUE2 de A
       STA RESULT      ; Armazena o resultado em RESULT
       COMP ZERO       ; Compara o resultado com zero
       JEQ FINISH      ; Se for zero, pula para FINISH
       RSUB            ; Retorna da sub-rotina
ZERO   WORD 000000
VALUE1 WORD 00000A
VALUE2 WORD 000005
RESULT WORD 000000
FINISH RSUB
