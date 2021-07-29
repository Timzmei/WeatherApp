import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;


public class Main {
    public static void main(String[] args) throws IOException {
        final URL url = new URL("https://api.openweathermap.org/data/2.5/onecall?lat=54.775002&lon=56.037498&exclude=current,minutely,hourly,alerts&appid=5bb528d90985eeb90ef1fea3021e44af&units=metric&lang=ru");
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String json = getResponse(con);
        con.disconnect();
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        printWeather(convertedObject);

    }

    public static String getResponse(HttpURLConnection con){
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            final StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            return content.toString();
        } catch (final Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    private static void printWeather(JsonObject jsonObject){
        System.out.printf("Координаты города: с.ш. %s, в.д. %s\n", jsonObject.get("lat").toString(), jsonObject.get("lon").toString());
        JsonArray daily = jsonObject.get("daily").getAsJsonArray();

        double diffNightMorn = 30d;
        long pressureDay = 0;
        long diffDay = 0;
        int pressure = 0;

        for (int i = 0; i < 5; i++){
            JsonObject dayObject = daily.get(i).getAsJsonObject();
            if (pressure < dayObject.get("pressure").getAsInt()){
                pressure = dayObject.get("pressure").getAsInt();
                pressureDay = dayObject.get("dt").getAsLong();
            }
            JsonObject tempObject = dayObject.get("temp").getAsJsonObject();
            Double newDiff = (Double) (tempObject.get("night").getAsDouble() - tempObject.get("morn").getAsDouble());
            if (diffNightMorn > newDiff){
                diffNightMorn = newDiff;
                diffDay = dayObject.get("dt").getAsLong();
            }
        }

        System.out.printf("Максимальное давление за предстоящие 5 дней (включая текущий): %s, дата: %s\n", pressure, Instant.ofEpochMilli(pressureDay * 1000).atZone(ZoneId.of("UTC")).toLocalDate());
        System.out.printf("День с минимальной разницей между ночной и утренней температурой : %.2f, дата: %s\n", diffNightMorn, Instant.ofEpochMilli(diffDay * 1000).atZone(ZoneId.of("UTC")).toLocalDate());
    }
}
