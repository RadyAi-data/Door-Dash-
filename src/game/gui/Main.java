package game.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.beans.binding.Bindings;

// --- ENGINE IMPORTS ---
import game.engine.Game;
import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;
import game.engine.cards.Card;
import game.engine.cells.Cell;
import game.engine.cells.DoorCell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.*;

public class Main extends Application {

    // ─── GUI VARIABLES ────────────────────────────────────────────────────────
    private MediaPlayer mediaPlayer;
    private MediaPlayer sfxPlayer; 
    private String currentAudioFile = ""; 
    private Game activeGame;

    // ─── DEBUG LOGGER ─────────────────────────────────────────────────────────
    private static final boolean DEBUG_MODE = true;

    private void debugLog(String message) {
        if (DEBUG_MODE) {
            System.out.println("[DEBUG-SYNC] " + message);
        }
    }

    // ─── INVALID ACTION POPUP ─────────────────────────────────────────────────
    private void showInvalidActionPopup(String header, String reason) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Action");
            alert.setHeaderText(header);
            alert.setContentText(reason);
            alert.showAndWait(); 
        });
    }

    // ─── HELPER FOR MONSTER TRAITS ────────────────────────────────────────────
    private String getMonsterTrait(Monster m) {
        if (m instanceof Dasher) return " (x2 Speed)";
        if (m instanceof Dynamo) return " (x2 Energy Diff)";
        if (m instanceof MultiTasker) return " (0.5x Speed, +200 Energy)";
        if (m instanceof Schemer) return " (+10 Eng Gain/Loss)";
        return "";
    }

    // ─── SCENES ───────────────────────────────────────────────────────────────

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Door Dash");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        
        primaryStage.setScene(createTitleScene(primaryStage));
        
        primaryStage.setMaximized(true); 
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private Scene createTitleScene(Stage stage) {
        StackPane root = new StackPane();
        double w = stage.getWidth() > 0 ? stage.getWidth() : 1024;
        double h = stage.getHeight() > 0 ? stage.getHeight() : 768;
        Scene scene = new Scene(root, w, h);
        
        stage.getIcons().add(new Image("gameNameTitle.png"));

        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);
        ImageView layer2 = new ImageView(new Image("titlegradientRectangle.png"));
        layer2.setPreserveRatio(false);

        ImageView titleLogo = new ImageView(new Image("gameNameTitle.png"));
        titleLogo.setPreserveRatio(true);
        titleLogo.fitWidthProperty().bind(root.widthProperty().multiply(0.4));

        Pane titleLayer = new Pane();
        titleLogo.layoutXProperty().bind(
            root.widthProperty().multiply(0.25)
            .subtract(root.widthProperty().multiply(0.4).divide(2))
        );
        titleLogo.layoutYProperty().bind(
            root.heightProperty().multiply(0.5)
            .subtract(titleLogo.fitWidthProperty().divide(2))
        );
        titleLayer.getChildren().add(titleLogo);
        titleLayer.setPickOnBounds(false);

        StackPane playBtn = createImageButton("titlescreenbuttonbackground.png", "PLAY.png", root, 0.22, 0.18, 0.8);
        StackPane creditsBtn = createImageButton("titlescreenbuttonbackground.png", "CREDITS.png", root, 0.22, 0.18, 0.6);
        playBtn.setOnMouseClicked(e -> stage.setScene(createInstructionsScene(stage)));
        creditsBtn.setOnMouseClicked(e -> stage.setScene(createCreditsScene(stage)));

        playBtn.setPickOnBounds(true);
        creditsBtn.setPickOnBounds(true);

        playBtn.layoutXProperty().bind(
            root.widthProperty().multiply(0.75)
            .subtract(root.widthProperty().multiply(0.01).divide(2))
        );
        playBtn.layoutYProperty().bind(
            root.heightProperty().multiply(0.55)
            .subtract(root.heightProperty().multiply(0.18).divide(2))
        );
        creditsBtn.layoutXProperty().bind(
            root.widthProperty().multiply(0.95)
            .subtract(root.widthProperty().multiply(0.40).divide(2))
        );
        creditsBtn.layoutYProperty().bind(
            root.heightProperty().multiply(0.73)
            .subtract(root.heightProperty().multiply(0.18).divide(2))
        );
        Pane buttonLayer = new Pane();
        buttonLayer.getChildren().addAll(playBtn, creditsBtn);
        buttonLayer.setPickOnBounds(false);

        root.getChildren().addAll(background, layer2, titleLayer, buttonLayer);

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        layer2.fitWidthProperty().bind(root.widthProperty());
        layer2.fitHeightProperty().bind(root.heightProperty());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.setFullScreen(!stage.isFullScreen());
        });
        playAudio("MonstersTheme.mp3");
        return scene;
    }

    private Scene createInstructionsScene(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);

        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);

        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.65);
        overlay.widthProperty().bind(root.widthProperty());
        overlay.heightProperty().bind(root.heightProperty());

        Label title = new Label("HOW TO PLAY");
        title.setStyle(
            "-fx-font-size: 50px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #ff6b35;" +
            "-fx-font-family: 'Impact';"
        );
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        Label instructions = new Label(
                "OBJECTIVE\n" +
                        "Reach Boo's Door (Cell 100/99) first with 1000+ energy.\n\n" +

                        "TURN SEQUENCE\n" +
                        "1. Optional Powerup — Pay 500 energy to activate.\n" +
                        "2. Dice Roll — Roll a 6-sided dice.\n" +
                        "3. Movement — Move forward. Re-roll if destination is occupied.\n\n" +

                        "CELL TYPES\n" +
                        "• Door Cells — Matching role gains energy; mismatch loses it.\n" +
                        "• Monster Cells — Same role gives a free powerup; opposite swaps energy.\n" +
                        "• Conveyor Belts / Socks — Move you forward, or send you back and drain 100 energy.\n" +
                        "• Card Cells — Draw and resolve an action card.\n" +
                        "• Normal Cells — No effect.\n\n" +

                        "WIN CONDITION\n" +
                        "Have 1000+ energy at the final cell. Press F to play"
        );
        instructions.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Arial';" +
            "-fx-line-spacing: 3px;"
        );
        instructions.setWrapText(true);
        instructions.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        instructions.setAlignment(Pos.CENTER);
        instructions.setMaxWidth(Double.MAX_VALUE);
        VBox content = new VBox(15, title, instructions);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40, 40, 40, 40));

        root.getChildren().addAll(background, overlay, content);

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        content.maxWidthProperty().bind(root.widthProperty().multiply(0.8));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F)
                stage.setScene(createRoleSelectionScene(stage));
        });
        
        // Starts the loop for the rest of the game
        playAudio("pizzaParlor.mp3"); 
        return scene;
    }

    private Scene createRoleSelectionScene(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);
        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);

        ImageView scarerGroup = new ImageView(new Image("ScarerPickGroup.png"));
        scarerGroup.setPreserveRatio(true);
        scarerGroup.fitWidthProperty().bind(root.widthProperty().multiply(0.30));

        ImageView scarerDesc = new ImageView(new Image("ScarerTitleDesc.png"));
        scarerDesc.setPreserveRatio(true);
        scarerDesc.fitWidthProperty().bind(root.widthProperty().multiply(0.20));
        ImageView laugherGroup = new ImageView(new Image("LaugherPickGroup.png"));
        laugherGroup.setPreserveRatio(true);
        laugherGroup.fitWidthProperty().bind(root.widthProperty().multiply(0.30));

        ImageView laugherDesc = new ImageView(new Image("LaugherTitleDesc.png"));
        laugherDesc.setPreserveRatio(true);
        laugherDesc.fitWidthProperty().bind(root.widthProperty().multiply(0.20));

        scarerGroup.layoutXProperty().bind(root.widthProperty().multiply(0.25).subtract(root.widthProperty().multiply(0.30).divide(2)));
        scarerGroup.layoutYProperty().bind(root.heightProperty().multiply(0.20));
        scarerDesc.layoutXProperty().bind(root.widthProperty().multiply(0.25).subtract(root.widthProperty().multiply(0.20).divide(2)));
        scarerDesc.layoutYProperty().bind(root.heightProperty().multiply(0.55));
        laugherGroup.layoutXProperty().bind(root.widthProperty().multiply(0.75).subtract(root.widthProperty().multiply(0.30).divide(2)));
        laugherGroup.layoutYProperty().bind(root.heightProperty().multiply(0.20));
        laugherDesc.layoutXProperty().bind(root.widthProperty().multiply(0.75).subtract(root.widthProperty().multiply(0.20).divide(2)));
        laugherDesc.layoutYProperty().bind(root.heightProperty().multiply(0.55));

        scarerGroup.setOnMouseClicked(e -> startGame(stage, Role.SCARER));
        scarerDesc.setOnMouseClicked(e -> startGame(stage, Role.SCARER));
        laugherGroup.setOnMouseClicked(e -> startGame(stage, Role.LAUGHER));
        laugherDesc.setOnMouseClicked(e -> startGame(stage, Role.LAUGHER));
        scarerGroup.setOnMouseEntered(e -> scarerGroup.setOpacity(0.8));
        scarerGroup.setOnMouseExited(e -> scarerGroup.setOpacity(1.0));
        scarerDesc.setOnMouseEntered(e -> scarerDesc.setOpacity(0.8));
        scarerDesc.setOnMouseExited(e -> scarerDesc.setOpacity(1.0));
        laugherGroup.setOnMouseEntered(e -> laugherGroup.setOpacity(0.8));
        laugherGroup.setOnMouseExited(e -> laugherGroup.setOpacity(1.0));
        laugherDesc.setOnMouseEntered(e -> laugherDesc.setOpacity(0.8));
        laugherDesc.setOnMouseExited(e -> laugherDesc.setOpacity(1.0));

        scarerGroup.setStyle("-fx-cursor: hand;");
        scarerDesc.setStyle("-fx-cursor: hand;");
        laugherGroup.setStyle("-fx-cursor: hand;");
        laugherDesc.setStyle("-fx-cursor: hand;");

        Pane contentLayer = new Pane();
        contentLayer.getChildren().addAll(scarerGroup, scarerDesc, laugherGroup, laugherDesc);
        contentLayer.setPickOnBounds(false);

        root.getChildren().addAll(background, contentLayer);

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.setScene(createTitleScene(stage));
        });
        
        // Ensure pizza parlor plays (if coming from Win screen "Play Again" it switches, if coming from instructions it seamlessly continues)
        playAudio("pizzaParlor.mp3");
        return scene;
    }

    private Scene createCreditsScene(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        Rectangle background = new Rectangle();
        background.setFill(Color.BLACK);
        background.widthProperty().bind(root.widthProperty());
        background.heightProperty().bind(root.heightProperty());
        Label title = new Label("GAME MADE BY TEAM 188");
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #ff6b35; -fx-font-family: 'Impact';");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        String[] members = {
            "  Youssef Ashraf Saber", "  Amr Mohamed Mossad Kandeel",
            "  Omar Osama Ahmed Rady", "  Abdulrahman Emad Eldin Adel Soliman Yousry"
        };
        VBox memberList = new VBox(20);
        memberList.setAlignment(Pos.CENTER);
        memberList.setMaxWidth(Double.MAX_VALUE);

        for (String member : members) {
            Label memberLabel = new Label(member);
            memberLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-family: 'Georgia';");
            memberLabel.setMaxWidth(Double.MAX_VALUE);
            memberLabel.setAlignment(Pos.CENTER);
            memberList.getChildren().add(memberLabel);
        }

        Label hint = new Label("Press ESC to go back");
        hint.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaaaaa; -fx-font-family: 'Georgia';");
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setAlignment(Pos.CENTER);

        VBox content = new VBox(50, title, memberList, hint);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setPadding(new Insets(60, 40, 40, 40));

        root.getChildren().addAll(background, content);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.setScene(createTitleScene(stage));
        });
        return scene;
    }

    // ─── POST-GAME ROLLING CREDITS SCENE ──────────────────────────────────────
    private Scene createPostGameCreditsScene(Stage stage, Scene winScene) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        Rectangle background = new Rectangle();
        background.setFill(Color.BLACK);
        background.widthProperty().bind(root.widthProperty());
        background.heightProperty().bind(root.heightProperty());
        
        // Define components
        Label title = new Label("GAME MADE BY TEAM 188");
        title.setStyle("-fx-font-size: 52px; -fx-font-weight: bold; -fx-text-fill: #ff6b35; -fx-font-family: 'Impact';");
        
        VBox memberList = new VBox(30);
        memberList.setAlignment(Pos.CENTER);
        String[] members = { "Youssef Ashraf Saber", "Amr Mohamed Mossad Kandeel", "Omar Osama Ahmed Rady", "Abdulrahman Emad Eldin Adel Soliman Yousry" };
        for (String member : members) {
            Label memberLabel = new Label(member);
            memberLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: white; -fx-font-family: 'Georgia';");
            memberList.getChildren().add(memberLabel);
        }

        Label hint = new Label("Press ESC to skip to Win Screen");
        hint.setStyle("-fx-font-size: 20px; -fx-text-fill: #aaaaaa; -fx-font-family: 'Georgia';");

        VBox content = new VBox(60, title, memberList, hint);
        content.setAlignment(Pos.CENTER);
        
        // 1. Immediately hide it to prevent the split-second center flash
        content.setOpacity(0);
        
        root.getChildren().addAll(background, content);

        Timeline rollTimeline = new Timeline();
        rollTimeline.setCycleCount(1);
        
        // 2. Setup the skip logic
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                rollTimeline.stop();
                stage.setScene(winScene);
            }
        });

        // 3. Wait for JavaFX to calculate exact heights, then start rolling
        Platform.runLater(() -> {
            // Start perfectly at the bottom edge of the window
            double startY = scene.getHeight(); 
            // End exactly when the bottom of the text clears the top of the window
            double endY = -content.getHeight() - 150; 
            
            // Snap it off-screen and make it visible
            content.setTranslateY(startY);
            content.setOpacity(1); 
            
            // Create a perfectly calculated smooth roll
            KeyFrame startFrame = new KeyFrame(Duration.ZERO, 
                new javafx.animation.KeyValue(content.translateYProperty(), startY, Interpolator.LINEAR)
            );
            KeyFrame endFrame = new KeyFrame(Duration.seconds(18), 
                new javafx.animation.KeyValue(content.translateYProperty(), endY, Interpolator.LINEAR)
            );
            
            rollTimeline.getKeyFrames().addAll(startFrame, endFrame);
            rollTimeline.setOnFinished(e -> stage.setScene(winScene));
            
            // Slight delay before movement begins
            PauseTransition delay = new PauseTransition(Duration.millis(300));
            delay.setOnFinished(e -> rollTimeline.play());
            delay.play();
        });

        return scene;
    }

    // ─── WIN SCENE ────────────────────────────────────────────────────────────
    private Scene createWinScene(Stage stage, Monster winner, Monster loser, boolean creditsRolled) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());

        ImageView background = new ImageView(new Image("Gameoverscreenbg.png"));
        background.setPreserveRatio(false);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());

        String winnerRoleStr = winner.getRole() == Role.SCARER ? "SCARER" : "LAUGHER";
        String loserRoleStr  = loser.getRole()  == Role.SCARER ? "SCARER" : "LAUGHER";

        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.BLACK);
        textShadow.setRadius(4);
        textShadow.setSpread(0.6);

        // --- WINNER PANEL (Text + Custom Win Image) ---
        Label winnerHeader = new Label("Winner:");
        winnerHeader.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: white;");
        winnerHeader.setEffect(textShadow);

        Label winnerName   = new Label(winner.getName());
        winnerName.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        winnerName.setEffect(textShadow);

        Label winnerRole   = new Label(winnerRoleStr);
        winnerRole.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        winnerRole.setEffect(textShadow);

        Label winnerEnergy = new Label("Final Energy: " + winner.getEnergy());
        winnerEnergy.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        winnerEnergy.setEffect(textShadow);

        VBox winnerText = new VBox(2, winnerHeader, winnerName, winnerRole, winnerEnergy);
        winnerText.setAlignment(Pos.CENTER_LEFT);

        // Load the custom win image (Shrunk size to prevent box stretching)
        ImageView winnerImg = new ImageView(new Image(getWinningMonsterImageByName(winner.getName())));
        winnerImg.setPreserveRatio(true);
        winnerImg.fitHeightProperty().bind(root.heightProperty().multiply(0.25)); 
        
        // Put the text and the single image side-by-side (Reduced spacing from 60 to 20)
        HBox winnerBox = new HBox(20, winnerText, winnerImg);
        winnerBox.setAlignment(Pos.CENTER_LEFT);


        // --- LOSER PANEL (Text + Normal Image) ---
        Label loserHeader = new Label("Loser:");
        loserHeader.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: white;");
        loserHeader.setEffect(textShadow);

        Label loserName   = new Label(loser.getName());
        loserName.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        loserName.setEffect(textShadow);

        Label loserRole   = new Label(loserRoleStr);
        loserRole.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        loserRole.setEffect(textShadow);

        Label loserEnergy = new Label("Final Energy: " + loser.getEnergy());
        loserEnergy.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        loserEnergy.setEffect(textShadow);

        VBox loserText = new VBox(2, loserHeader, loserName, loserRole, loserEnergy);
        loserText.setAlignment(Pos.CENTER_LEFT);

        // USING THE NORMAL IMAGES FOR THE LOSER HERE (Shrunk size to prevent box stretching)
        ImageView loserImg = new ImageView(new Image(getMonsterImageByName(loser.getName())));
        loserImg.setPreserveRatio(true);
        loserImg.fitHeightProperty().bind(root.heightProperty().multiply(0.20)); 
        loserImg.setOpacity(0.85); // Faded

        // Reduced spacing from 60 to 20
        HBox loserBox = new HBox(20, loserText, loserImg);
        loserBox.setAlignment(Pos.CENTER_LEFT);


        // --- COMBINE BOTH PANELS ---
        VBox statsPanel = new VBox(15, winnerBox, loserBox); 
        
        // Background box behind the winner and loser area (Reduced padding)
        statsPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55); -fx-padding: 20px; -fx-background-radius: 15px;");
        statsPanel.setMaxWidth(VBox.USE_PREF_SIZE);
        statsPanel.setMaxHeight(VBox.USE_PREF_SIZE);
        
        // Pinned to Top Left
        statsPanel.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(statsPanel, Pos.TOP_LEFT);
        
        // Insets: Pushed further up and to the left to clear the bottom text
        StackPane.setMargin(statsPanel, new Insets(30, 0, 0, 40)); 


        Font.loadFont(new File("IrishGrover-Regular.ttf").toURI().toString(), 10);
        String teamName = winner.getRole() == Role.SCARER ? "SCARERS" : "LAUGHERS";
        Label bigWinLabel = new Label(teamName + " WIN");
        
        // Slightly reduced the multiplier from 0.11 to 0.10 to help with spacing
        bigWinLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Irish Grover';" +
            "-fx-font-size: ",
            root.widthProperty().multiply(0.10).asString("%.0f"),
            "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: black;"
        ));
        
        DropShadow winTextShadow = new DropShadow();
        winTextShadow.setColor(Color.WHITE); 
        winTextShadow.setRadius(3); 
        winTextShadow.setSpread(1.0); 
        bigWinLabel.setEffect(winTextShadow);

        StackPane.setAlignment(bigWinLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(bigWinLabel, new Insets(0, 0, 80, 0));


        // --- BUTTONS (MOVED TO TOP RIGHT) ---
        Button playAgainBtn = new Button("PLAY AGAIN");
        playAgainBtn.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Impact'; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        playAgainBtn.setOnMouseEntered(e -> playAgainBtn.setStyle("-fx-background-color: #e05520; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Impact'; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;"));
        playAgainBtn.setOnMouseExited(e -> playAgainBtn.setStyle("-fx-background-color: #ff6b35; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Impact'; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;"));

        Button menuBtn = new Button("MAIN MENU");
        menuBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Impact'; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        menuBtn.setOnMouseEntered(e -> menuBtn.setStyle("-fx-background-color: #2170a0; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Impact'; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;"));
        menuBtn.setOnMouseExited(e -> menuBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-family: 'Impact'; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;"));

        HBox buttonRow = new HBox(20, playAgainBtn, menuBtn);
        buttonRow.setAlignment(Pos.CENTER);
        
        // This stops the box from stretching across the entire screen
        buttonRow.setMaxWidth(HBox.USE_PREF_SIZE);
        buttonRow.setMaxHeight(HBox.USE_PREF_SIZE);
        
        // Position to top right
        StackPane.setAlignment(buttonRow, Pos.TOP_RIGHT);
        StackPane.setMargin(buttonRow, new Insets(40, 40, 0, 0)); 

        // Credits delay logic
        PauseTransition creditsDelay = new PauseTransition(Duration.seconds(8));
        if (!creditsRolled) {
            creditsDelay.setOnFinished(e -> stage.setScene(createPostGameCreditsScene(stage, scene)));
            creditsDelay.play();
        }

        playAgainBtn.setOnAction(e -> {
            if (!creditsRolled) creditsDelay.stop();
            stage.setScene(createRoleSelectionScene(stage));
        });

        menuBtn.setOnAction(e -> {
            if (!creditsRolled) creditsDelay.stop();
            stage.setScene(createTitleScene(stage));
        });

        // Intro animation for big text
        bigWinLabel.setOpacity(0);
        bigWinLabel.setScaleX(0.8);
        bigWinLabel.setScaleY(0.8);
        FadeTransition ftWin = new FadeTransition(Duration.seconds(1.2), bigWinLabel);
        ftWin.setFromValue(0); ftWin.setToValue(1);
        ScaleTransition stWin = new ScaleTransition(Duration.seconds(1.2), bigWinLabel);
        stWin.setToX(1); stWin.setToY(1);
        new ParallelTransition(ftWin, stWin).play();

        // Updated root assembly
        root.getChildren().addAll(background, statsPanel, bigWinLabel, buttonRow);

        playAudio("Win.mp3");
        return scene;
    }

    // ─── START GAME LOGIC ─────────────────────────────────────────────────────
    private void startGame(Stage stage, Role chosenRole) {
        try {
            activeGame = new Game(chosenRole);
            debugLog("=== GAME STARTED ===");
            debugLog("Player 1: " + activeGame.getPlayer().getName() + " (" + activeGame.getPlayer().getClass().getSimpleName() + ")");
            debugLog("Player 2: " + activeGame.getOpponent().getName() + " (" + activeGame.getOpponent().getClass().getSimpleName() + ")");
            stage.setScene(createGameBoardScene(stage, chosenRole));
        } catch (Exception ex) {
            System.out.println("Failed to load game data! Check CSV files.");
            ex.printStackTrace();
        }
    }

    private void setupDiffLabel(Label label) {
        label.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 24px; -fx-font-weight: bold;");
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        ds.setRadius(3);
        ds.setSpread(0.8);
        label.setEffect(ds);
        label.setOpacity(0);
    }

    private Scene createGameBoardScene(Stage stage, Role playerRole) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);
        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);
        
        // Track Original Roles for Confusion Logic
        final Role p1OrigRole = activeGame.getPlayer().getRole();
        final Role p2OrigRole = activeGame.getOpponent().getRole();

        // State trackers for enforcing game logic rules
        final boolean[] isAnimating = {true}; // Initially true to block early moves
        final boolean[] mustDrawCard = {false};
        final boolean[] isRolling = {false};
        final Card[] pendingVisualCard = {null};
        
        final ImageView[] doorIcons = new ImageView[100];
        final Map<Monster, Label> monsterEnergyLabels = new HashMap<>();
        final Map<Monster, Label> monsterDiffLabels = new HashMap<>();
        
        final boolean[] isEffectDelayed = {false};
        final int[] delayedP1Pos = {0};
        final int[] delayedP2Pos = {0};
        final int[] delayedP1Energy = {1000};
        final int[] delayedP2Energy = {1000};
        final Monster[] delayedCurrentTurn = {null};
        
        final int[] prevP1Pos = {0};
        final int[] prevP2Pos = {0};
        
        final Map<Monster, Integer> previousEnergies = new HashMap<>();
        previousEnergies.put(activeGame.getPlayer(), activeGame.getPlayer().getEnergy());
        previousEnergies.put(activeGame.getOpponent(), activeGame.getOpponent().getEnergy());
        for (Monster m : Board.getStationedMonsters()) {
            previousEnergies.put(m, m.getEnergy());
        }
        
        final SequentialTransition[] p1Anim = {null};
        final SequentialTransition[] p2Anim = {null};

        DropShadow ds = new DropShadow();
        ds.setRadius(4.0); ds.setOffsetX(2.0); ds.setOffsetY(2.0);
        ds.setColor(Color.color(0, 0, 0, 0.6));

        // Dynamically assign token colors based on Role (Scarer = Red, Laugher = Blue)
        Color p1Color = p1OrigRole == Role.SCARER ? Color.web("#e74c3c") : Color.web("#3498db");
        Color p2Color = p2OrigRole == Role.SCARER ? Color.web("#e74c3c") : Color.web("#3498db");

        Circle playerToken = new Circle();
        playerToken.radiusProperty().bind(root.heightProperty().multiply(0.010)); 
        playerToken.setFill(p1Color);
        playerToken.setStroke(Color.WHITE);
        playerToken.setStrokeWidth(2);
        playerToken.setEffect(ds);
        
        Circle opponentToken = new Circle();
        opponentToken.radiusProperty().bind(root.heightProperty().multiply(0.010));
        opponentToken.setFill(p2Color);
        opponentToken.setStroke(Color.WHITE);
        opponentToken.setStrokeWidth(2);
        opponentToken.setEffect(ds);

        StackPane[] cellPanes = new StackPane[100];
        StackPane[] tokenOverlays = new StackPane[100]; 
        
        Label p1EnergyLabel = new Label("1000");
        Label p2EnergyLabel = new Label("1000");
        Label p1EnergyDiff = new Label();
        Label p2EnergyDiff = new Label();
        
        // --- SEPARATED DETAILS LABELS ---
        Label p1RoleLabel = new Label();
        Label p1PosLabel = new Label();
        Label p1ShieldLabel = new Label();
        Label p1EffectLabel = new Label();

        Label p2RoleLabel = new Label();
        Label p2PosLabel = new Label();
        Label p2ShieldLabel = new Label();
        Label p2EffectLabel = new Label();

        setupDiffLabel(p1EnergyDiff);
        setupDiffLabel(p2EnergyDiff);
        
        Label turnIndicator = new Label();
        turnIndicator.setStyle("-fx-text-fill: #ff6b35; -fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: 'Impact';");
        
        Button p1PowerupBtn = createPowerupButton();
        Button p2PowerupBtn = createPowerupButton();
        
        // Pass the calculated token colors and custom ImageViews to the player panels for the intro animation
        ImageView p1MonsterImg = new ImageView();
        ImageView p2MonsterImg = new ImageView();
        StackPane player1Panel = createPlayerPanel(root, "PLAYER 1", activeGame.getPlayer(), p1EnergyLabel, p1EnergyDiff, p1PowerupBtn, p1RoleLabel, p1PosLabel, p1ShieldLabel, p1EffectLabel, p1Color, p1MonsterImg);
        StackPane player2Panel = createPlayerPanel(root, "PLAYER 2", activeGame.getOpponent(), p2EnergyLabel, p2EnergyDiff, p2PowerupBtn, p2RoleLabel, p2PosLabel, p2ShieldLabel, p2EffectLabel, p2Color, p2MonsterImg);
        
        Runnable updateUI = () -> {
            Monster p1 = activeGame.getPlayer();
            Monster p2 = activeGame.getOpponent();

            int p1Pos = isEffectDelayed[0] ? delayedP1Pos[0] : p1.getPosition();
            int p2Pos = isEffectDelayed[0] ? delayedP2Pos[0] : p2.getPosition();
            int p1Energy = isEffectDelayed[0] ? delayedP1Energy[0] : p1.getEnergy();
            int p2Energy = isEffectDelayed[0] ? delayedP2Energy[0] : p2.getEnergy();
            Monster curr = isEffectDelayed[0] ? delayedCurrentTurn[0] : activeGame.getCurrent();

            p1EnergyLabel.setText(String.valueOf(p1Energy));
            p2EnergyLabel.setText(String.valueOf(p2Energy));
            
            p1PosLabel.setText("Pos: " + p1Pos);
            p2PosLabel.setText("Pos: " + p2Pos);

            // Role Confusion Check
            if (p1.getRole() != p1OrigRole) {
                p1RoleLabel.setText("Role: " + p1.getRole());
                p1RoleLabel.setTextFill(Color.web("#ff6b35"));
            } else {
                p1RoleLabel.setText("Role: " + p1.getRole());
                p1RoleLabel.setTextFill(Color.web("#aaaaaa"));
            }

            if (p2.getRole() != p2OrigRole) {
                p2RoleLabel.setText("Role: " + p2.getRole());
                p2RoleLabel.setTextFill(Color.web("#ff6b35"));
            } else {
                p2RoleLabel.setText("Role: " + p2.getRole());
                p2RoleLabel.setTextFill(Color.web("#aaaaaa"));
            }

            // Shield Tracking
            p1ShieldLabel.setText("Shield: " + (p1.isShielded() ? "ON" : "OFF"));
            p1ShieldLabel.setTextFill(p1.isShielded() ? Color.web("#3498db") : Color.web("#aaaaaa"));
            
            p2ShieldLabel.setText("Shield: " + (p2.isShielded() ? "ON" : "OFF"));
            p2ShieldLabel.setTextFill(p2.isShielded() ? Color.web("#3498db") : Color.web("#aaaaaa"));

         // Effect Tracking (Frozen / Momentum / Focus)
            String p1Eff = "None";
            if (p1.isFrozen()) p1Eff = "FROZEN";
            else if (p1 instanceof Dasher && ((Dasher)p1).getMomentumTurns() > 0) p1Eff = "Momentum (" + ((Dasher)p1).getMomentumTurns() + " Turns)";
            else if (p1 instanceof MultiTasker && ((MultiTasker)p1).getNormalSpeedTurns() > 0) p1Eff = "Focus (" + ((MultiTasker)p1).getNormalSpeedTurns() + " Turns)";
            p1EffectLabel.setText("Effect: " + p1Eff);
            p1EffectLabel.setTextFill(!p1Eff.equals("None") ? Color.web("#ff6b35") : Color.web("#aaaaaa"));

            String p2Eff = "None";
            if (p2.isFrozen()) p2Eff = "FROZEN";
            else if (p2 instanceof Dasher && ((Dasher)p2).getMomentumTurns() > 0) p2Eff = "Momentum (" + ((Dasher)p2).getMomentumTurns() + " Turns)";
            else if (p2 instanceof MultiTasker && ((MultiTasker)p2).getNormalSpeedTurns() > 0) p2Eff = "Focus (" + ((MultiTasker)p2).getNormalSpeedTurns() + " Turns)";
            p2EffectLabel.setText("Effect: " + p2Eff);
            p2EffectLabel.setTextFill(!p2Eff.equals("None") ? Color.web("#ff6b35") : Color.web("#aaaaaa"));

            for (Map.Entry<Monster, Label> entry : monsterEnergyLabels.entrySet()) {
                entry.getValue().setText(String.valueOf(entry.getKey().getEnergy()));
            }

            Monster winner = activeGame.getWinner();
            if (winner != null && !isEffectDelayed[0]) {
                turnIndicator.setText(winner.getName().toUpperCase() + " WINS!!!");
            } else if (mustDrawCard[0]) {
                turnIndicator.setText("DRAW YOUR CARD!");
            } else {
                turnIndicator.setText(curr == p1 ? "PLAYER 1'S TURN!" : "PLAYER 2'S TURN!");
            }

            double p1TargetX = (p1Pos == p2Pos) ? -8 : 0;
            double p2TargetX = (p1Pos == p2Pos) ? 8 : 0;
            
            if (playerToken.getParent() != null) ((Pane) playerToken.getParent()).getChildren().remove(playerToken);
            if (opponentToken.getParent() != null) ((Pane) opponentToken.getParent()).getChildren().remove(opponentToken);

            if (tokenOverlays[p1Pos] != null) {
                tokenOverlays[p1Pos].getChildren().add(playerToken);
                playerToken.toFront();
                // Brings the entire cell (and its token) above all other cells in the grid
                cellPanes[p1Pos].toFront(); 
            }
            if (tokenOverlays[p2Pos] != null) {
                tokenOverlays[p2Pos].getChildren().add(opponentToken);
                opponentToken.toFront();
                // Brings the entire cell (and its token) above all other cells in the grid
                cellPanes[p2Pos].toFront(); 
            }

            if (p1Anim[0] != null) p1Anim[0].stop();
            if (p2Anim[0] != null) p2Anim[0].stop();

            p1Anim[0] = animateToken(playerToken, prevP1Pos[0], p1Pos, p1TargetX, 0.0, cellPanes);
            p2Anim[0] = animateToken(opponentToken, prevP2Pos[0], p2Pos, p2TargetX, 0.0, cellPanes);

            if (p1Anim[0] != null) p1Anim[0].play();
            if (p2Anim[0] != null) p2Anim[0].play();
            prevP1Pos[0] = p1Pos;
            prevP2Pos[0] = p2Pos;
            
            if (!isEffectDelayed[0]) {
                int p1Diff = p1.getEnergy() - previousEnergies.get(p1);
                if (p1Diff != 0) {
                    animateDiffLabel(p1EnergyDiff, p1Diff);
                    previousEnergies.put(p1, p1.getEnergy());
                }
                
                int p2Diff = p2.getEnergy() - previousEnergies.get(p2);
                if (p2Diff != 0) {
                    animateDiffLabel(p2EnergyDiff, p2Diff);
                    previousEnergies.put(p2, p2.getEnergy());
                }
                
                for (Monster m : Board.getStationedMonsters()) {
                    int diff = m.getEnergy() - previousEnergies.get(m);
                    if (diff != 0) {
                        animateDiffLabel(monsterDiffLabels.get(m), diff);
                        previousEnergies.put(m, m.getEnergy());
                    }
                }
            }
        };

        Runnable checkAndNavigateWin = () -> {
            Monster winner = activeGame.getWinner();
            if (winner != null && !isEffectDelayed[0]) {
                Monster loser = (winner == activeGame.getPlayer()) ? activeGame.getOpponent() : activeGame.getPlayer();
                PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
                delay.setOnFinished(ev2 -> {
                    // Start Win Scene, pass 'false' to indicate credits haven't rolled yet
                    stage.setScene(createWinScene(stage, winner, loser, false));
                });
                delay.play();
            }
        };
        
        p1PowerupBtn.setOnAction(e -> {
            if (isAnimating[0]) return;
            if (activeGame.getCurrent() != activeGame.getPlayer()) {
                showInvalidActionPopup("Not Your Turn", "You cannot use your powerup during your opponent's turn.");
            } else if (activeGame.getPlayer().getEnergy() < Constants.POWERUP_COST) {
                showInvalidActionPopup("Insufficient Energy", "You need at least 500 energy to activate this powerup.");
            } else {
                try { 
                    debugLog("=== P1 POWERUP ATTEMPT ===");
                    activeGame.usePowerup(); 
                    updateUI.run(); 
                    turnIndicator.setText("POWERUP ACTIVATED!");
                } catch (Exception ex) {
                    debugLog("Powerup Failed: " + ex.getMessage());
                }
            }
        });
        
        p2PowerupBtn.setOnAction(e -> {
            if (isAnimating[0]) return;
            if (activeGame.getCurrent() != activeGame.getOpponent()) {
                showInvalidActionPopup("Not Your Turn", "You cannot use your powerup during your opponent's turn.");
            } else if (activeGame.getOpponent().getEnergy() < Constants.POWERUP_COST) {
                showInvalidActionPopup("Insufficient Energy", "You need at least 500 energy to activate this powerup.");
            } else {
                try { 
                    debugLog("=== P2 POWERUP ATTEMPT ===");
                    activeGame.usePowerup(); 
                    updateUI.run(); 
                    turnIndicator.setText("POWERUP ACTIVATED!");
                } catch (Exception ex) {
                    debugLog("Powerup Failed: " + ex.getMessage());
                }
            }
        });
        
        Label diceLabel = new Label("?");
        diceLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-family: 'Impact';");

        StackPane diceBox = new StackPane(diceLabel);
        diceBox.setMinSize(80, 80);
        diceBox.setMaxSize(80, 80);
        diceBox.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #cccccc; -fx-border-radius: 12px; -fx-border-width: 4px; -fx-cursor: hand;");
        diceBox.setOnMouseClicked(e -> {
            if (isRolling[0] || isAnimating[0] || activeGame.getWinner() != null) return;
            if (mustDrawCard[0]) {
                turnIndicator.setText("DRAW YOUR CARD FIRST!");
                return;
            }
            
            isRolling[0] = true;
            Timeline timeline = new Timeline();
            for (int i = 0; i < 10; i++) {
                KeyFrame frame = new KeyFrame(Duration.millis(i * 80), ev -> {
                    diceLabel.setText(String.valueOf((int)(Math.random() * 6) + 1));
                });
                timeline.getKeyFrames().add(frame);
            }
            KeyFrame finalFrame = new KeyFrame(Duration.millis(10 * 80), ev -> {
                
                Monster playingMonster = activeGame.getCurrent();
                boolean isP1 = (playingMonster == activeGame.getPlayer());
                
                debugLog("\n====== TURN START: " + playingMonster.getName() + " ======");
                
                int oldP1Pos = activeGame.getPlayer().getPosition();
                int oldP2Pos = activeGame.getOpponent().getPosition();
                int oldP1Energy = activeGame.getPlayer().getEnergy();
                int oldP2Energy = activeGame.getOpponent().getEnergy();
                int oldPos = playingMonster.getPosition();
                
                Map<String, Integer> preTurnEnergy = new HashMap<>();
                preTurnEnergy.put(activeGame.getPlayer().getName(), activeGame.getPlayer().getEnergy());
                preTurnEnergy.put(activeGame.getOpponent().getName(), activeGame.getOpponent().getEnergy());
                for (Monster m : Board.getStationedMonsters()) {
                    preTurnEnergy.put(m.getName(), m.getEnergy());
                }

                int momentumBefore = (playingMonster instanceof Dasher) ? ((Dasher)playingMonster).getMomentumTurns() : 0;
                int focusBefore = (playingMonster instanceof MultiTasker) ? ((MultiTasker)playingMonster).getNormalSpeedTurns() : 0;
                boolean shieldBefore = playingMonster.isShielded();
                
                ArrayList<Card> originalDeck = Board.getCards();
                if (originalDeck.isEmpty()) { 
                    Board.reloadCards();
                    originalDeck = Board.getCards(); 
                }
                
                Card expectedCard = originalDeck.get(0);
                ArrayList<Card> singleCardDeck = new ArrayList<>();
                singleCardDeck.add(expectedCard);
                Board.setCards(singleCardDeck);
                
                try {
                    activeGame.playTurn();
                    int roll = activeGame.getLastRoll();
                    debugLog(">>> Dice Rolled: " + roll + " <<<");
                    
                    // The final position they actually end up at after ALL engine rules process (socks/belts)
                    int finalLandPos = playingMonster.getPosition();
                    
                    // ─── SECONDARY EFFECT EVALUATION ───
                    // GUI now evaluates the Door and Monster logic based on finalLandPos, fixing the bug 
                    // where effects were ignored after riding a conveyor belt.
                    int cols = Constants.BOARD_COLS;
                    int r = finalLandPos / cols;
                    int c = finalLandPos % cols;
                    if (r % 2 == 1) c = cols - 1 - c;
                    Cell landedCell = activeGame.getBoard().getBoardCells()[r][c];
                    
                    if (landedCell instanceof DoorCell && roll != 0) {
                        DoorCell door = (DoorCell) landedCell;
                        if (!door.isActivated()) {
                            door.setActivated(true);
                            ImageView doorImg = doorIcons[finalLandPos];
                            if (doorImg != null) {
                                ColorAdjust grayscale = new ColorAdjust();
                                grayscale.setSaturation(-1);
                                grayscale.setBrightness(-0.4);
                                doorImg.setEffect(grayscale);
                            }
                            
                            boolean isMatch = (playingMonster.getRole() == door.getRole());
                            ArrayList<Monster> team = new ArrayList<>();
                            team.add(playingMonster);
                            for (Monster m : Board.getStationedMonsters()) {
                                if (m.getRole() == playingMonster.getRole()) team.add(m);
                            }
                            
                            if (isMatch) {
                                for (Monster m : team) {
                                    door.modifyCanisterEnergy(m, door.getEnergy());
                                }
                            } else {
                                boolean shieldActive = false;
                                for (Monster m : team) {
                                    if (m.isShielded()) {
                                        shieldActive = true;
                                        m.setShielded(false);
                                        break;
                                    }
                                }
                                if (!shieldActive) {
                                    for (Monster m : team) {
                                        door.modifyCanisterEnergy(m, -door.getEnergy());
                                    }
                                }
                            }
                        }
                    }
                    
                    if (landedCell instanceof game.engine.cells.MonsterCell && roll != 0) {
                        game.engine.cells.MonsterCell mc = (game.engine.cells.MonsterCell) landedCell;
                        Monster cellMonster = mc.getCellMonster();
                        
                        if (cellMonster.getName().equalsIgnoreCase("Roz")) {
                            playSoundEffect("Roz.mp3");
                        }

                        int oldCellEnergy = previousEnergies.get(cellMonster);
                        int oldPosEnergy = isP1 ? oldP1Energy : oldP2Energy;
                        
                        if (playingMonster.getRole() == cellMonster.getRole()) {
                            Monster opp = isP1 ? activeGame.getOpponent() : activeGame.getPlayer();
                            if (playingMonster instanceof Schemer && playingMonster.getEnergy() == oldPosEnergy) { 
                                playingMonster.executePowerupEffect(opp);
                            } else if (playingMonster instanceof Dynamo && !opp.isFrozen()) {
                                playingMonster.executePowerupEffect(opp);
                            } else if (playingMonster instanceof Dasher && ((Dasher)playingMonster).getMomentumTurns() == momentumBefore) {
                                playingMonster.executePowerupEffect(opp);
                            } else if (playingMonster instanceof MultiTasker && ((MultiTasker)playingMonster).getNormalSpeedTurns() == focusBefore) {
                                playingMonster.executePowerupEffect(opp);
                            }
                            Timeline notify = new Timeline(new KeyFrame(Duration.millis(500), ev2 -> turnIndicator.setText("ALLY MET! FREE POWERUP!")));
                            notify.play();
                        } else {
                            if (oldPosEnergy > oldCellEnergy) {
                                int currentLandingEnergy = playingMonster.getEnergy();
                                int currentCellEnergy = cellMonster.getEnergy();
                                
                                if (currentLandingEnergy == oldPosEnergy && currentCellEnergy == oldCellEnergy) {
                                    if (shieldBefore) {
                                        playingMonster.setShielded(false);
                                        cellMonster.setEnergy(oldPosEnergy);
                                        Timeline notify = new Timeline(new KeyFrame(Duration.millis(500), ev2 -> turnIndicator.setText("SHIELD BLOCKED SWAP PENALTY!")));
                                        notify.play();
                                    } else {
                                        playingMonster.setEnergy(oldCellEnergy);
                                        cellMonster.setEnergy(oldPosEnergy);
                                        Timeline notify = new Timeline(new KeyFrame(Duration.millis(500), ev2 -> turnIndicator.setText("ENERGY SWAPPED!")));
                                        notify.play();
                                    }
                                } else if (currentLandingEnergy < oldPosEnergy && shieldBefore) {
                                    playingMonster.setEnergy(oldPosEnergy); 
                                    playingMonster.setShielded(false);
                                    Timeline notify = new Timeline(new KeyFrame(Duration.millis(500), ev2 -> turnIndicator.setText("SHIELD BLOCKED SWAP PENALTY!")));
                                    notify.play();
                                } else {
                                    Timeline notify = new Timeline(new KeyFrame(Duration.millis(500), ev2 -> turnIndicator.setText("ENERGY SWAPPED!")));
                                    notify.play();
                                }
                            }
                        }
                    }

                    // --- CARD DRAW LOGIC FIX ---
                    boolean cardWasDrawn = containsIndex(Constants.CARD_CELL_INDICES, finalLandPos);
                    Board.setCards(originalDeck);
                    
                    if (cardWasDrawn && roll != 0) {
                        Board.getCards().remove(expectedCard);
                        mustDrawCard[0] = true;
                        pendingVisualCard[0] = expectedCard;
                        
                        isEffectDelayed[0] = true;
                        delayedP1Pos[0] = isP1 ? finalLandPos : oldP1Pos;
                        delayedP2Pos[0] = !isP1 ? finalLandPos : oldP2Pos;
                        delayedP1Energy[0] = oldP1Energy;
                        delayedP2Energy[0] = oldP2Energy;
                        delayedCurrentTurn[0] = playingMonster;
                    } else {
                        isEffectDelayed[0] = false;
                    }

                    diceLabel.setText(roll == 0 ? "X" : String.valueOf(roll));
                    updateUI.run();
                    checkAndNavigateWin.run();
                } catch (InvalidMoveException ex) {
                    Board.setCards(originalDeck);
                    diceLabel.setText(String.valueOf(activeGame.getLastRoll()));
                    showInvalidActionPopup("Cell Occupied", "The destination cell is occupied by the opponent. Please roll again!");
                } catch (Exception ex) {
                    Board.setCards(originalDeck);
                }
                isRolling[0] = false;
            });
            timeline.getKeyFrames().add(finalFrame);
            timeline.play();
        });

        // ─── CARD DRAWING ───
        ImageView cardBack = new ImageView(new Image("Cards/cardstack.png"));
        cardBack.setPreserveRatio(false);
        cardBack.fitWidthProperty().bind(root.widthProperty().multiply(0.11));
        cardBack.fitHeightProperty().bind(root.heightProperty().multiply(0.32));
        cardBack.setStyle("-fx-cursor: hand;");

        ImageView cardPicked = new ImageView();
        cardPicked.setPreserveRatio(false);
        cardPicked.fitWidthProperty().bind(root.widthProperty().multiply(0.11));
        cardPicked.fitHeightProperty().bind(root.heightProperty().multiply(0.32));
        cardPicked.setVisible(false);

        ImageView overlayCard = new ImageView();
        overlayCard.setPreserveRatio(false);
        overlayCard.fitWidthProperty().bind(root.widthProperty().multiply(0.18));
        overlayCard.fitHeightProperty().bind(root.heightProperty().multiply(0.50));
        overlayCard.setVisible(false);
        
        Label cardInfoLabel = new Label();
        cardInfoLabel.setStyle("-fx-font-size: 24px; -fx-font-family: 'Impact'; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.8); -fx-padding: 10px; -fx-background-radius: 8px;");
        cardInfoLabel.setVisible(false);
        cardInfoLabel.setWrapText(true);
        cardInfoLabel.setMaxWidth(400);
        cardInfoLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Rectangle overlayBg = new Rectangle();
        overlayBg.setFill(Color.BLACK);
        overlayBg.setOpacity(0.7);
        overlayBg.setVisible(false);
        overlayBg.widthProperty().bind(root.widthProperty());
        overlayBg.heightProperty().bind(root.heightProperty());

        overlayBg.setOnMouseClicked(e -> { 
            overlayBg.setVisible(false); 
            overlayCard.setVisible(false); 
            cardPicked.setVisible(true); 
            cardInfoLabel.setVisible(false);
        });
        overlayCard.setOnMouseClicked(e -> { 
            overlayBg.setVisible(false); 
            overlayCard.setVisible(false); 
            cardPicked.setVisible(true); 
            cardInfoLabel.setVisible(false);
        });

        cardBack.setOnMouseClicked(e -> {
            if (!mustDrawCard[0] || isAnimating[0]) return; 
            mustDrawCard[0] = false; 
            
            try {
                Card drawnCard = pendingVisualCard[0];
                if (drawnCard == null) return;
                
                /* * --- TATHEER MP3 LOGIC: CARDS ---
                 * This checks broadly for cards like "Contamination" or "Code 2319"
                 * which sends the monster back, and safely triggers the sound effect.
                 */
                String cNameLower = drawnCard.getName().toLowerCase();
                if (cNameLower.contains("contamination") || cNameLower.contains("code") || cNameLower.contains("2319")) {
                    playSoundEffect("Tatheer.mp3");
                }

                String imageFileName = drawnCard.getName().replace(" ", "") + ".png";
                Image realCardImage = new Image("Cards/" + imageFileName);
                
                overlayCard.setImage(realCardImage);
                cardPicked.setImage(realCardImage);
                cardPicked.setVisible(false);
                
                cardInfoLabel.setText(drawnCard.getName() + " Drawn!");

                ScaleTransition shrink = new ScaleTransition(Duration.millis(150), overlayCard);
                shrink.setFromX(1); shrink.setToX(0);

                ScaleTransition grow = new ScaleTransition(Duration.millis(150), overlayCard);
                grow.setFromX(0); grow.setToX(1);
                
                shrink.setOnFinished(ev -> {
                    overlayCard.setVisible(true);
                    cardInfoLabel.setVisible(true);
                    grow.play();
                    
                    isEffectDelayed[0] = false;
                    updateUI.run(); 
                    checkAndNavigateWin.run();
                });
                
                overlayBg.setVisible(true);
                overlayCard.setVisible(false);
                shrink.play();
                
            } catch (Exception ex) {
                System.out.println("Failed to perform card logic: " + ex.getMessage());
            }
        });

        HBox cardsRow = new HBox();
        cardsRow.setAlignment(Pos.BOTTOM_LEFT);
        cardsRow.spacingProperty().bind(root.widthProperty().multiply(0.01));
        
        cardsRow.translateYProperty().bind(root.heightProperty().multiply(-0.12)); 
        cardsRow.translateXProperty().bind(root.widthProperty().multiply(0.14));  
        cardsRow.getChildren().addAll(cardBack, cardPicked);
        
        // ─── LEFT PANEL ASSEMBLY ────────────────────────────────
        HBox diceRow = new HBox(15, new Label("Roll:"), diceBox);
        diceRow.setAlignment(Pos.CENTER_LEFT);
        diceRow.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-family: 'Impact';");

        VBox leftPanel = new VBox(5, player1Panel, player2Panel, turnIndicator, diceRow, cardsRow);
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.setPadding(new Insets(80, 10, 10, 10));
        leftPanel.prefWidthProperty().bind(root.widthProperty().multiply(0.35));
        leftPanel.prefHeightProperty().bind(root.heightProperty());

        // ─── GRID ─────────────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.hgapProperty().bind(root.widthProperty().multiply(0.003));
        grid.vgapProperty().bind(root.heightProperty().multiply(0.005));
        grid.paddingProperty().bind(root.widthProperty().asObject().map(w -> new Insets(w.doubleValue() * 0.008)));

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cellNumber = getCellNumber(row, col);
                int engineIndex = cellNumber - 1;

                StackPane cell = new StackPane();
                cell.prefWidthProperty().bind(root.heightProperty().multiply(0.85).divide(10).subtract(4));
                cell.prefHeightProperty().bind(root.heightProperty().multiply(0.85).divide(10).subtract(4));
                
                int r = engineIndex / 10;
                int c = engineIndex % 10;
                if (r % 2 == 1) c = 9 - c;
                Cell backendCell = activeGame.getBoard().getBoardCells()[r][c];

                if (engineIndex == 99) {
                    cell.setStyle("-fx-background-color: #FFD700; -fx-border-color: #FFA500; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    ImageView booView = new ImageView(new Image("boo.png"));
                    booView.setPreserveRatio(true);
                    booView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.85));
                    booView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                    cell.getChildren().add(booView);
                } else if (engineIndex == 0) {
                    cell.setStyle("-fx-background-color: #c8f0c8; -fx-border-color: #4caf50; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    Label startLabel = new Label("START");
                    startLabel.setStyle("-fx-font-size: 8px; -fx-text-fill: #1a5c1a; -fx-font-weight: bold;");
                    cell.getChildren().add(startLabel);
                } else if (backendCell instanceof game.engine.cells.MonsterCell) {
                    Monster stationedMonster = ((game.engine.cells.MonsterCell) backendCell).getCellMonster();
                    
                    // NEW DYNAMIC COLORING FOR MONSTER CELLS
                    if (stationedMonster.getRole() == Role.SCARER) {
                        cell.setStyle("-fx-background-color: #ff9999; -fx-border-color: #cc0000; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    } else {
                        cell.setStyle("-fx-background-color: #99ccff; -fx-border-color: #0066cc; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    }
                    
                    String monsterImg = getMonsterImageByName(stationedMonster.getName());
                    if (monsterImg != null) {
                        ImageView monsterView = new ImageView(new Image(monsterImg));
                        monsterView.setPreserveRatio(true);
                        monsterView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.80));
                        monsterView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.80));
                        cell.getChildren().add(monsterView);
                    }
                } else if (containsIndex(Constants.CONVEYOR_CELL_INDICES, engineIndex)) {
                    cell.setStyle("-fx-background-color: #5cb85c; -fx-border-color: #3d7a3d; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    ImageView conveyorView = new ImageView(new Image("conveyor_belt.png"));
                    conveyorView.setPreserveRatio(true);
                    conveyorView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.85));
                    conveyorView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                    cell.getChildren().add(conveyorView);
                } else if (containsIndex(Constants.SOCK_CELL_INDICES, engineIndex)) {
                    cell.setStyle("-fx-background-color: #e8a838; -fx-border-color: #b07820; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    ImageView sockView = new ImageView(new Image("socks.png"));
                    sockView.setPreserveRatio(true);
                    sockView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.75));
                    sockView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.75));
                    cell.getChildren().add(sockView);
                } else if (containsIndex(Constants.CARD_CELL_INDICES, engineIndex)) {
                    cell.setStyle("-fx-background-color: #d9534f; -fx-border-color: #8a2c2c; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    ImageView cardView = new ImageView(new Image("Cards/CardBack.png"));
                    cardView.setPreserveRatio(true);
                    cardView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.65));
                    cardView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                    cell.getChildren().add(cardView);
                } else if (backendCell instanceof game.engine.cells.DoorCell) {
                    Role doorRole = ((game.engine.cells.DoorCell) backendCell).getRole();
                    if (doorRole == Role.SCARER) {
                        cell.setStyle("-fx-background-color: #ffb6c1; -fx-border-color: #cc4466; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                        ImageView doorView = new ImageView(new Image("PinkDoor.png"));
                        doorView.setPreserveRatio(true);
                        doorView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.60));
                        doorView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                        doorIcons[engineIndex] = doorView;
                        cell.getChildren().add(doorView);
                    } else {
                        cell.setStyle("-fx-background-color: #aec6f0; -fx-border-color: #4a7ac2; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                        ImageView doorView = new ImageView(new Image("BlueDoor.png"));
                        doorView.setPreserveRatio(true);
                        doorView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.60));
                        doorView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                        doorIcons[engineIndex] = doorView;
                        cell.getChildren().add(doorView);
                    }
                } else {
                    cell.setStyle("-fx-background-color: #f5f0d0; -fx-border-color: #c8b870; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                }

                Label numberLabel = new Label(String.valueOf(cellNumber));
                numberLabel.styleProperty().bind(
                    root.heightProperty().multiply(0.013)
                    .asString("-fx-font-size: %.0fpx; -fx-text-fill: #333333; -fx-font-weight: bold;")
                );
                StackPane.setAlignment(numberLabel, Pos.TOP_LEFT);
                numberLabel.translateXProperty().bind(root.widthProperty().multiply(0.003));
                numberLabel.translateYProperty().bind(root.heightProperty().multiply(0.003));
                cell.getChildren().add(numberLabel);
                
                if (backendCell instanceof game.engine.cells.MonsterCell) {
                    Monster stationedMonster = ((game.engine.cells.MonsterCell) backendCell).getCellMonster();
                    Label staticEnergy = new Label(String.valueOf(stationedMonster.getEnergy()));
                    staticEnergy.setStyle("-fx-font-family: 'Impact'; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-color: rgba(0,0,0,0.6); -fx-padding: 2px; -fx-background-radius: 4px;");
                    StackPane.setAlignment(staticEnergy, Pos.BOTTOM_RIGHT);
                    staticEnergy.setTranslateX(-2); staticEnergy.setTranslateY(-2);
                    
                    Label diffLabel = new Label();
                    setupDiffLabel(diffLabel);
                    StackPane.setAlignment(diffLabel, Pos.TOP_RIGHT);
                    
                    monsterEnergyLabels.put(stationedMonster, staticEnergy);
                    monsterDiffLabels.put(stationedMonster, diffLabel);
                    
                    cell.getChildren().addAll(staticEnergy, diffLabel);
                } else if (backendCell instanceof game.engine.cells.DoorCell) {
                    int doorEnergy = ((game.engine.cells.DoorCell) backendCell).getEnergy();
                    Label staticEnergy = new Label(String.valueOf(doorEnergy));
                    staticEnergy.setStyle("-fx-font-family: 'Impact'; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-color: rgba(0,0,0,0.6); -fx-padding: 1px 3px; -fx-background-radius: 4px;");
                    StackPane.setAlignment(staticEnergy, Pos.BOTTOM_RIGHT);
                    staticEnergy.setTranslateX(-2); staticEnergy.setTranslateY(-2);
                    cell.getChildren().add(staticEnergy);
                } else if (containsIndex(Constants.SOCK_CELL_INDICES, engineIndex)) {
                    Label staticEnergy = new Label("-100");
                    staticEnergy.setStyle("-fx-font-family: 'Impact'; -fx-text-fill: #ff6b35; -fx-font-size: 13px; -fx-background-color: rgba(0,0,0,0.6); -fx-padding: 1px 3px; -fx-background-radius: 4px;");
                    StackPane.setAlignment(staticEnergy, Pos.BOTTOM_RIGHT);
                    staticEnergy.setTranslateX(-2); staticEnergy.setTranslateY(-2);
                    cell.getChildren().add(staticEnergy);
                }

                StackPane overlay = new StackPane();
                overlay.setPickOnBounds(false); 
                cell.getChildren().add(overlay);
                
                tokenOverlays[engineIndex] = overlay;
                cellPanes[engineIndex] = cell;
                grid.add(cell, col, row);
            }
        }

        StackPane gridContainer = new StackPane(grid);
        gridContainer.setAlignment(Pos.CENTER);
        gridContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.62));
        gridContainer.paddingProperty().bind(root.widthProperty().asObject().map(w -> 
            new Insets(w.doubleValue() * 0.015, w.doubleValue() * 0.02, w.doubleValue() * 0.015, 0)
        ));
        StackPane.setAlignment(gridContainer, Pos.CENTER_RIGHT);

        updateUI.run();

        HBox mainLayout = new HBox(leftPanel, gridContainer);
        mainLayout.setAlignment(Pos.CENTER);

        StackPane.setAlignment(overlayCard, Pos.CENTER);
        StackPane.setMargin(overlayCard, new Insets(150, 0, 0, 0));
        
        StackPane.setAlignment(cardInfoLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(cardInfoLabel, new Insets(0, 0, 100, 0));

        // Layers for the introductory animation
        Rectangle introOverlay = new Rectangle();
        introOverlay.widthProperty().bind(root.widthProperty());
        introOverlay.heightProperty().bind(root.heightProperty());
        introOverlay.setFill(Color.rgb(0, 0, 0, 0.8));
        introOverlay.setOpacity(0); // Start invisible for char roll

        Pane deckAnimLayer = new Pane();

        root.getChildren().addAll(background, mainLayout, introOverlay, deckAnimLayer, overlayBg, overlayCard, cardInfoLabel);

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());

        // Create the Intro Animations
        Timeline p1CharRoll = createCharacterRollTimeline(p1MonsterImg, activeGame.getPlayer().getName());
        Timeline p2CharRoll = createCharacterRollTimeline(p2MonsterImg, activeGame.getOpponent().getName());
        SequentialTransition deckAnim = createDeckShuffleAnimation(root, cardBack, deckAnimLayer, introOverlay, isAnimating);
        
        // Chain the animations
        p1CharRoll.setOnFinished(e -> {
            if (isAnimating[0]) { // Check if not skipped
                deckAnim.play();
            }
        });

        // Start char roll first
        p1CharRoll.play();
        p2CharRoll.play();

        scene.setOnKeyPressed(ev -> {
            if (isAnimating[0]) { // Allow skipping intro animations!
                if (ev.getCode() == KeyCode.SPACE || ev.getCode() == KeyCode.ESCAPE) {
                    p1CharRoll.stop();
                    p2CharRoll.stop();
                    deckAnim.stop();
                    p1MonsterImg.setImage(new Image(getMonsterImageByName(activeGame.getPlayer().getName())));
                    p2MonsterImg.setImage(new Image(getMonsterImageByName(activeGame.getOpponent().getName())));
                    root.getChildren().removeAll(introOverlay, deckAnimLayer);
                    isAnimating[0] = false;
                }
                return;
            }
            if (ev.getCode() == KeyCode.ESCAPE) stage.setScene(createRoleSelectionScene(stage));
            
            if (ev.getCode() == KeyCode.W && activeGame.getWinner() == null) {
                debugLog("\n[CHEAT CODE] Forcing " + activeGame.getCurrent().getName() + " to teleport to Boo's Door (Cell 99)...");
                Monster curr = activeGame.getCurrent();
                curr.setPosition(99);
                updateUI.run();
                checkAndNavigateWin.run();
            }
            if (ev.getCode() == KeyCode.E && activeGame.getWinner() == null) {
                debugLog("\n[CHEAT CODE] Adding 1500 energy to " + activeGame.getCurrent().getName() + "...");
                Monster curr = activeGame.getCurrent();
                int oldEnergy = curr.getEnergy();
                curr.setEnergy(oldEnergy + 1500);
                debugLog("Energy changed from " + oldEnergy + " to " + curr.getEnergy());
                updateUI.run();
                checkAndNavigateWin.run();
            }
        });

        return scene;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
    
    private void animateDiffLabel(Label label, int diff) {
        label.setText((diff > 0 ? "+" : "") + diff);
        label.setTextFill(diff > 0 ? Color.LIMEGREEN : Color.RED);
        label.setOpacity(1.0);
        label.setTranslateY(0);
        label.setTranslateX(0);
        label.setScaleX(1.0);
        label.setScaleY(1.0);
        
        TranslateTransition tt = new TranslateTransition(Duration.seconds(2), label);
        tt.setByY(-40);
        ScaleTransition st = new ScaleTransition(Duration.seconds(2), label);
        st.setToX(1.2); st.setToY(1.2);
        
        FadeTransition ft = new FadeTransition(Duration.seconds(2), label);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(tt, st, ft);
        pt.play();
    }

    private SequentialTransition animateToken(Node token, int oldPos, int newPos, double targetX, double targetY, StackPane[] cellPanes) {
        if (oldPos == newPos || cellPanes[oldPos] == null || cellPanes[newPos] == null) {
            token.setTranslateX(targetX);
            token.setTranslateY(targetY);
            token.setScaleX(1.0);
            token.setScaleY(1.0);
            token.setRotate(0);
            return null;
        }

        SequentialTransition seq = new SequentialTransition();
        int intermediatePos = newPos;

        if (containsIndex(Constants.CARD_CELL_INDICES, oldPos)) {
            intermediatePos = oldPos;
        } 
        else if (newPos < oldPos) {
            for (int i = oldPos + 1; i <= Math.min(99, oldPos + 18); i++) {
                if (containsIndex(Constants.SOCK_CELL_INDICES, i)) {
                    intermediatePos = i;
                    break;
                }
            }
        } 
        else {
            int foundConveyor = -1;
            for (int i = oldPos + 1; i <= Math.min(99, oldPos + 18); i++) {
                if (containsIndex(Constants.CONVEYOR_CELL_INDICES, i)) {
                    foundConveyor = i;
                    break;
                }
            }
            if (foundConveyor != -1 && newPos >= foundConveyor + 6) {
                intermediatePos = foundConveyor;
            }
        }

        if (intermediatePos > oldPos) {
            token.setTranslateX(cellPanes[oldPos].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
            token.setTranslateY(cellPanes[oldPos].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);

            for (int i = oldPos; i < intermediatePos; i++) {
                TranslateTransition step = new TranslateTransition(Duration.millis(300), token);
                step.setFromX(cellPanes[i].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
                step.setFromY(cellPanes[i].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);
                step.setToX(cellPanes[i+1].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
                step.setToY(cellPanes[i+1].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);
                seq.getChildren().add(step);
            }

            // Sync Tatheer audio PERFECTLY when the token physically hits a sock cell before it bounces backwards
            if (newPos < oldPos) {
                PauseTransition hitSockSoundTrigger = new PauseTransition(Duration.millis(1));
                hitSockSoundTrigger.setOnFinished(e -> playSoundEffect("Tatheer.mp3"));
                seq.getChildren().add(hitSockSoundTrigger);
            }

        } else {
            intermediatePos = oldPos;
            token.setTranslateX(cellPanes[oldPos].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
            token.setTranslateY(cellPanes[oldPos].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);
        }

        if (intermediatePos != newPos) {
            PauseTransition pause = new PauseTransition(Duration.millis(600));
            seq.getChildren().add(pause);

            TranslateTransition jump = new TranslateTransition(Duration.millis(900), token);
            jump.setFromX(cellPanes[intermediatePos].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
            jump.setFromY(cellPanes[intermediatePos].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);
            jump.setToX(targetX);
            jump.setToY(targetY);
            RotateTransition rt = new RotateTransition(Duration.millis(900), token);
            rt.setByAngle(720);

            ScaleTransition st = new ScaleTransition(Duration.millis(450), token);
            st.setFromX(1.0); st.setFromY(1.0);
            st.setToX(1.6); st.setToY(1.6);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            ParallelTransition jumpPt = new ParallelTransition(jump, rt, st);
            seq.getChildren().add(jumpPt);
        }

        return seq;
    }

    // ─── PLAYER PANEL ─────────────────────────────────────────────────────────
    private StackPane createPlayerPanel(Pane root, String titleName, Monster monster, Label energyLabel, Label diffLabel, Button powerupBtn, Label roleLabel, Label posLabel, Label shieldLabel, Label effectLabel, Color tokenColor, ImageView monsterImg) {
        monsterImg.setPreserveRatio(true);
        monsterImg.fitWidthProperty().bind(root.widthProperty().multiply(0.08));
        monsterImg.fitHeightProperty().bind(root.heightProperty().multiply(0.08));

        Circle miniToken = new Circle(8, tokenColor);
        miniToken.setStroke(Color.WHITE);
        miniToken.setStrokeWidth(1.5);
        DropShadow dsToken = new DropShadow(); 
        dsToken.setRadius(2); 
        dsToken.setColor(Color.BLACK);
        miniToken.setEffect(dsToken);

        Label titleLabel = new Label(titleName);
        titleLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Impact'; -fx-text-fill: #ff6b35; -fx-font-size: ",
            root.heightProperty().multiply(0.022).asString("%.0f"),
            "px;"
        ));
        
        HBox titleBox = new HBox(8, miniToken, titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(monster.getName());
        nameLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Impact'; -fx-text-fill: white; -fx-font-size: ",
            root.heightProperty().multiply(0.024).asString("%.0f"),
            "px;"
        ));
        
        VBox nameTextBox = new VBox(1, titleBox, nameLabel);
        nameTextBox.setAlignment(Pos.CENTER_LEFT);

        HBox nameRow = new HBox(8, monsterImg, nameTextBox);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        
        ImageView brushBg = new ImageView(new Image("titlescreenbuttonbackground.png"));
        brushBg.setPreserveRatio(false);
        brushBg.fitHeightProperty().bind(root.heightProperty().multiply(0.04));
        
        Label typeLabel = new Label(monster.getClass().getSimpleName() + getMonsterTrait(monster));
        typeLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Impact'; -fx-text-fill: #FFD700; -fx-font-size: ",
            root.heightProperty().multiply(0.022).asString("%.0f"),
            "px;"
        ));
        StackPane typeBox = new StackPane(brushBg, typeLabel);
        StackPane.setAlignment(typeLabel, Pos.CENTER);

        ImageView canisterImg = new ImageView(new Image("Scream_Canister.png"));
        canisterImg.setPreserveRatio(true);
        canisterImg.fitWidthProperty().bind(root.widthProperty().multiply(0.018));

        energyLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Impact'; -fx-text-fill: white; -fx-font-size: ",
            root.heightProperty().multiply(0.022).asString("%.0f"),
            "px;"
        ));
        HBox energyRow = new HBox(8, canisterImg, energyLabel, diffLabel);
        energyRow.setAlignment(Pos.CENTER_LEFT);

        String labelStyle = "-fx-font-family: 'Impact'; -fx-text-fill: #aaaaaa; -fx-font-size: 13px;";
        roleLabel.setStyle(labelStyle);
        posLabel.setStyle(labelStyle);
        shieldLabel.setStyle(labelStyle);
        effectLabel.setStyle(labelStyle);

        HBox detailsRow = new HBox(12, roleLabel, posLabel, shieldLabel, effectLabel);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        VBox blackArea = new VBox(8, energyRow, detailsRow, powerupBtn);
        blackArea.setStyle(
            "-fx-background-color: rgba(0,0,0,0.5);" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 6;"
        );
        blackArea.prefWidthProperty().bind(root.widthProperty().multiply(0.18));

        ImageView panelBg = new ImageView(new Image("titlescreenbuttonbackground.png"));
        panelBg.setPreserveRatio(false);
        panelBg.fitWidthProperty().bind(root.widthProperty().multiply(0.28));
        panelBg.fitHeightProperty().bind(root.heightProperty().multiply(0.19));
        panelBg.setOpacity(0.4);

        VBox content = new VBox(4, nameRow, typeBox, blackArea);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(20, 8, 8, 8));
        StackPane panelStack = new StackPane(panelBg, content);
        panelStack.setAlignment(Pos.TOP_LEFT);

        return panelStack;
    }

    private Button createPowerupButton() {
        Button btn = new Button("Use PowerUp (500 Energy)");
        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;"));
        btn.setMaxWidth(Double.MAX_VALUE); 
        return btn;
    }

    private StackPane createImageButton(String brushstrokePath, String labelImagePath, Pane root, double widthRatio, double heightRatio, double width_enhancer) {
        ImageView brush = new ImageView(new Image(brushstrokePath));
        brush.setPreserveRatio(false);
        brush.fitWidthProperty().bind(root.widthProperty().multiply(widthRatio));
        brush.fitHeightProperty().bind(root.heightProperty().multiply(heightRatio));

        ImageView label = new ImageView(new Image(labelImagePath));
        label.setPreserveRatio(true);
        label.fitWidthProperty().bind(root.widthProperty().multiply(widthRatio * width_enhancer));

        StackPane btn = new StackPane(brush, label);
        btn.setStyle("-fx-cursor: hand;");
        btn.setPickOnBounds(true);
        btn.setOnMouseEntered(e -> { btn.setOpacity(0.8); btn.setScaleX(1.05); btn.setScaleY(1.05); });
        btn.setOnMouseExited(e -> { btn.setOpacity(1.0); btn.setScaleX(1.0); btn.setScaleY(1.0); });
        return btn;
    }
    
    private Timeline createCharacterRollTimeline(ImageView imgView, String finalMonsterName) {
        String[] allMonsters = {
            "Monsters/char_sulley.png", "Monsters/char_mike.png", 
            "Monsters/char_randall.png", "Monsters/char_celia.png", 
            "Monsters/char_roz.png", "Monsters/char_fungus.png", 
            "Monsters/char_waternoose.png", "Monsters/char_yeti.png"
        };
        Timeline tl = new Timeline();
        // 18 rapid cycles
        for (int i = 0; i < 18; i++) {
            int index = i % allMonsters.length;
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * 140), e -> {
                imgView.setImage(new Image(allMonsters[index]));
            }));
        }
        // Finally land on the real monster
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(18 * 140), e -> {
            imgView.setImage(new Image(getMonsterImageByName(finalMonsterName)));
        }));
        return tl;
    }

    private String getMonsterImageByName(String name) {
        switch (name) {
            case "James P. Sullivan": return "Monsters/char_sulley.png";
            case "Mike Wazowski":     return "Monsters/char_mike.png";
            case "Randall Boggs":     return "Monsters/char_randall.png";
            case "Celia Mae":         return "Monsters/char_celia.png";
            case "Roz":               return "Monsters/char_roz.png";
            case "Fungus":            return "Monsters/char_fungus.png";
            case "Henry J. Waternoose": return "Monsters/char_waternoose.png";
            case "Yeti":              return "Monsters/char_yeti.png";
            default:                  return "Monsters/char_sulley.png";
        }
    }

    private String getWinningMonsterImageByName(String name) {
        switch (name) {
            case "James P. Sullivan": return "Monsters/char_sulley_win.png";
            case "Mike Wazowski":     return "Monsters/char_mike_win.png";
            case "Randall Boggs":     return "Monsters/char_randall_win.png";
            case "Celia Mae":         return "Monsters/char_celia_win.png";
            case "Roz":               return "Monsters/char_roz_win.png";
            case "Fungus":            return "Monsters/char_fungus_win.png";
            case "Henry J. Waternoose": return "Monsters/char_waternoose_win.png";
            case "Yeti":              return "Monsters/char_yeti_win.png";
            default:                  return "Monsters/char_sulley_win.png";
        }
    }
    
    private int getCellNumber(int row, int col) {
        int boardRow = 9 - row;
        if (boardRow % 2 == 0) {
            return boardRow * 10 + col + 1;
        } else {
            return boardRow * 10 + (9 - col) + 1;
        }
    }
    
    private boolean containsIndex(int[] arr, int target) {
        for (int val : arr)
            if (val == target) return true;
        return false;
    }

    private void playAudio(String filename) {
        if (currentAudioFile.equals(filename) && mediaPlayer != null) return;
        
        if (mediaPlayer != null) { 
            mediaPlayer.stop();
            mediaPlayer.dispose(); 
        }
        try {
            Media media = new Media(new File(filename).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            currentAudioFile = filename;
        } catch (Exception e) {
            System.out.println("Audio not found: " + filename);
        }
    }

    private void playSoundEffect(String filename) {
        try {
            // Safely flag if background music was actually playing, so we don't accidentally resume it if it shouldn't be
            boolean wasPlaying = false;
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                wasPlaying = true;
            }
            
            // Stop and dispose the previous sound effect if one is still lingering
            if (sfxPlayer != null) {
                sfxPlayer.stop();
                sfxPlayer.dispose();
            }
            
            Media media = new Media(new File(filename).toURI().toString());
            sfxPlayer = new MediaPlayer(media); 
            sfxPlayer.play();
            
            final boolean resumeBgm = wasPlaying;
            sfxPlayer.setOnEndOfMedia(() -> {
                sfxPlayer.dispose();
                sfxPlayer = null;
                // Only resume the background music if it was paused by THIS sound effect
                if (mediaPlayer != null && resumeBgm) {
                    mediaPlayer.play();
                }
            });
        } catch (Exception e) {
            System.out.println("SFX not found: " + filename);
            if (mediaPlayer != null) {
                mediaPlayer.play();
            }
        }
    }

    private SequentialTransition createDeckShuffleAnimation(StackPane root, ImageView deckNode, Pane animLayer, Rectangle overlay, boolean[] isAnimating) {
        ArrayList<Card> deckCards = Board.getCards();
        int numCards = Math.min(25, deckCards.size());
        ImageView[] visualCards = new ImageView[numCards];
        Image backImage = new Image("Cards/CardBack.png");

        SequentialTransition masterAnim = new SequentialTransition();

        Platform.runLater(() -> {
            if (!isAnimating[0]) return; // Safely abort if the user skipped before this thread ran
            
            double centerX = root.getWidth() / 2;
            double centerY = root.getHeight() / 2;

            Bounds deckBounds = deckNode.localToScene(deckNode.getBoundsInLocal());
            Point2D targetLocal = animLayer.sceneToLocal(deckBounds.getMinX(), deckBounds.getMinY());
            double deckX = targetLocal.getX();
            double deckY = targetLocal.getY();

            ParallelTransition showGrid = new ParallelTransition();
            ParallelTransition flipCards = new ParallelTransition();
            SequentialTransition shuffleAnim = new SequentialTransition();
            ParallelTransition stackAnim = new ParallelTransition();

            double cardW = 100;
            double cardH = 140;

            for (int i = 0; i < numCards; i++) {
                Card c = deckCards.get(i);
                String imgName = "Cards/" + c.getName().replace(" ", "") + ".png";
                Image faceImage = new Image(imgName);

                ImageView cardView = new ImageView(faceImage);
                cardView.setFitWidth(cardW);
                cardView.setFitHeight(cardH);
                
                cardView.setX(centerX - (cardW / 2));
                cardView.setY(centerY - (cardH / 2));
                cardView.setScaleX(0);
                cardView.setScaleY(0);

                animLayer.getChildren().add(cardView);
                visualCards[i] = cardView;

                int row = i / 5;
                int col = i % 5;
                double targetX = centerX - 260 + (col * 130);
                double targetY = centerY - 350 + (row * 150);

                TranslateTransition ttOut = new TranslateTransition(Duration.millis(700), cardView);
                ttOut.setToX(targetX - cardView.getX());
                ttOut.setToY(targetY - cardView.getY());

                ScaleTransition stOut = new ScaleTransition(Duration.millis(700), cardView);
                stOut.setToX(1.0);
                stOut.setToY(1.0);

                RotateTransition rtOut = new RotateTransition(Duration.millis(700), cardView);
                rtOut.setFromAngle(-180);
                rtOut.setToAngle(0);

                showGrid.getChildren().addAll(ttOut, stOut, rtOut);

                ScaleTransition flipHide = new ScaleTransition(Duration.millis(200), cardView);
                flipHide.setToX(0);
                flipHide.setOnFinished(ev -> cardView.setImage(backImage));

                ScaleTransition flipShow = new ScaleTransition(Duration.millis(200), cardView);
                flipShow.setToX(1);

                flipCards.getChildren().add(new SequentialTransition(flipHide, flipShow));

                TranslateTransition ttStack = new TranslateTransition(Duration.seconds(1), cardView);
                ttStack.setToX(deckX - cardView.getX());
                ttStack.setToY(deckY - cardView.getY());

                RotateTransition rtStack = new RotateTransition(Duration.seconds(1), cardView);
                rtStack.setToAngle(0);

                ScaleTransition stStack = new ScaleTransition(Duration.seconds(1), cardView);
                stStack.setToX(deckBounds.getWidth() / cardW);
                stStack.setToY(deckBounds.getHeight() / cardH);

                stackAnim.getChildren().addAll(ttStack, rtStack, stStack);
            }

            for (int step = 0; step < 6; step++) {
                ParallelTransition mixStep = new ParallelTransition();
                for (int i = 0; i < numCards; i++) {
                    TranslateTransition ttMix = new TranslateTransition(Duration.millis(180), visualCards[i]);
                    ttMix.setToX((centerX - 150 + Math.random() * 300) - visualCards[i].getX());
                    ttMix.setToY((centerY - 150 + Math.random() * 300) - visualCards[i].getY());

                    RotateTransition rtMix = new RotateTransition(Duration.millis(180), visualCards[i]);
                    rtMix.setToAngle(-90 + Math.random() * 180);

                    mixStep.getChildren().addAll(ttMix, rtMix);
                }
                shuffleAnim.getChildren().add(mixStep);
            }

            FadeTransition ftIn = new FadeTransition(Duration.millis(400), overlay);
            ftIn.setToValue(1.0);

            masterAnim.getChildren().addAll(
                ftIn,
                showGrid,
                new PauseTransition(Duration.seconds(2.5)), 
                flipCards,
                new PauseTransition(Duration.millis(300)),
                shuffleAnim,
                new PauseTransition(Duration.millis(300)),
                stackAnim
            );

            masterAnim.setOnFinished(ev -> {
                FadeTransition ftOut = new FadeTransition(Duration.millis(400), overlay);
                ftOut.setToValue(0);
                ftOut.setOnFinished(e -> {
                    root.getChildren().removeAll(overlay, animLayer);
                    isAnimating[0] = false; 
                });
                ftOut.play();
            });
        });

        return masterAnim;
    }
    
    public static void main(String[] args) {
        launch();
    }
}