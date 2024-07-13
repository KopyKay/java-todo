package view;

import java.net.URL;
import java.util.ResourceBundle;

import application.Controller;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Klasa reprezentująca główne okno aplikacji z graficznym interfejsem
 * użytkownika, korzystająca z biblioteki {@code JavaFX}. Implementuje interfejs
 * {@code Initializable}, obsługując załączone widoki i kontrolki użytkownika,
 * które inicjalizuje oraz przedstawia w obiekcie tej klasy {@link #MainView()}.
 */
public class MainView implements Initializable
{
	@FXML
	private AnchorPane mainAnchorPane;

	@FXML
	private AnchorPane calendarAnchorPane;

	@FXML
	private TabPane tabPane;

	private Controller controller;
	private CalendarView calendarView;
	private Alert alert;

	/**
	 * Inicjalizuje główne okno aplikacji wraz z dodatkowymi widokami i kontrolkami.
	 * Tworzy nowy obiekt kontrolera, który zostanie przekazany do kolejnych widoków
	 * i kontrolek. Następnie inicjalizuje ten kontroler, który zaczyna wczytywać
	 * dane aplikacji. Finalnie przekazuje obsługę metody
	 * {@link #handleCloseRequest()} do głównego wątku.
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.alert = new Alert(AlertType.NONE);

		try
		{
			controller = new Controller();
			controller.init();
		}
		catch (Exception e)
		{
			displaySynchronizationError();
		}

		// calendar must be first initialized
		loadCalendar();
		loadEventsTab();
		loadContactsTab();
		loadCategoriesTab();
		loadXMLTab();

		Platform.runLater(() ->
		{
			Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
			stage.setOnCloseRequest(event -> handleCloseRequest());
		});
	}

	/**
	 * Ładuje widok kalendarza i przekazuje kontroller do klasy reprezentującej
	 * widok kalendarza. Następnie załącza ten widok do {@link #calendarAnchorPane}.
	 */
	private void loadCalendar()
	{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CalendarView.fxml"));
			AnchorPane calendarContent = fxmlLoader.load();

			CalendarView calendar = fxmlLoader.getController();
			this.calendarView = calendar; // reference

			calendar.init(this.controller);

			calendarAnchorPane.getChildren().add(calendarContent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Ładuje zakładkę "Events" zawierającą widok wydarzeń. Inicjalizuje obiekt tego
	 * widoku, przekazuje obiekt kontrolera aplikacji oraz kalendarza. Dodaje
	 * zakładkę do głównego panelu zakładek {@link #tabPane}. Ustawia obsługę
	 * zdarzenia zmiany zaznaczenia zakładki, w wyniku której odświeżane są listy
	 * wydarzeń i kontaktów, a także aktualizowana jest lista rozwijana kategorii
	 * wydarzeń.
	 */
	private void loadEventsTab()
	{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EventsTabView.fxml"));
			AnchorPane content = fxmlLoader.load();

			EventsTabView eventsTab = fxmlLoader.getController();
			eventsTab.init(this.controller, this.calendarView);

			Tab tab = new Tab("Events");
			tab.setContent(content);
			tabPane.getTabs().add(tab);

			tab.setOnSelectionChanged(e ->
			{
				if (tab.isSelected())
				{
					eventsTab.refreshEventList();
					eventsTab.refreshContactList();
					eventsTab.getComboBoxEvent_Category().getItems().clear();
					eventsTab.getComboBoxEvent_Category().getItems().addAll(this.controller.getCategories());
				}
			});
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Ładuje zakładkę "Contacts" zawierającą widok kontaktów. Inicjalizuje obiekt
	 * tego widoku, przekazuje obiekt kontrolera aplikacji. Dodaje zakładkę do
	 * głównego panelu zakładek {@link #tabPane}. Ustawia obsługę zdarzenia zmiany
	 * zaznaczenia zakładki, w wyniku której odświeżane są listy kontaktów i
	 * wydarzeń.
	 */
	private void loadContactsTab()
	{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ContactsTabView.fxml"));
			AnchorPane content = fxmlLoader.load();

			ContactsTabView contactsTab = fxmlLoader.getController();
			contactsTab.init(this.controller, this.calendarView);

			Tab tab = new Tab("Contacts");
			tab.setContent(content);
			tabPane.getTabs().add(tab);

			tab.setOnSelectionChanged(e ->
			{
				if (tab.isSelected())
				{
					contactsTab.refreshContactList();
					contactsTab.refreshEventList();
				}
			});
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Ładuje zakładkę "Categories" zawierającą widok kategorii. Inicjalizuje obiekt
	 * tego widoku, przekazuje obiekt kontrolera aplikacji oraz kalendarza. Dodaje
	 * zakładkę do głównego panelu zakładek {@link #tabPane}.
	 */
	private void loadCategoriesTab()
	{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CategoriesTabView.fxml"));
			AnchorPane content = fxmlLoader.load();

			CategoriesTabView categoriesTab = fxmlLoader.getController();
			categoriesTab.init(this.controller, this.calendarView);

			Tab tab = new Tab("Categories");
			tab.setContent(content);
			tabPane.getTabs().add(tab);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Ładuje zakładkę "XML" zawierającą widok obsługi plików XML. Inicjalizuje
	 * obiekt tego widoku, przekazuje obiekt kontrolera aplikacji. Dodaje zakładkę
	 * do głównego panelu zakładek {@link #tabPane}.
	 */
	private void loadXMLTab()
	{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("XMLTabView.fxml"));
			AnchorPane content = fxmlLoader.load();

			XMLTabView xmlTab = fxmlLoader.getController();
			xmlTab.init(this.controller);

			Tab tab = new Tab("XML");
			tab.setContent(content);
			tabPane.getTabs().add(tab);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Obsługuje zdarzenie zamknięcia głównego okna aplikacji. Wywołuje metodę
	 * {@link Controller#saveToXML()} w celu zapisania danych aplikacji do pliku
	 * XML. Następnie wywołuje metodę {@link Platform#exit()} w celu zamknięcia
	 * programu.
	 */
	private void handleCloseRequest()
	{
		try
		{
			this.controller.saveToXML();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Platform.exit();
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "?" w głównym oknie aplikacji.
	 * Wyświetla informacyjne okno dialogowe z krótkim opisem aplikacji.
	 */
	@FXML
	void buttonMain_Question_Click(ActionEvent actionEvent)
	{
		this.alert.setAlertType(AlertType.INFORMATION);
		this.alert.setHeaderText("About this application");
		this.alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		this.alert.setContentText(
				"The Calendar app is a program that helps users organize their time.\nYou can add personal events and contacts to the calendar. Events can be divided into categories, and you can color-code them to stand out in the calendar. The XML tab lets users save the application's data to a .xml file. This file can be used as a backup or just to check its contents. In the Calendar, user can see the date, time, and a visual representation of tasks for that day. This helps in viewing the details of a specific day.");
		this.alert.showAndWait();
	}

	/**
	 * Wyświetla okno dialogowe o błędzie synchronizacji bazy danych, oczekując na
	 * potwierdzenie przeczytania komunikatu przez użytkownika, zanim załaduje się
	 * główne okno aplikacji.
	 */
	private void displaySynchronizationError()
	{
		this.alert.setAlertType(AlertType.ERROR);
		this.alert.setHeaderText("Database synchronization error");
		this.alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		this.alert.setContentText(
				"Failed to synchronize with the database.\nYour data will be saved locally, and it will be synchronized with\nthe database upon the next connection.");
		this.alert.showAndWait();
	}
}
