package com.team8.socialmedia.vu.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAloT5Lsw:APA91bGTjwvNeps0eEuuC-TLC1LLyTt3vqmovmpf8LCuOKxGzZ4wy9VmNCHnlhmQ5XN4FySrjVsfBU398kvNL_F4umPVCxqy3aamtKBM-Vuex4Bn5TMrFUSRcQhANP_9fKIXEdzpMD9i"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
