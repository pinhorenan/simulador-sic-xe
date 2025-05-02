package sicxesimulator.ui.factory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public final class HBoxFactory {
    private HBoxFactory() {}

    /**
     * Cria um HBox com espaçamento, padding e alinhamento configurados e adiciona os filhos.
     *
     * @param spacing  espaço entre filhos
     * @param padding  padding externo (use null para none)
     * @param alignment alinhamento (use null para padrão)
     * @param children nós a serem adicionados
     */
    public static HBox createHBox(double spacing, Insets padding, Pos alignment, Node... children) {
        HBox box = new HBox(spacing, children);
        if (padding != null)  box.setPadding(padding);
        if (alignment != null) box.setAlignment(alignment);
        return box;
    }

    /**
     * Shorthand para um HBox centralizado.
     */
    public static HBox createCenteredHBox(double spacing, Insets padding, Node... children) {
        return createHBox(spacing, padding, Pos.CENTER, children);
    }
}
