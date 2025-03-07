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
}

