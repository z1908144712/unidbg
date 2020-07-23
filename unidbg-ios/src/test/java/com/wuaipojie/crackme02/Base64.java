package com.wuaipojie.crackme02;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * base64 工具类
 */
public class Base64 {
    private static String alphabet = "AzSxleoQp02MtvisIZUF8ThRaEL9Nd57qG6DfOkW4JHXmYjwV1Pn3uycrCgbKB-_";
    private static final int RANGE = 0xff;
    private static char[] base64ByteToStr;
    private static byte[] strToBase64Byte = new byte[128];

    static {
        base64ByteToStr = alphabet.toCharArray();

        for (int i = 0; i <= strToBase64Byte.length - 1; i++) {
            strToBase64Byte[i] = -1;
        }
        for (int i = 0; i <= base64ByteToStr.length - 1; i++) {
            strToBase64Byte[base64ByteToStr[i]] = (byte) i;
        }
    }

    /**
     * 加密
     *
     * @param bytesToEncode
     * @return
     */
    public static byte[] encode(byte[] bytesToEncode) {
        StringBuilder res = new StringBuilder();
        //per 3 bytes scan and switch to 4 bytes
        for (int i = 0; i <= bytesToEncode.length - 1; i += 3) {
            byte[] enBytes = new byte[4];
            byte tmp = (byte) 0x00;// save the right move bit to next position's bit
            //3 bytes to 4 bytes
            for (int k = 0; k <= 2; k++) {// 0 ~ 2 is a line
                if ((i + k) <= bytesToEncode.length - 1) {
                    enBytes[k] = (byte) (((((int) bytesToEncode[i + k] & RANGE) >>> (2 + 2 * k))) | (int) tmp);//note , we only get 0 ~ 127 ???
                    tmp = (byte) (((((int) bytesToEncode[i + k] & RANGE) << (2 + 2 * (2 - k))) & RANGE) >>> 2);
                } else {
                    enBytes[k] = tmp;
                    tmp = (byte) 64;//if tmp > 64 then the char is '=' hen '=' -> byte is -1 , so it is EOF or not print char
                }
            }
            enBytes[3] = tmp;//forth byte
            //4 bytes to encode string
            for (int k = 0; k <= 3; k++) {
                if ((int) enBytes[k] <= 63) {
                    res.append(base64ByteToStr[(int) enBytes[k]]);
                } else {
                    res.append('=');
                }
            }
        }
        return res.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 解密
     *
     * @param bytesEncoded
     * @return
     */
    public static byte[] decode(byte[] bytesEncoded) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();//destination bytes, valid string that we want
        byte[] base64bytes = new byte[bytesEncoded.length];
        //get the base64 bytes (the value is -1 or 0 ~ 63)
        for (int i = 0; i <= bytesEncoded.length - 1; i++) {
            int ind = (int) bytesEncoded[i];
            base64bytes[i] = strToBase64Byte[ind];
        }
        //base64 bytes (4 bytes) to normal bytes (3 bytes)
        for (int i = 0; i <= base64bytes.length - 1; i += 4) {
            byte[] deBytes = new byte[3];
            int delen = 0;// if basebytes[i] = -1, then debytes not append this value
            byte tmp;
            for (int k = 0; k <= 2; k++) {
                if ((i + k + 1) <= base64bytes.length - 1 && base64bytes[i + k + 1] >= 0) {
                    tmp = (byte) (((int) base64bytes[i + k + 1] & RANGE) >>> (2 + 2 * (2 - (k + 1))));
                    deBytes[k] = (byte) ((((int) base64bytes[i + k] & RANGE) << (2 + 2 * k) & RANGE) | (int) tmp);
                    delen++;
                }
            }
            for (int k = 0; k <= delen - 1; k++) {
                bos.write((int) deBytes[k]);
            }
        }
        return bos.toByteArray();
    }
}
