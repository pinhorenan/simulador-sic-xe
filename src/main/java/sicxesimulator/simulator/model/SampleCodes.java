package sicxesimulator.simulator.model;

public class SampleCodes {
    public static final String SAMPLE_CODE_1 = """
            SAMP01 START 0
            FIRST  LDA   FIVE
                   ADD   FOUR
                   STA   RESULT
                   RSUB
            FIVE   WORD  5
            FOUR   WORD  4
            RESULT RESW  1
            END    FIRST
            """;

    public static final String SAMPLE_CODE_2 = """
            SAMP02 START 0
            BEGIN  LDA   ALPHA
                   SUB   BETA
                   STA   GAMMA
                   RSUB
            ALPHA  WORD  10
            BETA   WORD  3
            GAMMA  RESW  1
            END    BEGIN
            """;

    public static final String SAMPLE_CODE_3 = """
            SAMP3 START 30
            START  LDA   NUM1
                   MUL   NUM2
                   STA   RESULT
                   RSUB
            NUM1   WORD  7
            NUM2   WORD  8
            RESULT RESW  1
            END    START
            """;

    public static final String SAMPLE_CODE_4 = """
            COPYMAC MACRO
                   LDA SRC
                   STA DST
                   MEND
            START   START 0
                    COPYMAC
                    RSUB
            SRC     WORD  5
            DST     RESW  1
            END     START
            """;

    // Exemplo 5: Macro para Adição de Dois Números
    public static final String SAMPLE_CODE_5 = """
            ADDMAC MACRO
                   LDA   NUM1
                   ADD   NUM2
                   STA   RESULT
                   MEND
            PROG   START 0
                   ADDMAC
                   RSUB
            NUM1   WORD  10
            NUM2   WORD  20
            RESULT RESW  1
            END    PROG
            """;

    // Exemplo 6: Macro Aninhada
    public static final String SAMPLE_CODE_6 = """
            MAC1   MACRO
                   LDA   X
                   MEND
            MAC2   MACRO
                   MAC1
                   ADD   Y
                   MEND
            PROG   START 0
                   MAC2
                   RSUB
            X      WORD  3
            Y      WORD  4
            END    PROG
            """;
}
