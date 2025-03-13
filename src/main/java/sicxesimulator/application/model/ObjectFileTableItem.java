package sicxesimulator.application.model;

import javafx.beans.property.*;
import sicxesimulator.models.ObjectFile;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ObjectFileTableItem {
    private final StringProperty programName;  // Nome do programa
    private final StringProperty size;  // Tamanho do arquivo
    private final StringProperty mountDate;  // Data de montagem
    private final ObjectFile objectFile;  // O próprio arquivo ObjectFile

    // Construtor da classe que inicializa as propriedades
    public ObjectFileTableItem(ObjectFile objectFile) {
        this.objectFile = objectFile;
        this.programName = new SimpleStringProperty(objectFile.getProgramName());  // Nome do programa
        this.size = new SimpleStringProperty(objectFile.getProgramLength() + " bytes");  // Tamanho do programa
        this.mountDate = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));  // Data atual
    }

    // Métodos para manipulação do nome do programa
    public String getProgramName() {
        return programName.get();
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

    // Métodos para manipulação da data de montagem
    public String getMountDate() {
        return mountDate.get();
    }

    public StringProperty mountDateProperty() {
        return mountDate;
    }

    // TODO: Esses getters aparecem como sem uso mas são usados em ObjectFileTable.java

    // Acesso ao ObjectFile original
    public ObjectFile getObjectFile() {
        return objectFile;
    }
}

