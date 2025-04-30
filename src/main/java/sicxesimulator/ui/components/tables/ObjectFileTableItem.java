package sicxesimulator.ui.components.tables;

import javafx.beans.property.*;
import sicxesimulator.software.data.ObjectFile;

import static sicxesimulator.software.data.ObjectFile.ObjectFileOrigin.SINGLE_MODULE;

public class ObjectFileTableItem {
    private final StringProperty programName;
    private final StringProperty size;
    private final ObjectFile objectFile;

    public ObjectFileTableItem(ObjectFile objectFile) {
        this.objectFile = objectFile;
        this.programName = new SimpleStringProperty(objectFile.getProgramName());
        this.size = new SimpleStringProperty(objectFile.getProgramLength() + " bytes");

        @SuppressWarnings("unused") StringProperty origin;
        if (objectFile.getOrigin() == SINGLE_MODULE) {
            //noinspection UnusedAssignment
            origin = new SimpleStringProperty("Módulo Simples");
        } else {
            //noinspection UnusedAssignment
            origin = new SimpleStringProperty("Módulo Composto");
        }
    }

    public String getProgramName() {
        return programName.get();
    }

    public String getSize() {
        return size.get();
    }

    public ObjectFile getObjectFile() {
        return objectFile;
    }
}
