package view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import application.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Contact;
import model.Event;

/**
 * Klasa obsługująca widok zakładki z wydarzeniami w aplikacji. Umożliwia
 * dodawanie, przeglądanie, aktualizowanie oraz usuwanie kontaktów. Implementuje
 * graficzny interfejs użytkownika przy użyciu JavaFX.
 */
public class ContactsTabView
{
	private Controller controller;
	private CalendarView calendarView;
	private Alert alert;

	private List<Contact> contactList;
	private List<Event> eventList;
	private String[] contactSortByMethods;

	@FXML
	private ComboBox<String> comboBoxContact_SortBy;

	@FXML
	private DatePicker datePickerContact_FilterBy;

	@FXML
	private ListView<Contact> listViewContact_ContactList;

	@FXML
	private ListView<Event> listViewContact_Events;

	@FXML
	private ListView<Event> listViewContact_EventsSelected;

	@FXML
	private TextField textFieldContact_FirstName;

	@FXML
	private TextField textFieldContact_LastName;

	@FXML
	private TextField textFieldContact_PhoneNumber;

	/**
	 * Obsługuję zdarzenie kliknięcia przycisku "Add New Contact". Dodaje nowy
	 * kontakt na podstawie wprowadzonych danych i odświeża listę kontaktów.
	 */
	@FXML
	private void buttonContact_AddNewContact_Click(ActionEvent actionEvent)
	{
		String contactFirstName = textFieldContact_FirstName.getText();
		String contactLastName = textFieldContact_LastName.getText();
		String contactPhoneNumber = textFieldContact_PhoneNumber.getText();
		List<Event> contactEvents = new ArrayList<Event>(listViewContact_EventsSelected.getItems());

		if (contactFirstName.isBlank() || contactLastName.isBlank() || contactPhoneNumber.isBlank())
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Empty fields");
			alert.setContentText("Fields cannot be empty!");
			alert.showAndWait();
			return;
		}

