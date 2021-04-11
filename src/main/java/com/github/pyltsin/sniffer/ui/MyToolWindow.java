// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.pyltsin.sniffer.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.pom.Navigatable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.pyltsin.sniffer.ui.TransactionDebugUpdateListener.TransactionDebugUpdate;

public class MyToolWindow {

    private JPanel myToolWindowContent;
    private JTable table1;
    private JComboBox<ViewModel> comboBox1;
    private JScrollPane pane;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton goToButton;
    private final ConcurrentHashMap<String, ViewModel> data = new ConcurrentHashMap<>();

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

        deleteButton.addActionListener(e -> {
            final Object selectedObject = comboBox1.getSelectedItem();
            if (selectedObject == null) {
                return;
            }
            final int index = comboBox1.getSelectedIndex();
            data.remove(((ViewModel) selectedObject).getGuid());
            if (data.isEmpty()) {
                final DefaultTableModel model = (DefaultTableModel) table1.getModel();
                model.setRowCount(0);
            }
            comboBox1.removeItem(selectedObject);
        });

        clearButton.addActionListener(e -> {
            data.clear();
            comboBox1.removeAllItems();
            final DefaultTableModel model = (DefaultTableModel) table1.getModel();
            model.setRowCount(0);
        });

        goToButton.addActionListener(e -> {
            final Object selectedObject = comboBox1.getSelectedItem();
            if (selectedObject == null) {
                return;
            }
            final int index = comboBox1.getSelectedIndex();
            final ViewModel viewmodel = (ViewModel) selectedObject;
            if (viewmodel.element.canNavigateToSource()) {
                viewmodel.element.navigate(true);
            }
        });

        comboBox1.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final ViewModel item = (ViewModel) e.getItem();
                updateTable(item);
            }
        });

        project.getMessageBus().connect().subscribe(TransactionDebugUpdate, new TransactionDebugUpdateListener() {
            @Override
            public void addData(String guid, int order, String key, String value) {
                final ViewModel viewModel = data.get(guid);
                if (viewModel != null) {
                    viewModel.add(order, key, value);
                }
                final ViewModel selectedItem = (ViewModel) comboBox1.getSelectedItem();
                if (selectedItem == null) {
                    return;
                }
                ApplicationManager.getApplication().invokeLater(() -> updateTable(selectedItem));
            }

            @Override
            public void createRow(String guid, Navigatable element, String name) {
                final ViewModel value = new ViewModel(element, name, guid);
                data.put(guid, value);
                ApplicationManager.getApplication().invokeLater(() -> {
                    comboBox1.addItem(value);
                    comboBox1.setSelectedItem(value);
                });
            }
        });
    }

    private void updateTable(ViewModel item) {
        final DefaultTableModel model = (DefaultTableModel) table1.getModel();
        model.setRowCount(0);
        final ConcurrentHashMap<Integer, Pair<String, String>> rows = item.getRows();
        for (int i = 0; i < rows.size(); i++) {
            final Pair<String, String> stringStringPair = rows.get(i);
            if (stringStringPair != null) {
                model.addRow(new String[]{stringStringPair.getKey(), stringStringPair.getValue()});
            }
        }
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    private static class ViewModel {
        private final Navigatable element;
        private final String name;
        private final String guid;
        private final ConcurrentHashMap<Integer, Pair<String, String>> rows = new ConcurrentHashMap<>();

        @Override
        public String toString() {
            return name;
        }

        public String getGuid() {
            return guid;
        }

        public ViewModel(Navigatable element, String name, String guid) {
            this.element = element;
            this.name = name;
            this.guid = guid;
        }

        public Navigatable getElement() {
            return element;
        }

        public String getName() {
            return name;
        }

        public void add(Integer order, String key, String value) {
            rows.put(order, new ImmutablePair<>(key, value));
        }

        public ConcurrentHashMap<Integer, Pair<String, String>> getRows() {
            return rows;
        }
    }
}
