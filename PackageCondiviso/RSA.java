package PackageCondiviso;
import java.math.BigInteger;

public class RSA {
    BigInteger n;
    BigInteger e, d;

    public RSA()
    {
    }

    public RSA(BigInteger p, BigInteger q)
    {
        n = p.multiply(q); // Visto che usiamo BigInteger la moltiplicazione si esegue con questa funzione
        BigInteger phi;
        phi = (p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)));
        e = new BigInteger("3"); //e è inizializzata a 3
        while(e.gcd(phi).compareTo(BigInteger.ONE)!=0)//gcd calcola MCD
        {
            e = e.add(new BigInteger("2"));

        }

        //ed = 1 + k * phi
        //d = (1 + k * phi) / e -> d è intero -> (1 + k * phi) divisibile per e
        d = BigInteger.ONE; //iniziamo da k = 0 -> 1 + 0 * phi = 1

        while(d.remainder(e).compareTo(BigInteger.ZERO) != 0) //finchè d non è divisibile per e
            d = d.add(phi);

        d = d.divide(e);

        //System.out.println("e, d "+n+" "+phi+" "+e+" "+d);
    }

    public BigInteger getE()
    {
        return e;
    }

    public BigInteger getN()
    {
        return n;
    }

    public void set(BigInteger e, BigInteger n)
    {
        this.e = e;
        this.n = n;
    }

    public BigInteger crypt(BigInteger m)
    {
        return m.modPow(e, n);
    }

    public BigInteger decrypt(BigInteger c)
    {
        return c.modPow(d, n);
    }
}
