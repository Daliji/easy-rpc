package com.codelifeliwan.rpc.samples.models.server;

import java.util.Arrays;

/**
 * 测试复杂类型
 */
public class SampleDoSort {
    public int[] sort(int[] data) {
        Arrays.sort(data);
        return data;
    }
}
