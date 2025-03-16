package sicxesimulator.application.model;

import javafx.beans.property.*;
import sicxesimulator.data.ObjectFile;

import static sicxesimulator.data.ObjectFile.ObjectFileOrigin.SINGLE_MODULE;

@SuppressWarnings("ALL")
public class ObjectFileTableItem {
    private final StringProperty programName;   // Nome do programa
    private final StringProperty size;          // Tamanho do arquivo
    private final StringProperty origin;        // Indica se o arquivo é simples ou foi gerado pela ligação de diversos módulos

    private final ObjectFile objectFile;        // O próprio arquivo ObjectFile

    // Construtor da classe que inicializa as propriedades
    public ObjectFileTableItem(ObjectFile objectFile) {
        this.objectFile = objectFile;
        this.programName = new SimpleStringProperty(objectFile.getProgramName());  // Nome do programa
        this.size = new SimpleStringProperty(objectFile.getProgramLength() + " bytes");  // Tamanho do programa

        // Define a origem do arquivo objeto

        if (objectFile.getOrigin() == SINGLE_MODULE) {
            this.origin = new SimpleStringProperty("Módulo Simples");
        } else {
            this.origin = new SimpleStringProperty("Módulo Composto");
        }
    }

    // Métodos para manipulação do nome do programa
    public String getProgramName() {
        return programName.get();
    }

    public String getOrigin() {
        return origin.get();
    }

    public StringProperty originProperty() {
        return origin;
    }

    public StringProperty programNameProperty() {
        return programName;
    }

    public String getSize() {
        return size.get();
    }

    public StringProperty sizeProperty() {
        return size;
    }

    // Acesso ao ObjectFile original
    public ObjectFile getObjectFile() {
        return objectFile;
    }
}

