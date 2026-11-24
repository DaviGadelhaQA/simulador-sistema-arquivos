package simulador;

public class Main {
    public static void main(String[] args) {

        FileSystemSimulator sistema = new FileSystemSimulator();

        System.out.println("================================");
        System.out.println(" SIMULADOR DE SISTEMA DE ARQUIVOS");
        System.out.println("================================");

        try {
            sistema.criarDiretorio("/documentos");
            sistema.criarDiretorio("/imagens");

            sistema.criarArquivo(
                "/documentos/relatorio.txt",
                "Conteudo do relatorio."
            );

            sistema.criarArquivo(
                "/documentos/atividade.txt",
                "Atividade de Sistemas Operacionais."
            );

            sistema.copiarArquivo(
                "/documentos/relatorio.txt",
                "/imagens",
                null
            );

            sistema.renomearArquivo(
                "/documentos/atividade.txt",
                "trabalho.txt"
            );

            System.out.println("\nConteudo de /documentos:");

            for (String item :
                    sistema.listarDiretorio("/documentos")) {

                System.out.println(item);
            }

            sistema.renomearDiretorio(
                "/imagens",
                "fotos"
            );

            sistema.apagarArquivo(
                "/documentos/trabalho.txt"
            );

            System.out.println("\nConteudo da raiz:");

            for (String item : sistema.listarDiretorio("/")) {
                System.out.println(item);
            }

            System.out.println("\nTestes executados!");

        } catch (IllegalArgumentException e) {

            System.out.println("Erro: " + e.getMessage());

        }
    }
}