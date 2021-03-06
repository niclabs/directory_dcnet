package crypto;

import java.math.BigInteger;
import java.util.Random;

public class PedersenCommitment {

    private final int roomSize;
    private int messageSize;

    private BigInteger g, h;
    private BigInteger q, p;

    private int padLength;

    public PedersenCommitment(int messageSize, int padLength, int roomSize) {
        this.messageSize = messageSize;
        this.padLength = padLength;
        this.roomSize = roomSize;

        // Generate prime q bigger than the message and prime p s.t. q|(p-1)
        this.q = generateQ(messageSize, roomSize);
        this.p = generateP();

        // Generate generators {g,h} of group G_q
        this.g = findGenerator();
        this.h = findGenerator();
    }

    public PedersenCommitment(int roomSize, BigInteger g, BigInteger h, BigInteger q, BigInteger p, int padLength) {
        this.roomSize = roomSize;
        this.g = g;
        this.h = h;
        this.q = q;
        this.p = p;
        this.padLength = padLength;
    }

    private BigInteger generateQ(int messageSize, int roomSize) {
        int maxMessageLength = (int) ((Math.log(roomSize + 1)/Math.log(2))*3 + (messageSize + this.padLength)*8 + 1);
        BigInteger _a = new BigInteger(maxMessageLength, new Random());
        while (_a.bitLength() < maxMessageLength) {
            _a = new BigInteger(maxMessageLength, new Random());
        }
        return _a.nextProbablePrime();
    }

    private BigInteger generateP() {
        int i = 1;
        BigInteger p = this.q.multiply(new BigInteger("" + i)).add(BigInteger.ONE);
        while (true) {
            int CERTAINTY = 100;
            if (p.isProbablePrime(CERTAINTY))
                break;
            p = this.q.multiply(new BigInteger("" + i)).add(BigInteger.ONE);
            i++;
        }
        return p;
    }

    private BigInteger findGenerator() {
        // Select a random possible <generator> in Z_p
        BigInteger generator = new BigInteger(this.p.bitCount(), new Random()).mod(this.p);
        BigInteger result;
        while (true) {
            // Check that <generator> is not 1 and in Z_p*
            if (!generator.equals(BigInteger.ONE) && generator.gcd(this.p).compareTo(BigInteger.ONE) == 0) {
                // Check that <generator> is in G_q
                result = generator.modPow(this.q, this.p);
                if (result.equals(BigInteger.ONE)) {
                    break;
                }
            }
            // Try with another possible generator
            generator = new BigInteger(this.p.bitCount(), new Random()).mod(this.p);
        }
        return generator;
    }

    private BigInteger generateRandom() {
        // Generate random in Z_q
        BigInteger random = new BigInteger(this.q.bitCount(), new Random());
        return random.mod(this.q);
    }

    public BigInteger calculateCommitment(BigInteger secret) {
        return this.g.modPow(secret, this.p).multiply(this.h.modPow(generateRandom(), this.p)).mod(this.p);
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getH() {
        return h;
    }

    public BigInteger getQ() {
        return q;
    }

    public BigInteger getP() {
        return p;
    }

    private BigInteger myPow(BigInteger base, BigInteger exponent) {
        BigInteger result = BigInteger.ONE;
        while (exponent.signum() > 0) {
            if (exponent.testBit(0)) result = result.multiply(base);
            base = base.multiply(base);
            exponent = exponent.shiftRight(1);
        }
        return result;
    }

}
