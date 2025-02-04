START   0500
        LDA     NUM1
        AND     NUM2
        STA     RESULT1

        LDA     NUM1
        OR      NUM2
        STA     RESULT2

        LDA     NUM1
        CLEAR   A
        STA     RESULT3

        LDA     NUM1
        RMO     A,X
        STA     RESULT4

NUM1    WORD    4
NUM2    WORD    2
RESULT1 WORD    0
RESULT2 WORD    0
RESULT3 WORD    0
RESULT4 WORD    0
END     START