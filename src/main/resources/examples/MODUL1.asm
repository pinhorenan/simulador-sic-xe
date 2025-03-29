MODUL1  START   0x000000
         EXTDEF  DATAA
         EXTREF  DATAB
MAIN     LDA     #5        
         ADD     DATAB         
         STA     DATAA         
         RSUB                
DATAA    WORD    9             
         END     MODUL1