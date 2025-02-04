package sicxesimulator;

public class Operations {

    /**
     * Ação:
     * A <- (A) + (m..m+2)
     *
     * @param m
     */
    public void add(int m) {

    }

    /**
     * Ação:
     * r2 <- (r2) + (r1)
     *
     * @param r1
     * @param r2
     */
    public void addr(Register r1, Register r2) {

    }

    /**
     * Ação:
     * A <- (A) & (m..m+2)
     *
     * @param m
     */
    public void and(int m) {

    }

    /**
     * Ação:
     * r1 <- 0
     *
     * @param r1
     */
    public void clear(Register r1) {

    }

    /**
     * Ação:
     * A : (m..m+2)
     *
     * @param m
     */
    public void comp(int m) {

    }

    /**
     * Ação:
     * (r1) : (r2)
     *
     * @param r1
     * @param r2
     */
    public void compr(Register r1, Register r2) {

    }

    /**
     * Ação:
     * A : (A)/(m..m+2)
     *
     * @param m
     */
    public void div(int m) {

    }

    /**
     * Ação:
     * A : (m..m+2)
     *
     * @param m
     */
    public void divr() {

    }

    /**
     * Ação:
     * PC ← m
     *
     * @param
     */
    public void j() {

    }

    /**
     * Ação:
     * PC ← m if CC set to =
     *
     * @param
     */
    public void jeq() {

    }

    /**
     * Ação:
     * PC ← m if CC set to >
     *
     * @param
     */
    public void jgt() {

    }

    /**
     * Ação:
     * PC ← m if CC set to <
     *
     * @param
     */
    public void jlt() {

    }

    /**
     * Ação:
     * L ← (PC); PC ← m<
     *
     * @param
     */
    public void jsub() {

    }

    /**
     * Ação:
     * A ← (m..m+2)
     *
     * @param
     */
    public void lda() {

    }

    /**
     * Ação:
     * B ← (m..m+2)
     *
     * @param
     */
    public void ldb() {

    }

    /**
     * Ação:
     * A [byte mais a direita] ← (m)
     *
     * @param
     */
    public void ldch() {

    }

    /**
     * Ação:
     * L ← (m..m+2)
     *
     * @param
     */
    public void ldl() {

    }

    /**
     * Ação:
     * S ← (m..m+2)
     *
     * @param
     */
    public void lds() {

    }

    /**
     * Ação:
     * T ← (m..m+2)
     *
     * @param
     */
    public void ldt() {

    }

    /**
     * Ação:
     * X ← (m..m+2)
     *
     * @param
     */
    public void ldx() {

    }

    /**
     * Ação:
     * A ← (A) * (m..m+2)
     *
     * @param
     */
    public void mul() {

    }

    /**
     * Ação:
     * r2 ← (r2) * (r1)
     *
     * @param
     */
    public void mulr() {

    }

    /**
     * Ação:
     * A ← (A) | (m..m+2)
     *
     * @param
     */
    public void or() {

    }

    /**
     * Ação:
     * r2 ← (r1)
     *
     * @param
     */
    public void rmo() {

    }

    /**
     * Ação:
     * PC ← (L)
     *
     * @param
     */
    public void rsub() {

    }

    /**
     * Ação:
     * r1 ← (r1)
     *
     * @param
     */
    public void shiftl() {

    }

    /**
     * Ação:
     *  r1 ← (r1)
     *
     * @param
     */
    public void shiftr() {

    }

    /**
     * Ação:
     * m..m+2 ← (A)
     *
     * @param
     */
    public void sta() {

    }

    /**
     * Ação:
     * m..m+2 ← (B)
     *
     * @param
     */
    public void stb() {

    }

    /**
     * Ação:
     *  m ← (A)
     *
     * @param
     */
    public void stch() {

    }

    /**
     * Ação:
     * m..m+2 ← (L)
     *
     * @param
     */
    public void stl() {

    }

    /**
     * Ação:
     * m..m+2 ← (S)
     *
     * @param
     */
    public void sts() {

    }

    /**
     * Ação:
     * m..m+2 ← (T)
     *
     * @param
     */
    public void stt() {

    }

    /**
     * Ação:
     *  m..m+2 ← (X)
     *
     * @param
     */
    public void stx() {

    }

    /**
     * Ação:
     * A ← (A) - (m..m+2)
     *
     * @param
     */
    public void sub() {

    }

    /**
     * Ação:
     * r2 ← (r2) - (r1)
     *
     * @param
     */
    public void subr() {

    }

    /**
     * Ação:
     * X ← (X) + 1; (X) : (m..m+2)
     *
     * @param
     */
    public void tix() {

    }

    /**
     * Ação:
     * X ← (X) + 1; (X) : (r1)
     *
     * @param
     */
    public void tixr() {

    }
}
