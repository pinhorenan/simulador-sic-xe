package sicxesimulator.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MemoryEntry {
    private final StringProperty address;
    private final StringProperty value;

    public MemoryEntry(String address, String value) {
        this.address = new SimpleStringProperty(address);
        this.value = new SimpleStringProperty(value);
    }

    public String getAddress() {
        return address.get();
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty addressProperty() {
        return address;
    }

    public StringProperty valueProperty() {
        return value;
    }
}
