package icu.spider007;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.sun.istack.NotNull;

import java.util.Objects;

public class SalaryCalculatorAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(
                Objects.requireNonNull(e.getProject())).getToolWindow("PayPerTick");
        if (toolWindow != null) {
            toolWindow.show();
        }
    }
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setIcon(AllIcons.Toolwindows.ToolWindowStructure);
    }
}
