package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.scene.media.Media;
import org.apache.commons.io.FilenameUtils;
import view.Menu;

public class PMedia implements Comparable<PMedia> {

	private Media media;
	private String title, artist;
	private double seconds;
	private File file;
        
	public PMedia(File file) {
		this.file = file;
		this.media = Util.getMedia(file);
		
		newName();
		seconds = Util.getLength(file);
		//fillInfo(); //too slow
	}

	private void newName() {
		String[] parts = file.getName().split("-");
		if (parts.length > 1) {
			setArtist(parts[0]);
			setTitle(Util.removeExt(parts[1]));
		}
		else {
			setArtist("");
			setTitle(Util.removeExt(file.getName()));
		}
	}

	public void setArtist(String artist) {
		artist.replaceAll("\\s{2,}", " ");
		this.artist = artist;
	}

	public void setTitle(String title) {
		title.replaceAll("\\s{2,}", " "); //remove extra spaces
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public Media getMedia() {
		return media;
	}

	public String toAddress() {
		return super.toString();
	}

	public String toString() {
		return artist.length() > 0?artist+"-"+title:title;
	}

	public String getFilename() {
		return file.getName();
	}

	public String getYtUrl() {
		return "http://youtube.com/results?search_query="+toString().replaceAll("-|\\s+|&", "+");
	}

	public double getLength() { //length of media in seconds
		return seconds;
	}

	@Override
	public int compareTo(PMedia m) {
		return this.toString().toLowerCase().compareTo(m.toString().toLowerCase());
	}

	public boolean save(File dir) { //save into directory
		//add this file to local storage if are not already there
		try {
			return Paths.get(dir.getAbsolutePath(),file.getName()).toFile().createNewFile();
		} catch (IOException e) {
			Util.print("error creating file "+file.getName());
			Menu.error.setText(Menu.error.getText()+
					"\nerror creating file "+file.getAbsolutePath()+"error:\n"+e.getMessage());
		}
		return false;
	}

	public void delete() {
		this.file.delete();
	}

	public void rename(String new_name) { //name without the extension
		File parent_dir = file.getParentFile();
		String ext = FilenameUtils.getExtension(file.getAbsolutePath());
		File new_file = new File(Paths.get(parent_dir.getAbsolutePath(), new_name)+"."+ext);
		boolean x = file.renameTo(new_file);
		file = new_file;
		newName();
	}
	
	public void open() {
		try {
			if (Util.isOs("mac")) {
				String file_path = file.getAbsolutePath().toString();//.replace(" ", "\\ ");
				Process p =	Runtime.getRuntime().exec(new String[]{"/usr/bin/open", "-R",
                        file_path});		
				p.waitFor();
			/*	Util.print("error:");
				BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line;
				while ((line = err.readLine()) != null)
					Util.print(line);
				Util.print("\ninput");
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null)
					Util.print(line); 
				input.close();
				err.close();*/
			}
		}
		catch (Exception e){
			System.err.println("opening "+ file.getAbsolutePath().toString()+" exception");
		}
	}

	/*
	private void fillInfo() {
		try {
			InputStream input = new FileInputStream(file);
			ContentHandler handler = new DefaultHandler();
			Metadata metadata = new Metadata();
			Mp3Parser parser = new Mp3Parser();
			ParseContext parseCtx = new ParseContext();
			parser.parse(input, handler, metadata, parseCtx);
			input.close();
			this.seconds = (int)Double.parseDouble(metadata.get("xmpDM:duration"));
		}
		catch (java.io.IOException e) {
			Util.print("error getting mp3 info from "+file.getName());
		}
		catch (org.xml.sax.SAXException e) {
			Util.print("error getting mp3 info from "+file.getName());
		}
		catch (TikaException e) {
			Util.print("error getting mp3 info from "+file.getName());
		}
	}

	 */

}
