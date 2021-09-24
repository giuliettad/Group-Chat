//Gestisce la comunicazione con i client (gestisce l'invio e la ricezione dei messaggi)
package PackageServer;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;
import PackageCondiviso.Protocollo;
import PackageCondiviso.RSA;
import PackageCondiviso.Util;

public class ServerC {
    ServerSocket conSock;
    HashMap<Integer, DataSocket> dataSocks;
    int idact;

    //rsa usato per decriptare messaggi del client
    RSA rsa;

    public ServerC()
    {
        idact = 0;
        dataSocks = new HashMap<Integer, DataSocket>();

        BigInteger n = new BigInteger(300, new Random()).add(new BigInteger("2").pow(300));
        BigInteger p = Util.getNthPrime(n);
        n = n.add(new BigInteger(70, new Random())).add(BigInteger.ONE);
        BigInteger q = Util.getNthPrime(n);

        rsa = new RSA(p, q);
    }

    public void open(int port) throws Exception
    {
        conSock = new ServerSocket(port, 100);
        acceptThread.start();
    }

    //CREAZIONE del thread che gestisce l'accept
    Thread acceptThread = new Thread()
    {
        @Override
        public void run()
        {
            while(true) //SEMPRE in ascolto
            {
                try
                {
                    Socket tmp = conSock.accept(); // con accept accettiamo un nuovo client e creiamo il datasocket che serve a inviare e ricevere i dati da quel client specifico
                    DataSocket s = new DataSocket(tmp, ServerC.this, idact);//dal socket dell'accept creiamo il datasocket (gli passiamo anche il server su cui sta girando il thread) e l'id attuale

                    synchronized(dataSocks) //sincronizzo hashmap
                    {
                        dataSocks.put(idact, s); //associamo al socket 's' l'id 'idact' (FISSO!)
                        clientAccepted(idact);
                        idact++;
                    }
                }catch(Exception e){ System.out.println(e.getMessage()); e.printStackTrace(); }
            }
        }
    };

    public void send(String msg, int ind) throws Exception
    {
        dataSocks.get(ind).sendMsg(msg); //invia al client con indice 'ind' il messaggio
    }

    //metodo richiamato dal datasocket nel thread che riceve i messaggi
    public void messageReceived(String str, int id) throws Exception
    {
        if(str.startsWith(Protocollo.TAG_CRI)) //se abbiamo ricevuto un messaggio criptato
        {
            str = str.substring(str.indexOf('-')+1, str.length());
            str = Util.toString(rsa.decrypt(new BigInteger(str))); //non rifacciamo conversione con util perchè il messaggio è già un biginteger (però possiamo inviare solo stringhe)
        }

        String tag = str.substring(0, str.indexOf('-')); //prendiamo sottostringa da 0 a primo indice di '-' (il tag)
        String msg = str.substring(str.indexOf('-')+1, str.length());  //prendiamo messaggio vero e proprio

        //if(tag.equals(ClientC.TAG_MSG)) //server stampa il messaggio
        //System.out.println(msg);
        if(tag.equals(Protocollo.TAG_SYS))
        {
            if(msg.equals("close")) //ricevuto comando di chiusura... chiudo chi ha inviato il messaggio
            {
                synchronized(dataSocks) //chiamato dal thread di datasocket... potrei essere in parallelo
                {
                    clientDisconnected(id);
                    dataSocks.get(id).close();
                    dataSocks.remove(id);
                }
            }
            else //abbiamo ricevuto le chiavi pubbliche
            {
                final String strs [] = msg.split("-"); //estrapoliamo chiavi pubbliche dal messaggio
                synchronized(dataSocks)
                { //otteniamo {pub, e, n}
                    dataSocks.get(id).rsa = new RSA();
                    dataSocks.get(id).rsa.set(new BigInteger(strs[1]), new BigInteger(strs[2]));
                }
            }
        }

        if(tag.equals(Protocollo.TAG_MSG))
            for(Entry<Integer, DataSocket> s : dataSocks.entrySet()) //quando riceviamo il messaggio lo rigiriamo a tutti quanti
                if(s.getKey() != id)
                    s.getValue().sendMsg(Protocollo.TAG_MSG+"-"+"Client "+id+": "+msg);
    }

    public void clientAccepted(int id) throws Exception
    {
        System.out.println("Connessione di client "+id);

        dataSocks.get(id).sendMsg(Protocollo.TAG_SYS+"-pub-"+rsa.getE()+"-"+rsa.getN()); //quando un client si connette gli inviamo le chiavi pubbliche del server
    }

    public void clientDisconnected(int id)
    {
        System.out.println("Disconnessione di client "+id);
    }

    public void close() throws Exception
    {
        acceptThread.interrupt();
        for(Entry<Integer, DataSocket> s : dataSocks.entrySet()) //scorri l'hashmap, chiudi tutti i datasocket
            s.getValue().close();

        conSock.close();
        System.exit(0);
    }
}
