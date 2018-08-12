package hoo.etahk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.jetbrains.annotations.Contract;

import java.security.MessageDigest;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NwfbSecretCodeOrig {

    private static final String SHA256_INSTANCE = "SHA-256";

    // HASH.encodeHex
    @Contract("null -> null")
    private static String encodeHex(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(bArr.length * 2);
        for (byte b : bArr) {
            stringBuilder.append("0123456789ABCDEF".charAt((b & 240) >> 4));
            stringBuilder.append("0123456789ABCDEF".charAt(b & 15));
        }
        return stringBuilder.toString();
    }

    @NonNull
    private static String decodeIntArray(int[] iArr) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < iArr.length; i++) {
            if (i % 2 == 0) {
                stringBuilder.append(Character.toString((char) (iArr[i] - 35)));
            }
        }
        return stringBuilder.toString();
    }

    @NonNull
    private static SecretKeySpec getSecretKeySpec(String password) {
        if (password == null) {
            password = "";
        }
        StringBuilder stringBuilder = new StringBuilder(16);
        stringBuilder.append(password);
        while (stringBuilder.length() < 16) {
            stringBuilder.append("0");
        }
        if (stringBuilder.length() > 16) {
            stringBuilder.setLength(16);
        }
        byte[] key;
        try {
            key = stringBuilder.toString().getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            key = null;
        }
        return new SecretKeySpec(key, "AES");
    }

    @Nullable
    private static byte[] encrypt(byte[] data, String passwordForKey, String passwordForIvKey) {
        try {
            SecretKeySpec keySpec = getSecretKeySpec(passwordForKey);
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            instance.init(Cipher.ENCRYPT_MODE, keySpec, getIvParameterSpec(passwordForIvKey));
            return instance.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String encrypt(String data, String passwordForKey, String passwordForIvParam) {
        byte[] bytes;
        try {
            bytes = data.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            bytes = null;
        }
        return encodeHex(encrypt(bytes, passwordForKey, passwordForIvParam));
    }

    @NonNull
    private static IvParameterSpec getIvParameterSpec(String password) {
        if (password == null) {
            password = "";
        }
        StringBuilder stringBuilder = new StringBuilder(16);
        stringBuilder.append(password);
        while (stringBuilder.length() < 16) {
            stringBuilder.append("0");
        }
        if (stringBuilder.length() > 16) {
            stringBuilder.setLength(16);
        }
        byte[] key;
        try {
            key = stringBuilder.toString().getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            key = null;
        }
        return new IvParameterSpec(key);
    }

    private static byte[] encryptedStrToByteArray(String str) {
        int length = str.length();
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }

    public static String getCode() {
        // random
        StringBuilder random = new StringBuilder(Integer.toString(new Random().nextInt(10000)));
        while (random.length() < 5) {
            random.append("0");
        }

        // timestampStr
        String timestampStr = String.valueOf(System.currentTimeMillis() / 1000);
        StringBuilder stringBuilderT = new StringBuilder();
        stringBuilderT.append(timestampStr.substring(2, 3));
        stringBuilderT.append(timestampStr.substring(9, 10));
        stringBuilderT.append(timestampStr.substring(4, 5));
        stringBuilderT.append(timestampStr.substring(6, 7));
        stringBuilderT.append(timestampStr.substring(3, 4));
        stringBuilderT.append(timestampStr.substring(0, 1));
        stringBuilderT.append(timestampStr.substring(8, 9));
        stringBuilderT.append(timestampStr.substring(7, 8));
        stringBuilderT.append(timestampStr.substring(5, 6));
        stringBuilderT.append(timestampStr.substring(1, 2));
        timestampStr = stringBuilderT.toString();

        // str0 = nwstmwyinfopro -> siwmytnw
        String str0 = decodeIntArray(new int[]{145, 82, 154, 104, 150, 77, 151, 53, 144, 53, 154, 112, 156, 100, 140, 79, 145, 50, 137, 50, 146, 74, 147, 115, 149, 57, 146, 94});
        System.out.println("str0 B4 = " + str0);
        StringBuilder stringBuilder0 = new StringBuilder();
        stringBuilder0.append(str0.substring(2, 3));
        stringBuilder0.append(str0.substring(7, 8));
        stringBuilder0.append(str0.substring(5, 6));
        stringBuilder0.append(str0.substring(4, 5));
        stringBuilder0.append(str0.substring(6, 7));
        stringBuilder0.append(str0.substring(3, 4));
        stringBuilder0.append(str0.substring(0, 1));
        stringBuilder0.append(str0.substring(1, 2));
        str0 = stringBuilder0.toString();
        System.out.println("str0 AF = " + str0);

        try {
            MessageDigest instance = MessageDigest.getInstance(SHA256_INSTANCE);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(timestampStr);
            stringBuilder.append(str0);
            stringBuilder.append(random);
            instance.update(stringBuilder.toString().getBytes());
            str0 = encodeHex(instance.digest());
        } catch (Exception e) {
            e.printStackTrace();
            str0 = "";
        }

        // random = timestampStr + str0 + random
        StringBuilder stringBuilderT0R = new StringBuilder();
        stringBuilderT0R.append(timestampStr);
        stringBuilderT0R.append(str0.toLowerCase());
        random.insert(0, stringBuilderT0R.toString());

        // str1 = nwstmwyinfoprorts2 -> siwmytnwinfomwyy
        String str1 = decodeIntArray(new int[]{145, 53, 154, 104, 150, 106, 151, 107, 144, 90, 154, 56, 156, 100, 140, 80, 145, 104, 137, 89, 146, 113, 147, 71, 149, 120, 146, 87, 149, 56, 151, 96, 150, 81, 85, 71});
        System.out.println("str1 B4 = " + str1);
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append(str1.substring(2, 3));
        stringBuilder1.append(str1.substring(7, 8));
        stringBuilder1.append(str1.substring(5, 6));
        stringBuilder1.append(str1.substring(4, 5));
        stringBuilder1.append(str1.substring(6, 7));
        stringBuilder1.append(str1.substring(3, 4));
        stringBuilder1.append(str1.substring(0, 1));
        stringBuilder1.append(str1.substring(1, 2));
        stringBuilder1.append("infomwyy");
        str1 = stringBuilder1.toString();
        System.out.println("str1 AF = " + str1);

        // str1 = 02fd33eea60600f3 -> a20330efd3f6060e
        String str2 = decodeIntArray(new int[]{83, 108, 85, 52, 137, 71, 135, 89, 86, 51, 86, 101, 136, 118, 136, 94, 132, 63, 89, 90, 83, 94, 89, 118, 83, 109, 83, 74, 137, 52, 86, 115});
        System.out.println("str2 B4 = " + str2);
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(str2.substring(8, 9));
        stringBuilder2.append(str2.substring(1, 2));
        stringBuilder2.append(str2.substring(13, 14));
        stringBuilder2.append(str2.substring(15, 16));
        stringBuilder2.append(str2.substring(5, 6));
        stringBuilder2.append(str2.substring(0, 1));
        stringBuilder2.append(str2.substring(6, 7));
        stringBuilder2.append(str2.substring(2, 3));
        stringBuilder2.append(str2.substring(3, 4));
        stringBuilder2.append(str2.substring(4, 5));
        stringBuilder2.append(str2.substring(14, 15));
        stringBuilder2.append(str2.substring(9, 10));
        stringBuilder2.append(str2.substring(10, 11));
        stringBuilder2.append(str2.substring(11, 12));
        stringBuilder2.append(str2.substring(12, 13));
        stringBuilder2.append(str2.substring(7, 8));
        str2 = stringBuilder2.toString();
        System.out.println("str2 AF = " + str2);

        // [random] siwmytnwinfomwyy a20330efd3f6060e
        System.out.println(random.toString() + " " + str1 + " " + str2);
        return Base64.encodeToString(encryptedStrToByteArray(encrypt(random.toString(), str1, str2)), Base64.DEFAULT)
                    .replaceAll("\r", "")
                    .replaceAll("\n", "")
                    .replaceAll("=", "");
    }
}
