package icu.spider007;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class SalaryCalculatorToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(
            @NotNull Project project,
            @NotNull ToolWindow toolWindow
    ) {
        // 必须调用setTitle确保名称生效
        toolWindow.setTitle("PayPerTick");
        toolWindow.setIcon(AllIcons.Toolwindows.ToolWindowStructure);
        // 创建内容面板
        SalaryCalculatorToolWindow window = new SalaryCalculatorToolWindow();
        Content content = ContentFactory.getInstance().createContent(
                window.getContent(),
                "",  // 显示名称由toolWindow控制
                false
        );
        toolWindow.getContentManager().addContent(content);
    }
}
