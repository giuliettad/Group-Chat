package PackageServer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

import PackageCondiviso.Protocollo;
import PackageCondiviso.RSA;
import PackageCondiviso.Util;

//creiamo la classe DataSocket per non avere 3 arrayList nel SeverC
public class DataSocket {
    Socket sock;
    ObjectOutputStream out;
    ObjectInputStream in;
    ServerC server;
    int id;

    //rsa usato per inviare messaggi cifrati al client
    RSA rsa;

    public DataSocket(Socket s, ServerC s2, int id) throws Exception //il socket del parametro è quello generato dall'accept
    {
        sock = s;
        out = new ObjectOutputStream(s.getOutputStream()); //creazione stream per inviare
        out.flush();
        in = new ObjectInputStream(s.getInputStream()); //creazione stream per ricevere
        server = s2;
        this.id = id;
        getMsg.start();
    }

    public void sendMsg(String str) throws Exception
    {
        if(rsa != null) //abbiamo ricevuto chiavi dal client
        {
            str = Protocollo.TAG_CRI + "-" + rsa.crypt(Util.toBigInt(str));
        }

        out.writeObject(str);
        out.flush();
    }

    Thread getMsg = new Thread() //thread per ricevere messaggi (sul data socket, perchè è qui che abbiamo gli stream)
    {
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    String str = (String)in.readObject();

                    synchronized(server) //un solo thread alla volta accederà al server (al parametro della synchronized)
                    {
                        server.messageReceived(str, id);
                    }
                }catch(Exception e){}
            }
        }
    };

    public void close() throws Exception
    {
        getMsg.interrupt();
        out.close();
        in.close();
        sock.close();
    }
}
