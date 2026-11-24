
package simulador;

public class Main {

    public static void main(String[] args) {

        try {

            FileSystemSimulator sistema =
                    new FileSystemSimulator();

            Shell shell = new Shell(sistema);

            shell.executar();

        } catch (Exception e) {

            System.out.println(
                "Erro ao iniciar o simulador: "
                + e.getMessage()
            );
        }
    }
}