package com.hackattic;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import org.json.JSONObject;

public class Main {
    String access_token = "ACCESS_TOKEN";
    public static void main(String[] args){
        Main obj = new Main();
        try {
            obj.fetchQRImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void fetchQRImage() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://hackattic.com/challenges/reading_qr/problem?access_token="+access_token)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject responseJSON = new JSONObject(response.body());
        System.out.println(responseJSON.get("image_url"));
        URL imageUrl = URI.create(responseJSON.get("image_url").toString()).toURL();
        String result = readQRImage(imageUrl);
        if(result!=null){
            System.out.println("Sending code...");
            JSONObject body = new JSONObject();
            body.put("code",result);
            System.out.println("Body: "+body.toString());
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://hackattic.com/challenges/reading_qr/solve?access_token="+access_token))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
            HttpResponse<String> postResponse = client.send(postRequest,HttpResponse.BodyHandlers.ofString());
            System.out.println("POST Request response: "+postResponse.body());
        }
    }

    public String readQRImage(URL url) throws IOException {
        BufferedImage image = ImageIO.read(url);

        if(image != null) {
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            QRCodeReader reader = new QRCodeReader();
            try {
                Result result = reader.decode(bitmap);
                System.out.println("QR content: "+ result.getText());
                return result.getText();
            } catch (NotFoundException e){
                System.out.println("No QR found in image");
            } catch (ChecksumException e){
                System.out.println("Checksum exception: "+ e.toString());
            } catch (FormatException e){
                System.out.println("Format exception: "+ e.toString());
            }
        }
        return null;
    }
}