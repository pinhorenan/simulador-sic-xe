package sicxesimulator.simulation.util;

import javafx.scene.control.Button;

/**
 * Fábrica utilitária para criação rápida de botões JavaFX com ação associada.
 *
 * <p>Permite padronizar a criação de botões na interface com estilo e comportamento consistente.</p>
 *
 * <p>Esta classe é utilitária e não deve ser instanciada.</p>
 */
public class ButtonFactory {
    public static Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefSize(80, 25);
        button.setOnAction(e -> action.run());
        return button;
    }
}
