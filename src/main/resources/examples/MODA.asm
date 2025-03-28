MODA  START   0x000000
         EXTDEF  DATAA
         EXTREF  DATAB
MAIN     LDA     #5            ; Carrega imediato 5 em A
         ADD     DATAB         ; Soma com o valor de DATAB (importado)
         STA     DATAA         ; Armazena o resultado em DATAA
         RSUB                ; Encerra o programa
DATAA    WORD    9             ; Define a variável global DATAA com valor 9
         END     MODA