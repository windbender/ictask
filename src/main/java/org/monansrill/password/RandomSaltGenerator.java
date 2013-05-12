package org.monansrill.password;

import java.util.Random;

/**
 * Generates a salt using a static instance of {@link Random}. Because Random is thread-safe,
 * salt generation using this class is also thread-safe.
 * <p/>
 * Clients should not use this class directly, but pass an instance to {@link DefaultPasswordEncoder}.
 */
public final class RandomSaltGenerator implements SaltGenerator
{
    private final static Random random = new Random();

    /**
     * @inheritDoc
     */
    public byte[] generateSalt(int length)
    {
        byte[] result = new byte[length];
        random.nextBytes(result);
        return result;
    }
}
