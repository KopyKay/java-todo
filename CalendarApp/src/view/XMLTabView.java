package view;

import java.io.File;

import application.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Category;
import model.Contact;
import model.Event;
import serializer.XMLDataWrapper;

/**
 * Klasa obsługująca widok zakładki XML w aplikacji. Umożliwia przeglądanie
 * danych znajdujących się w plikach XML związanych ściśle z strukturą danych
 * modelów programu. Implementuje graficzny interfejs użytkownika przy użyciu
 * JavaFX.
 */
public class XMLTabView
{
	private Controller controller;
	private FileChooser fileChooser;
	private Alert alert;

	@FXML
	private ListView<Category> listViewXML_CategoryList;

	@FXML
	private ListView<Contact> listViewXML_ContactList;

	@FXML
	private ListView<Event> listViewXML_EventList;

	/**
	 * Obsługuje zdarzenie przycisku wyświetlania danych z pliku XML. Otwiera okno
	 * dialogowe do wyboru pliku, wczytuje dane z pliku, a następnie wyświetla
	 * kategorie, kontakty i wydarzenia na odpowiednich listach.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonXML_DisplayData(ActionEvent actionEvent)
	{
		File selectedFile = fileChooser.showOpenDialog(new Stage());

		if (selectedFile == null)
		{
			this.alert.setHeaderText("Empty file");
			this.alert.setContentText("The file you trying to open is empty.");
			this.alert.showAndWait();
			return;
		}

		String filePath = selectedFile.getAbsolutePath();

		if (!filePath.toLowerCase().endsWith(".xml"))
		{
			this.alert.setHeaderText("Cannot read this file");
			this.alert.setContentText("The file you trying to open is not valid.");
			this.alert.showAndWait();
			return;
		}

		XMLDataWrapper xmlData = this.controller.loadFromXML(filePath);

		if (xmlData != null)
		{
			listViewXML_CategoryList.getItems().setAll(xmlData.getCategories());
			listViewXML_ContactList.getItems().setAll(xmlData.getContacts());
			listViewXML_EventList.getItems().setAll(xmlData.getEvents());
		}
	}

	/**
	 * Inicjalizuje widok zakładki XML, ustawiając referencję do kontrolera oraz
	 * inicjalizując obiekty do obsługi plików i ostrzeżeń.
	 *
	 * @param controller Obiekt kontrolera, który zarządza logiką biznesową.
	 */
	public void init(Controller controller)
	{
		this.controller = controller;
		this.fileChooser = new FileChooser();
		this.fileChooser.setInitialDirectory(new File("data/xml_files/"));
		this.alert = new Alert(AlertType.ERROR);
	}
}
