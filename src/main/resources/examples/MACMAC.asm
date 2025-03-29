MAC1    MACRO
        LDA     X
        MEND

MAC2    MACRO
        MAC1
        ADD     Y
        MEND

MACMAC  START   0x000000
        MAC2
        RSUB
X       WORD    3
Y       WORD    4
        END     MACMAC
