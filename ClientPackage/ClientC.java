package ClientPackage;


import PackageCondiviso.Protocollo;
import PackageCondiviso.RSA;
import PackageCondiviso.Util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

//Gestisce la comunicazione con il server (gestisce l'invio e la ricezione dei messaggi)
public class ClientC {
    Socket connection;
    ObjectOutputStream out;
    ObjectInputStream in;

    //public static final String TAG_MSG = "msg"; messaggio normale (vedi protocollo), costante statica
    //public static final String TAG_SYS = "sys"; messaggio 'di sistema'

    //serve per decifrare messaggi ricevuti dal server
    RSA rsa;

    //serve per mandare messaggi criptati al server
    RSA rsaserver;

    public ClientC()
    {
        //generazione chiavi RSA
        BigInteger n = new BigInteger(300, new Random()).add(new BigInteger("2").pow(300));
        BigInteger p = Util.getNthPrime(n);
        n = n.add(new BigInteger(70, new Random())).add(BigInteger.ONE);
        BigInteger q = Util.getNthPrime(n);

        rsa = new RSA(p, q);
    }

    public void connect(String ip, int port) throws Exception //passiamo porta e ip a cui connettersi
    {
        connection = new Socket(InetAddress.getByName(ip), port); //crea connessione con server
        out = new ObjectOutputStream(connection.getOutputStream()); //crea stream per inviare dati
        out.flush();
        in = new ObjectInputStream(connection.getInputStream()); //crea stream per ricevere dati
        getMsg.start();

        send(Protocollo.TAG_SYS+"-pub-"+rsa.getE()+"-"+rsa.getN()); //comunichiamo al server la chiave pubblica
    }

    Thread getMsg = new Thread()
    {
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    String str = (String)in.readObject();
                    if(str.startsWith(Protocollo.TAG_CRI)) //il messaggio è cifrato
                    {
                        str = str.substring(str.indexOf('-')+1, str.length()); //togliamo il tag_cri
                        str = Util.toString(rsa.decrypt(new BigInteger(str)));
                    }
                    messageReceived(str);
                }catch(Exception e){}
            }
        }
    };

    public void send(String str) throws Exception
    {
        if(rsaserver != null) //abbiamo ricevuto la chiave, possiamo mandare messaggi criptati
        {
            str = Protocollo.TAG_CRI + "-" + rsaserver.crypt(Util.toBigInt(str));
        }

        out.writeObject(str);
        out.flush();
    }

    public void messageReceived(String str)
    {
        if(str.startsWith(Protocollo.TAG_SYS+"-pub")) //il server ci ha mandato le chiavi pubbliche
        {
            rsaserver = new RSA();
            String strs[] = str.split("-");
            rsaserver.set(new BigInteger(strs[2]), new BigInteger(strs[3]));
        }
    }

    public void close() throws Exception
    {
        send(Protocollo.TAG_SYS+"-close");
        getMsg.interrupt();
        in.close();
        out.close();
        connection.close();
    }
    }
