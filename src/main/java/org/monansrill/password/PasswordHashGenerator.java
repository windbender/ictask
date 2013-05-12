package org.monansrill.password;                     

/**
 * Used by {@link DefaultPasswordEncoder} to generate encoded password bytes.
 */
public interface PasswordHashGenerator
{
    /**
     * Encodes the rawPassword bytes and salt according to their method specified
     * by the implementation. The encoded bytes should be returned without
     * including the salt.
     *
     * @param password the password to be encoded
     * @param salt a salt which may be used by some algorithms
     * @return the encoded password bytes (without appending or prepending the salt)
     */
    byte[] generateHash(byte[] password, byte[] salt);

    /**
     * Returns the required salt length for this hash generator, zero if this generator does not
     * require a salt or -1 if the generator accepts salts of varying length.
     */
    int getRequiredSaltLength();
}
