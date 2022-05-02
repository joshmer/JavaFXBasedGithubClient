import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class HTTPService extends Service<String> {
    URL url;

    HTTPService(URL url) {
        this.url = url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    protected Task<String> createTask() {

        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                StringBuilder response = null;
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                } catch (Exception ex) {
                    return "";
                }
                return response.toString();
            }
        };
    }

}
