START   0500
        LDA     NUM1
        DIV     NUM2
        STA     RESULT1

        LDX     NUM1
        LDA     NUM2
        DIVR    X,A
        STX     RESULT2

        LDA     NUM1
        COMP    NUM2
        JEQ     EQUAL
        JGT     GREATER
        JLT     LESS

EQUAL   LDA     #1
        STA     RESULT3
        J       END

GREATER LDA     #2
        STA     RESULT3
        J       END

LESS    LDA     #3
        STA     RESULT3

NUM1    WORD    8
NUM2    WORD    2
RESULT1 WORD    0
RESULT2 WORD    0
RESULT3 WORD    0
END     START