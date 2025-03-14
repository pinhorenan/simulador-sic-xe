package sicxesimulator.linker;

import sicxesimulator.models.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Linker {

    /**
     * linkModules: une os módulos objeto em um único executável final.
     *
     * @param modules Lista de ObjectFile a serem ligados.
     * @param finalRelocation Se true, o Linker soma os endereços absolutos, gerando um executável fixo.
     *                        Se false, deixa a relocação para o Carregador.
     * @param loadAddress Endereço de carga inicial (em bytes) do primeiro módulo, se finalRelocation = true.
     * @param outputFileName Nome do programa final gerado.
     * @return Um ObjectFile representando o executável final.
     */
    public ObjectFile linkModules(
            List<ObjectFile> modules,
            boolean finalRelocation,
            int loadAddress,
            String outputFileName
    ) {
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("Nenhum módulo para linkar.");
        }

        // Passo 1: atribuir base a cada módulo e coletar símbolos públicos (exportados)
        int currentBase = loadAddress;
        Map<ObjectFile, Integer> baseMap = new HashMap<>();     // Módulo -> base
        Map<String, Integer> globalSymbols = new HashMap<>();   // symbolName -> globalAddress

        // Coleção para checar duplicatas: se algum símbolo for exportado por > 1 módulos
        Set<String> alreadyExported = new HashSet<>();


        // A) Monta a tabela globalSymbols a partir de cada módulo
        int totalSize = 0;
        for (ObjectFile module : modules) {
            baseMap.put(module, currentBase);

            // Para cada símbolo local do módulo
            for (var entry : module.getSymbolTable().getAllSymbols().entrySet()) {
                String symbolName = entry.getKey();
                SymbolTable.SymbolInfo info = entry.getValue();

                if (info.isPublic) {
                    // Calcula o address "absoluto" se finalRelocation==true,
                    // caso contrário, usamos o address "relativo" local ao módulo
                    int globalAddress = finalRelocation ? (currentBase + info.address) : info.address;

                    // Checa duplicação
                    if (alreadyExported.contains(symbolName)) {
                        throw new IllegalStateException("Símbolo " + symbolName + "exportado por mais de um módulo.");
                    }
                    alreadyExported.add(symbolName);

                    // Insere no map
                    globalSymbols.put(symbolName, globalAddress);
                }
            }

            totalSize += module.getProgramLength();
            currentBase += module.getProgramLength();
        }

        // B) Verifica símbolos importados
        for (ObjectFile module : modules) {
            for (String symbol : module.getImportedSymbols()) {
                // Se não existe em globalSymbols, significa que esse símbolo não foi exportado por nenhum módulo
                if (!globalSymbols.containsKey(symbol)) {
                    throw new IllegalArgumentException("Símbolo importado [" + symbol + "] não foi definido em nenhum módulo.");
                }
            }
        }

        // Passo 2: Gerar um array final "finalCode" concatenando todos
        byte[] finalCode = new byte[totalSize];

        // Cria uma SymbolTable final (vai conter todos os símbolos com seus endereços ajustados)
        SymbolTable finalSymbolTable = new SymbolTable();

        // Precisamos também agrupar relocationRecords se finalRelocation==false
        List<RelocationRecord> finalRelocs = new ArrayList<>();

        int offsetGlobal = 0;
        for (ObjectFile module : modules) {
            byte[] code = module.getObjectCode();
            int base = baseMap.get(module);

            // Se finalRelocation==true, aplicamos a correção no code do módulo (chamando fixAddressInCode) antes de copiá-lo
            if (finalRelocation) {
                for (RelocationRecord rec : module.getRelocationRecords()) {
                    fixAddressInCode(code, rec, module, base, globalSymbols);
                }
            } else {
                // Se finalRelocation==false, reagrupamos relocationRecords ajustando offsetGlobal
                for (RelocationRecord rec : module.getRelocationRecords()) {
                    int newOffset = rec.offset() + offsetGlobal;
                    RelocationRecord newRec = new RelocationRecord(
                            newOffset,
                            rec.symbol(),
                            rec.length(),
                            rec.pcRelative()
                    );
                    finalRelocs.add(newRec);
                }
            }

            // Copia o code do módulo no finalCode
            System.arraycopy(code, 0, finalCode, offsetGlobal, code.length);

            // Adiciona símbolos do module no finalSymbolTable
            for (var entry : module.getSymbolTable().getAllSymbols().entrySet()) {
                String symbolName = entry.getKey();
                SymbolTable.SymbolInfo info = entry.getValue();
                int finalAddress = finalRelocation ? (base + info.address) : (offsetGlobal + info.address);

                // Se for duplicado e isPublic, gerará o mesmo erro do "alreadyExported" TODO: melhorar mensagem
                finalSymbolTable.addSymbol(symbolName, finalAddress, info.isPublic);
            }

            offsetGlobal += code.length;
        }

        // Determina o start final
        int finalStart = finalRelocation ? loadAddress : modules.getFirst().getStartAddress();

        // Monta uma lista de source combinada (concatenando o .getRawSourceCode de cada módulo *Isso é só pra frufru*)
        List<String> combinedSource = new ArrayList<>();
        for (ObjectFile module : modules) {
            if (module.getRawSourceCode() != null) {
                combinedSource.addAll(module.getRawSourceCode());
            }
            // TODO: Verificar se o montador está interpretando ';' como comentário (antes tava '.')
            combinedSource.add(";---- Fim do Módulo " + module.getProgramName() + " ----");
        }

        // Gera um objectFile final
        // Se finalRelocation==true => isRelocated = true, e sem relocRecords
        // Se finalRelocation==false => isRelocated = false, e finalRelocs
        ObjectFile finalObject = new ObjectFile(
                finalStart,
                finalCode,
                finalSymbolTable,
                outputFileName,     // TODO: Definir um dialog pedindo nome na hora de linkar
                combinedSource,
                Collections.emptySet(),     // Sem símbolos importados no finalObject
                finalRelocation ? Collections.emptyList() : finalRelocs
        );
        finalObject.setRelocated(finalRelocation);

        // Passo 3.1) Gerar um .obj textual final (unido) com H/D/R/T/M/E
        try {
            writeLinkedHTME(finalObject, globalSymbols, outputFileName + ".obj");
        } catch (IOException e) {
            // Se der erro, apenas exibe/LOG
            System.err.println("Falha ao gravar .obj final textual: " + e.getMessage());
        }

        // Passo 3.2) Gerar também um .meta binário final para a interface gráfica
        finalObject.saveToFile(new File(outputFileName + ".meta"));

        return finalObject;
    }

    /**
     * Ajusta o code do módulo (array de bytes) aplicando a soma de "moduleBase" + eventuais subtrações
     * para pcRelative. A ser usado caso finalRelocation==true.
     */
    private void fixAddressInCode(
            byte[] code,
            RelocationRecord rec,
            ObjectFile mod,
            int moduleBase,
            Map<String,Integer> globalSymbols
    ) {
        int offset = rec.offset();
        int length = rec.length();
        if (offset + length > code.length) {
            throw new IllegalArgumentException(
                    "RelocationRecord fora dos limites do code do modulo " + mod.getProgramName()
            );
        }

        // Lê valor atual
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 8) | (code[offset + i] & 0xFF);
        }

        // Descobre o address final do símbolo
        String symbol = rec.symbol();
        Integer resolvedAddr = globalSymbols.get(symbol);
        if (resolvedAddr == null) {
            // Tenta ver se é local
            Integer localAddr = mod.getSymbolTable().getAddress(symbol);
            if (localAddr != null) {
                resolvedAddr = moduleBase + localAddr;
            } else {
                throw new IllegalArgumentException("Simbolo [" + symbol + "] nao encontrado " +
                        "nem em globalSymbols nem local no modulo " + mod.getProgramName());
            }
        }

        // soma
        int newValue = value + resolvedAddr;

        // Se for PC-relative, subtrai algo
        if (rec.pcRelative()) {
            // Mínimo: subtrair 3 se for SIC/XE normal. Ajuste de design
            newValue -= 3;
        }

        // Escreve de volta
        int temp = newValue;
        for (int i = length - 1; i >= 0; i--) {
            code[offset + i] = (byte)(temp & 0xFF);
            temp >>>= 8;
        }
    }


    /**
     * Gera um arquivo textual .obj final unificado contendo:
     * - H record (nome, start, length)
     * - D record(s) a partir de globalSymbols (apenas *exportados*? Se preferir)
     * - R record(s) - em link final, a rigor, R não teria muito sentido, pois tudo resolvido
     * - T records de 30 bytes
     * - M records, se finalRelocation==false? (aqui depende do design: ou já unificamos no finalObj)
     * - E record
     * Para simplificar, iremos gerar algo estilo "SIC normal", mas sem R se já resolvemos tudo.
     */
    private void writeLinkedHTME(ObjectFile finalObj,
                                 Map<String,Integer> globalSymbols,
                                 String outFileName) throws IOException
    {
        // Extrai do finalObj
        int startAddr = finalObj.getStartAddress();
        byte[] code = finalObj.getObjectCode();
        int programLength = code.length;
        String progName = finalObj.getProgramName();
        SymbolTable finalSymTab = finalObj.getSymbolTable();

        // Vamos dividir o code em blocos T de ~30 bytes
        List<TBlock> tblocks = buildTextRecords(startAddr, code);

        // Abre .obj textual
        try (FileWriter fw = new FileWriter(outFileName)) {

            // 1) H
            String header = String.format("H^%-6s^%06X^%06X",
                    fitProgramName(progName),
                    startAddr,
                    programLength
            );
            fw.write(header + "\n");

            // 2) D record (apenas para símbolos public do finalSymTab?)
            List<SymbolTable.SymbolInfo> exported = new ArrayList<>();
            for (SymbolTable.SymbolInfo sinfo : finalSymTab.getAllSymbols().values()) {
                if (sinfo.isPublic) {
                    exported.add(sinfo);
                }
            }
            if (!exported.isEmpty()) {
                StringBuilder dRec = new StringBuilder("D");
                for (SymbolTable.SymbolInfo sinfo : exported) {
                    dRec.append("^").append(sinfo.name)
                            .append("^").append(String.format("%06X", sinfo.address));
                }
                fw.write(dRec + "\n");
            }

            // 3) R record?
            // Se neste design finalRelocation==true, tudo resolvido => R record não tem muito uso
            // Mas se fosse multifase, poderíamos inserir. Vamos deixar de fora ou apenas demarcar:
            // fw.write("R\n");
            // TODO: Revisar a necessidade

            // 4) T records
            for (TBlock block : tblocks) {
                StringBuilder hex = new StringBuilder();
                for (byte b : block.data) {
                    hex.append(String.format("%02X", b & 0xFF));
                }
                int length = block.data.size();
                String textRec = String.format("T^%06X^%02X^%s",
                        block.startAddr, length, hex);
                fw.write(textRec + "\n");
            }

            // 5) M records?
            // Se finalRelocation==true e já resolvemos tudo, não faz sentido M no final.
            // Se finalRelocation==false, poderíamos unificar M records do finalObj. Depende do design.
            // Exemplo: se finalObj tiver relocationRecords e isRelocated=false, podemos escrever
            // TODO: Revisar a necessidade
            List<RelocationRecord> finalRecs = finalObj.getRelocationRecords();
            if (!finalRecs.isEmpty()) {
                for (RelocationRecord rec : finalRecs) {
                    int address = startAddr + rec.offset();
                    // length em half-bytes = rec.length()*2
                    int halfBytes = rec.length() * 2;
                    // notamos: rec.symbol() => ex: +FOO
                    String mLine = String.format("M^%06X^%02X^+%s",
                            address, halfBytes, rec.symbol());
                    fw.write(mLine + "\n");
                }
            }

            // 6) E record
            String endRec = String.format("E^%06X", startAddr);
            fw.write(endRec + "\n");
        }
    }

    // Construtor de TBlock
    static class TBlock {
        int startAddr;
        List<Byte> data = new ArrayList<>();

        TBlock(int start) {
            this.startAddr = start;
        }
    }
    private List<TBlock> buildTextRecords(int start, byte[] code) {
        List<TBlock> blocks = new ArrayList<>();
        final int MAX_BYTES = 0x1E;

        int index = 0;
        while (index < code.length) {
            int blockLen = Math.min(MAX_BYTES, code.length - index);
            TBlock block = new TBlock(start + index);
            for (int i = 0; i < blockLen; i++) {
                block.data.add(code[index + i]);
            }
            blocks.add(block);
            index += blockLen;
        }
        return blocks;
    }

    // Ajusta nome do programa para caber em 6 chars
    private String fitProgramName(String name) {
        if (name == null) return "NONAME";
        if (name.length() > 6) return name.substring(0, 6);
        return name;
    }
}
