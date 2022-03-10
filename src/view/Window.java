package view;

import java.nio.file.Paths;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import resources.Images;
import util.Util;


public class Window extends Application {	
    
    
    private Stage stage;
    private Menu menu;
    
	public void start(Stage stage) {
		this.stage = stage;
		this.menu = new Menu(this);
		
		Scene scene = new Scene(menu, 600, 400); //avoids quick resize as application pops up
		scene.getStylesheets().add(Paths.get("view", "style.css").toString());
		
		stage.setTitle("Music Dome");
		stage.setMaximized(false);
		stage.setScene(scene);
		stage.centerOnScreen();
		stage.setResizable(true);
		
		stage.getIcons().add(Images.icon);
		stage.show();
		
		addEvents();
		menu.show();
		stage.setMinWidth(400);
		stage.setMinHeight(300);
		stage.centerOnScreen();
		stage.setHeight(400); //lets views resize
		
		Parameters p = this.getParameters();
		menu.doLongTasks(p);
	}	
	
	public void addEvents() {
		stage.setOnCloseRequest(e-> {
			Platform.exit();
		});
		stage.widthProperty().addListener((c, old, ne)->{	
			resize();
		});
		stage.heightProperty().addListener((c, old, ne)->{
			resize();
		});		
		stage.addEventFilter(KeyEvent.KEY_PRESSED, k->{
			if (k.getCode() == KeyCode.ESCAPE)
				stage.setFullScreen(false);			
		});
		stage.setOnCloseRequest(c->{   
			menu.exit();
		});
	}
	
	public void resize() {
		menu.my_resize(stage.getWidth(), stage.getHeight()-20);
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