		if (!this.controller.isPhoneNumberValid(contactPhoneNumber))
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Bad phone number format");
			alert.setContentText("Phone number must be in 9-digit sequence!");
			alert.showAndWait();
			return;
		}

		if (this.controller.isPhoneNumberExists(contactPhoneNumber))
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Phone number already exists");
			alert.setContentText("A contact with the same phone number [" + contactPhoneNumber + "] already exists!");
			alert.showAndWait();
			return;
		}

		try
		{
			this.controller.addNewContact(contactFirstName, contactLastName, contactPhoneNumber, contactEvents);

			clearFields();
			refreshContactList();
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.ERROR);
			alert.setHeaderText("Something goes wrong");
			alert.setContentText(ex.getMessage());
			alert.showAndWait();
		}
	}

	/**
	 * Obsługuję zdarzenie kliknięcia przycisku "Clear Fields". Czyści wprowadzone
	 * dane w polach formularza.
	 */
	@FXML
	private void buttonContact_ClearFields_Click(ActionEvent actionEvent)
	{
		clearFields();
	}

	/**
	 * Obsługuję zdarzenie kliknięcia przycisku "Clear Selected Events". Usuwa
	 * zaznaczone wydarzenie z listy wybranych wydarzeń.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonContact_ClearSelectedEvents_Click(ActionEvent actionEvent)
	{
		Event selectedItem = listViewContact_EventsSelected.getSelectionModel().getSelectedItem();
		listViewContact_EventsSelected.getItems().remove(selectedItem);
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Delete". Usuwa zaznaczony kontakt z
	 * listy. Wyświetla ostrzeżenie, jeśli nie wybrano żadnego kontaktu.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonContact_Delete_Click(ActionEvent actionEvent)
	{
		Contact c = listViewContact_ContactList.getSelectionModel().getSelectedItem();

		if (c == null)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("No selected contact");
			alert.setContentText("Select contact from list to delete it!");
			alert.showAndWait();
			return;
		}

		try
		{
			this.controller.deleteContact(c);
			refreshContactList();
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Cannot delete this contact [" + c.getFirstName() + " " + c.getLastName() + "]");
			alert.setContentText(ex.getMessage());
			alert.showAndWait();
			ex.printStackTrace();
		}
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Select Events". Dodaje zaznaczone
	 * wydarzenia z listy dostępnych wydarzeń do listy wybranych wydarzeń.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonContact_SelectEvents_Click(ActionEvent actionEvent)
	{
		// ObservableList<Event> eventsSelectedList =
		// listViewContact_EventsSelected.getItems();
		List<Event> eventsSelectedList = listViewContact_EventsSelected.getItems();
		List<Event> chosenEvents = listViewContact_Events.getSelectionModel().getSelectedItems();

		for (Event e : chosenEvents)
		{
			if (!eventsSelectedList.contains(e))
				eventsSelectedList.add(e);
		}
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Update". Otwiera okno aktualizacji
	 * wybranego kontaktu, jeżeli jakiś kontakt jest zaznaczone na liście,
	 * przekazując dalej referencję głównego kontrolera oraz danych wybranego
	 * obiektu kontaktu. Dodatkowo dodaje ustawienie obsługi zdarzenia podczas
	 * zamknięcia okna, aby odświeżyło listę kontaktów.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonContact_Update_Click(ActionEvent actionEvent)
	{
		if (listViewContact_ContactList.getSelectionModel().getSelectedItem() == null)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("No selected contact");
			alert.setContentText("Select contact from list to update it!");
			alert.showAndWait();
			return;
		}

		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UpdateContactPopupView.fxml"));
			Parent root = fxmlLoader.load();

			UpdateContactPopupView updateContactPopup = fxmlLoader.getController();
			Contact selectedContact = listViewContact_ContactList.getSelectionModel().getSelectedItem();
			updateContactPopup.init(this.controller, selectedContact);

			Stage stage = new Stage();
			stage.initStyle(StageStyle.UTILITY);
			stage.setScene(new Scene(root));

			stage.setOnHidden(e ->
			{
				refreshContactList();
				this.calendarView.refreshCalendar();
			});

			stage.show();
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Something goes wrong");
			alert.setContentText("Can't load update window.\n" + ex.getMessage());
			alert.showAndWait();
			ex.printStackTrace();
		}
	}

	/**
	 * Obsługuje zdarzenie wyboru elementu z ComboBox do sortowania kontaktów.
	 * Odświeża listę kontaktów zgodnie z wybranym kryterium sortowania.
	 *
	 * @param actionEvent Zdarzenie akcji wyboru z ComboBox.
	 */
	@FXML
	private void comboBoxContact_SortBy_Select(ActionEvent actionEvent)
	{
		String selectedSortBy = comboBoxContact_SortBy.getValue();
		refreshContactList(selectedSortBy);
	}

	/**
	 * Obsługuje zdarzenie wyboru daty z DatePicker do filtrowania wydarzeń.
	 * Odświeża listę wydarzeń zgodnie z wybraną datą.
	 *
	 * @param actionEvent Zdarzenie akcji wyboru daty z DatePicker.
	 */
	@FXML
	private void datePickerContact_FilterBy_Select(ActionEvent actionEvent)
	{
		LocalDate selectedDate = datePickerContact_FilterBy.getValue();
		List<Event> filteredEvents = this.controller.getEventsByDate(selectedDate);

		listViewContact_Events.getItems().clear();
		listViewContact_Events.getItems().addAll(filteredEvents);
	}

	/**
	 * Czyści pola formularza dodawania nowego kontaktu.
	 */
	private void clearFields()
	{
		textFieldContact_FirstName.clear();
		textFieldContact_LastName.clear();
		textFieldContact_PhoneNumber.clear();
		listViewContact_EventsSelected.getItems().clear();
	}

	/**
	 * Odświeża listę kontaktów w interfejsie użytkownika, używając domyślnego
	 * sortowania.
	 */
	public void refreshContactList()
	{
		refreshContactList("");
	}

	/**
	 * Odświeża listę kontaktów w interfejsie użytkownika, używając określonej
	 * metody sortowania.
	 *
	 * @param sortBy Metoda sortowania, według której mają być posortowane kontakty.
	 */
	private void refreshContactList(String sortBy)
	{
		listViewContact_ContactList.getItems().clear();

		this.controller.sortContacts(sortBy);

		listViewContact_ContactList.getItems().addAll(this.contactList);
	}

	/**
	 * Odświeża listę wydarzeń w interfejsie użytkownika, używając domyślnego
	 * sortowania.
	 */
	public void refreshEventList()
	{
		refreshEventList("");
	}

	/**
	 * Odświeża listę wydarzeń w interfejsie użytkownika, używając określonej metody
	 * sortowania.
	 *
	 * @param sortBy Metoda sortowania, według której mają być posortowane
	 *               wydarzenia.
	 */
	private void refreshEventList(String sortBy)
	{
		listViewContact_Events.getItems().clear();

		this.controller.sortEvents(sortBy);

		listViewContact_Events.getItems().addAll(this.eventList);
	}

	/**
	 * Inicjalizuje widok zakładki z kontaktami, ustawiając referencje do kontrolera
	 * oraz kalendarza. Wczytuje kolejno dane o wydarzeniach i kontaktach.
	 * Dodatkowo, inicjalizuje interfejs użytkownika, ustawiając domyślne wartości i
	 * ustawienia.
	 *
	 * @param controller   Obiekt kontrolera, który zarządza logiką biznesową.
	 * @param calendarView Widok kalendarza, z którym ta klasa współpracuje.
	 */
	public void init(Controller controller, CalendarView calendarView)
	{
		this.controller = controller;
		this.calendarView = calendarView;

		this.contactList = this.controller.getContacts();
		this.eventList = this.controller.getEvents();
		this.contactSortByMethods = this.controller.getContactSortBy();
		this.alert = new Alert(AlertType.NONE);

		refreshContactList();
		refreshEventList();

		comboBoxContact_SortBy.getItems().addAll(this.contactSortByMethods);
		comboBoxContact_SortBy.setValue(this.contactSortByMethods[0]);

		datePickerContact_FilterBy.setValue(LocalDate.now());

		listViewContact_Events.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
}
