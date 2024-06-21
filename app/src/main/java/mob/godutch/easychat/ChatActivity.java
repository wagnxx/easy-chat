package mob.godutch.easychat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

import mob.godutch.easychat.adpter.ChatRecyclerAdapter;
import mob.godutch.easychat.adpter.SearchUserRecyclerAdapter;
import mob.godutch.easychat.model.ChatMessageModel;
import mob.godutch.easychat.model.ChatRoomModel;
import mob.godutch.easychat.model.UserModel;
import mob.godutch.easychat.utils.AndroidUtil;
import mob.godutch.easychat.utils.FirebaseUtil;

public class ChatActivity extends AppCompatActivity {

    UserModel   otherUser;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;

    String chatRoomId;
    ChatRoomModel chatRoomModel;
    ChatRecyclerAdapter adapter;

    @Override
    public void onBackPressed() {
        if (shouldReturnToMainActivity()) {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除上面的Activity，确保返回到MainActivity
            startActivity(intent);
            finish(); // 结束当前Activity
        } else {
            super.onBackPressed(); // 调用默认的返回操作，返回上一个Activity
        }
    }

    private boolean shouldReturnToMainActivity() {
        // 根据你的业务逻辑判断是否返回MainActivity
        return true; // 示例中总是返回MainActivity，你可以根据实际需求修改条件
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // get uerModel
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatRoomId = FirebaseUtil.getChatRoomId(FirebaseUtil.currentUserId(), otherUser.getUesrId());

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.chat_back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);


        backBtn.setOnClickListener((v) -> {
            onBackPressed();
        });



        otherUsername.setText(otherUser.getUsername());

        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) return;

            sendMessageToUser(message);

        });
        getOrCreateChatRoomModel();
        setupRecylerView();

    }



    void setupRecylerView() {
        Query query = FirebaseUtil.getChatRoomMessageRefrence(chatRoomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatMessageModel>  options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options,this);

        // 设置 RecyclerView 的布局管理器
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }
    void  sendMessageToUser(String message){
        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage(message);
        FirebaseUtil.getChatRoomRefrence(chatRoomId).set(chatRoomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(),Timestamp.now());
        FirebaseUtil.getChatRoomMessageRefrence(chatRoomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                        }
                    }
                });

    }
    void getOrCreateChatRoomModel() {
        FirebaseUtil.getChatRoomRefrence(chatRoomId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);
                if(chatRoomModel==null){
                    //first time chat
                    chatRoomModel = new ChatRoomModel(
                            chatRoomId,
                            Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUesrId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatRoomRefrence(chatRoomId).set(chatRoomModel);
                }
            }
        });
    }

}