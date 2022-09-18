package com.usst.kgfusion.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @program: kgfusion
 * @description: 生成一些东西
 * @author: JH_D
 * @create: 2022-01-10 11:52
 **/

public class GenerateUtil {
    public static Long generateUniqueId() {
        Long val = -1L;
        final UUID uid = UUID.randomUUID();
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uid.getLeastSignificantBits());
        buffer.putLong(uid.getMostSignificantBits());
        final BigInteger bi = new BigInteger(buffer.array());
        val = bi.longValue() & Long.MAX_VALUE;
        return val;
    }

    public static void main(String[] args) {
        System.out.println(generateUniqueId());
    }
}
