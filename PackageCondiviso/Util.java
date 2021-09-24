package PackageCondiviso;
//Lo usiamo per la generazione dei numeri primi casuali ultilizzato in ClientC e ServerC

import java.math.BigInteger;
public class Util {
    //numero di primi in [1, n] è ~n/logn
    //numero di primi in [1, nlogn] è ~n
    public static BigInteger getNthPrime(BigInteger n)
    {
        //x = logn
        //2^x = n -> iteriamo su x
        int x = 1;
        BigInteger b = new BigInteger("2");
        while(b.pow(x+1).compareTo(n) < 0)
            x++;

        BigInteger nlogn = n.multiply(new BigInteger(x+""));
        while(!nlogn.isProbablePrime(100)) //restituisce se è primo con una certa probabilità (100 non è certezza!)
            nlogn = nlogn.add(BigInteger.ONE);

        return nlogn;
    }

    public static boolean isPrime(BigInteger bi)
    {
        BigInteger i = new BigInteger("2");
        while(i.multiply(i).compareTo(bi) <= 0)
        {
            if(bi.remainder(i).compareTo(BigInteger.ZERO) == 0)
                return false;
            else
                i = i.add(BigInteger.ONE);
        }
        return true;
    }

    //converte stringa in bigint (la stringa è come un numero in base 256)
    public static BigInteger toBigInt(String str)
    {
        //1110 -> 0*256^0 +
        BigInteger base = new BigInteger("1");
        BigInteger somma = BigInteger.ZERO;
        for(int i=str.length()-1; i>=0; i--)
        {
            BigInteger act = new BigInteger((int)str.charAt(i)+""); //prendiamo carattere attuale dalla stringa
            somma = somma.add(base.multiply(act));
            base = base.multiply(new BigInteger("256"));
        }
        return somma;
    }

    //converte bigint in stringa
    public static String toString(BigInteger bi)
    {
        String res = "";

        while(bi.compareTo(BigInteger.ZERO) != 0)
        {
            int resto = bi.remainder(new BigInteger("256")).intValue();
            res += ((char)resto); //concatena carattere alla fine della stringa
            bi = bi.divide(new BigInteger("256"));
        }

        String inv = "";
        for(int i=res.length()-1; i>=0; i--)
            inv += res.charAt(i);

        return inv;
    }
}
