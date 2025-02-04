START   0500
        LDA     NUM1
        COMP    NUM2
        JEQ     EQUAL
        JGT     GREATER
        JLT     LESS

EQUAL   LDA     #1
        STA     RESULT
        J       END

GREATER LDA     #2
        STA     RESULT
        J       END

LESS    LDA     #3
        STA     RESULT

NUM1    WORD    4
NUM2    WORD    4
RESULT  WORD    0
END     START