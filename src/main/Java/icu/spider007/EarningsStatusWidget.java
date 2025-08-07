package icu.spider007;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Consumer;
import icu.spider007.config.SalaryCalculatorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EarningsStatusWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {
    private final SalaryCalculatorConfig config = SalaryCalculatorConfig.getInstance();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // 唯一标识符
    @NotNull
    @Override
    public String ID() {
        return "SalaryCalculatorEarningsWidget";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        // 不需要特殊安装逻辑
    }

    @Override
    public void dispose() {
        // 不需要特殊清理逻辑
    }

    @NotNull
    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    // 状态栏显示的文本
    @NotNull
    @Override
    public String getText() {
        try {
            double monthlySalary = Double.parseDouble(config.getState().monthlySalary);
            int workDays = Integer.parseInt(config.getState().monthlyWorkDays);
            double dailySalary = monthlySalary / workDays;

            // 计算当日收入比例
            LocalTime startTime = LocalTime.parse(config.getState().startTime, timeFormatter);
            LocalTime endTime = LocalTime.parse(config.getState().endTime, timeFormatter);
            LocalTime now = LocalTime.now();

            double progress = calculateWorkProgress(startTime, endTime, now);
            double todayEarnings = dailySalary * progress;

            return String.format("今日: ¥%.2f", todayEarnings);
        } catch (Exception e) {
            return "薪资: 未配置";
        }
    }

    // 计算工作进度 (0.0 - 1.0)
    private double calculateWorkProgress(LocalTime start, LocalTime end, LocalTime now) {
        if (now.isBefore(start)) return 0.0;
        if (now.isAfter(end)) return 1.0;

        long totalMinutes = Duration.between(start, end).toMinutes();
        long passedMinutes = Duration.between(start, now).toMinutes();

        return (double) passedMinutes / totalMinutes;
    }

    // 文本对齐方式
    @Override
    public float getAlignment() {
        return Component.CENTER_ALIGNMENT;
    }

    // 鼠标悬停提示
    @Nullable
    @Override
    public String getTooltipText() {
        return "点击查看详细薪资信息";
    }

    // 点击事件处理
    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return event -> {
            JComponent component = (JComponent) event.getSource();
            showDetailsPopup(component);
        };
    }

    // 显示详细信息的弹出框
    private void showDetailsPopup(JComponent component) {
        try {
            double monthlySalary = Double.parseDouble(config.getState().monthlySalary);
            int workDays = Integer.parseInt(config.getState().monthlyWorkDays);
            double dailySalary = monthlySalary / workDays;

            LocalTime startTime = LocalTime.parse(config.getState().startTime, timeFormatter);
            LocalTime endTime = LocalTime.parse(config.getState().endTime, timeFormatter);
            LocalTime now = LocalTime.now();
            double progress = calculateWorkProgress(startTime, endTime, now);

            String details = String.format(
                "<html><div style='padding:5px;width:200px;'>" +
                "<b>薪资详情</b><br>" +
                "月薪: ¥%.2f<br>" +
                "工作天数: %d天<br>" +
                "日薪: ¥%.2f<br>" +
                "今日进度: %.1f%%<br>" +
                "已赚: ¥%.2f<br>" +
                "下班时间: %s" +
                "</div></html>",
                monthlySalary, workDays, dailySalary,
                progress * 100, dailySalary * progress,
                config.getState().endTime
            );

            JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(details, null,
                        new Color(240, 248, 255), null)
                .setFadeoutTime(5000)
                .createBalloon()
                .show(RelativePoint.getNorthWestOf(component), Balloon.Position.atRight);

        } catch (Exception e) {
            JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder("薪资配置错误", null,
                        new Color(255, 200, 200), null)
                .createBalloon()
                .show(RelativePoint.getNorthWestOf(component), Balloon.Position.atRight);
        }
    }

    // 提供者类
    public static class Provider implements StatusBarWidgetProvider {
        @Nullable
        @Override
        public StatusBarWidget getWidget(@NotNull Project project) {
            return new EarningsStatusWidget();
        }
    }
}
