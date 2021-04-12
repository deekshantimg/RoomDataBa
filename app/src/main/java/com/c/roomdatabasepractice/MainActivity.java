package com.c.roomdatabasepractice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addTask;
    RecyclerView fullList;

    TextView noTaskText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noTaskText = findViewById(R.id.noTaskText);

        fullList= findViewById(R.id.fullList);
        fullList.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        getTasks();

        addTask = findViewById(R.id.addTask);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });
    } // end ONcrceate MEthod

    private void getTasks() {
        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
        class GetTasks extends AsyncTask<Void, Void, List<Task>> {
            @Override protected List<Task> doInBackground(Void... voids) {
                List<Task> taskList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .dataDao()
                        .getAll();
                return taskList;
            }
            @Override protected void onPostExecute(List<Task> tasks) {
                super.onPostExecute(tasks);

                if(tasks.size()>0){
                    noTaskText.setVisibility(View.GONE);
                    fullList.setVisibility(View.VISIBLE);
                    TasksAdapter adapter = new TasksAdapter(MainActivity.this, tasks);
                    fullList.setAdapter(adapter);
                }
                else{
                    noTaskText.setVisibility(View.VISIBLE);
                    fullList.setVisibility(View.GONE);
                }
            }
        }

        GetTasks gt = new GetTasks();
        gt.execute();
    }

    public void showNoteDialog(final boolean shouldUpdate,final Task task, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        final EditText editTextTask = view.findViewById(R.id.editTextTask);
        final EditText editTextDesc = view.findViewById(R.id.editTextDesc);
        final EditText editTextFinishBy = view.findViewById(R.id.editTextFinishBy);

        Button button_save = view.findViewById(R.id.button_save);
        button_save.setText(!shouldUpdate ? "Add New": "Update");

        if (shouldUpdate && task != null) {
            // append sets text to EditText and places the cursor at the end
            editTextTask.append(task.getTask());
            editTextDesc.append(task.getDesc());
            editTextFinishBy.append(task.getFinishBy());
        }

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String sTask = editTextTask.getText().toString().trim();
                final String sDesc = editTextDesc.getText().toString().trim();
                final String sFinishBy = editTextFinishBy.getText().toString().trim();

                if (sTask.isEmpty()) {
                    editTextTask.setError("Task required");
                    editTextTask.requestFocus();
                    return;
                }
                else{
                    if (sDesc.isEmpty()) {
                        editTextDesc.setError("Description required");
                        editTextDesc.requestFocus();
                        return;
                    }
                    else{
                        if (sFinishBy.isEmpty()) {
                            editTextFinishBy.setError("Finish by required");
                            editTextFinishBy.requestFocus();
                            return;
                        }

                        else{
                            if(shouldUpdate==false){
                                alertDialog.dismiss();
                                SaveTask st = new SaveTask(sTask,sDesc,sFinishBy);
                                st.execute();
                            }
                            else{
                                alertDialog.dismiss();

                                tastUpdate(sTask,sDesc,sFinishBy);
                            }

                        }
                    }
                }

            }
        });

    }

    class SaveTask extends AsyncTask<Void, Void, Void> {
        String sTask, sDesc,sFinishBy;
        public SaveTask(String sTask, String sDesc, String sFinishBy) {
            this.sTask=sTask;
            this.sDesc=sDesc;
            this.sFinishBy=sFinishBy;

        }

        @Override
        protected Void doInBackground(Void... voids) {

            //creating a task
            Task task = new Task();
            task.setTask(sTask);
            task.setDesc(sDesc);
            task.setFinishBy(sFinishBy);
            task.setFinished(false);

            //adding to database
            DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                    .dataDao()
                    .insert(task);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            getTasks();
            Toast.makeText(getApplicationContext(), "Task Addedd Successfully!!", Toast.LENGTH_LONG).show();
        }
    }






    public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder> {

        private Context mCtx;
        private List<Task> taskList;

        public TasksAdapter(Context mCtx, List<Task> taskList) {
            this.mCtx = mCtx;
            this.taskList = taskList;
        }

        @Override public TasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_tasks, parent, false);
            return new TasksViewHolder(view);
        }

        @Override public void onBindViewHolder(TasksViewHolder holder, int position) {
            Task tas = taskList.get(position);
            holder.textViewTask.setText(tas.getTask());
            holder.textViewDesc.setText(tas.getDesc());
            holder.textViewFinishBy.setText(tas.getFinishBy());

            if (tas.isFinished())
                holder.textViewStatus.setText("Completed");
            else
                holder.textViewStatus.setText("Not Completed");



            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                    builder.setTitle("Choose Action");
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {

                                dialog.dismiss();
                                showNoteDialog(true, tas, position);
                            } else {
                                dialog.dismiss();

                               deleteTask(tas);
                            }
                        }
                    });
                    builder.show();


                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }

        class TasksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView textViewStatus, textViewTask, textViewDesc, textViewFinishBy;

            public TasksViewHolder(View itemView) {
                super(itemView);

                textViewStatus = itemView.findViewById(R.id.textViewStatus);
                textViewTask = itemView.findViewById(R.id.textViewTask);
                textViewDesc = itemView.findViewById(R.id.textViewDesc);
                textViewFinishBy = itemView.findViewById(R.id.textViewFinishBy);


                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {

                //Task task = taskList.get(getAdapterPosition());

            }
        }
    }






    private void tastUpdate(String sTask, String sDesc, String sFinishBy){
        class UpdateTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                Task task = new Task();
                task.setTask(sTask);
                task.setDesc(sDesc);
                task.setFinishBy(sFinishBy);

                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .dataDao()
                        .update(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Task Updated Successfully!!", Toast.LENGTH_LONG).show();

                getTasks();

            }
        }

        UpdateTask ut = new UpdateTask();
        ut.execute();
    }


    private void deleteTask(final Task task) {
        class DeleteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .dataDao()
                        .delete(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Task Deleted Successfully!", Toast.LENGTH_LONG).show();
                getTasks();
            }
        }

        DeleteTask dt = new DeleteTask();
        dt.execute();

    }



}