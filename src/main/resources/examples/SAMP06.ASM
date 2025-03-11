. Exemplo 6: Macro Aninhada
MAC1    MACRO
        LDA     X
        MEND

MAC2    MACRO
        MAC1
        ADD     Y
        MEND

SAMP06  START   0
        MAC2
        RSUB
X       WORD    3
Y       WORD    4
END     SAMP06
