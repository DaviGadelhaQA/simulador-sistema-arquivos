package simulador;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Shell {
    private final FileSystemSimulator sistema;
    private final Scanner scanner;
    private String diretorioAtual = "/";

    public Shell(FileSystemSimulator sistema) {
        this.sistema = sistema;
        this.scanner = new Scanner(System.in);
    }

    public void executar() {
        System.out.println("======================================");
        System.out.println(" SIMULADOR DE SISTEMA DE ARQUIVOS");
        System.out.println(" Digite help para ver os comandos.");
        System.out.println("======================================");

        while (true) {
            revalidarDiretorioAtual();
            System.out.print("fs:" + diretorioAtual + "$ ");

            String entrada = scanner.nextLine().trim();

            if (entrada.isEmpty()) {
                continue;
            }

            try {
                List<String> comandos = interpretar(entrada);

                if (comandos.isEmpty()) {
                    continue;
                }

                String comando = comandos.get(0);

                switch (comando) {
                    case "mkdir":
                        exigirArgumentos(comandos, 2);
                        sistema.criarDiretorio(
                            caminho(comandos.get(1))
                        );
                        break;

                    case "create":
                        exigirArgumentos(comandos, 3);
                        sistema.criarArquivo(
                            caminho(comandos.get(1)),
                            comandos.get(2)
                        );
                        break;

                    case "copy":
                        if (comandos.size() < 3
                                || comandos.size() > 4) {
                            throw new IllegalArgumentException(
                                "Uso: copy origem destino [novoNome]"
                            );
                        }

                        String novoNome = comandos.size() == 4
                                ? comandos.get(3)
                                : null;

                        sistema.copiarArquivo(
                            caminho(comandos.get(1)),
                            caminho(comandos.get(2)),
                            novoNome
                        );
                        break;

                    case "rm":
                        exigirArgumentos(comandos, 2);
                        sistema.apagarArquivo(
                            caminho(comandos.get(1))
                        );
                        break;

                    case "rename":
                        exigirArgumentos(comandos, 3);
                        sistema.renomearArquivo(
                            caminho(comandos.get(1)),
                            comandos.get(2)
                        );
                        break;

                    case "rmdir":
                        exigirArgumentos(comandos, 2);
                        sistema.apagarDiretorio(
                            caminho(comandos.get(1))
                        );
                        break;

                    case "mvdir":
                        exigirArgumentos(comandos, 3);
                        sistema.renomearDiretorio(
                            caminho(comandos.get(1)),
                            comandos.get(2)
                        );
                        break;

                    case "cd":
                        exigirArgumentos(comandos, 2);
                        diretorioAtual = sistema.resolverDiretorio(
                            caminho(comandos.get(1))
                        );
                        break;

                    case "pwd":
                        exigirArgumentos(comandos, 1);
                        System.out.println(diretorioAtual);
                        break;

                    case "ls":
                        if (comandos.size() > 2) {
                            throw new IllegalArgumentException(
                                "Uso: ls [diretorio]"
                            );
                        }

                        String diretorio = comandos.size() == 2
                                ? caminho(comandos.get(1))
                                : diretorioAtual;

                        List<String> itens = sistema.listarDiretorio(diretorio);

                        if (itens.isEmpty()) {
                            System.out.println(
                                "Diretório vazio."
                            );
                        } else {
                            for (String item : itens) {
                                System.out.println(item);
                            }
                        }

                        break;

                    case "help":
                        mostrarAjuda();
                        break;

                    case "exit":
                        System.out.println("Encerrando simulador...");
                        return;

                    default: System.out.println("Comando desconhecido. Digite help.");
                }

            } catch (RuntimeException e) {
                System.out.println(
                    "Erro: " + e.getMessage()
                );
            }
        }
    }

    private List<String> interpretar(String entrada) {
        List<String> argumentos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean aspas = false;

        for (int i = 0; i < entrada.length(); i++) {
            char caractere = entrada.charAt(i);

            if (caractere == '"') {
                aspas = !aspas;

            } else if (Character.isWhitespace(caractere) && !aspas) {
                if (!atual.isEmpty()) {
                    argumentos.add(atual.toString());
                    atual.setLength(0);
                }

            } else {
                atual.append(caractere);
            }
        }

        if (aspas) {
            throw new IllegalArgumentException("Aspas não foram fechadas.");
        }

        if (!atual.isEmpty()) {
            argumentos.add(atual.toString());
        }

        return argumentos;
    }

    private void exigirArgumentos(List<String> comandos, int quantidade) {
        if (comandos.size() != quantidade) {
            throw new IllegalArgumentException(
                "Quantidade incorreta de argumentos."
            );
        }
    }

    private String caminho(String path) {
        String normalizado = path.replace("\\", "/");

        if (!normalizado.startsWith("/")) {
            normalizado = diretorioAtual.equals("/")
                    ? "/" + normalizado
                    : diretorioAtual + "/" + normalizado;
        }

        return normalizado;
    }

    private void revalidarDiretorioAtual() {
        try {
            diretorioAtual = sistema.resolverDiretorio(diretorioAtual);

        } catch (IllegalArgumentException e) {
            System.out.println(
                "Diretório atual não existe mais. Voltando para /."
            );
            diretorioAtual = "/";
        }
    }

    private void mostrarAjuda() {
        System.out.println("\nCOMANDOS DISPONIVEIS:");

        System.out.println(
            "mkdir <diretorio>             - Criar diretorio"
        );

        System.out.println(
            "create <arquivo> \"conteudo\"   - Criar arquivo"
        );

        System.out.println(
            "copy <origem> <destino>        - Copiar arquivo"
        );

        System.out.println(
            "copy <origem> <destino> <nome> - Copiar com novo nome"
        );

        System.out.println(
            "rm <arquivo>                  - Apagar arquivo"
        );

        System.out.println(
            "rename <arquivo> <nome>       - Renomear arquivo"
        );

        System.out.println(
            "rmdir <diretorio>             - Apagar diretorio vazio"
        );

        System.out.println(
            "mvdir <diretorio> <nome>      - Renomear diretorio"
        );

        System.out.println(
            "ls [diretorio]                - Listar conteudo"
        );

        System.out.println(
            "cd <diretorio>                - Mudar diretorio atual"
        );

        System.out.println(
            "pwd                           - Mostrar diretorio atual"
        );

        System.out.println(
            "help                          - Mostrar ajuda"
        );

        System.out.println(
            "exit                          - Encerrar simulador"
        );

        System.out.println();
    }
}