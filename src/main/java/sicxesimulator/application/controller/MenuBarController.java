package sicxesimulator.application.controller;

import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import sicxesimulator.application.model.Model;
import sicxesimulator.data.ObjectFile;
import sicxesimulator.utils.Constants;
import sicxesimulator.application.util.DialogUtil;
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
        controller.getMainLayout().getInputPanel().getExpandedCodeArea().setText("=== OBJ TEXT ===\n" + textual + "\n");
    }

    public MenuBarController(Controller controller) {
        this.controller = controller;
    }

    /// ===================== MENU SUPERIOR ===================== ///

    // Ação do menuItem "Importar código-fonte"
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

    /// ===================== MENU MEMÓRIA ===================== ///

    // Ação do menuItem "Limpar memória"
    public void handleClearMemoryAction() {
        controller.clearMemory();
    }

    // Ação do menuItem "Alterar tamanho da memória"
    public void handleChangeMemorySizeAction() {
        int newSizeInBytes;
        try {
            newSizeInBytes = DialogUtil.askForInteger("Atualizar tamanho da memória", "Digite o novo tamanho da memória (em bytes):", "Tamanho da memória (em bytes):");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        controller.changeMemorySize(newSizeInBytes);
    }

    /// ===================== MENU LIGADOR ===================== ///

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


    public void handleChangeRunningSpeedAction(int newSimulationSpeed) {
        controller.setSimulationSpeed(newSimulationSpeed);
    }

    /// ===================== MENU EXIBIÇÃO ===================== ///

    public void handleSetHexViewAction() {
        controller.setViewFormat("HEX");
    }

    public void handleSetOctalViewAction() {
        controller.setViewFormat("OCT");
    }

    public void handleSetDecimalViewAction() {
        controller.setViewFormat("DEC");
    }

    /// ===================== MENU AJUDA ===================== ///

    public void handleHelpAction() {
        controller.showHelpWindow();
    }
}
