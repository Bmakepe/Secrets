package com.makepe.blackout.GettingStarted.OtherClasses;

import com.makepe.blackout.GettingStarted.Notifications.MyResponse;
import com.makepe.blackout.GettingStarted.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key = AAAAGQ9XQtg:APA91bFd91bvy9UCyZKfhVaKvbyFTFMLHK4_rrIoRLpidscy958NbDrKNQa13yasI1wMCEQHm1s6wC0KJCrf_38tt1tvDk2ZQw4IeHQwhP9e6rmRBAhx17CMNWUSqliQTuluwf0pOGij"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
