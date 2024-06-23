package mob.godutch.easychat;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import org.json.JSONObject;

import java.util.Arrays;

import mob.godutch.easychat.adpter.ChatRecyclerAdapter;
import mob.godutch.easychat.adpter.SearchUserRecyclerAdapter;
import mob.godutch.easychat.model.ChatMessageModel;
import mob.godutch.easychat.model.ChatRoomModel;
import mob.godutch.easychat.model.UserModel;
import mob.godutch.easychat.utils.AccessTokenTask;
import mob.godutch.easychat.utils.AndroidUtil;
import mob.godutch.easychat.utils.FCMHelper;
import mob.godutch.easychat.utils.FirebaseUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import android.content.Context;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.InputStream;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;


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
        imageView = findViewById(R.id.profile_pic_image_view);

        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUesrId())
                .getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(this, uri, imageView);
                    }
                });


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
        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options, this);

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

    void sendMessageToUser(String message) {
        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage(message);
        FirebaseUtil.getChatRoomRefrence(chatRoomId).set(chatRoomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now());
        FirebaseUtil.getChatRoomMessageRefrence(chatRoomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
//                            sendNotification(message);
                            deailSendInfo(message);
                        }
                    }
                });

    }

    void  deailSendInfo(String message) {
        FirebaseUtil.currentUserDetails()
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel currentUserModel = task.getResult().toObject(UserModel.class);
                        String deviceToken = otherUser.getFcmToken();
                        String title = currentUserModel.getUsername();
                        String body = message;

//                            FCMHelper.sendNotification(accessToken, deviceToken, title, body);




                        // Fetch the access token
                        new AccessTokenTask(this, deviceToken, title, body, new AccessTokenTask.OnTaskCompleted() {
                            @Override
                            public void onTaskCompleted(boolean success) {
                                Log.i("AccessTokenTask", String.valueOf(success));
                            }
                        }).execute();




                    }
                });
    }

    void getOrCreateChatRoomModel() {
        FirebaseUtil.getChatRoomRefrence(chatRoomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);
                if (chatRoomModel == null) {
                    //first time chat
                    chatRoomModel = new ChatRoomModel(
                            chatRoomId,
                            Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUesrId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatRoomRefrence(chatRoomId).set(chatRoomModel);
                }
            }
        });
    }

    void sendNotification(String message) {
        FirebaseUtil.currentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel currentUser = task.getResult().toObject(UserModel.class);
                        try {
                            JSONObject jsonObj = new JSONObject();

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("userId", currentUser.getUesrId());

                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title", currentUser.getUsername());
                            notificationObj.put("body", message);

                            jsonObj.put("notification", notificationObj);
                            jsonObj.put("data", dataObj);
                            jsonObj.put("to", otherUser.getFcmToken());

                            callApi(jsonObj);

                        } catch (Exception e) {

                        }
                    }
                });
    }

    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();

//        String url = "https://fcm.googleapis.com/fcm/send";
//        String url = "https://fcm.googleapis.com/v1/projects/fb-dev-fde3a/messages:send";
        String url = "https://fcm.googleapis.com/v1/projects/fb-dev-fde3a/messages:send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
//        String token = "Bearer " + getAccessToken(this);
//        Log.i("Bearer_Token", token);





        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Bearer_Token", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token =  "Bearer " +  task.getResult();

                        // Log and toast
//                        String token = "Bearer " + getAccessToken(this);
                        Log.i("Bearer_Token", token);

                        Request request = new Request.Builder()
                                .url(url)
                                .post(body)
                                .addHeader("Authorization",  token)
                                .addHeader("Content-Type", "application/json")
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                }
                                Log.i("Bearer_Token",response.body().string());
                            }
                        });




                    }
                });






    }


}