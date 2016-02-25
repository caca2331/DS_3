package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.geom.Rectangle2D;


public class Main extends Application {
    static GridPane root;
    static int screenX,screenY;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Controller control = new Controller();

        screenX = (int)Screen.getPrimary().getVisualBounds().getWidth();screenY  = (int)Screen.getPrimary().getVisualBounds().getHeight();

        root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Minesweeper");
        primaryStage.setScene(new Scene(root));
        control.beforeShow();
        primaryStage.show();
        update_root(primaryStage);


    }
    public static void main(String[] args) {
        launch(args);
    }

    public static void update_root(Stage stage){
        stage.setX(screenX/2-root.getMaxWidth()/2);
        stage.setY(screenY/2-root.getMaxHeight()/2-50);
        stage.setMinWidth(root.getMaxWidth());
        stage.setMinHeight(root.getMaxHeight());
        stage.setMaxWidth(root.getMaxWidth());
        stage.setMaxHeight(root.getMaxHeight());
    }

    private void p(Object o) {System.out.println(o);}
}

/**
 * git remote add origin https://github.com/caca2331/DS_3.git
 * git push -u origin master
 */