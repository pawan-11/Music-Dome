package resources;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import util.Util;

public final class Images { //final means class cant be extended/overriden
	
	public static Image icon = getImg("Music Dome.png");	
	public static Image back, next, play, pause, shuffle, repeat, yt;
	
	static {
		back = getImg("back.png");
		next = getImg("next.png");
		play = getImg("play.png");
		pause = getImg("pause.png");
		repeat = getImg("repeat.png");
		shuffle = getImg("shuffle.png");
		yt = getImg("yt.png");
	}
	
	public static ArrayList<Image> getBgs(String talker) {
		talker = talker.toLowerCase();
		ArrayList<Image> talker_bgs = new ArrayList<Image>(0);
		Image talker_bg = null;
		String exts[] = {".jpeg", ".png", ".jpg", ".gif"};
		int bg_idx = 1, ext_idx = 0;
		do {
			ext_idx = 0;
			while (ext_idx < exts.length && (talker_bg = getImg(talker+"bg"+bg_idx+exts[ext_idx])) == null)
				ext_idx++;
			bg_idx += 1;
			if (ext_idx != exts.length)
				talker_bgs.add(talker_bg);
		} while (ext_idx != exts.length);
		return talker_bgs;
	}
	
	
	private static Image getImg(String name) {
		URL url = Images.class.getResource(Paths.get(name).toString());
		if (url == null) {
			Util.print(name +" image not found");
			return null;
		}
		return new Image(url.toString());
	}
	
	public static Image askImg() {
		try {
			FileChooser filechooser = new FileChooser();
			filechooser.setInitialDirectory(new File(System.getProperty("user.home")));
			filechooser.setTitle("Open Image (jpg/png/gif)");
			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif");
			filechooser.getExtensionFilters().add(filter);
			File file = filechooser.showOpenDialog(null);
			
			return new Image(file.toURI().toString());
		}
		catch (Exception e) {
		}
		
		return null;
	}
	
}
