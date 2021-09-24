package PackageServer;
import java.util.Scanner;

public class Server {
    public static void main(String [] args)
    {
        ServerC s = new ServerC();
        try
        {
            s.open(2432);
            System.out.println("server pronto");
            String str = "";
            Scanner sc = new Scanner(System.in);
            while(!str.equals("exit"))
            {
                str = sc.nextLine();
                try
                {
                    s.send(str, 0);
                }catch(Exception e){ System.out.println("non sono riuscito ad inviare"); }
            }

            sc.close();

            s.close();
        }catch(Exception e)
        {
            System.out.println("Errori con connessione");
        }
    }
}
