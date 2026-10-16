package com.kotharbaksho;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNName, reciverstatus;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public static String senderImg;
    public static String reciverIImg;
    CardView sendbtn, sendimgbtn;
    EditText textmsg;
    ImageView backbtn;

    String senderRoom, reciverRoom;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwindo);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>();

        sendbtn = findViewById(R.id.sendbtnn);
        sendimgbtn = findViewById(R.id.sendimgbtn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        reciverstatus = findViewById(R.id.reciverstatus);
        profile = findViewById(R.id.profileimgg);
        messageAdpter = findViewById(R.id.msgadpter);
        backbtn = findViewById(R.id.back_btn);

        SenderUID = firebaseAuth.getUid();
        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdpter.setLayoutManager(linearLayoutManager);
        mmessagesAdpter = new messagesAdpter(chatwindo.this, messagesArrayList, senderRoom, reciverRoom);
        messageAdpter.setAdapter(mmessagesAdpter);

        Picasso.get().load(reciverimg).into(profile);
        reciverNName.setText("" + reciverName);

        // Listen for receiver's live online status
        database.getReference().child("user").child(reciverUid).child("onlineStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if ("Online".equalsIgnoreCase(status)) {
                        reciverstatus.setText("● Online");
                        reciverstatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
                    } else {
                        reciverstatus.setText("Offline");
                        reciverstatus.setTextColor(android.graphics.Color.parseColor("#555555"));
                    }
                } else {
                    reciverstatus.setText("Offline");
                    reciverstatus.setTextColor(android.graphics.Color.parseColor("#555555"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");

        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    if (messages != null) {
                        if (messages.getMessageId() == null) {
                            messages.setMessageId(dataSnapshot.getKey());
                        }
                        messagesArrayList.add(messages);
                    }
                }
                mmessagesAdpter.notifyDataSetChanged();
                if (messagesArrayList.size() > 0) {
                    messageAdpter.smoothScrollToPosition(messagesArrayList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("profilepic").getValue() != null) {
                    senderImg = snapshot.child("profilepic").getValue().toString();
                }
                reciverIImg = reciverimg;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Send text message
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textmsg.getText().toString().trim();
                if (message.isEmpty()) {
                    Toast.makeText(chatwindo.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                    return;
                }
                textmsg.setText("");
                Date date = new Date();
                String messageId = database.getReference().child("chats").child(senderRoom).child("messages").push().getKey();
                msgModelclass messagess = new msgModelclass(message, SenderUID, date.getTime(), messageId, "text");

                database.getReference().child("chats").child(senderRoom).child("messages").child(messageId).setValue(messagess);
                database.getReference().child("chats").child(reciverRoom).child("messages").child(messageId).setValue(messagess);
            }
        });

        // Send image message
        sendimgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 25);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Sending image...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Date date = new Date();
            String messageId = database.getReference().child("chats").child(senderRoom).child("messages").push().getKey();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("chats").child("images").child(messageId + ".jpg");

            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();
                                String downloadUrl = uri.toString();
                                msgModelclass messagess = new msgModelclass(downloadUrl, SenderUID, date.getTime(), messageId, "image");
                                database.getReference().child("chats").child(senderRoom).child("messages").child(messageId).setValue(messagess);
                                database.getReference().child("chats").child(reciverRoom).child("messages").child(messageId).setValue(messagess);
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(chatwindo.this, "Failed to send image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
}
