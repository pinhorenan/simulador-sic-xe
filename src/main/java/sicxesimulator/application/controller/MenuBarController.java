package sicxesimulator.application.controller;

import javafx.stage.FileChooser;
import sicxesimulator.utils.Constants;
import sicxesimulator.utils.DialogUtil;
import sicxesimulator.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MenuBarController {
    private final Controller controller;

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


    // Ação do menuItem "Exportar Arquivo Objeto"
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

    /// ===================== MENU MEMÓRIA ===================== ///

    // Ação do menuItem "Limpar memória"
    public void handleClearMemoryAction() {
        controller.clearMemory();
    }

    // Ação do menuItem "Alterar tamanho da memória"
    public void handleChangeMemorySizeAction(int newSizeInBytes) {
        controller.changeMemorySize(newSizeInBytes);
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
