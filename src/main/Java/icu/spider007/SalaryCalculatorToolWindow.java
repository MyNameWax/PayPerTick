package icu.spider007;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import icu.spider007.config.SalaryCalculatorConfig;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;
import com.intellij.icons.AllIcons;
public class SalaryCalculatorToolWindow {
    private JPanel mainPanel;
    private JBTextField monthlySalaryField = new JBTextField();
    private JBTextField monthlyWorkDaysField = new JBTextField();
    private JBTextField startTimeField = new JBTextField();
    private JBTextField endTimeField = new JBTextField();
    private JBTextField morningEndField = new JBTextField();
    private JBTextField afternoonStartField = new JBTextField();
    private JBTextField lunchStartField = new JBTextField();
    private JBTextField lunchEndField = new JBTextField();
    private JBLabel resultLabel = new JBLabel("每日工资: 0.00元");
    private JBLabel todayEarningsLabel = new JBLabel("今日已赚: 0.00元");
    private JBLabel workingHoursLabel = new JBLabel("有效工作时间: 0小时0分钟");
    private JBLabel timeLeftLabel = new JBLabel("距离下班: --");
    private ComboBox<String> currencyComboBox = new ComboBox<>(new String[]{"人民币", "美元", "欧元", "日元"});

    private Timer refreshTimer;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Random random = new Random();

    public SalaryCalculatorToolWindow() {
        // 初始化UI和监听器
        initializeComponents();
        setupListeners();
        startAutoRefresh();
        buildUI();

        // 初始计算
        calculateSalary();
        updateCountdown();
    }

    private void initializeComponents() {
        // 加载保存的设置
        SalaryCalculatorConfig.State config = SalaryCalculatorConfig.getInstance().getState();

        monthlySalaryField.setText(config.monthlySalary);
        monthlyWorkDaysField.setText(config.monthlyWorkDays);
        startTimeField.setText(config.startTime);
        endTimeField.setText(config.endTime);
        morningEndField.setText(config.morningEndTime);
        afternoonStartField.setText(config.afternoonStartTime);
        lunchStartField.setText(config.lunchStart);
        lunchEndField.setText(config.lunchEnd);
        currencyComboBox.setSelectedItem(config.currency);
    }

