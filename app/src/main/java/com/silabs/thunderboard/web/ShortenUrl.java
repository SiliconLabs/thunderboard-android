package com.silabs.thunderboard.web;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.silabs.thunderboard.BuildConfig;

import java.lang.reflect.Type;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

/**
 * Uses http://is.gd to obtain a short version of the shared url
 */
public class ShortenUrl {

    public static ShortenUrl getInstance() {
        if(instance == null) {
            instance = new ShortenUrl();
        }
        return instance;
    }

    private static ShortenUrl instance = getInstance();
    private static final String API_URL = "https://is.gd";

    public  final Shorten shorten;
    private final RestAdapter restAdapter;

    private ShortenUrl() {
        // Create a very simple REST adapter which points the API_URL endpoint.
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setConverter(new GsonConverter(getGsonBuilder().create()))
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .build();

        // Create an instance of our Shorten API interface.
        shorten = restAdapter.create(Shorten.class);
    }

    public static GsonBuilder getGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();

        // class types
        builder.registerTypeAdapter(Integer.class, new JsonDeserializer<Integer>() {
            @Override
            public Integer deserialize(JsonElement json, Type typeOfT,
                                       JsonDeserializationContext context) throws JsonParseException {
                try {
                    return Integer.valueOf(json.getAsInt());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
        return builder;
    }

    public interface Shorten {
        @POST("/create.php")
        Observable<ShortUrl> convert(
                @Query("url") String url,
                @Query("format") String format,
                @Body Object dummy
        );
    }

    public static class ShortUrl {
        public String shorturl;
    }
}
