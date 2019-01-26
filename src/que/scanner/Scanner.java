package que.scanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jsoup.Jsoup;

public class Scanner extends Application {

    public Boolean isRunning = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new Image("scanpic.jpg"));
        Button btnScan = new Button();
        btnScan.setText("Scan");
        VBox container = new VBox();
        HBox root = new HBox();
        root.setSpacing(5.0);
        Label lblLadder = new Label("Choose ladder");
        ChoiceBox<String> laddersBox = new ChoiceBox<>();
        TextArea taTeamsDisplay = new TextArea();
        Label madeBylbl = new Label("(c) Yagger of");
        Label icecrownLbl = new Label("Icecrown");
        madeBylbl.setTextFill(Color.BLACK);
        icecrownLbl.setTextFill(Color.BLUE);
        laddersBox.getItems().addAll("2v2", "3v3");
        Insets topInset = new Insets(5,0,0,0);
        HBox.setMargin(lblLadder, topInset);
        HBox.setMargin(laddersBox, new Insets(0, 0, 5, 0));
        HBox.setMargin(madeBylbl, topInset);
        HBox.setMargin(icecrownLbl, topInset);
        root.getChildren().addAll(lblLadder, laddersBox, madeBylbl, icecrownLbl);
        HBox taBox = new HBox();
        taBox.getChildren().add(taTeamsDisplay);
        HBox btnBox = new HBox();
        btnBox.getChildren().add(btnScan);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(5);
        HBox.setMargin(btnScan, new Insets(5, 0, 0, 0));

        container.getChildren().addAll(root, taBox, btnBox);
        btnScan.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                Thread getTeamsThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                            try {
                                isRunning = true;
                                taTeamsDisplay.setText("");
                                String selectedLadder = laddersBox.getSelectionModel().getSelectedItem();
                                String mainPage = null;
                                mainPage = Jsoup.connect("http://armory.warmane.com/ladder/" + selectedLadder + "/Icecrown").get().toString();
                                mainPage = mainPage.replace("/", "&");
                                mainPage = mainPage.replace("\"", "");
                                Pattern p = Pattern.compile("<td><a href=&team&(.*?)&Icecrown&summary>");
                                Matcher m = p.matcher(mainPage);
                                List<String> teams = new ArrayList<>();

                                while (m.find()) {
                                    teams.add(m.group());
                                }

                                for (String s : teams) {
                                    s = s.replace("&", "/");
                                    s = s.replace("<td><a href=/team/", "");
                                    s = s.replace("/Icecrown/summary>", "");
                                    String newLink = s;
                                    newLink = "http://armory.warmane.com/team/" + newLink + "/Icecrown/match-history";
                                    String matchHistory = Jsoup.connect(newLink).get().toString();

                                    matchHistory = matchHistory.replace("\"", "");
                                    matchHistory = matchHistory.replace("/", "");

                                    Pattern p2 = Pattern.compile("<td class=dt-center data-order=(.*?)<td>");
                                    Matcher m2 = p2.matcher(matchHistory);

                                    List<String> teamsPlayed = new ArrayList();

                                    while (m2.find()) {
                                        teamsPlayed.add(m2.group());
                                    }

                                    String lastGame = (teamsPlayed.get(teamsPlayed.size() - 1));
                                    lastGame = lastGame.substring(42);
                                    lastGame = lastGame.replace("<td>", "");
                                    s = s.replace("+", " ");
                                    String finalString = lastGame + " " + "team " + s + " on Icecrown";
                                    System.out.println(finalString);
                                    if (lastGame.toLowerCase().contains("minutes ago")) {
                                        String teamText = taTeamsDisplay.getText();
                                        taTeamsDisplay.setText(teamText + finalString + "\n");
                                        System.out.println(finalString);
                                    }
                                }
                                isRunning = false;
                            } catch (Exception ex) {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not connect to Warmane server", ButtonType.OK);
                                alert.show();
                            }
                            
                        }
                    
                });
                
                if (!isRunning) getTeamsThread.start();
                
            }
        });
        Scene scene = new Scene(container, 300, 240);
        GridPane.setHalignment(btnScan, HPos.RIGHT);
        primaryStage.setTitle("Warmane Queue Scanner");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
