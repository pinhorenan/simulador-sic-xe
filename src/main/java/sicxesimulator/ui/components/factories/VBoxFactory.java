package sicxesimulator.ui.components.factories;

import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class VBoxFactory {
    private VBoxFactory() {}

    /** Cria um VBox com espaçamento e filhos. */
    public static VBox createVBox(double spacing, Node... children) {
        return new VBox(spacing, children);
    }

    /** Ativa grow ALWAYS num filho (VBox.setVgrow). */
    public static void alwaysGrow(Node node) {
        VBox.setVgrow(node, Priority.ALWAYS);
    }
}
