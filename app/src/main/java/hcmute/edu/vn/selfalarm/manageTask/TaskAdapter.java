package hcmute.edu.vn.selfalarm.manageTask;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.selfalarm.R;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {
    private List<TaskModel> taskModelList;
    private ManageTaskActivity mainActivity;
    private DataBaseHelper dataBaseHelper;

    public TaskAdapter(DataBaseHelper dataBaseHelper, ManageTaskActivity mainActivity) {
        this.dataBaseHelper = dataBaseHelper;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final TaskModel taskModel = taskModelList.get(position);
        holder.title.setText(taskModel.getTitle());
        holder.description.setText(taskModel.getDescription());
        holder.date.setText(taskModel.getDue_time());
        holder.checkBox.setChecked(taskModel.getStatus() != 0);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dataBaseHelper.updateStatus(taskModel.getId(), 1);
            } else {
                dataBaseHelper.updateStatus(taskModel.getId(), 0);
            }
        });
    }

    public Context getContext() {
        return mainActivity;
    }

    public void setTasks(List<TaskModel> taskModelList) {
        this.taskModelList = taskModelList;
        notifyDataSetChanged();
    }

    public void deleteTask(int position) {
        TaskModel todoModel = taskModelList.get(position);
        dataBaseHelper.deleteTask(todoModel.getId());
        taskModelList.remove(position);
        notifyItemRemoved(position);
    }

    public void editTask(int position) {
        TaskModel todoModel = taskModelList.get(position);
        Bundle bundle = new Bundle();
        bundle.putLong("id", todoModel.getId());
        bundle.putString("title", todoModel.getTitle());
        bundle.putString("description", todoModel.getDescription());
        bundle.putString("date", todoModel.getDue_time());

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(mainActivity.getSupportFragmentManager(), addNewTask.getTag());
    }

    @Override
    public int getItemCount() {
        return taskModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        CheckBox checkBox;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textView_title);
            description = itemView.findViewById(R.id.textView_description);
            date = itemView.findViewById(R.id.textView_date);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
