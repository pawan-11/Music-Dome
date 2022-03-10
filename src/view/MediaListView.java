package view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import model.Controls;
import model.MediaList;
import resources.Images;
import util.Observable;
import util.Observer;
import util.PMedia;
import util.Util;
import util.View;
import util.View.SubView;

public class MediaListView implements SubView, Observer {

	private MediaList media_list;
	private Controls controls;
	private VBox vbox;
	private ScrollPane sp;
	private boolean key_scroll = true;
	private ContextMenu contextMenu;
	private int rename_idx = -1;
	
	public MediaListView(MediaList music_list, Controls controls) {	//view and controller
		this.media_list = music_list;
		this.controls = controls;
		addContent();
		addEvents();
		addLayout();
		
		music_list.addObserver("view", this);
	}
	
	public ScrollPane getContent() {
		return sp;
	}
	
	@Override
	public void addContent() {
		sp = new ScrollPane();
		vbox = new VBox();
		
		MenuItem open = new MenuItem("Open");
		open.setOnAction(m->{
			media_list.getMedias().get(rename_idx).open();	
		});
		MenuItem rename = new MenuItem("Rename");
		rename.setOnAction(m->{
			key_scroll = false;
			HBox hbox = (HBox)vbox.getChildren().get(rename_idx);
			TextField name = (TextField)hbox.getChildren().get(0);
			name.setEditable(true);
		});
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(m->{
			media_list.getMedias().remove(rename_idx);
			media_list.getMedias().get(rename_idx).delete();			
		});
		contextMenu = new ContextMenu(); //set visible when mouse is clicked on a hbox
		contextMenu.getItems().addAll(open, rename); //delete
		
		sp.setContent(vbox);
	}
	
	@Override
	public void addEvents() {
		//one event here to handle all clicks on hboxes
		vbox.setOnMouseClicked(m-> {
			int idx = (int)(m.getY()/(vbox.getLayoutBounds().getHeight()/vbox.getChildren().size()));

			if (!m.isShortcutDown()) //to select multiple medias if command is down
				media_list.clearSelectedMedias();				
			
			media_list.selectMedia(idx, !media_list.getSelectedMediaIdxes().contains(idx)); //select/unselect
			
			if (m.getClickCount() > 1) { //shortcut is also down if more than one song is currently selected
				media_list.setPlaylist(media_list.getSelectedMedias().size() > 1?
						media_list.getSelectedMedias():media_list.getMedias());
			//	controls.setPlay(false);
				controls.setPlay(true); //play if double clicked
			//	Util.print(media_list.getPlaylist());
			}
			else if (m.getButton() == MouseButton.SECONDARY) { //right click
				contextMenu.show(vbox, m.getScreenX(), m.getScreenY());
				rename_idx = idx;
			}
			else {
				rename();
			}		
		});
		
		vbox.addEventFilter(KeyEvent.ANY, k->{
			if (k.getCode() == KeyCode.ENTER)
				rename();
		});
	}
	
	public void handle(KeyEvent k) {
		if (k.getText().length() != 0 && key_scroll)
			media_list.view(k.getText()); //nums and alphas
	}
	
