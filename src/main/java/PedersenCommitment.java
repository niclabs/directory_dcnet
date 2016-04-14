import java.math.BigInteger;
import java.util.Random;

class PedersenCommitment {

    private int messageSize;

    private BigInteger g, h;
    private BigInteger q, p;

    int padLength;

    PedersenCommitment(int messageSize, int padLength) {
        this.messageSize = messageSize;
        this.padLength = padLength;

        // Generate prime q bigger than the message and prime p s.t. q|(p-1)
        this.q = generateQ(messageSize);
        this.p = generateP();

        // Generate generators {g,h} of group G_q
        this.g = findGenerator();
        this.h = findGenerator();
    }

    public PedersenCommitment(BigInteger g, BigInteger h, BigInteger q, BigInteger p, int padLength) {
        this.g = g;
        this.h = h;
        this.q = q;
        this.p = p;
        this.padLength = padLength;
    }

    private BigInteger generateQ(int messageSize) {
        int maxMessageLength = (messageSize + padLength + 4)*8 + 1;
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

    BigInteger getG() {
        return g;
    }

    BigInteger getH() {
        return h;
    }

    BigInteger getQ() {
        return q;
    }

    BigInteger getP() {
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
