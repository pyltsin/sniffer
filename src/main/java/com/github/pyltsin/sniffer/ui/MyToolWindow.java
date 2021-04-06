// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.pyltsin.sniffer.ui;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

public class MyToolWindow {

    private JPanel myToolWindowContent;
    private JTable table1;
    private JComboBox comboBox1;

    public MyToolWindow(ToolWindow toolWindow) {
        System.out.println(toolWindow);
    }


    public JPanel getContent() {
        return myToolWindowContent;
    }
}
