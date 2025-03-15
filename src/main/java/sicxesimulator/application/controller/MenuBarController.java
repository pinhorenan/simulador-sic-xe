package sicxesimulator.application.controller;

import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.utils.Constants;
import sicxesimulator.application.util.DialogUtil;
import sicxesimulator.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
        // TODO: Não é pra mostrar no OutputPanel, mas por enquanto vai ser.
        String textual = obj.getObjectCodeAsString();  // Implementar no ObjectFile
        controller.getMainLayout().getOutputPanel().getOutputArea().appendText("=== OBJ TEXT ===\n" + textual + "\n");
    }
    
    // TODO: Mover isso para model
    public enum LinkerMode {
        ABSOLUTO, RELOCAVEL
    }

    // TODO: Mover isso para model
    private LinkerMode currentLinkerMode = LinkerMode.RELOCAVEL;

    // TODO: Mover isso para model
    // Endereço base (apenas se estivermos no modo Absoluto).
    @SuppressWarnings("unused")
    private final int baseAddress = 0;


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


    // Ação do menuItem "Exportar código expandido"
    public void handleExportASM() {
        try {
            List<String> expandedCode = controller.getExpandedCode();
            if (expandedCode.isEmpty()) {
                DialogUtil.showError("Nenhum código para exportar.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly Expandido", "*.asm"));
            fileChooser.setInitialFileName(controller.getSuggestedFileName(".asm"));

            File file = fileChooser.showSaveDialog(controller.getStage());

            if (file != null) {
                FileUtils.writeFile(file.getAbsolutePath(), String.join("\n", expandedCode));
                DialogUtil.showInfo("Arquivo .ASM Expandido exportado com sucesso!");
            }
        } catch (IOException e) {
            DialogUtil.showError("Erro ao exportar código ASM: " + e.getMessage());
        }
    }


    // Ação do menuItem "Exportar arquivo .obj"
    public void handleExportOBJ() {
        try {
            byte[] objFileContent = controller.getObjectFileBytes();
            if (objFileContent == null) {
                DialogUtil.showError("Nenhum código montado disponível.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos .OBJ", "*.obj"));
            fileChooser.setInitialFileName(controller.getSuggestedFileName(".obj"));

            File file = fileChooser.showSaveDialog(controller.getStage());

            if (file != null) {
                FileUtils.writeFile(file.getAbsolutePath(), Arrays.toString(objFileContent));
                DialogUtil.showInfo("Arquivo .OBJ exportado com sucesso!");
            }
        } catch (IOException e) {
            DialogUtil.showError("Erro ao exportar arquivo OBJ: " + e.getMessage());
        }
    }

    // Ação do menuItem "Limpar arquivos salvos"
    public void handleClearObjectDirectory() {
        File savedDir = new File(Constants.SAVE_DIR);
        if (!savedDir.exists()) return;
        File[] files = savedDir.listFiles((d,name)-> (name.endsWith(".obj") || name.endsWith(".meta")));
        assert files != null;
        for (File f : files) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
        DialogUtil.showInfo("Diretório de arquivos salvos limpo.");
        controller.initializeFilesView();
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

    // Ação do MenuItem "Modo de Ligação"
    public void handleSetLinkerModeAction() {
        // Diálogo para o usuário escolher ABSOLUTO ou RELOCAVEL
        ChoiceDialog<LinkerMode> dialog = new ChoiceDialog<>(currentLinkerMode, LinkerMode.values());
        dialog.setTitle("Modo de Ligação");
        dialog.setHeaderText("Selecione o modo de ligação:");
        dialog.setContentText("Modo:");

        Optional<LinkerMode> result = dialog.showAndWait();
        if (result.isPresent()) {
            currentLinkerMode = result.get();
            System.out.println("Modo de Ligação definido para: " + currentLinkerMode);

            // Informa ao Controller principal qual modo foi escolhido
            controller.setLinkerMode(currentLinkerMode);
        }
    }

    /// ===================== MENU EXECUÇÃO ===================== ///

    // Ação do menuItem "Mudar velocidade de execução"
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

    public void handleSetBinaryViewAction() {
        controller.setViewFormat("BIN");
    }

    /// ===================== MENU AJUDA ===================== ///

    public void handleHelpAction() {
        controller.showHelpWindow();
    }
}
