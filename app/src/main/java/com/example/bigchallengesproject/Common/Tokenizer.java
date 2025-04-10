package com.example.bigchallengesproject.Common;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Tokenizer {
    private Map<String, Integer> vocab;
    private Map<Integer, String> idToToken;
    private int startTokenId;
    private int endTokenId;

    public Tokenizer(Context context) throws IOException {
        vocab = new HashMap<>();
        idToToken = new HashMap<>();
//        loadVocab(context, "vocab.json");

        startTokenId = vocab.get("<s>");
        endTokenId = vocab.get("</s>");
    }

    private void loadVocab(Context context, String vocabFile) throws IOException, JSONException {
        InputStream inputStream = context.getAssets().open(vocabFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder jsonString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) jsonString.append(line);
        reader.close();

        JSONObject jsonObject = new JSONObject(jsonString.toString());
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            int value = jsonObject.getInt(key);
            vocab.put(key, value);
            idToToken.put(value, key);
        }
    }

    public int getStartTokenId() {
        return startTokenId;
    }

    public int getEndTokenId() {
        return endTokenId;
    }

    public int[] tokenize(String text) {
        String[] tokens = text.split(" ");
        int[] tokenIds = new int[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            tokenIds[i] = vocab.getOrDefault(tokens[i], vocab.get("<unk>"));
        }
        return tokenIds;
    }

    public String decode(List<Integer> tokenIds) {
        StringBuilder decodedText = new StringBuilder();
        for (int tokenId : tokenIds) {
            if (tokenId == endTokenId) break;
            decodedText.append(idToToken.getOrDefault(tokenId, "")).append(" ");
        }
        return decodedText.toString().trim();
    }
}