MODULE1  START   0x000102
         EXTDEF  ALPHA               ; Exporta o símbolo ALPHA
         EXTREF  BETA, ADDALPHA, CLEARALPHA

ALPHA    WORD    0                   ; Variável global (pública)

MAIN     LDA     #5                  ; Imediato agora está correto
         STA     ALPHA
         JSUB    ADDALPHA
         JSUB    CLEARALPHA
         RSUB
         END     MODULE1
