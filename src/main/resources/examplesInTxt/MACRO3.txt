MAC1    MACRO
        LDA     X
        MEND

MAC2    MACRO
        MAC1
        ADD     Y
        MEND

MACRO3  START   0
        MAC2
        RSUB
X       WORD    3
Y       WORD    4
        END     MACRO3
