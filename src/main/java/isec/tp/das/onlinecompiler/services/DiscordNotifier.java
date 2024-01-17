package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class DiscordNotifier implements BuildListener{
    public DiscordNotifier() {
    }
    @Override
    public void onBuildCompleted(ProjectEntity project, ResultEntity result) {
        String discordHook = "https://discord.com/api/webhooks/1197204619730886727/z4NZDbawY6BZ2ZV1qs4652dJjqnnwMd3MzKB4EPMb9rV3xSVG1oMoO9iXxXHU9iBYeLj";

        // Construct the message
        String message = String.format(
                "Build Notification:\nProject Name: %s\nProject ID: %d\nResult: %s",
                project.getName(),
                project.getId(),
                result.getMessage()
        );

        try {
            URL url = new URL(discordHook);
            String jsonPayload = "{\"content\":\"" + message + "\"}";
            byte[] out = jsonPayload.getBytes();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setDoOutput(true);
            http.setRequestMethod("POST");
            http.addRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Content-Length", String.valueOf(out.length));


            OutputStream stream = http.getOutputStream();
            stream.write(out);

            System.out.println("Response Code: " + http.getResponseCode());
            System.out.println("Response Message: " + http.getResponseMessage());

            http.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



