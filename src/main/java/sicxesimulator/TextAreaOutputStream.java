package sicxesimulator;

import java.io.OutputStream;
import javafx.scene.control.TextArea;

public class TextAreaOutputStream extends OutputStream {
    private final TextArea textArea;

    public TextAreaOutputStream(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        textArea.appendText(String.valueOf((char) b));
    }
}