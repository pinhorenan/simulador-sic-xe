SAMP01  START   0x000000
FIRST   LDA     FIVE
        ADD     FOUR
        STA     RESULT
        RSUB
FIVE    WORD    5
FOUR    WORD    4
RESULT  RESW    1
        END     FIRST
