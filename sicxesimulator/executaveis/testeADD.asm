START   0500
        LDA     NUM1
        ADD     NUM2
        STA     RESULT1

        LDA     NUM1
        SUB     NUM2
        STA     RESULT2

        LDA     NUM1
        MUL     NUM2
        STA     RESULT3

NUM1    WORD    4
NUM2    WORD    2
RESULT1 WORD    0
RESULT2 WORD    0
RESULT3 WORD    0
END     START