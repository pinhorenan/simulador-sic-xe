package sicxesimulator.simulation.model.data;

import javafx.beans.property.*;
import sicxesimulator.software.data.ObjectFile;

import static sicxesimulator.software.data.ObjectFile.ObjectFileOrigin.SINGLE_MODULE;

public class ObjectFileTableItem {
    private final StringProperty programName;   // Nome do programa
    private final StringProperty size;          // Tamanho do arquivo
    private final StringProperty origin;        // Indica se o arquivo é simples ou foi gerado pela ligação de diversos módulos
    private final ObjectFile objectFile;        // O próprio arquivo ObjectFile

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

    public String getProgramName() {
        return programName.get();
    }

    public String getOrigin() {
        return origin.get();
    }

    public String getSize() {
        return size.get();
    }

    public ObjectFile getObjectFile() {
        return objectFile;
    }
}
