START   0500
        LDA     NUM1
        STA     RESULT1

        LDX     NUM1
        STX     RESULT2

        LDB     NUM1
        STB     RESULT3

        LDL     NUM1
        STL     RESULT4

        LDS     NUM1
        STS     RESULT5

        LDT     NUM1
        STT     RESULT6

NUM1    WORD    4
RESULT1 WORD    0
RESULT2 WORD    0
RESULT3 WORD    0
RESULT4 WORD    0
RESULT5 WORD    0
RESULT6 WORD    0
END     START