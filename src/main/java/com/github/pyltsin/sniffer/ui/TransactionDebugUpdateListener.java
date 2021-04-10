package com.github.pyltsin.sniffer.ui;

import com.intellij.pom.Navigatable;
import com.intellij.util.messages.Topic;

public interface TransactionDebugUpdateListener {
    Topic<TransactionDebugUpdateListener> TransactionDebugUpdate =
            new Topic<>(TransactionDebugUpdateListener.class, Topic.BroadcastDirection.TO_PARENT);

   void addData(String guid, int order, String key, String value);

    void createRow(String guid, Navigatable element, String name);
}
