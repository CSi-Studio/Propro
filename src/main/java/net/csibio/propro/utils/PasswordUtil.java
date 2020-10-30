package net.csibio.propro.utils;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;

public class PasswordUtil {

    public static String getRandomSalt() {
        return new SecureRandomNumberGenerator().nextBytes().toHex();
    }

    public static String getHashPassword(String password, String salt) {
        return new Md5Hash(password, salt, 3).toString();
    }
}
