package com.easymeeting.utils;

public class TableSplitUtils {
    private static final String SPLIT_TABLE_MEETING_CHAT_MESSAGE = "meeting_chat_message";
    private static final String CREATE_TABLE_TEMP = "CREATE TABLE IF NOT EXISTS %s like %s";

    private static final Integer SPLIT_TABLE_COUNT = 32;

    public static String getCreateTableSql(String templateTableName, Integer tableIndex, Integer tableCount) {
        Integer padLen = String.valueOf(tableCount).length();
        String tableName = templateTableName + "_" + String.format("%0" + padLen + "d", tableIndex);
        return String.format(CREATE_TABLE_TEMP, tableName, templateTableName);
    }

    private static String getTableName(String prefix, Integer tableCount, String key) {
        int hashCode = Math.abs(murmurHash(key));
        int tableName = hashCode % tableCount + 1;
        int tableNumberLength = String.valueOf(tableCount).length();
        return prefix + "_" + String.format("%0" + tableNumberLength + "d", tableName);
    }

    public static String getMeetingChatMessageTable(String meetingId){
        return getTableName(SPLIT_TABLE_MEETING_CHAT_MESSAGE, SPLIT_TABLE_COUNT, meetingId);
    }

    private static void getSplitTableSql(){
        for (int i = 1; i <= SPLIT_TABLE_COUNT; i++) {
            String sql = getCreateTableSql(SPLIT_TABLE_MEETING_CHAT_MESSAGE, i, SPLIT_TABLE_COUNT);
        }
    }

    private static int murmurHash(String key) {
        final byte[] data = key.getBytes();
        final int length = data.length;
        final int seed = 0x9747b28c;
        final int m = 0x5bd1e995;
        final int r = 24;

        int h = seed ^ length;
        int len_4 = length >> 2;

        for (int i = 0; i < len_4; i++) {
            int i_4 = i << 2;
            int k = data[i_4 + 3];
            k = k << 8;
            k = k | (data[i_4 + 2] & 0xff);
            k = k << 8;
            k = k | (data[i_4 + 1] & 0xff);
            k = k << 8;
            k = k | (data[i_4 + 0] & 0xff);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        int len_m = len_4 << 2;
        int left = length - len_m;

        if (left != 0) {
            if (left >= 3) h ^= (data[length - 3] & 0xff) << 16;
            if (left >= 2) h ^= (data[length - 2] & 0xff) << 8;
            if (left >= 1) h ^= (data[length - 1] & 0xff);

            h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }
}
