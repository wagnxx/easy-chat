package mob.godutch.easychat.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
}
