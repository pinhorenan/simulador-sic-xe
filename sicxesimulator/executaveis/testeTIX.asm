START   0500
        LDX     NUM1
        TIX     NUM2
        STX     RESULT1

        LDX     NUM1
        TIXR    A
        STX     RESULT2

NUM1    WORD    4
NUM2    WORD    2
RESULT1 WORD    0
RESULT2 WORD    0
END     START