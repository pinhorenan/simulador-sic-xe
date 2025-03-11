package sicxesimulator.application.components.buttons;

import javafx.scene.control.Button;

public class ButtonFactory {
    public static Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefSize(80, 25);
        button.setOnAction(e -> action.run());
        return button;
    }
}
