START   0500
        JSUB    SUBROUTINE
        LDA     RESULT
        STA     FINAL

SUBROUTINE
        LDA     #1
        STA     RESULT
        RSUB

RESULT  WORD    0
FINAL   WORD    0
END     START