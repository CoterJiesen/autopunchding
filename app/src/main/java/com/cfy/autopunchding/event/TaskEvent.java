package com.cfy.autopunchding.event;

/**
 * author: aaron.chen
 * created on: 2018/9/5 08:45
 * description: 完成打卡事件
 */
public class TaskEvent {

    private TaskType taskType;

    private String tip;

    public TaskEvent(TaskType taskType, String tip) {
        this.taskType = taskType;
        this.tip = tip;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }
}
