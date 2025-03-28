MODULEA  START   0x000000
         EXTDEF  DATAA
         EXTREF  DATAB
DATAA    WORD    10            ; Define a variável global DATAA com valor 10
MAIN     LDA     #5            ; Carrega imediato 5 em A
         ADD     DATAB         ; Soma com o valor de DATAB (importado)
         STA     DATAA         ; Armazena o resultado em DATAA
         RSUB                ; Encerra o programa
         END     MODULEA
