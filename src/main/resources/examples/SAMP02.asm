SAMP02  START   0
BEGIN   LDA     ALPHA
        SUB     BETA
        STA     GAMMA
        RSUB
ALPHA   WORD    10
BETA    WORD    3
GAMMA   RESW    1
        END     BEGIN