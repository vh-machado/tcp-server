package com.tcpserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class TCPServlet extends HttpServlet {

    public static JsonArray shuffleJsonArray (JsonArray array) throws JsonIOException {
        // Implementing Fisher–Yates shuffle
            Random rnd = new Random();
            for (int i = array.size() - 1; i >= 0; i--)
            {
              int j = rnd.nextInt(i + 1);
              // Simple swap
              JsonElement object = array.get(j);
              array.set(j, array.get(i));
              array.set(i, object);
            }
        return array;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Obter a parte "keywords" da URI da solicitação
        String uri = request.getRequestURI();
        String[] uriParts = uri.split("/");

        System.out.println("Keywords recebidas:" + uriParts[2]);
        
        if (uriParts.length > 2) {
            String[] receivedKeywords = uriParts[2].split(",");

            if (receivedKeywords != null && receivedKeywords.length > 0) {

                ArrayList<String> keywordsIDs = new ArrayList<String>();
                OkHttpClient client = new OkHttpClient();

                for(int i = 0; i < receivedKeywords.length; i++){
                    try {

                        // Realizar a pesquisa de palavras-chave
                        Request requestKeyWord = new Request.Builder()
                                .url("https://api.themoviedb.org/3/search/keyword?query=" + receivedKeywords[i] + "&page=1")
                                .get()
                                .addHeader("accept", "application/json")
                                .addHeader("Authorization",
                                        "Bearer {TMDB_API_KEY}")
                                .build();

                        Response responseKeyWord = client.newCall(requestKeyWord).execute();

                        // Obter o ID da palavra-chave
                        String jsonKeyWord = responseKeyWord.body().string();
                        JsonObject jsonObjectKeyWord = JsonParser.parseString(jsonKeyWord).getAsJsonObject();
                        JsonArray jsonArrayKeyWord = jsonObjectKeyWord.getAsJsonArray("results");
                        JsonObject jsonObjectArrayKeyWord = jsonArrayKeyWord.get(0).getAsJsonObject();

                        keywordsIDs.add(jsonObjectArrayKeyWord.get("id").getAsString());

                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().println("Erro ao processar a solicitação.");
                    }
                }

                String keywordsIDsFormatted = String.join(",",keywordsIDs);

                if (keywordsIDs != null && keywordsIDs.size() > 0) {
                    try {
                        // Realizar a pesquisa de filmes com base na palavra-chave
                        Request requestMovies = new Request.Builder()
                                .url("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_keywords="
                                        + keywordsIDsFormatted)
                                .get()
                                .addHeader("accept", "application/json")
                                .addHeader("Authorization",
                                        "Bearer {TMDB_API_KEY}")
                                .build();

                        Response responseMovies = client.newCall(requestMovies).execute();

                        String jsonMovies = responseMovies.body().string();
                        JsonObject jsonObjectMovies = JsonParser.parseString(jsonMovies).getAsJsonObject();
                        JsonArray jsonArrayMovies = jsonObjectMovies.getAsJsonArray("results");
                        jsonArrayMovies = shuffleJsonArray(jsonArrayMovies);

                        //System.out.println(jsonArrayMovies);

                        if (jsonArrayMovies.size() != 0) {
                            // Construir uma resposta JSON com base nas informações dos filmes
                            JsonObject jsonResponse = new JsonObject();
                            JsonArray jsonArray = new JsonArray();

                            for (int i = 0; i < 4 && i < jsonArrayMovies.size(); i++) {
                                JsonObject jsonObjectArray = jsonArrayMovies.get(i).getAsJsonObject();
                                String title = jsonObjectArray.get("title").getAsString();
                                String releaseDate = jsonObjectArray.get("release_date").getAsString();
                                String year = releaseDate.split("-")[0];
                                String posterPath = jsonObjectArray.get("poster_path").getAsString();
                                String imageUrl = "https://image.tmdb.org/t/p/original" + posterPath;

                                JsonObject movieJson = new JsonObject();
                                movieJson.addProperty("title", title);
                                movieJson.addProperty("poster_path", imageUrl);
                                movieJson.addProperty("release_date", year);
                                jsonArray.add(movieJson);
                            }


                            jsonResponse.add("movies", jsonArray);

                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");

                            PrintWriter writer = response.getWriter();
                            writer.println(jsonResponse.toString());
                            System.out.println("Recomendações enviadas.");

                        } else {
                            JsonObject jsonResponse = new JsonObject();
                            jsonResponse.add("movies", new JsonArray()); // Nenhum filme encontrado

                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");

                            PrintWriter writer = response.getWriter();
                            writer.println(jsonResponse.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().println("Erro ao processar a solicitação.");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("Keywords não encontradas.");
                }

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Parâmetro 'keywords' vazio na solicitação.");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Parâmetro 'keywords' ausente na solicitação.");
        }
    }
}
