package org.edx.mobile.http.provider;

import static org.edx.mobile.http.TrustAllCertsClient.getTrustAllCertsClient;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.edx.mobile.util.Config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public interface RetrofitProvider {

    @NonNull
    Retrofit get();

    @NonNull
    Retrofit getWithOfflineCache();

    @NonNull
    Retrofit getNonOAuthBased();

    @NonNull
    Retrofit getIAPAuth();

    @Singleton
    class Impl implements RetrofitProvider {
        private static final int CLIENT_INDEX_DEFAULT = 0;
        private static final int CLIENT_INDEX_WITH_OFFLINE_CACHE = 1;
        private static final int CLIENT_INDEX_NON_OAUTH_BASED = 2;
        private static final int CLIENT_INDEX_ECOMMERCE = 3;
        private static final int CLIENTS_COUNT = 4;

        @Inject
        Config config;

        @Inject
        OkHttpClientProvider clientProvider;

        @Inject
        Gson gson;

        @Inject
        public Impl() {
        }

        private final Retrofit[] retrofits = new Retrofit[CLIENTS_COUNT];

        @NonNull
        @Override
        public Retrofit get() {
            return get(CLIENT_INDEX_DEFAULT);
        }

        @NonNull
        public Retrofit getWithOfflineCache() {
            return get(CLIENT_INDEX_WITH_OFFLINE_CACHE);
        }

        @NonNull
        public Retrofit getNonOAuthBased() {
            return get(CLIENT_INDEX_NON_OAUTH_BASED);
        }

        @NonNull
        public Retrofit getIAPAuth() {
            return get(CLIENT_INDEX_ECOMMERCE);
        }

        @NonNull
        private synchronized Retrofit get(final int index) {
            Retrofit retrofit = retrofits[index];
            OkHttpClient client = null;
            try {
                client = getTrustAllCertsClient();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException("=============> client error");
            }

            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .client(client)
                        .baseUrl(getBaseUrl(index))
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
                retrofits[index] = retrofit;
            }
            return retrofit;
        }

        @NonNull
        private String getBaseUrl(final int client) {
            if (client == CLIENT_INDEX_ECOMMERCE) {
                return config.getEcommerceURL();
            }
            return config.getApiHostURL();
        }
    }
}
