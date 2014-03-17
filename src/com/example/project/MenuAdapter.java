package com.example.project;

import java.util.List;

import task.Task;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.failurereporter.R;

public class MenuAdapter extends BaseAdapter{

	List<Task> list;
	Activity context;

	public MenuAdapter(Activity context, List<Task> list) {
		super();
		this.context = context;
		this.list = list;
	}

	private class ViewHolder {
		TextView title;
		ImageButton deleteButton;
		ImageButton editButton;
		CheckBox done;
		ImageButton reportButton;
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		LayoutInflater inflater = context.getLayoutInflater();
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.menu_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.item_title);
			holder.deleteButton = (ImageButton) convertView.findViewById(R.id.menu_delete_button);
			holder.editButton = (ImageButton) convertView.findViewById(R.id.menu_edit_button);
			holder.done = (CheckBox) convertView.findViewById(R.id.done_check);
			holder.reportButton = (ImageButton) convertView.findViewById(R.id.menu_context);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Task task = list.get(position);
		final int pos = position;
		holder.title.setText(task.getTitle());
		holder.deleteButton.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	Log.i("ongoing","delete!");
	        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	        	// Add the buttons
	        	builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
	        	           public void onClick(DialogInterface dialog, int id) {
	        	        	   OngoingActivity.dbHelper.delete(task.getId());
	        	        	   refresh(pos);
	        	           }
	        	       });
	        	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	        	           public void onClick(DialogInterface dialog, int id) {
	        	               // User cancelled the dialog
	        	           }
	        	       });
	        	builder.setMessage(R.string.sure_to_delete);
	        	// Set other dialog properties
	        	// Create the AlertDialog
	        	AlertDialog dialog = builder.create();
	        	dialog.show();
	        }
	    });
		holder.editButton.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	Log.i("ongoing","edit!");
	        	Intent i = new Intent();
	        	i.setClass(context, EditTaskActivity.class);
	        	i.putExtra("task", task);
	        	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	context.startActivity(i);
	        }
	    });
		if (context.getClass() == HistoryActivity.class){
			holder.done.setEnabled(false);
			holder.done.setVisibility(View.INVISIBLE);
		}
		holder.done.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		    {
		        if (isChecked)
		        {
		        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
		        	// Add the buttons
		        	builder.setPositiveButton(R.string.y_move, new DialogInterface.OnClickListener() {
		        	           public void onClick(DialogInterface dialog, int id) {
		        	        	   task.setDone(1);
		        	        	   OngoingActivity.dbHelper.update(task);
		        	        	   refresh(pos);
		        	           }
		        	       });
		        	builder.setNeutralButton(R.string.n_delete, new DialogInterface.OnClickListener() {
	        	           public void onClick(DialogInterface dialog, int id) {
	        	        	   OngoingActivity.dbHelper.delete(task.getId()); 
	        	        	   refresh(pos);
	        	           }
	        	       });
		        	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		        	           public void onClick(DialogInterface dialog, int id) {
		        	       
		        	           }
		        	       });
		        	builder.setMessage(R.string.past_or_delete);
		        	// Set other dialog properties
		        	// Create the AlertDialog
		        	AlertDialog dialog = builder.create();
		        	dialog.show();  
		        }

		    }
		});
		
		final ImageButton but = holder.reportButton;
		holder.reportButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				context.registerForContextMenu(but);
                context.openContextMenu(but);
                OngoingActivity.setCurrent(pos);
			}
		});
		
		
	
		return convertView;
	}
	
	private void refresh(int pos){
		if (context.getClass() == OngoingActivity.class) {
		list = OngoingActivity.dbHelper.listAll();
		} else {
			list = OngoingActivity.dbHelper.listAllDone();
		}
		this.notifyDataSetChanged();
	}
	

}
