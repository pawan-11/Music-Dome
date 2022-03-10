package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;


public class Util {
	
	public static String toCamelCase(String s) {
	//	s = s.toLowerCase();
		String words[] = s.split(" ");
		String new_s = "";
		for (String word: words) {
			if (word.length() == 0) continue;
			new_s += Character.toUpperCase(word.charAt(0));
			new_s += word.substring(1, word.length())+" ";
		}
		return new_s.trim();
	}
	
	public static void print(String s) {
if (s.equals("upadte")) {
		 	Util.print("!!!");
		}
		System.out.println(s);
	}
	
	public static void print(Object o) {
		if (o.toString().equals("upadte")) {
			Util.print("!!!???");
		}
		System.out.println(o);
	}
	
	public static void print(String[] ss) {
		for (String s: ss)
			System.out.print(s+"|");
		print("");
	}
	
	public static void println(Collection<String> lines) {
		for (String line: lines)
			print(line);
		print("");
	}
	
	public static int rand_int(int start_idx, int end_idx) { //inclusive
		int rand_idx = (int)(Math.random()*(end_idx+1)+start_idx);
		return rand_idx;
	}

	public static boolean isOs(String os) {
		os = os.toLowerCase();
		String real_os = System.getProperty("os.name").toLowerCase();
		return real_os.contains(os);
	}
	
	public static void printObjectSize(Object object) {
		//     System.out.println("Object type: " + object.getClass() +
		//         ", size: " + InstrumentationAgent.getObjectSize(object) + " bytes");
	}
	
	public static String trim_last_dir(String path) {
		String new_path = path;
		int idx = path.length()-2;
		while (idx >= 0 && path.charAt(idx) != '/')
			idx -= 1;		
		new_path = new_path.substring(0, idx);
		return new_path;
	}
	
	public static String getParentDir() {
		String path;
		try {
			path = new File(Util.class.getProtectionDomain().getCodeSource().
						getLocation().toURI()).getPath();
			path = trim_last_dir(path);
			return path;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String removeExt(String file_name) {
		int i;
		for (i = file_name.length()-1; i > -1; i-=1)
			if (file_name.charAt(i) == '.')
				break;
		//i is the index of '.'
		return i == -1?file_name:file_name.substring(0, i);
	}
	
	public static String getExt(String file_name) {
		String parts[] = file_name.split(".");
		return parts.length > 0?parts[parts.length-1]:"";
	}
	
	static FileChooser filechooser = new FileChooser();
	static DirectoryChooser dirchooser = new DirectoryChooser();
	{
		filechooser.setInitialDirectory(new File(System.getProperty("user.home")));
		dirchooser.setInitialDirectory(new File(System.getProperty("user.home")));
	}
	
	public static File askFile(String prompt, String file_type, String... type_exts) {
		try {
			filechooser.setTitle(prompt);
			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(file_type, type_exts);
			filechooser.getExtensionFilters().add(filter);
			File file = filechooser.showOpenDialog(null);
			
			return file;
		}
		catch (Exception e) {
			Util.print("bad file uploaded");
		}	
		return null;
	}
	
	public static File askDir(String prompt) {
		try {
			dirchooser.setTitle(prompt);
			File file = dirchooser.showDialog(null);
			return file;
		}
		catch (Exception e) {
			Util.print("bad directory uploaded");
		}	
		return null;
	}
	
	
	public static List<File> getFiles(File file_dir, String... regexes) {
		List<File> files = new ArrayList<File>();
		
		if (file_dir.exists() && file_dir.isDirectory()) {
			File[] mp3_files = file_dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File file, String name) {
					for (String regex: regexes)
						if (name.matches(regex)) return true;
					return false;
				}
			});
			files.addAll(Arrays.asList(mp3_files));
		}
		return files;
	}
		
	public static String exec(String[] cmd) {
		String output = "";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			
			//get command output
			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			while ((line = err.readLine()) != null) {
				output += line+"\n";
			}
			err.close();
			err = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = err.readLine()) != null) {
				output += line+"\n";
			}
			err.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public static void openUrl(String url) {
		try {			
			if(Util.isOs("win")) {
				Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				p.waitFor();
			}
			else {
				Process p = Runtime.getRuntime().exec("open " + url);
				p.waitFor();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static void openDir(File dir) {
		try {			
			if(Util.isOs("win")){
			//	Runtime rt = Runtime.getRuntime();
			//	rt.exec("rundll32 url.dll,FileProtocolHandler " + url); //TODO
			}
			else {
				Process p = Runtime.getRuntime().exec("open " + dir.toString());
				p.waitFor();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	//not used
	public static MediaPlayer getLocalMedia(String name) {
		URL url = Util.class.getResource(Paths.get("media", name).toString());
		if (url == null)  {
			System.out.println("media "+name+" not found");
		}
		return new MediaPlayer(new Media(url.toString()));
	}
	
	public static Media getMedia(String abs_path) {
		return getMedia(new File(abs_path));
	}
	
	public static Media getMedia(File file) { //used in pmedia
		try {
			return new Media(file.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static double getLength(File file) {
		try {
			AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
			Map<String, Object> properties = baseFileFormat.properties();
			Long duration = (Long) properties.get("duration");
			return duration/(1000*1000);
		} catch (UnsupportedAudioFileException | IOException e) {
			Util.print("invalid file for length "+file);
		}
		return 0;
	}


	/*
	StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
	StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
	String methodName = e.getMethodName();
	System.out.println(methodName);

	 */
}

