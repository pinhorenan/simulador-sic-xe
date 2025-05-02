package sicxesimulator.ui.factory;

import javafx.scene.control.TextArea;

public final class TextAreaFactory {
    private TextAreaFactory() {}

    /**
     * Cria um TextArea com fonte monoespaçada, wrapText e cor configurados.
     *
     * @param editable se o campo é editável
     * @param prompt texto de prompt (placeholder)
     * @param cssColor cor do texto (ex: "green" ou "#006400"); use null para cor padrão preta
     */
    public static TextArea codeArea(boolean editable, String prompt, String cssColor) {
        TextArea ta = new TextArea();
        ta.setEditable(editable);
        ta.setWrapText(true);
        ta.setPromptText(prompt);
        String color = cssColor == null ? "black" : cssColor;
        ta.setStyle(String.format(
                "-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: %s;",
                color
        ));
        return ta;
    }
}
