package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DiscordNotifier implements BuildListener {
    public DiscordNotifier() {
    }


    @Override
    public void onBuildCompleted(ProjectEntity project, ResultEntity result) {

        String discordHook = "https://discord.com/api/webhooks/1197204619730886727/z4NZDbawY6BZ2ZV1qs4652dJjqnnwMd3MzKB4EPMb9rV3xSVG1oMoO9iXxXHU9iBYeLj";
        String projectName = project.getName();
        long projectId = project.getId();

        String jsonPayload = String.format("{\"content\": \"Build Notification\", \"embeds\": [{\"title\": \"Project Details\", \"fields\": [{\"name\": \"Project Name\", \"value\": \"%s\"}, {\"name\": \"Project ID\", \"value\": \"%s\"}]}]}", projectName, projectId);
        System.out.println("Testing JsonPayLoad: " + jsonPayload);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(discordHook))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




