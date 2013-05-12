package org.monansrill.password;

/**
 * Created by rshuttleworth @ 16/02/11 4:58 PM
 */
public class PasswordEncoderDecorator implements PasswordEncoder
{
    private volatile PasswordEncoder passwordEncoder;

    public PasswordEncoderDecorator (PasswordEncoder passwordEncoder)
    {
        setEncoder (passwordEncoder);
    }

    public void setEncoder (PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public PasswordEncoder getEncoder ()
    {
        return passwordEncoder;
    }

    public String encodePassword(String rawPassword) throws IllegalArgumentException
    {
        return passwordEncoder.encodePassword (rawPassword);
    }

    public boolean isValidPassword(String rawPassword, String encodedPassword) throws IllegalArgumentException
    {
        return passwordEncoder.isValidPassword (rawPassword, encodedPassword);
    }

    public boolean canDecodePassword(String encodedPassword)
    {
        return passwordEncoder.canDecodePassword (encodedPassword);
    }
}
