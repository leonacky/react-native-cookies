package com.psykar.cookiemanager;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.modules.network.ForwardingCookieHandler;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CookieManagerModule extends ReactContextBaseJavaModule {

    private ForwardingCookieHandler cookieHandler;

    public CookieManagerModule(ReactApplicationContext context) {
        super(context);
        this.cookieHandler = new ForwardingCookieHandler(context);
    }

    public String getName() {
        return "RNCookieManagerAndroid";
    }

    @ReactMethod
    public void set(ReadableMap cookie, final Callback callback) throws Exception {
        CookieSyncManager.createInstance(getReactApplicationContext());
        CookieManager localCookieManager = CookieManager.getInstance();
        localCookieManager.setAcceptCookie(true);
        localCookieManager.setCookie(cookie.getString("domain"), String.format("%s=%s", cookie.getString("name"), cookie.getString("value")));
        localCookieManager.setCookie(cookie.getString("name"), cookie.getString("value"));
        CookieSyncManager.getInstance().sync();
        callback.invoke(null, null);
    }

    @ReactMethod
    public void setFromResponse(String url, ReadableMap map, final Callback callback) throws URISyntaxException, IOException {
        Map headers = new HashMap<String, List<String>>();
        String value = "";
        ReadableMapKeySetIterator iterator = map.keySetIterator();
        while(iterator.hasNextKey()) {
            String _key = iterator.nextKey();
            String _value = map.getString(_key);
            if(iterator.hasNextKey()) {
                value += _key+"="+_value+"; ";
            } else {
                value += _key+"="+_value;
            }
        }
        // Pretend this is a header
        headers.put("Set-cookie", Collections.singletonList(value));
        URI uri = new URI(url);
        this.cookieHandler.put(uri, headers);
        callback.invoke(null, null);
    }

    @ReactMethod
    public void getAll(Callback callback) throws Exception {
        throw new Exception("Cannot get all cookies on android, try getCookieHeader(url)");
    }

    @ReactMethod
    public void get(String url, Callback callback) throws URISyntaxException, IOException {
        URI uri = new URI(url);

        Map<String, List<String>> cookieMap = this.cookieHandler.get(uri, new HashMap());
        // If only the variables were public
        List<String> cookieList = cookieMap.get("Cookie");
        WritableMap map = Arguments.createMap();
        if (cookieList != null) {
            String[] cookies = cookieList.get(0).split(";");
            for (int i = 0; i < cookies.length; i++) {
                String[] cookie = cookies[i].split("=", 2);
                if(cookie.length > 1) {
                  map.putString(cookie[0].trim(), cookie[1]);
                }
            }
        }
        callback.invoke(null, map);
    }

    @ReactMethod
    public void clearAll(final Callback callback) {
        CookieSyncManager.createInstance(getReactApplicationContext());
        CookieManager localCookieManager = CookieManager.getInstance();
        localCookieManager.setAcceptCookie(true);
        localCookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
        this.cookieHandler.clearCookies(new Callback() {
            public void invoke(Object... args) {
                callback.invoke(null, null);
            }
        });
    }
}
