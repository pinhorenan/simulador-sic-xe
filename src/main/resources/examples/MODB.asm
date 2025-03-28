MODULEB  START   0x001002
         EXTDEF  DATAB
         EXTREF  DATAA
DATAB    WORD    20            ; Define a variável global DATAB com valor 20
         LDA     DATAA         ; Carrega o valor de DATAA (importado)
         STA     DATAB         ; Armazena o valor lido em DATAB (exemplo de uso)
         RSUB                ; Encerra o programa
         END     MODULEB
