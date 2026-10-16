package com.kotharbaksho;

import static com.kotharbaksho.chatwindo.reciverIImg;
import static com.kotharbaksho.chatwindo.senderImg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class messagesAdpter extends RecyclerView.Adapter {
    Context context;
    ArrayList<msgModelclass> messagesAdpterArrayList;
    String senderRoom, reciverRoom;
    int ITEM_SEND = 1;
    int ITEM_RECIVE = 2;

    public messagesAdpter(Context context, ArrayList<msgModelclass> messagesAdpterArrayList, String senderRoom, String reciverRoom) {
        this.context = context;
        this.messagesAdpterArrayList = messagesAdpterArrayList;
        this.senderRoom = senderRoom;
        this.reciverRoom = reciverRoom;
    }

    public messagesAdpter(Context context, ArrayList<msgModelclass> messagesAdpterArrayList) {
        this.context = context;
        this.messagesAdpterArrayList = messagesAdpterArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new senderVierwHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_layout, parent, false);
            return new reciverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        msgModelclass messages = messagesAdpterArrayList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(messages.getTimeStamp()));

        // Long click to unsend / delete message
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "";
                boolean isSender = currentUserId.equals(messages.getSenderid());

                if (isSender) {
                    CharSequence[] options = new CharSequence[]{"Unsend for Everyone", "Delete for Me", "Cancel"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Message");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) { // Unsend for Everyone
                                if (messages.getMessageId() != null && senderRoom != null && reciverRoom != null) {
                                    FirebaseDatabase.getInstance().getReference().child("chats")
                                            .child(senderRoom).child("messages").child(messages.getMessageId()).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("chats")
                                            .child(reciverRoom).child("messages").child(messages.getMessageId()).removeValue();
                                    Toast.makeText(context, "Message unsent", Toast.LENGTH_SHORT).show();
                                }
                            } else if (which == 1) { // Delete for Me
                                if (messages.getMessageId() != null && senderRoom != null) {
                                    FirebaseDatabase.getInstance().getReference().child("chats")
                                            .child(senderRoom).child("messages").child(messages.getMessageId()).removeValue();
                                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    builder.show();
                } else {
                    CharSequence[] options = new CharSequence[]{"Delete for Me", "Cancel"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Message");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) { // Delete for Me
                                if (messages.getMessageId() != null && senderRoom != null) {
                                    FirebaseDatabase.getInstance().getReference().child("chats")
                                            .child(senderRoom).child("messages").child(messages.getMessageId()).removeValue();
                                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    builder.show();
                }
                return true;
            }
        });

        if (holder.getClass() == senderVierwHolder.class) {
            senderVierwHolder viewHolder = (senderVierwHolder) holder;
            viewHolder.msgtime.setText(formattedTime);

            if ("image".equalsIgnoreCase(messages.getType())) {
                viewHolder.msgtxt.setVisibility(View.GONE);
                viewHolder.msgimage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.imageadd).into(viewHolder.msgimage);
            } else {
                viewHolder.msgimage.setVisibility(View.GONE);
                viewHolder.msgtxt.setVisibility(View.VISIBLE);
                viewHolder.msgtxt.setText(messages.getMessage());
            }

            if (senderImg != null && !senderImg.isEmpty()) {
                Picasso.get().load(senderImg).into(viewHolder.circleImageView);
            }
        } else {
            reciverViewHolder viewHolder = (reciverViewHolder) holder;
            viewHolder.msgtime.setText(formattedTime);

            if ("image".equalsIgnoreCase(messages.getType())) {
                viewHolder.msgtxt.setVisibility(View.GONE);
                viewHolder.msgimage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.imageadd).into(viewHolder.msgimage);
            } else {
                viewHolder.msgimage.setVisibility(View.GONE);
                viewHolder.msgtxt.setVisibility(View.VISIBLE);
                viewHolder.msgtxt.setText(messages.getMessage());
            }

            if (reciverIImg != null && !reciverIImg.isEmpty()) {
                Picasso.get().load(reciverIImg).into(viewHolder.circleImageView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messagesAdpterArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        msgModelclass messages = messagesAdpterArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderid())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECIVE;
        }
    }

    class senderVierwHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView msgtxt;
        TextView msgtime;
        ImageView msgimage;

        public senderVierwHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.profilerggg);
            msgtxt = itemView.findViewById(R.id.msgsendertyp);
            msgtime = itemView.findViewById(R.id.msgsendertime);
            msgimage = itemView.findViewById(R.id.sender_img_msg);
        }
    }

    class reciverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView msgtxt;
        TextView msgtime;
        ImageView msgimage;

        public reciverViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.pro);
            msgtxt = itemView.findViewById(R.id.recivertextset);
            msgtime = itemView.findViewById(R.id.recivertime);
            msgimage = itemView.findViewById(R.id.reciver_img_msg);
        }
    }
}
