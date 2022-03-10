package util;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import model.Controls.MediaProperty;
import util.Lambda.Function;

public interface View {

	public void addContent();
	public void addEvents();
	public void addLayout();
	public void my_resize(double width, double height);
	
	public interface SubView extends View {
		public Node getContent();
	}
	
	public class cVBox extends VBox {		
		public cVBox(Node...nodes) {
			super(nodes);
			this.setAlignment(Pos.CENTER);
		}
	}
	
	public class cHBox extends HBox {		
		public cHBox(Node...nodes) {
			super(nodes);
			this.setAlignment(Pos.CENTER);
		}
	}
	
	public class cText extends Text {
		public cText(String txt) {
			super(txt);
			this.setTextAlignment(TextAlignment.CENTER);
		}
		public cText() {
			this("");
		}
	}

}
