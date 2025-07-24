package com.example.stregourist;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("FCM", "Messaggio ricevuto");

        if (remoteMessage.getData().size() > 0) {
            String titolo = remoteMessage.getData().get("title");
            String corpo = remoteMessage.getData().get("body");

            Log.d("FCM-omr", "Titolo: " + titolo);
            Log.d("FCM-omr", "Corpo: " + corpo);

            mostraNotifica(titolo, corpo);
        }
    }

    private void mostraNotifica(String titolo, String messaggio) {
        Log.d("FCM-mostranotifica", "Mostra notifica: " + titolo + " - " + messaggio);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_events")
                .setSmallIcon(R.drawable.ic_notification_32)
                .setContentTitle(titolo)
                .setContentText(messaggio)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("FCM", "Permesso POST_NOTIFICATIONS non concesso");
            return;
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}