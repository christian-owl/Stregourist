package com.example.stregourist;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.List;
import java.util.Random;

public class DailyTipsWorker extends Worker {
    public DailyTipsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    @NonNull
    @Override
    public Result doWork() {
        StrgDatabase db = StrgDatabase.getDatabase(getApplicationContext());
        List<Place> unvisited = db.placeDAO().getAllNoVisitatiList();

        if(!unvisited.isEmpty()){
            Place p = unvisited.get(new Random().nextInt(unvisited.size()));
            NotificationManagerHelper.sendDailyTips(getApplicationContext(), p.getNome());
        }
        return Result.success();
    }
}