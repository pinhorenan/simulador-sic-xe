package sicxesimulator;

public class Word {

    /**
     * Atributo em inteiro (32 bits) para armazenar a palavra (24 bits).
     * Uma máscara é aplicada para garantir apenas 24 bits.
     */
    private int value;

    /**
     * Construtor padrão.
     * Inicializa a palavra com valor 0.
     */
    public Word() {
        this.value = 0;
    }

    /**
     * Construtor que inicializa a palavra com um valor inteiro.
     * Uma máscara é aplicada para descartar os 8 bits mais significativos do inteiro.
     * Os 8 bits são eliminados para garantir apenas os 24 bits especificados.
     *
     * @param value O valor inicial em inteiro.
     */
    @SuppressWarnings("unused")
    public Word(int value) {
        setValue(value);
    }

    /**
     * Construtor que inicia a palavra com uma String hexadecimal.
     *
     * @param hexValue O valor inicial em hexadecimal.
     */
    public Word(String hexValue) {
        try {
            int intValue = Integer.parseInt(hexValue, 16);
            setValue(intValue);
        } catch (NumberFormatException e) {
            System.out.println("Erro: Valor hexadecimal inválido.");
            this.value = 0;
        }
    }

    /**
     * Retorna o valor atual da palavra.
     *
     * @return O 24bits como um valor inteiro.
     */
    public int getValue() {
        return value;
    }

    /**
     * Seta o valor da palavra.
     * O tipo inteiro tem 32 bits, então aplicamos uma máscara para eliminar os 8 bits mais significativos.
     *
     * @param value O novo valor para a palavra.
     */
    public void setValue(int value) {
        // Aplica-se uma máscara para garantir apenas 24 bits
        this.value = value & 0xFFFFFF;
    }

    /**
     * Retorna o hexadecimal armazenado na palavra como uma string.
     * A saída são 6 dígitos hexadecimais (por exemplo: "00FFAA").
     *
     * @return String hexadecimal.
     */
    @Override
    public String toString() {
        return String.format("%06X", value);
    }
}
