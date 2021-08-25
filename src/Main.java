import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;
import java.security.SecureRandom;

//Main Function
public class Main {

    private static SecureRandom random = new SecureRandom();
    private final static BigInteger one = new BigInteger("1");
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        //Finding large primes p and q
        BigInteger p = BigInteger.probablePrime(64, random);
        BigInteger q = BigInteger.probablePrime(64, random);

        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));

        //Finding an int e such that 1 < e < Phi(n) 	and gcd(e,Phi) = 1
        BigInteger e = generateE(phi);

        //Calculating d where  d ≡ e^(-1) (mod Phi(n))
        BigInteger d = extendedEuclid(e, phi)[1];

        // Printing the generated values for reference
        System.out.println("p: " + p);
        System.out.println("q: " + q);
        System.out.println("n: " + n);
        System.out.println("Phi: " + phi);
        System.out.println("e: " + e);
        System.out.println("d: " + d);

        //Reading the file
        Scanner scanner1 = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter plaintext file name: ");
        String filename = scanner1.nextLine();
        scanner1.close();
        File file = new File(filename);
        Scanner scanner2 = new Scanner(file);
        String text = "";
        while (scanner2.hasNextLine()) {
            text += scanner2.nextLine();
        }
        scanner2.close();
        //Converting text to BigInteger
        BigInteger textInt =  new BigInteger(text.getBytes("UTF-8"));
        //Encrypting the BigInteger
        String ciphertext = encrypt(textInt, e, n);
        //Converting ciphertext to hex and writing to file

        BufferedWriter out1 = new BufferedWriter(new FileWriter("ciphertext.txt"));
        out1.write(String.format("%040x", new BigInteger(1, ciphertext.getBytes())));
        out1.close();

        //Decrypting to BigInteger
        BigInteger decrypted = decrypt(ciphertext, d, n);
        //BigInteger to string
        String plaintext = new String(decrypted.toByteArray(),"UTF-8");
        //Writing original plaintext to file
        BufferedWriter out2 = new BufferedWriter(new FileWriter("original.txt"));
        out2.write(plaintext);
        out2.close();
    }

    //Encryption function
    public static String encrypt(BigInteger message, BigInteger e, BigInteger n) {
        String enc = "";
        String data = message.toString();
        int len = data.length();
        for(int i=0; i<len; i+=12) {
            String str;
            try {
                str = data.substring(i, i+12);
            } catch(Exception ex) {
                str = data.substring(i, i+len%i);
            }
            BigInteger s = new BigInteger(str);
            String padded_string = pad((s.modPow(e, n)).toString(2));
            enc += padded_string;
        }
        return enc;
    }

    //Decryption function
    public static BigInteger decrypt(String ciphertext, BigInteger d, BigInteger n) {
        String dec = "";
        for(int i=0; i<ciphertext.length(); i+=256) {
            String str_256 = ciphertext.substring(i,i+256);
            BigInteger s = new BigInteger(str_256, 2);
            String out = (s.modPow(d, n)).toString();
            if (i >= ciphertext.length() - 256) {
                dec += out;
            } else {
                dec += padC(out);
            }
        }
        return new BigInteger(dec);
    }

    //Padding bits function
    public static String pad(String bits) {
        String padded = String.format("%256s", bits).replace(' ', '0');
        return padded;
    }

    //Padding decimal integer function
    public static String padC(String num) {
        while(num.length()%12 != 0){
            num = "0" + num;
        }
        return num;
    }

    //Finding Co-primes to get 'e'
    public static BigInteger generateE(BigInteger phi) {
        SecureRandom rand = new SecureRandom();
        BigInteger e = new BigInteger(64, rand);
        do {
            e = new BigInteger(64, rand);
            while (e.min(phi).equals(phi)) { // while phi is smaller than e, look for a new e
                e = new BigInteger(64, rand);
            }
        } while (!gcd(e, phi).equals(BigInteger.ONE)); // if gcd(e,phi) isnt 1 then stay in loop
        return e;
    }

    //Greatest common divisor function
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return a;
        } else {
            return gcd(b, a.mod(b));
        }
    }

    //Extended Euclidean algorithm
    public static BigInteger[] extendedEuclid(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) return new BigInteger[] {
                a, BigInteger.ONE, BigInteger.ZERO
        };
        BigInteger[] val = extendedEuclid(b, a.mod(b));
        BigInteger d = val[0];
        BigInteger p = val[2];
        BigInteger q = val[1].subtract(a.divide(b).multiply(val[2]));
        return new BigInteger[] {d, p, q};
    }

}