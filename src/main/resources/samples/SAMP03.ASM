SAMP03   START   30
START   LDA     NUM1
        MUL     NUM2
        STA     RESULT
        RSUB
NUM1    WORD    7
NUM2    WORD    8
RESULT  RESW    1
END     START
