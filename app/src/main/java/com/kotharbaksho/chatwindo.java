package com.kotharbaksho;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatwindo extends AppCompatActivity {
    private String reciverImg, reciverUid, reciverName, senderUid;
    private CircleImageView profile;
    private TextView reciverNName, reciverStatus;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private CardView sendBtn, sendImgBtn;
    private EditText textMsg;
    private ImageView backBtn;

    private String senderRoom, reciverRoom;
    private RecyclerView messageRecyclerView;
    private ArrayList<msgModelclass> messagesArrayList;
    private messagesAdpter adapter;

    private ValueEventListener chatListener, statusListener;
    private DatabaseReference chatRef, statusRef;
    private static final int PICK_IMAGE_REQUEST = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwindo);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        senderUid = firebaseAuth.getUid();
        reciverName = getIntent().getStringExtra("nameeee");
        reciverImg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        if (reciverUid == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        senderRoom = senderUid + reciverUid;
        reciverRoom = reciverUid + senderUid;

        messagesArrayList = new ArrayList<>();

        sendBtn = findViewById(R.id.sendbtnn);
        sendImgBtn = findViewById(R.id.sendimgbtn);
        textMsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        reciverStatus = findViewById(R.id.reciverstatus);
        profile = findViewById(R.id.profileimgg);
        messageRecyclerView = findViewById(R.id.msgadpter);
        backBtn = findViewById(R.id.back_btn);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(layoutManager);
        adapter = new messagesAdpter(chatwindo.this, messagesArrayList, senderRoom, reciverRoom);
        messageRecyclerView.setAdapter(adapter);

        reciverNName.setText(reciverName != null ? reciverName : "User");

        if (reciverImg != null && !reciverImg.isEmpty()) {
            Picasso.get().load(reciverImg).placeholder(R.drawable.photocamera).into(profile);
        } else {
            profile.setImageResource(R.drawable.photocamera);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listenForReceiverStatus();
        listenForMessages();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });

        sendImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Share Image"), PICK_IMAGE_REQUEST);
            }
        });
    }

    private void listenForReceiverStatus() {
        statusRef = database.getReference().child("user").child(reciverUid).child("onlineStatus");
        statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if ("Online".equalsIgnoreCase(status)) {
                        reciverStatus.setText("● Online");
                        reciverStatus.setTextColor(getResources().getColor(R.color.status_online));
                    } else {
                        reciverStatus.setText("Offline");
                        reciverStatus.setTextColor(getResources().getColor(R.color.status_offline));
                    }
                } else {
                    reciverStatus.setText("Offline");
                    reciverStatus.setTextColor(getResources().getColor(R.color.status_offline));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        statusRef.addValueEventListener(statusListener);
    }

    private void listenForMessages() {
        chatRef = database.getReference().child("chats").child(senderRoom).child("messages");
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass message = dataSnapshot.getValue(msgModelclass.class);
                    if (message != null) {
                        if (message.getMessageId() == null) {
                            message.setMessageId(dataSnapshot.getKey());
                        }
                        messagesArrayList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messagesArrayList.isEmpty()) {
                    messageRecyclerView.smoothScrollToPosition(messagesArrayList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        chatRef.addValueEventListener(chatListener);
    }

    private void sendTextMessage() {
        String msg = textMsg.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        textMsg.setText("");
        Date date = new Date();
        DatabaseReference pushRef = database.getReference().child("chats").child(senderRoom).child("messages").push();
        String messageId = pushRef.getKey();

        msgModelclass messageObj = new msgModelclass(msg, senderUid, date.getTime(), messageId, "text");

        if (messageId != null) {
            database.getReference().child("chats").child(senderRoom).child("messages").child(messageId).setValue(messageObj);
            database.getReference().child("chats").child(reciverRoom).child("messages").child(messageId).setValue(messageObj);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Sending photo...");
            dialog.setCancelable(false);
            dialog.show();

            Date date = new Date();
            DatabaseReference pushRef = database.getReference().child("chats").child(senderRoom).child("messages").push();
            String messageId = pushRef.getKey();

            if (messageId != null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("chats").child("images").child(messageId + ".jpg");

                storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    dialog.dismiss();
                                    msgModelclass messageObj = new msgModelclass(uri.toString(), senderUid, date.getTime(), messageId, "image");
                                    database.getReference().child("chats").child(senderRoom).child("messages").child(messageId).setValue(messageObj);
                                    database.getReference().child("chats").child(reciverRoom).child("messages").child(messageId).setValue(messageObj);
                                }
                            });
                        } else {
                            dialog.dismiss();
                            Toast.makeText(chatwindo.this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                dialog.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseAuth.getUid() != null) {
            database.getReference().child("user").child(firebaseAuth.getUid()).child("onlineStatus").setValue("Online");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth.getUid() != null) {
            database.getReference().child("user").child(firebaseAuth.getUid()).child("onlineStatus").setValue("Offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRef != null && chatListener != null) {
            chatRef.removeEventListener(chatListener);
        }
        if (statusRef != null && statusListener != null) {
            statusRef.removeEventListener(statusListener);
        }
    }
}
