package org.monansrill.password;

/**
 * Used by {@link DefaultPasswordEncoder} to generate a password salt of a desired length.
 * <p/>
 * A password salt is a random value used when encoding passwords to ensure that encoding
 * the same password multiple times gives a different encoded value each time. The salt must
 * be available later when verifying the password, so in some implementations it is stored with
 * the password.
 */
public interface SaltGenerator
{
    /**
     * Returns a byte array of the required length containing a random salt value.
     */
    byte[] generateSalt(int length);
}
