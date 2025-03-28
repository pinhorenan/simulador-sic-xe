MODB  START   0x000102
         EXTDEF  DATAB
         EXTREF  DATAA
         LDA     DATAA         ; Carrega o valor de DATAA (importado)
         STA     DATAB         ; Armazena o valor lido em DATAB (exemplo de uso)
         RSUB                  ; Encerra o programa
DATAB    WORD    18            ; Define a variável global DATAB com valor 18
         END     MODB
