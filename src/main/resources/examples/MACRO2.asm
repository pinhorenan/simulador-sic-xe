ADDMAC  MACRO
        LDA     NUM1
        ADD     NUM2
        STA     RESULT
        MEND

MACRO2  START   0x000000
        ADDMAC
        RSUB
NUM1    WORD    10
NUM2    WORD    20
RESULT  RESW    1
        END     MACRO2
