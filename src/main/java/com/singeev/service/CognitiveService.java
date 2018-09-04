package com.singeev.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singeev.entity.DetectedFace;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class CognitiveService {

    private final static Logger LOGGER = LoggerFactory.getLogger(CognitiveService.class);

    public List<DetectedFace> lookAtThePhoto(String photoUrl) {
        HttpClient httpclient = HttpClients.createDefault();

        try {
            URIBuilder builder = new URIBuilder("https://westeurope.api.cognitive.microsoft.com/face/v1.0/detect");

            builder.setParameter("returnFaceId", "true");
            builder.setParameter("returnFaceLandmarks", "false");
            builder.setParameter("returnFaceAttributes", "age,gender,smile,emotion");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", "320e589da30a4f0eb87d3a54e71b8105");


            // Request body
            String body = "{\n" +
                    "    \"url\": \"" + photoUrl + "\"\n" +
                    "}";
            StringEntity reqEntity = new StringEntity(body);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String json = EntityUtils.toString(entity);
                LOGGER.info("Photo recognized: {}", json);

                ObjectMapper objectMapper = new ObjectMapper();
                List<DetectedFace> facesList = null;
                try {
                    facesList = objectMapper.readValue(json, new TypeReference<List<DetectedFace>>(){});
                } catch (IOException e) {
                    LOGGER.error("Can't parse json: {}", e.getMessage());
                    e.printStackTrace();
                }
                return facesList;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }
}
