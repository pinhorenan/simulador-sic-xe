package sicxesimulator.utils;

import java.util.ArrayList;
import java.util.List;

public class ViewConfig {
    private String addressFormat = "HEX";
    private final List<FormatChangeListener> listeners = new ArrayList<>();

    public interface FormatChangeListener {
        void onFormatChanged(String newFormat);
    }

    public void addFormatChangeListener(FormatChangeListener listener) {
        listeners.add(listener);
    }

    public void setAddressFormat(String format) {
        this.addressFormat = format;
        listeners.forEach(l -> l.onFormatChanged(format));
    }

    public String getAddressFormat() {
        return addressFormat;
    }
}
