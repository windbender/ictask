package org.monansrill.password;

import static org.monansrill.password.StringUtils.getBytesUtf8;
import static org.monansrill.password.StringUtils.newStringUtf8;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

/**
 * Converts salt and encoded password bytes into a standard base64 encoding
 * for storage. Strings are converted to and from bytes using the UTF-8 encoding.
 * A prefix is added in braces (e.g. "{SHA}") to distinguish between different
 * implementations.
 * <p/>
 * <b>It is strongly recommended that clients use the default implementation returned
 * by {@link #getDefaultInstance()}, which uses {@link PKCS5S2PasswordHashGenerator}
 * with {@link RandomSaltGenerator}.</b>
 * <p/>
 * The storage format used by this class is {@code "{" + identifier + "}" + encodedSaltAndHash},
 * where identifier and saltPlusHash are defined as follows:
 * <ul>
 * <li>{@code identifier}: the identifier string provided to the constructor</li>
 * <li>{@code encodedSaltAndHash}: the result of {@code new String(encodeBase64(saltAndHash), "UTF-8")}</li>
 * <li>{@code encodeBase64}: the result of {@link Base64#encodeBase64(byte[]) encodeBase64(saltAndHash)}</li>
 * <li>{@code saltAndHash}: the result of {@link ArrayUtils#add(byte[], byte) ArrayUtils.add(salt, hash)}</li>
 * <li>{@code salt}: the result of {@link SaltGenerator#generateSalt(int)}</li>
 * <li>{@code hash}: the result of {@link PasswordHashGenerator#generateHash(byte[], byte[]) passwordHashGenerator.generateHash(password.getBytes("UTF-8"), salt)}</li>
 * </ul>
 * <p/>
 * Clients must provide an identifier, hash generator and salt generator in the
 * constructor, or use the default implementation returned by {@link #getDefaultInstance()}.
 * <p/>
 * The thread-safety of this encoder depends on the thread-safety of the hash and salt generators
 * used. The encoder returned by {@link #getDefaultInstance()} is safe for use on multiple threads.
 *
 * @see Base64#encodeBase64(byte[])
 * @see PasswordHashGenerator
 * @see SaltGenerator
 */
public final class DefaultPasswordEncoder implements PasswordEncoder
{
    private static volatile PasswordEncoderDecorator DEFAULT_INSTANCE
            = new PasswordEncoderDecorator (new DefaultPasswordEncoder("PKCS5S2", new PKCS5S2PasswordHashGenerator(), new RandomSaltGenerator()));

    private static final int DEFAULT_SALT_LENGTH_BYTES = 16;

    private final String prefix;
    private final PasswordHashGenerator hashGenerator;
    private final SaltGenerator saltGenerator;

    public static PasswordEncoder getDefaultEncoder ()
    {
        return DEFAULT_INSTANCE.getEncoder();
    }

    public static void setDefaultEncoder (PasswordEncoder passwordEncoder)
    {
        DEFAULT_INSTANCE.setEncoder (passwordEncoder);
    }
    /**
     * Returns a new encoder with identifier "PKCS5S2" using {@link PKCS5S2PasswordHashGenerator}
     * as the hash generator and {@link RandomSaltGenerator} as the salt generator.
     * <p/>
     * This instance is safe for use by multiple threads.
     *
     * @see PKCS5S2PasswordHashGenerator
     * @see RandomSaltGenerator
     */
    public static PasswordEncoder getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }

    /**
     * Returns a new encoder with specified identifier and hash generator, using{@link RandomSaltGenerator}
     * as the salt generator.
     * <p/>
     * The thread-safety of this instance depends on the thread-safety of the hash generator implementation.
     */
    public static PasswordEncoder newInstance(String identifier, PasswordHashGenerator hashGenerator)
    {
        return new DefaultPasswordEncoder(identifier, hashGenerator, new RandomSaltGenerator());
    }

    /**
     * Constructs a new encoder with specified identifier, hash generator and salt generator.
     * <p/>
     * The thread-safety of this instance depends on the thread-safety of the hash and salt generator implementations.
     */
    public DefaultPasswordEncoder(String identifier, PasswordHashGenerator hashGenerator, SaltGenerator saltGenerator)
    {
        this.prefix = "{" + identifier + "}";
        this.hashGenerator = hashGenerator;
        this.saltGenerator = saltGenerator;
    }

    /**
     * @inheritDoc
     */
    public final boolean canDecodePassword(String encodedPassword)
    {
        return encodedPassword != null && encodedPassword.startsWith(prefix);
    }

    /**
     * @inheritDoc
     */
    public final String encodePassword(String rawPassword) throws IllegalArgumentException
    {
        Validate.notEmpty(rawPassword, "Password must not be empty");

        byte[] salt = saltGenerator.generateSalt(getSaltLength());
        byte[] hash = hashGenerator.generateHash(getBytesUtf8(rawPassword), salt);
        String encodedPassword = toEncodedForm(salt, hash);
        return prependPrefix(encodedPassword);
    }

    private int getSaltLength()
    {
        if (hashGenerator.getRequiredSaltLength() > 0)
            return hashGenerator.getRequiredSaltLength();
        return DEFAULT_SALT_LENGTH_BYTES;
    }

    /**
     * @inheritDoc
     */
    public final boolean isValidPassword(String rawPassword, String prefixedEncodedPassword) throws IllegalArgumentException
    {
        Validate.notNull(rawPassword);
        Validate.notNull(prefixedEncodedPassword);
        if (!canDecodePassword(prefixedEncodedPassword))
        {
            return false;
        }

        String encodedPassword = removePrefix(prefixedEncodedPassword);
        byte[] storedBytes = fromEncodedForm(encodedPassword);

        int saltLength = getSaltLength();
        byte[] salt = ArrayUtils.subarray(storedBytes, 0, saltLength);
        byte[] storedHash = ArrayUtils.subarray(storedBytes, saltLength, storedBytes.length);

        byte[] hashAttempt = hashGenerator.generateHash(getBytesUtf8(rawPassword), salt);

        return Arrays.equals(storedHash, hashAttempt);
    }

    private String prependPrefix(String encodedPassword)
    {
        return prefix + encodedPassword;
    }

    private String removePrefix(String encodedPassword)
    {
        return encodedPassword.substring(prefix.length());
    }

    private byte[] fromEncodedForm(String encodedPassword)
    {
        return Base64.decodeBase64(getBytesUtf8(encodedPassword));
    }

    private String toEncodedForm(byte[] salt, byte[] hash)
    {
        byte[] saltAndHash = ArrayUtils.addAll(salt, hash);
        byte[] base64 = Base64.encodeBase64(saltAndHash);
        return newStringUtf8(base64);
    }
}
