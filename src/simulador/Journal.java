
package simulador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

public class Journal {

    private final Path journalPath;
    private String ultimoId;

    public Journal(String path) throws IOException {

        this.journalPath = Path.of(path);

        Files.createDirectories(journalPath.getParent());

        if (!Files.exists(journalPath)) {
            Files.createFile(journalPath);
        }
    }

    public void registrarInicio(
            String operacao,
            byte[] estado) throws IOException {

        String id = UUID.randomUUID().toString();

        String dados = Base64.getEncoder()
                .encodeToString(estado);

        String registro = "BEGIN|" + id + "|"
                + operacao + "|" + dados;

        gravar(registro);

        ultimoId = id;
    }

    public void registrarCommit() throws IOException {

        if (ultimoId == null) {
            throw new IllegalStateException(
                "Não existe transação iniciada."
            );
        }

        gravar("COMMIT|" + ultimoId);

        ultimoId = null;
    }

    private void gravar(String registro) throws IOException {

        try (var stream = new java.io.FileOutputStream(
                journalPath.toFile(), true)) {

            stream.write(
                (registro + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8)
            );

            stream.flush();

            stream.getFD().sync();
        }
    }

    public byte[] recuperarUltimoEstado() throws IOException {

        byte[] ultimoEstado = null;

        for (String linha : Files.readAllLines(journalPath)) {

            if (!linha.startsWith("BEGIN|")) {
                continue;
            }

            String[] partes = linha.split("\\|", 4);

            if (partes.length != 4) {
                continue;
            }

            try {
                ultimoEstado = Base64.getDecoder()
                        .decode(partes[3]);
            } catch (IllegalArgumentException e) {
                // Ignora registro incompleto ou inválido.
            }
        }

        return ultimoEstado;
    }
}
