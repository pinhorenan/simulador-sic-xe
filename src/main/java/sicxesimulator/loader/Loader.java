package sicxesimulator.loader;

import sicxesimulator.machine.Memory;
import sicxesimulator.machine.cpu.ControlUnit;
import java.io.*;
import java.util.*;

public class Loader {

    private int loadAddress;
    private int programLength;

    public void load(String objectFilePath, Memory memory, ControlUnit controlUnit) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(objectFilePath))) {
            String line;
            List<String> modificationRecords = new ArrayList<>();

            // Processa o cabeçalho (Header)
            line = reader.readLine();
            if (line == null || !line.startsWith("H")) {
                throw new LoaderException("Arquivo objeto inválido: cabeçalho ausente");
            }

            String[] headerParts = line.split(" ");
            if (headerParts.length < 3) {
                throw new LoaderException("Formato do cabeçalho inválido");
            }

            loadAddress = Integer.parseInt(headerParts[1], 16);
            programLength = Integer.parseInt(headerParts[2], 16);

            // Processa os registros Text (T) e Modification (M)
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length < 2) continue;

                switch (parts[0]) {
                    case "T":
                        int textStart = Integer.parseInt(parts[1], 16);
                        String objectCode = parts[2];
                        for (int i = 0; i < objectCode.length(); i += 2) {
                            int value = Integer.parseInt(objectCode.substring(i, i + 2), 16);
                            memory.writeByte(loadAddress + textStart + (i / 2), value);
                        }
                        break;
                    case "M":
                        modificationRecords.add(line);
                        break;
                    case "E":
                        int execAddress = Integer.parseInt(parts[1], 16);
                        controlUnit.setPC(loadAddress + execAddress);
                        return;
                }
            }

            // Processa os registros de modificação (M)
            for (String modRec : modificationRecords) {
                String[] modParts = modRec.split(" ");
                if (modParts.length < 3) continue;

                int modAddress = Integer.parseInt(modParts[1], 16);
                int modLength = Integer.parseInt(modParts[2], 16);
                int targetAddress = loadAddress + modAddress;

                if (modLength == 5) {
                    int originalValue = memory.readExtended(targetAddress);
                    int modifiedValue = originalValue + loadAddress;
                    memory.writeExtended(targetAddress, modifiedValue);
                }
            }
        }
    }
}
