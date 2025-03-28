MODULE2  START   0x000204
         EXTDEF  BETA, ADDALPHA
         EXTREF  ALPHA

BETA     WORD    10

ADDALPHA LDA     ALPHA
         ADD     BETA
         STA     ALPHA
         RSUB
         END     MODULE2
