package com.github.pyltsin.sniffer.ui;

import com.intellij.util.messages.Topic;

import java.util.Map;

public interface TransactionDebugUpdateListener {
    Topic<TransactionDebugUpdateListener> TransactionDebugUpdate =
            new Topic<>(TransactionDebugUpdateListener.class, Topic.BroadcastDirection.TO_PARENT);

   void setData(Map<String, String> data);
}
