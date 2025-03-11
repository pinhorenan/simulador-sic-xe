package sicxesimulator.application.controller;

import javafx.stage.FileChooser;
import sicxesimulator.utils.SimulatorLogger;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.application.model.Model;
import sicxesimulator.application.view.MainView;
import sicxesimulator.application.model.ObjectFileTableItem;
import sicxesimulator.utils.DialogUtil;
import sicxesimulator.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MenuBarController {
    private final MainView view;
    private final Model model;

    public MenuBarController(Controller controller) {
        this.view = controller.getView();
        this.model = controller.getModel();
    }

    ///  ============== MENU SUPERIOR =================== ///

    // Ação do menuItem "Importar código-fonte"
    public void handleImportASM() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly", "*.asm"));

        // Define o diretório inicial
        File initialDir = new File(System.getProperty("user.dir") + "/src/main/resources/samples");
        if (initialDir.exists() && initialDir.isDirectory()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File file = fileChooser.showOpenDialog(view.getStage());

        if (file != null) {
            try {
                // Usa FileUtils para ler o conteúdo do arquivo inteiro com UTF-8
                String content = FileUtils.readFile(file.getAbsolutePath());
                // Atualiza o TextArea com o conteúdo lido
                view.getInputField().setText(content);
            } catch (IOException e) {
                DialogUtil.showError("Erro ao importar arquivo ASM: " + e.getMessage());
                SimulatorLogger.logError("Erro ao importar arquivo ASM", e);
            }
        }
    }

    // Ação do menuItem "Exportar código expandido"
    public void handleExportASM() throws IOException {
        // Pega o código fonte do campo de entrada e processa os macros
        List<String> sourceLines = Arrays.asList(view.getInputField().getText().split("\\r?\\n"));
        List<String> expanded = model.processCodeMacros(sourceLines);

        // Configura o FileChooser com o diretório inicial desejado
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly Expandido", "*.asm"));

        // Define o diretório inicial para exportação
        File outputDir = new File(System.getProperty("user.dir") + "/src/main/resources/output");
        if (!outputDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdirs(); // Cria o diretório, se não existir
        }
        fileChooser.setInitialDirectory(outputDir);

        // Define o nome padrão: se houver um objeto montado, usa o programName, senão "Programa.asm"
        String defaultName = "Programa.asm";
        if (model.getLastLoadedCode() != null && model.getLastLoadedCode().getFilename() != null) {
            defaultName = model.getLastLoadedCode().getFilename() + ".asm";
        }
        fileChooser.setInitialFileName(defaultName);

        File file = fileChooser.showSaveDialog(view.getStage());
        if (file != null) {
            // Concatena as linhas em uma única string
            String expandedCode = String.join("\n", expanded);
            try {
                FileUtils.writeFile(file.getAbsolutePath(), expandedCode);
                System.out.println("Arquivo .ASM Expandido exportado com sucesso!");
            } catch (IOException e) {
                DialogUtil.showError("Erro ao tentar escrever o código expandido: " + e.getMessage());
                SimulatorLogger.logError("Erro ao tentar escrever o código expandido:", e);
            }
        }
    }

    // Ação do menuItem "Exportar Arquivo Objeto"
    public void handleExportOBJ() {
        // Obtém a entrada selecionada na TableView de arquivos montados
        ObjectFileTableItem selectedItem = view.getObjectFileTableView().getSelectionModel().getSelectedItem();

        // Se nenhum item foi selecionado, utiliza o objeto mais recente, se existir
        ObjectFile selectedFile;
        if (selectedItem == null) {
            if (model.getLastLoadedCode() != null) {
                selectedFile = model.getLastLoadedCode();
            } else {
                DialogUtil.showError("Nenhum código foi montado ainda.");
                return;
            }
        } else {
            selectedFile = selectedItem.getObjectFile(); // Obtém o ObjectFile a partir do ObjectFileTableItem
        }

        // Verifica se o ObjectFile foi encontrado
        if (selectedFile == null) {
            DialogUtil.showError("Arquivo de objeto não encontrado.");
            return;
        }

        // Obtém o conteúdo do código objeto
        byte[] objFileContent = selectedFile.getMachineCode();

        // Configura o FileChooser para salvar o arquivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos .OBJ", "*.obj"));
        File outputDir = new File(System.getProperty("user.dir") + "/src/main/resources/output");
        if (!outputDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdirs();
        }
        fileChooser.setInitialDirectory(outputDir);

        // Define o nome padrão para o OBJ: usa o filename do ObjectFile
        String defaultName = selectedFile.getFilename();
        if (!defaultName.toLowerCase().endsWith(".obj")) {
            defaultName += ".obj";
        }
        fileChooser.setInitialFileName(defaultName);

        // Abre o FileChooser para o usuário escolher onde salvar
        File file = fileChooser.showSaveDialog(view.getStage());
        if (file != null) {
            try {
                // Converte o conteúdo do arquivo OBJ para hexadecimal
                StringBuilder hexContent = new StringBuilder();
                for (byte b : objFileContent) {
                    hexContent.append(String.format("%02X ", b));
                }
                // Escreve o conteúdo hexadecimal no arquivo
                FileUtils.writeFile(file.getAbsolutePath(), hexContent.toString());
                System.out.println("Arquivo .OBJ exportado com sucesso!");
            } catch (IOException e) {
                // Caso ocorra algum erro durante a escrita do arquivo
                DialogUtil.showError("Erro ao exportar arquivo OBJ: " + e.getMessage());
                SimulatorLogger.logError("Erro ao exportar arquivo OBJ", e);
            }
        }
    }

    ///  2. menuBar Memória

    // Ação do menuItem "Limpar memória"
    public void handleClearMemoryAction() {
        model.getMachine().getMemory().clearMemory();
        view.updateMemoryTableView();
        view.appendOutput("Memória limpa!");
    }

    // Ação do menuItem "Alterar tamanho da memória"
    public void handleChangeMemorySizeAction(int newSizeInBytes) {
        try {
            model.getMachine().changeMemorySize(newSizeInBytes);
            model.setMemorySize(newSizeInBytes);

            view.appendOutput("Memória alterada para " + newSizeInBytes + " bytes.");
            view.updateMemoryTableView();
        } catch (Exception e) {
            DialogUtil.showError("Erro ao alterar o tamanho da memória: " + e.getMessage());
        }
    }

    ///  4) menuBar Execução

    // Ação do menuItem "Mudar velocidade de execução"
    public void handleChangeRunningSpeedAction(int newSimulationSpeed) {
        model.setSimulationSpeed(newSimulationSpeed);
    }

    ///  5) menuBar Exibir

    // Ação do menuItem "Exibição → Hexadecimal"
    public void handleSetHexViewAction() {
        view.setViewFormat("HEX");
        view.updateViewFormatLabel();
    }

    // Ação do menuItem "Exibição → Octal"
    public void handleSetOctalViewAction() {
        view.setViewFormat("OCT");
        view.updateViewFormatLabel();
    }

    // Ação do menuItem "Exibição → Decimal"
    public void handleSetDecimalViewAction() {
        view.setViewFormat("DEC");
        view.updateViewFormatLabel();
    }

    // Ação do menuItem "Exibição → Binário"
    public void handleSetBinaryViewAction() {
        view.setViewFormat("BIN");
        view.updateViewFormatLabel();
    }

    ///  6) menuBar Ajuda

    // Ação do menuItem "Ajuda" (exibe a janela de ajuda)
    public void handleHelpAction() {
        view.showHelpWindow();
    }

}
