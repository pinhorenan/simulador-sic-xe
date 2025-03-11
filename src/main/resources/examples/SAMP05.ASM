. Exemplo 5: Macro para Adição de Dois Números
ADDMAC  MACRO
        LDA     NUM1
        ADD     NUM2
        STA     RESULT
        MEND

SAMP05  START   0
        ADDMAC
        RSUB
NUM1    WORD    10
NUM2    WORD    20
RESULT  RESW    1
END     SAMP05
