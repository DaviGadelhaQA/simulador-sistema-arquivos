package simulador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.nio.file.*;

public class FileSystemSimulator {
    private Directory root;
    private final Journal journal;
    private final Path storagePath = Path.of("storage/filesystem.dat");

    public FileSystemSimulator() {
        try {
            Files.createDirectories(storagePath.getParent());
            journal = new Journal("storage/journal.log");
            byte[] estado = journal.recuperarUltimoEstado();

            if (estado != null) {
                root = desserializar(estado);
                salvarEstado(estado);

                System.out.println("Sistema recuperado pelo Journal.");

            } else if (Files.exists(storagePath)) {
                root = carregarEstado();
            } else {
                root = new Directory("/", null);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                "Erro ao inicializar o sistema de arquivos.", e
            );
        }
    }

    private byte[] serializar(Directory diretorio) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (ObjectOutputStream output = new ObjectOutputStream(buffer)) {
            output.writeObject(diretorio);
        }

        return buffer.toByteArray();
    }


    private void executarTransacao(String operacao) {
        try {
            byte[] estado = serializar(root);
            journal.registrarInicio(operacao, estado);

            salvarEstado(estado);
            journal.registrarCommit();

        } catch (IOException e) {
            throw new java.io.UncheckedIOException(
                "Falha ao persistir a operação: " + operacao,
                e
            );
        }
    }


    private Directory desserializar(byte[] dados) throws IOException, ClassNotFoundException {
        try (ObjectInputStream input =
                new ObjectInputStream(new ByteArrayInputStream(dados))) {

            return (Directory) input.readObject();
        }
    }

    private Directory carregarEstado() throws IOException, ClassNotFoundException {
        byte[] dados = Files.readAllBytes(storagePath);

        return desserializar(dados);
    }

    private void salvarEstado(byte[] dados) throws IOException {
        Path temporario = Path.of(
            storagePath.toString() + ".tmp"
        );

        Files.write(
            temporario,
            dados,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );

        try {
            Files.move(
                temporario,
                storagePath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );

        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                temporario,
                storagePath,
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }


    private String[] separarCaminho(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Caminho inválido.");
        }

        return path.replace("\\", "/").split("/+");
    }

    private String obterCaminhoPai(String path) {
        String normalizado = path.replace("\\", "/");

        int ultimaBarra = normalizado.lastIndexOf('/');

        if (ultimaBarra <= 0) {
            return "/";
        }

        return normalizado.substring(0, ultimaBarra);
    }

    private Directory localizarDiretorio(String path) {
        Directory atual = root;

        for (String parte : separarCaminho(path)) {

            if (parte.isEmpty() || parte.equals(".")) {
                continue;
            }

            if (parte.equals("..")) {
                if (atual.getParent() != null) {
                    atual = atual.getParent();
                }
                continue;
            }

            Directory proximo = atual.getDirectories().get(parte);

            if (proximo == null) {
                throw new IllegalArgumentException(
                        "Diretório não encontrado: " + parte);
            }

            atual = proximo;
        }

        return atual;
    }

    public String resolverDiretorio(String path) {
        Directory atual = localizarDiretorio(path);
        StringBuilder caminho = new StringBuilder();

        while (atual.getParent() != null) {
            caminho.insert(0, "/" + atual.getName());
            atual = atual.getParent();
        }

        return caminho.isEmpty() ? "/" : caminho.toString();
    }

    private String validarNome(String name) {
        if (name == null || name.isBlank()
                || name.contains("/")
                || name.contains("\\")
                || name.equals(".")
                || name.equals("..")) {

            throw new IllegalArgumentException("Nome inválido.");
        }

        return name;
    }

    private void verificarNomeDisponivel(Directory directory, String name) {
        if (directory.getFiles().containsKey(name)
                || directory.getDirectories().containsKey(name)) {

            throw new IllegalArgumentException(
                    "Já existe um arquivo ou diretório com esse nome.");
        }
    }

    public void criarDiretorio(String path) {
        String[] partes = separarCaminho(path);

        if (partes.length == 0) {
            throw new IllegalArgumentException("Caminho inválido.");
        }

        String nome = validarNome(partes[partes.length - 1]);

        String caminhoPai = obterCaminhoPai(path);

        if (caminhoPai.isEmpty()) {
            caminhoPai = "/";
        }

        Directory pai = localizarDiretorio(caminhoPai);

        verificarNomeDisponivel(pai, nome);

        Directory novo = new Directory(nome, pai);

        pai.getDirectories().put(nome, novo);
        executarTransacao("mkdir " + path);

        System.out.println("Diretório criado: " + path);
    }

    public void criarArquivo(String path, String content) {
        String[] partes = separarCaminho(path);
        String nome = validarNome(partes[partes.length - 1]);
        String caminhoPai = obterCaminhoPai(path);
        Directory pai = localizarDiretorio(caminhoPai);
        verificarNomeDisponivel(pai, nome);
        File novo = new File(nome, content);
        pai.getFiles().put(nome, novo);
        executarTransacao("create " + path);
        System.out.println("Arquivo criado: " + path);
    }

    public void copiarArquivo(String origem, String destino, String novoNome) {
        String[] partes = separarCaminho(origem);
        String nomeOriginal = partes[partes.length - 1];
        String caminhoPai = obterCaminhoPai(origem);

        if (caminhoPai.isEmpty()) {
            caminhoPai = "/";
        }

        Directory diretorioOrigem = localizarDiretorio(caminhoPai);
        File original = diretorioOrigem.getFiles().get(nomeOriginal);

        if (original == null) {
            throw new IllegalArgumentException("Arquivo de origem não encontrado.");
        }

        Directory diretorioDestino = localizarDiretorio(destino);

        String nomeFinal = novoNome == null || novoNome.isBlank() ? nomeOriginal : validarNome(novoNome);

        verificarNomeDisponivel(diretorioDestino, nomeFinal);

        File copia = new File(nomeFinal, original.getContent());
        diretorioDestino.getFiles().put(nomeFinal, copia);
        executarTransacao("copy " + origem + " " + destino);
        System.out.println("Arquivo copiado para: " + destino);
    }

    public void apagarArquivo(String path) {
        String[] partes = separarCaminho(path);
        String nome = partes[partes.length - 1];
        String caminhoPai = obterCaminhoPai(path);

        if (caminhoPai.isEmpty()) {
            caminhoPai = "/";
        }

        Directory pai = localizarDiretorio(caminhoPai);
        File removido = pai.getFiles().remove(nome);

        if (removido == null) {
            throw new IllegalArgumentException("Arquivo não encontrado: " + path);
        }
        executarTransacao("delete " + path);
        System.out.println("Arquivo apagado: " + path);
    }

    public void renomearArquivo(String path, String novoNome) {
        String[] partes = separarCaminho(path);
        String nomeAntigo = partes[partes.length - 1];
        novoNome = validarNome(novoNome);

        String caminhoPai = obterCaminhoPai(path);

        if (caminhoPai.isEmpty()) {
            caminhoPai = "/";
        }

        Directory pai = localizarDiretorio(caminhoPai);

        File arquivo = pai.getFiles().get(nomeAntigo);

        if (arquivo == null) {
            throw new IllegalArgumentException(
                    "Arquivo não encontrado.");
        }

        verificarNomeDisponivel(pai, novoNome);

        pai.getFiles().remove(nomeAntigo);

        arquivo.setName(novoNome);

        pai.getFiles().put(novoNome, arquivo);
        executarTransacao("rename " + path + " " + novoNome);

        System.out.println("Arquivo renomeado para: " + novoNome);
    }

    public void apagarDiretorio(String path) {
        Directory diretorio = localizarDiretorio(path);

        if (diretorio == root) {
            throw new IllegalArgumentException(
                    "Não é permitido apagar a raiz.");
        }

        if (!diretorio.getFiles().isEmpty()
                || !diretorio.getDirectories().isEmpty()) {

            throw new IllegalArgumentException(
                    "O diretório não está vazio.");
        }

        Directory pai = diretorio.getParent();

        pai.getDirectories().remove(diretorio.getName());
        executarTransacao("rmdir " + path);

        System.out.println("Diretório apagado: " + path);
    }

    public void renomearDiretorio(String path, String novoNome) {
        Directory diretorio = localizarDiretorio(path);

        if (diretorio == root) {
            throw new IllegalArgumentException(
                    "Não é permitido renomear a raiz.");
        }

        novoNome = validarNome(novoNome);

        Directory pai = diretorio.getParent();

        verificarNomeDisponivel(pai, novoNome);

        pai.getDirectories().remove(diretorio.getName());

        diretorio.setName(novoNome);

        pai.getDirectories().put(novoNome, diretorio);
        executarTransacao("mvdir " + path + " " + novoNome);

        System.out.println("Diretório renomeado para: " + novoNome);
    }

    public List<String> listarDiretorio(String path) {
        Directory diretorio = localizarDiretorio(path);
        List<String> resultado = new ArrayList<>();

        for (Map.Entry<String, Directory> entrada : diretorio.getDirectories().entrySet()) {
            resultado.add("[DIR] " + entrada.getKey());
        }

        for (Map.Entry<String, File> entrada : diretorio.getFiles().entrySet()) {
            resultado.add("[ARQ] " + entrada.getKey());
        }

        return resultado;
    }
}