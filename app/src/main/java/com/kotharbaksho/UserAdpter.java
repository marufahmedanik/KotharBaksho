package com.kotharbaksho;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.ViewHolder> {
    private final Context context;
    private ArrayList<Users> usersArrayList;

    public UserAdpter(Context context, ArrayList<Users> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;
    }

    public void setFilterList(ArrayList<Users> filteredList) {
        this.usersArrayList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = usersArrayList.get(position);

        holder.username.setText(user.getUserName() != null ? user.getUserName() : "User");

        String lastMsg = user.getLastMessage();
        if (lastMsg != null && !lastMsg.trim().isEmpty()) {
            holder.userstatus.setText(lastMsg);
        } else {
            holder.userstatus.setText(user.getStatus() != null && !user.getStatus().isEmpty() ? user.getStatus() : "Hey! I'm using KotharBaksho");
        }

        if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
            Picasso.get().load(user.getProfilepic())
                    .placeholder(R.drawable.photocamera)
                    .error(R.drawable.photocamera)
                    .into(holder.userimg);
        } else {
            holder.userimg.setImageResource(R.drawable.photocamera);
        }

        String online = user.getOnlineStatus();
        if ("Online".equalsIgnoreCase(online)) {
            holder.useronline.setText("● Online");
            holder.useronline.setTextColor(context.getResources().getColor(R.color.status_online));
            holder.onlineDotIndicator.setVisibility(View.VISIBLE);
            holder.userimg.setBorderColor(context.getResources().getColor(R.color.status_online));
            holder.userimg.setBorderWidth(4);
        } else {
            holder.useronline.setText("Offline");
            holder.useronline.setTextColor(context.getResources().getColor(R.color.status_offline));
            holder.onlineDotIndicator.setVisibility(View.GONE);
            holder.userimg.setBorderColor(context.getResources().getColor(R.color.divider_line));
            holder.userimg.setBorderWidth(2);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, chatwindo.class);
                intent.putExtra("nameeee", user.getUserName());
                intent.putExtra("reciverImg", user.getProfilepic());
                intent.putExtra("uid", user.getUserId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList != null ? usersArrayList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        View onlineDotIndicator;
        TextView username, userstatus, useronline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            onlineDotIndicator = itemView.findViewById(R.id.onlineDotIndicator);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
            useronline = itemView.findViewById(R.id.user_online_indicator);
        }
    }
}
