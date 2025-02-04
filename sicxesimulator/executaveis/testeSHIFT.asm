START   0500
        LDA     NUM1
        SHIFTL  A,1
        STA     RESULT1

        LDA     NUM1
        SHIFTR  A,1
        STA     RESULT2

NUM1    WORD    4
RESULT1 WORD    0
RESULT2 WORD    0
END     START