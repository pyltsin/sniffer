// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.pyltsin.sniffer.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.util.Map;

import static com.github.pyltsin.sniffer.ui.TransactionDebugUpdateListener.TransactionDebugUpdate;

public class MyToolWindow {

    private JPanel myToolWindowContent;
    private JTable table1;
    private JComboBox<String> comboBox1;
    private JScrollPane pane;
    private Map<String, String> data;

    public MyToolWindow(ToolWindow toolWindow, @NotNull Project project) {
        String[] tblHead = {"Name", "Property"};
        DefaultTableModel dtm = new DefaultTableModel(tblHead, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table1.setModel(dtm);
        table1.setCellSelectionEnabled(true);
        table1.setRowSelectionAllowed(false);
        table1.setColumnSelectionAllowed(false);

        comboBox1.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final DefaultTableModel model = (DefaultTableModel) table1.getModel();
                model.setRowCount(0);
                final String item = (String) e.getItem();
                model.addRow(new String[]{item, data.get(item)});
            }
        });

        project.getMessageBus().connect().subscribe(TransactionDebugUpdate, this::resetData);
    }

    private synchronized void resetData(Map<String, String> data) {
        this.data = data;
        data.forEach((s, s2) -> comboBox1.addItem(s));
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }
}
