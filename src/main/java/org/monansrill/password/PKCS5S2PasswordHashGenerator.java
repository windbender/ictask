package org.monansrill.password;

import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * Encodes passwords using PKCS 5 version 2, as published by RSA and implemented in BouncyCastle.
 * The iteration count is 10000 and the provided salt must be exactly 16 bytes. The generated hash
 * will be 256 bits (32 bytes) long.
 * <p/>
 * Clients should not use this class directly, but pass an instance to {@link DefaultPasswordEncoder}.
 * <p/>
 * This generator is safe for use on multiple threads because it creates a new instance of
 * the underlying generator for each call to {@link #generateHash(byte[], byte[])}.
 *
 * @see <a href="http://www.rsa.com/rsalabs/node.asp?id=2127">RSA specification</a>
 * @see PKCS5S2ParametersGenerator
 */
public final class PKCS5S2PasswordHashGenerator implements PasswordHashGenerator
{
    private static final int ITERATION_COUNT = 10000;
    private static final int OUTPUT_SIZE_BITS = 256;
    private static final int SALT_LENGTH = 16;

    /**
     * @inheritDoc
     */
    public byte[] generateHash(byte[] rawPassword, byte[] salt)
    {
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator();

        generator.init(rawPassword, salt, ITERATION_COUNT);
        KeyParameter output = (KeyParameter) generator.generateDerivedMacParameters(OUTPUT_SIZE_BITS);

        return output.getKey();
    }

    /**
     * @inheritDoc
     */
    public int getRequiredSaltLength()
    {
        return SALT_LENGTH;
    }
}
