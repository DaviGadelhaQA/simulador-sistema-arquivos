package simulador;

public class TestePersistencia {
    public static void main(String[] args) {
        System.out.println("==============================");
        System.out.println(" TESTE DE PERSISTENCIA");
        System.out.println("==============================");

        FileSystemSimulator sistema = new FileSystemSimulator();

        System.out.println("\nConteudo da raiz:");

        for (String item : sistema.listarDiretorio("/")) {
            System.out.println(item);
        }

        System.out.println("\nConteudo de /documentos:");

        for (String item : sistema.listarDiretorio("/documentos")) {
            System.out.println(item);
        }

        System.out.println("\nConteudo de /fotos:");

        for (String item : sistema.listarDiretorio("/fotos")) {
            System.out.println(item);
        }

        System.out.println("\nTeste de persistencia concluido!");
    }
}