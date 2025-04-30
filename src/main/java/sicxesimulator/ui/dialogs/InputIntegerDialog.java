package sicxesimulator.ui.dialogs;

import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.util.Optional;

public class InputIntegerDialog {
    public static int ask(String title, String header, String content) throws IOException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) throw new IOException("Usuário cancelou a entrada de inteiro.");

        String input = result.get().trim();
        try {
            if (input.startsWith("0x") || input.startsWith("0X")) {
                return Integer.parseInt(input.substring(2), 16);
            } else {
                return Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            throw new IOException("Valor inválido: " + input, e);
        }
    }
}
