package org.example;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Bot extends TelegramLongPollingBot {
    private final String OPENAI_API_KEY = "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";



    public void sendText (Long who, String what) {
        SendMessage sm = SendMessage.builder().chatId(who.toString()).text(what).build();
        try{
            execute(sm);
        } catch (TelegramApiException e) {throw new RuntimeException (e);}
    }
  public void sendSticker (Long who, InputFile what ) {
        SendSticker ss = SendSticker.builder()
                .chatId(who)
                .sticker(what).build();

            try{
                execute(ss);
            } catch (TelegramApiException e) {throw new RuntimeException (e);}
  }
    @Override
    public void onUpdateReceived(Update update) {
        String userMessage = update.getMessage().getText();
        String chatGPTResponse = generateChatGPTResponse(userMessage);
        System.out.println(update);

//        sendText(1002654125L, chatGPTResponse);
        if (update.getMessage().getChat().getId() != null) {
            sendText(update.getMessage().getChat().getId(), chatGPTResponse);
        }
        else {
            sendText(update.getMessage().getFrom().getId(), chatGPTResponse);
            sendSticker(update.getMessage().getFrom().getId(),new InputFile(new File("src/image/telegram-cloud-document-2-245582613622817052.webp")));
        }



//       var msg = update.getMessage();
//       var user = msg.getFrom();
//         var lang = user.getLanguageCode();
//        System.out.println(user.getFirstName() + " aka " + user.getUserName() + " wrote "+ msg.getText());

    }
    @Override
    public String getBotUsername() {
        return "HappyBDayIgor_bot";
    }
    public String getBotToken () {
        return "6122184661:AAECm_FMBkh47GIVDmPNk1a_sg3Gla3J5PM";
    }
    public String generateChatGPTResponse(String userMessage) {
        String response = "";


        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .build();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"model\": \"text-davinci-003\", \"prompt\": \"" + userMessage + "\", \"max_tokens\": 1000}");


        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .build();
        System.out.println(body);
        try {
            Response apiResponse = client.newCall(request).execute();
            String responseJson = apiResponse.body().string();


            JSONObject responseObject = new JSONObject(responseJson);
            JSONArray choicesArray = responseObject.getJSONArray("choices");
            JSONObject firstChoice = choicesArray.getJSONObject(0);
            response = firstChoice.getString("text");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return response;
    }
}