	@Override
	public void addLayout() {
		sp.setFitToHeight(false);
		sp.setFitToWidth(true);
		sp.setPannable(true);
		sp.setCache(true);
		sp.setCacheShape(true);
		sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);
	}

	@Override
	public void my_resize(double width, double height) {
		//double k = Menu.getK(width, height);		
		sp.setPrefSize(width, height);
		sp.setMaxSize(width, height);
		sp.layout();
		sp.requestLayout();
		vbox.setPrefSize(sp.getViewportBounds().getWidth(), sp.getViewportBounds().getHeight());
	}

	private void addHbox(int idx, PMedia media) { //change back to labels?
		HBox hbox = new cHBox();
		Region r = new Region();
		HBox.setHgrow(r, Priority.ALWAYS);
		ImageView iv = new ImageView(Images.yt);
		iv.setFitWidth(25);
		iv.setFitHeight(25);
		
		iv.setOnMouseClicked(m->{
			m.consume();
			Util.openUrl(media.getYtUrl());
		});
		//Text name = new Text(media.toString());
		TextField name = new TextField(media.toString());
		name.setFocusTraversable(true);
		name.setPickOnBounds(false);
		name.mouseTransparentProperty().bind(Bindings.createBooleanBinding(()->!name.isEditable(), 
				name.editableProperty()));
		name.setCursor(Cursor.TEXT);
		name.setContextMenu(contextMenu);
		name.setPadding(new Insets(0));
		name.editableProperty().addListener((c,o,n)->{
			if (n) {
				name.requestFocus();
				name.selectEnd();
				name.getStyleClass().clear();
				name.getStyleClass().add("song_name_editable");
			}
			else {
				name.getStyleClass().clear();
				name.getStyleClass().add("song_name_uneditable");
			}
		}); 
		name.setPrefWidth(250);
		name.setEditable(false);
		hbox.getChildren().addAll(name, r, iv);
		hbox.getStyleClass().add("song_hbox");
		vbox.getChildren().add(idx, hbox);
	}
	
	private void rename() {
		if (rename_idx != -1) {
			HBox hbox = (HBox)vbox.getChildren().get(rename_idx);
			TextField t = (TextField)hbox.getChildren().get(0);
			t.setEditable(false);
			key_scroll = true;
			//check if name changed
			PMedia media = media_list.getMedias().get(rename_idx);
			if (!t.getText().equals(media.toString())) {
				media.rename(t.getText());
			}
		//	vbox.getChildren().remove(rename_idx);
		//	addHbox(rename_idx, media);
			vbox.requestFocus();
		}
		rename_idx = -1;
	}
	
	@Override
	public void update(String msg) {
		if (msg.equals("imported files") || msg.equals("deleted files"))
			refreshVbox(); //update all at once, instead of one by one and then sorted version
		else if (msg.contains("added media")) {
			String parts[] = msg.split(" ");
			int idx = Integer.parseInt(parts[parts.length-1]);
			addHbox(idx, media_list.getMedias().get(idx));
		}
		else if (msg.contains("view media"))
			updateViewMedia();
		else if (msg.contains("highlighted media"))
			updateHighlightedMedia(!msg.contains("unhighlight"));
		else if (msg.contains("selected")) {
			String parts[] = msg.split(" ");
			int idx = Integer.parseInt(parts[parts.length-1]);
			selectMedia(idx, !msg.contains("unselected"));
		}
		
	}
	
	private void selectMedia(int idx, boolean select) {
		HBox hbox = (HBox)vbox.getChildren().get(idx);
		if (select)
			hbox.getStyleClass().add("song_hbox_selected");
		else {
			hbox.getStyleClass().remove("song_hbox_selected");
		}
	}
	
	private void updateViewMedia() { //scroll to the media medialist wants to view
		int i = media_list.getViewMediaIdx();
		sp.setVvalue(i*((sp.getVmax()-sp.getVmin())/vbox.getChildren().size()));
	}
	
	private void refreshVbox() { //called by importMusic after music has been imported
		vbox.getChildren().clear();
		for (PMedia media: media_list.getMedias())
			addHbox(vbox.getChildren().size(), media);	
	}

	private void updateHighlightedMedia(boolean highlight) { //media that is playing
		int idx = media_list.getHighlightedMediaIdx();
		if (idx == -1) return;
		if (!highlight) {
			HBox hbox = (HBox)vbox.getChildren().get(idx);
			hbox.getStyleClass().remove("song_hbox_playing");
		}
		else {
			HBox hbox = (HBox)vbox.getChildren().get(idx);
			hbox.getStyleClass().add("song_hbox_playing");
		}
	}
	
}
