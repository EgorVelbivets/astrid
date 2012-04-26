package com.todoroo.astrid.service.abtesting;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.service.RestClient;

@SuppressWarnings("nls")
public class ABTestReporter {

    /** NOTE: these values are development values and will not work on production */
    private static final String URL = "http://analytics.astrid.com/api/1/retention";
    private static final String API_KEY = "ryyubd";
    private static final String API_SECRET = "q9ef3i";

    @Autowired private RestClient restClient;

    public ABTestReporter() {
        DependencyInjectionService.getInstance().inject(this);
    }

    public JSONObject post(JSONArray payload) throws IOException {
        try {
            HttpEntity postData  = createPostData(payload);
            if (postData == null)
                throw new IOException("Unsupported URL encoding");
            String response = restClient.post(URL, postData);
            JSONObject object = new JSONObject(response);
            if (object.getString("status").equals("error")) {
                throw new IOException("Error reporting ABTestEvent: " +
                        object.optString("message"));
            }
            return object;
        } catch (JSONException e) {
            throw new IOException(e.getMessage());
        }

    }

    private HttpEntity createPostData(JSONArray payload) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("apikey", API_KEY));
        params.add(new BasicNameValuePair("payload", payload.toString()));

        StringBuilder sigBuilder = new StringBuilder();
        for(NameValuePair entry : params) {
            if(entry.getValue() == null)
                continue;

            String key = entry.getName();
            String value = entry.getValue();

            sigBuilder.append(key).append(value);
        }

        sigBuilder.append(API_SECRET);
        String signature = DigestUtils.md5Hex(sigBuilder.toString());
        params.add(new BasicNameValuePair("sig", signature));

        try {
            return new UrlEncodedFormEntity(params, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
