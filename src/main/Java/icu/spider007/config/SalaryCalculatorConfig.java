package icu.spider007.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;

@State(
        name = "SalaryCalculatorConfig",
        storages = @Storage("SalaryCalculatorSettings.xml")
)
public class SalaryCalculatorConfig implements PersistentStateComponent<SalaryCalculatorConfig.State> {

    public static class State {
        public String monthlySalary = "10000";
        public String monthlyWorkDays = "22";
        public String startTime = "09:00";
        public String endTime = "18:00";
        public String morningEndTime = "12:00";
        public String afternoonStartTime = "13:30";
        public String lunchStart = "12:00";
        public String lunchEnd = "13:30";
        public String currency = "人民币";
    }

    private State state = new State();

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(State state) {
        this.state = state;
    }

    public static SalaryCalculatorConfig getInstance() {
        return ApplicationManager.getApplication().getService(SalaryCalculatorConfig.class);
    }


    public String getMonthlySalary() {
        return state.monthlySalary;
    }

    public String getMonthlyWorkDays() {
        return state.monthlyWorkDays;
    }

    public String getStartTime() {
        return state.startTime;
    }

    public String getEndTime() {
        return state.endTime;
    }
}
