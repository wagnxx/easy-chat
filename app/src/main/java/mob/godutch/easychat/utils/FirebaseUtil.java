package mob.godutch.easychat.utils;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {

    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
    public static boolean isLoggedIn() {
        return currentUserId() != null;
    }
    public static DocumentReference currentUserDetails() {
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }
    public static CollectionReference allUserCollectionReference() {
        return FirebaseFirestore.getInstance().collection("users");
    }
    public static DocumentReference getChatRoomRefrence(String chatRoomId) {
        return FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomId);
    }
    public static CollectionReference getChatRoomMessageRefrence(String chatRoomId) {
        return getChatRoomRefrence(chatRoomId).collection("chats");
    }
    public static String getChatRoomId(String userId1, String userid2) {
        if (userId1.hashCode() < userid2.hashCode()) {
            return userId1 + "_" + userid2;
        } else {
            return userid2 + "_" + userId1;
        }
    }
    public static CollectionReference allChatRoomCollectionRefrence() {
        return  FirebaseFirestore.getInstance().collection("chatRooms");
    }

    public static DocumentReference getOtherUserFromChatRoom(List<String> userIds) {
        String userId = userIds.get(0);
        if (userId.equals(currentUserId())) {
            userId = userIds.get(1);
        }
       return allUserCollectionReference().document(userId);
    }
    public static String timestampToString(Timestamp timestamp) {
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }
}