    private void setupListeners() {
        KeyAdapter calculationAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateSalary();
                saveSettings();
            }
        };

        monthlySalaryField.addKeyListener(calculationAdapter);
        monthlyWorkDaysField.addKeyListener(calculationAdapter);
        startTimeField.addKeyListener(calculationAdapter);
        endTimeField.addKeyListener(calculationAdapter);
        morningEndField.addKeyListener(calculationAdapter);
        afternoonStartField.addKeyListener(calculationAdapter);
        lunchStartField.addKeyListener(calculationAdapter);
        lunchEndField.addKeyListener(calculationAdapter);

        currencyComboBox.addActionListener(e -> {
            calculateSalary();
            saveSettings();
        });
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(60000, e -> {
            calculateSalary();
            updateCountdown();
        });
        refreshTimer.start();
    }

    private void buildUI() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("月薪:", monthlySalaryField)
                .addLabeledComponent("每月工作天数:", monthlyWorkDaysField)
                .addLabeledComponent("上午上班:", startTimeField)
                .addLabeledComponent("上午下班:", morningEndField)
                .addLabeledComponent("下午上班:", afternoonStartField)
                .addLabeledComponent("下午下班:", endTimeField)
                .addLabeledComponent("午休开始:", lunchStartField)
                .addLabeledComponent("午休结束:", lunchEndField)
                .addLabeledComponent("货币:", currencyComboBox)
                .addComponent(resultLabel)
                .addComponent(todayEarningsLabel)
                .addComponent(workingHoursLabel)
                .addComponent(timeLeftLabel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void calculateSalary() {
        try {
            // 基础薪资计算
            double monthlySalary = Double.parseDouble(monthlySalaryField.getText());
            int monthlyWorkDays = Integer.parseInt(monthlyWorkDaysField.getText());

            if (monthlySalary <= 0 || monthlyWorkDays <= 0) {
                resultLabel.setText("请输入正数");
                return;
            }

            // 计算每日固定工资
            double dailySalary = monthlySalary / monthlyWorkDays;

            String currencySymbol = getCurrencySymbol();
            DecimalFormat df = new DecimalFormat("0.00");
            resultLabel.setText(String.format("每日工资: %s%s", currencySymbol, df.format(dailySalary)));

            // 计算当日收入比例
            LocalTime startTime = LocalTime.parse(startTimeField.getText(), timeFormatter);
            LocalTime morningEnd = LocalTime.parse(morningEndField.getText(), timeFormatter);
            LocalTime afternoonStart = LocalTime.parse(afternoonStartField.getText(), timeFormatter);
            LocalTime endTime = LocalTime.parse(endTimeField.getText(), timeFormatter);
            LocalTime lunchStart = LocalTime.parse(lunchStartField.getText(), timeFormatter);
            LocalTime lunchEnd = LocalTime.parse(lunchEndField.getText(), timeFormatter);

            // 验证时间合理性
            if (morningEnd.isBefore(startTime) || afternoonStart.isBefore(morningEnd) || endTime.isBefore(afternoonStart)) {
                todayEarningsLabel.setText("时间顺序不正确");
                return;
            }

            // 计算总工作时间和已完成工作时间
            Duration totalWorkDuration = calculateTotalWorkDuration(startTime, morningEnd, afternoonStart, endTime, lunchStart, lunchEnd);
            LocalTime now = LocalTime.now();
            Duration completedWorkDuration = calculateCompletedWorkDuration(startTime, morningEnd, afternoonStart, endTime, lunchStart, lunchEnd, now);

            // 计算当日收入 (按比例)
            double todayEarnings = dailySalary * (completedWorkDuration.toMinutes() / (double) totalWorkDuration.toMinutes());

            // 工作时间统计
            long hours = completedWorkDuration.toHours();
            long minutes = completedWorkDuration.minusHours(hours).toMinutes();

            todayEarningsLabel.setText(String.format("今日已赚: %s%s",
                    currencySymbol, df.format(todayEarnings)));
            workingHoursLabel.setText(String.format("有效工作时间: %d小时%d分钟", hours, minutes));

        } catch (NumberFormatException e) {
            resultLabel.setText("请输入有效数字");
        } catch (DateTimeParseException e) {
            todayEarningsLabel.setText("时间格式应为 HH:mm");
        }
    }

    private Duration calculateTotalWorkDuration(LocalTime start, LocalTime morningEnd, LocalTime afternoonStart,
                                                LocalTime end, LocalTime lunchStart, LocalTime lunchEnd) {
        Duration morning = Duration.between(start, morningEnd);
        Duration afternoon = Duration.between(afternoonStart, end);

        // 扣除午休时间
        if (lunchStart.isAfter(start) && lunchEnd.isBefore(end)) {
            Duration lunch = Duration.between(lunchStart, lunchEnd);
            return morning.plus(afternoon).minus(lunch);
        }
        return morning.plus(afternoon);
    }

    private Duration calculateCompletedWorkDuration(LocalTime start, LocalTime morningEnd, LocalTime afternoonStart,
                                                    LocalTime end, LocalTime lunchStart, LocalTime lunchEnd, LocalTime now) {
        if (now.isBefore(start)) {
            return Duration.ZERO;
        } else if (now.isBefore(morningEnd)) {
            return Duration.between(start, now);
        } else if (now.isBefore(afternoonStart)) {
            return Duration.between(start, morningEnd);
        } else if (now.isBefore(end)) {
            Duration morning = Duration.between(start, morningEnd);
            Duration afternoon = Duration.between(afternoonStart, now);
            return morning.plus(afternoon);
        } else {
            return calculateTotalWorkDuration(start, morningEnd, afternoonStart, end, lunchStart, lunchEnd);
        }
    }

    private void updateCountdown() {
        try {
            LocalTime now = LocalTime.now();
            LocalTime morningEnd = LocalTime.parse(morningEndField.getText(), timeFormatter);
            LocalTime afternoonStart = LocalTime.parse(afternoonStartField.getText(), timeFormatter);
            LocalTime endTime = LocalTime.parse(endTimeField.getText(), timeFormatter);

            if (now.isBefore(morningEnd)) {
                // 上午工作时间
                String message = getFunCountdownMessage(now, morningEnd, true);
                setCountdownUI(message, new Color(0, 100, 0)); // 绿色
            } else if (now.isBefore(afternoonStart)) {
                // 午休时间
                if (now.isBefore(morningEnd.plusMinutes(30))) {
                    setCountdownUI("🍚 午饭时间！\n😋 好好享受美食吧~", new Color(150, 0, 150)); // 紫色
                } else {
                    String message = getFunCountdownMessage(now, afternoonStart, true);
                    setCountdownUI("💤 午休中...\n" + message, new Color(150, 0, 150)); // 紫色
                }
            } else if (now.isBefore(endTime)) {
                // 下午工作时间
                String message = getFunCountdownMessage(now, endTime, false);
                setCountdownUI(message, new Color(0, 0, 150)); // 蓝色
            } else {
                // 加班时间
                String message = getWarmOvertimeMessage(Duration.between(endTime, now), false);
                setWarmOvertimeUI(message);
            }

        } catch (DateTimeParseException e) {
            timeLeftLabel.setText("时间格式错误(应为HH:mm)");
        }
    }

    private void setWarmOvertimeUI(String message) {
        timeLeftLabel.setText(String.format("<html><div style='text-align:center;'>%s</div></html>",
                message.replace("\n", "<br>")));

        // 使用温暖的色调
        timeLeftLabel.setForeground(new Color(150, 80, 0)); // 暖橙色
        timeLeftLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));

        // 添加柔和的背景
        timeLeftLabel.setOpaque(true);
        timeLeftLabel.setBackground(new Color(255, 248, 225)); // 淡米黄色背景
        timeLeftLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private String getWarmOvertimeMessage(Duration overtime, boolean isMorning) {
        long hours = overtime.toHours();
        long minutes = overtime.minusHours(hours).toMinutes();

        String[] warmEmojis = {"🌙", "✨", "🕯️", "🍵", "📚", "🛋️", "🌃", "🌠"};
        String randomEmoji = warmEmojis[random.nextInt(warmEmojis.length)];

        if (isMorning) {
            // 上午加班提醒
            if (hours >= 1) {
                return String.format("%s 已经工作 %d小时%d分钟了\n" +
                                "🍲 记得按时吃午饭哦，身体最重要~",
                        randomEmoji, hours, minutes);
            } else {
                return String.format("%s 已经超时 %d分钟\n" +
                                "☕ 喝杯热饮休息一下吧，别让胃等太久~",
                        randomEmoji, minutes);
            }
        } else {
            // 下午/晚上加班提醒
            if (hours >= 3) {
                return String.format("%s 已经加班 %d小时%d分钟\n" +
                                "🌙 夜深了，今天的努力已经足够\n" +
                                "请为明天的自己保留些精力~",
                        randomEmoji, hours, minutes);
            } else if (hours >= 2) {
                return String.format("%s 加班 %d小时%d分钟了\n" +
                                "🏠 家人朋友都在等你回家\n" +
                                "工作永远做不完，但相聚的时光很珍贵~",
                        randomEmoji, hours, minutes);
            } else if (hours >= 1) {
                return String.format("%s 已超时 %d小时%d分钟\n" +
                                "📖 今天的任务完成得很棒了\n" +
                                "给自己一个放松的夜晚吧~",
                        randomEmoji, hours, minutes);
            } else {
                return String.format("%s 加班 %d分钟\n" +
                                "🛋️ 收拾心情准备回家吧\n" +
                                "温暖的被窝和好梦在等着你~",
                        randomEmoji, minutes);
            }
        }
    }

    private void setCountdownUI(String message, Color color) {
        timeLeftLabel.setText(String.format("<html><div style='text-align:center;'>%s</div></html>",
                message.replace("\n", "<br>")));
        timeLeftLabel.setForeground(color);
        timeLeftLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    }

    private String getFunCountdownMessage(LocalTime now, LocalTime endTime, boolean isMorning) {
        Duration duration = Duration.between(now, endTime);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();

        String[] emojis = {"🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼"};
        String randomEmoji = emojis[random.nextInt(emojis.length)];

        if (isMorning) {
            // 上午下班倒计时提示
            if (hours >= 1) {
                return String.format("%s 距离午饭还有 %d小时%d分钟\n🍚 想好吃什么了吗？",
                        randomEmoji, hours, minutes);
            } else if (minutes >= 30) {
                return String.format("%s 还有 %d分钟就能干饭啦~\n🍜 坚持就是胜利！",
                        randomEmoji, minutes);
            } else if (minutes >= 10) {
                return String.format("%s 只剩 %d分钟就解放了！\n🍔 外卖可以点起来了~",
                        randomEmoji, minutes);
            } else {
                return String.format("%s 最后 %d分钟！\n🍱 筷子已备好，准备冲刺！",
                        randomEmoji, minutes);
            }
        } else {
            // 下午下班倒计时提示
            if (hours >= 2) {
                return String.format("%s 距离下班还有 %d小时%d分钟\n🎮 今天的任务完成多少啦？",
                        randomEmoji, hours, minutes);
            } else if (hours >= 1) {
                return String.format("%s 再坚持 %d小时%d分钟\n☕ 需要来杯咖啡提神吗？",
                        randomEmoji, hours, minutes);
            } else if (minutes >= 30) {
                return String.format("%s 只剩 %d分钟啦~\n🏃‍ 收拾包包准备开溜~",
                        randomEmoji, minutes);
            } else if (minutes >= 10) {
                return String.format("%s 最后 %d分钟！\n🚪 手指放在打卡机上方待命！",
                        randomEmoji, minutes);
            } else {
                return String.format("%s 倒计时 %d分钟！\n🎉 准备迎接自由时光！",
                        randomEmoji, minutes);
            }
        }
    }
    private void saveSettings() {
        SalaryCalculatorConfig.State config = SalaryCalculatorConfig.getInstance().getState();

        config.monthlySalary = monthlySalaryField.getText();
        config.monthlyWorkDays = monthlyWorkDaysField.getText();
        config.startTime = startTimeField.getText();
        config.endTime = endTimeField.getText();
        config.morningEndTime = morningEndField.getText();
        config.afternoonStartTime = afternoonStartField.getText();
        config.lunchStart = lunchStartField.getText();
        config.lunchEnd = lunchEndField.getText();
        config.currency = (String) currencyComboBox.getSelectedItem();
    }

    private String getCurrencySymbol() {
        switch (currencyComboBox.getSelectedItem().toString()) {
            case "美元":
                return "$";
            case "欧元":
                return "€";
            case "日元":
                return "¥";
            default:
                return "¥";
        }
    }

    public JPanel getContent() {
        return mainPanel;
    }
}
