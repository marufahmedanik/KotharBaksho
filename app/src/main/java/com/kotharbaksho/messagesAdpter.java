package com.kotharbaksho;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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

public class messagesAdpter extends RecyclerView.Adapter {
    private final Context context;
    private final ArrayList<msgModelclass> messagesAdpterArrayList;
    private final String senderRoom;
    private final String reciverRoom;
    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECIVE = 2;

    public messagesAdpter(Context context, ArrayList<msgModelclass> messagesAdpterArrayList, String senderRoom, String reciverRoom) {
        this.context = context;
        this.messagesAdpterArrayList = messagesAdpterArrayList;
        this.senderRoom = senderRoom;
        this.reciverRoom = reciverRoom;
    }

    public messagesAdpter(Context context, ArrayList<msgModelclass> messagesAdpterArrayList) {
        this(context, messagesAdpterArrayList, null, null);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        msgModelclass message = messagesAdpterArrayList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(message.getTimeStamp()));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showMessageActionDialog(message);
                return true;
            }
        });

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.msgtime.setText(formattedTime);

            if ("image".equalsIgnoreCase(message.getType())) {
                viewHolder.msgtxt.setVisibility(View.GONE);
                viewHolder.msgimage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage())
                        .placeholder(R.drawable.ic_image_attach)
                        .into(viewHolder.msgimage);
            } else {
                viewHolder.msgimage.setVisibility(View.GONE);
                viewHolder.msgtxt.setVisibility(View.VISIBLE);
                viewHolder.msgtxt.setText(message.getMessage());
            }
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.msgtime.setText(formattedTime);

            if ("image".equalsIgnoreCase(message.getType())) {
                viewHolder.msgtxt.setVisibility(View.GONE);
                viewHolder.msgimage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage())
                        .placeholder(R.drawable.ic_image_attach)
                        .into(viewHolder.msgimage);
            } else {
                viewHolder.msgimage.setVisibility(View.GONE);
                viewHolder.msgtxt.setVisibility(View.VISIBLE);
                viewHolder.msgtxt.setText(message.getMessage());
            }
        }
    }

    private void showMessageActionDialog(msgModelclass message) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        boolean isSender = currentUserId.equals(message.getSenderid());
        boolean isText = !"image".equalsIgnoreCase(message.getType());

        ArrayList<String> optionsList = new ArrayList<>();
        if (isText) optionsList.add("Copy Text");
        if (isSender) optionsList.add("Unsend for Everyone");
        optionsList.add("Delete for Me");
        optionsList.add("Cancel");

        CharSequence[] options = optionsList.toArray(new CharSequence[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Message Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selected = options[which].toString();
                switch (selected) {
                    case "Copy Text":
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied Message", message.getMessage());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "Unsend for Everyone":
                        if (message.getMessageId() != null && senderRoom != null && reciverRoom != null) {
                            FirebaseDatabase.getInstance().getReference().child("chats")
                                    .child(senderRoom).child("messages").child(message.getMessageId()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("chats")
                                    .child(reciverRoom).child("messages").child(message.getMessageId()).removeValue();
                            Toast.makeText(context, "Message unsent", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "Delete for Me":
                        if (message.getMessageId() != null && senderRoom != null) {
                            FirebaseDatabase.getInstance().getReference().child("chats")
                                    .child(senderRoom).child("messages").child(message.getMessageId()).removeValue();
                            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return messagesAdpterArrayList != null ? messagesAdpterArrayList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        msgModelclass message = messagesAdpterArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderid())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECIVE;
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView msgtxt, msgtime;
        ImageView msgimage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            msgtxt = itemView.findViewById(R.id.msgsendertyp);
            msgtime = itemView.findViewById(R.id.msgsendertime);
            msgimage = itemView.findViewById(R.id.sender_img_msg);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView msgtxt, msgtime;
        ImageView msgimage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            msgtxt = itemView.findViewById(R.id.recivertextset);
            msgtime = itemView.findViewById(R.id.recivertime);
            msgimage = itemView.findViewById(R.id.reciver_img_msg);
        }
    }
}
