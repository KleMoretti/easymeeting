package com.easymeeting.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableSplitUtilsTest {

    @Test
    void getCreateTableSqlPadsTableIndexUsingTableCountWidth() {
        String sql = TableSplitUtils.getCreateTableSql("meeting_chat_message", 3, 32);

        assertEquals("CREATE TABLE IF NOT EXISTS meeting_chat_message_03 like meeting_chat_message", sql);
    }

    @Test
    void getCreateTableSqlExpandsPaddingForLargerTableCounts() {
        String sql = TableSplitUtils.getCreateTableSql("meeting_chat_message", 7, 128);

        assertEquals("CREATE TABLE IF NOT EXISTS meeting_chat_message_007 like meeting_chat_message", sql);
    }

    @Test
    void getMeetingChatMessageTableReturnsStableShardedTableName() {
        String first = TableSplitUtils.getMeetingChatMessageTable("meeting-1001");
        String second = TableSplitUtils.getMeetingChatMessageTable("meeting-1001");

        assertEquals(first, second);
        assertTrue(first.matches("meeting_chat_message_(0[1-9]|[12][0-9]|3[0-2])"));
    }
}
