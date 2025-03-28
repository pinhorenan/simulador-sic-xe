package sicxesimulator.simulation.controller;

import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import sicxesimulator.simulation.model.Model;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.utils.Constants;
import sicxesimulator.simulation.util.DialogUtil;
import sicxesimulator.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MenuBarController {
    private final Controller controller;

    public void handleShowObjectCode() {
        var table = controller.getMainLayout().getObjectFilePanel().getObjectFileTable();
        var sel = table.getSelectionModel().getSelectedItems();
        if (sel.size() != 1) {
            DialogUtil.showError("Selecione um único arquivo .obj");
            return;
        }
        //noinspection SequencedCollectionMethodCanBeUsed
        ObjectFile obj = sel.get(0).getObjectFile();

        String textual = obj.getObjectCodeAsString();  // Implementar no ObjectFile
        controller.getMainLayout().getInputPanel().getExpandedCodeArea().setText("=== Código Objeto ===\n" + textual + "\n");
    }

    public MenuBarController(Controller controller) {
        this.controller = controller;
    }

    public void handleImportASM() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly", "*.asm"));
        fileChooser.setInitialDirectory(new File(Constants.SAMPLES_DIR));


        File file = fileChooser.showOpenDialog(controller.getStage());

        if (file != null) {
            try {
                String content = FileUtils.readFile(file.getAbsolutePath());
                controller.loadInputField(content);
            } catch (IOException e) {
                DialogUtil.showError("Erro ao importar arquivo ASM: " + e.getMessage());
            }
        }
    }

    public void handleClearMemoryAction() {
        controller.handleClearMemory();
    }

    public void handleChangeMemorySizeAction() {
        int newSizeInBytes;
        try {
            newSizeInBytes = DialogUtil.askForInteger("Atualizar tamanho da memória", "Digite o novo tamanho da memória (em bytes):", "Tamanho da memória (em bytes):");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        controller.handleChangeMemorySize(newSizeInBytes);
    }

    public void handleSetLinkerModeAction() {
        // Pega o modo atual do Model
        Model.LinkerMode currentMode = controller.getModel().getLinkerMode();

        // Abre um ChoiceDialog para trocar
        ChoiceDialog<Model.LinkerMode> dialog = new ChoiceDialog<>(currentMode, Model.LinkerMode.values());
        dialog.setTitle("Modo de Ligação");
        dialog.setHeaderText("Selecione o modo de ligação:");
        dialog.setContentText("Modo:");


        Optional<Model.LinkerMode> result = dialog.showAndWait();
        if (result.isPresent()) {
            Model.LinkerMode newMode = result.get();
            controller.setLinkerMode(newMode);
        }
    }

    public void handleHelpAction() {
        controller.showHelpWindow();
    }
}
