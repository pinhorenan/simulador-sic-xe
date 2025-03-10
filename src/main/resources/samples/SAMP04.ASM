. Exemplo 4: Programa que usa macros
COPYMAC MACRO
        LDA     SRC
        STA     DST
        MEND

SAMP04  START   0
        COPYMAC
        RSUB
SRC     WORD    5
DST     RESW    1
END     SAMP04
