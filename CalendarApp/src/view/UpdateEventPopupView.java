package view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import application.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Category;
import model.Contact;
import model.Event;

/**
 * Ta klasa reprezentuje widok do aktualizacji wydarzeń w wyskakującym oknie.
 */
public class UpdateEventPopupView
{
	private Controller controller;
	private Alert alert;

	private Event event;
	private List<Contact> contactList;
	private List<Category> categoryList;
	private String[] contactSortByMethods;

	@FXML
	private ComboBox<Category> comboBoxEvent_Category;

	@FXML
	private ComboBox<String> comboBoxEvent_ContactsSortBy;

	@FXML
	private DatePicker datePickerEvent_Date;

	@FXML
	private ListView<Contact> listViewEvent_Contacts;

	@FXML
	private ListView<Contact> listViewEvent_ContactsSelected;

	@FXML
	private TextArea textAreaEvent_Description;

	@FXML
	private TextField textFieldEvent_Location;

	@FXML
	private TextField textFieldEvent_Name;

	@FXML
	private TextField textFieldEvent_NotifyOffset;

	@FXML
	private TextField textFieldEvent_Time;

	/**
	 * Obsługuje zdarzenie anulowania aktualizacji wydarzenia.
	 *
	 * @param actionEvent Zdarzenie akcji.
	 */
	@FXML
	private void buttonEvent_Cancel_Click(ActionEvent actionEvent)
	{
		closeWindow(actionEvent);
	}

	/**
	 * Obsługuję zdarzenie kliknięcia przycisku "Clear Selected Contacts". Usuwa
	 * zaznaczony kontakt z listy wybranych kontaktów.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonEvent_ClearSelectedContacts_Click(ActionEvent actionEvent)
	{
		Contact selectedItem = listViewEvent_ContactsSelected.getSelectionModel().getSelectedItem();
		listViewEvent_ContactsSelected.getItems().remove(selectedItem);
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Select Contacts". Dodaje zaznaczone
	 * kontakty z listy dostępnych kontaktów do listy wybranych kontaktów.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonEvent_SelectContacts_Click(ActionEvent actionEvent)
	{
		List<Contact> contactsSelectedList = listViewEvent_ContactsSelected.getItems();
		List<Contact> chosenContacts = listViewEvent_Contacts.getSelectionModel().getSelectedItems();

		for (Contact c : chosenContacts)
		{
			if (!contactsSelectedList.contains(c))
				contactsSelectedList.add(c);
		}
	}

	/**
	 * Obsługuje zdarzenie przycisku aktualizacji wydarzenia w widoku edycji.
	 * Pobiera dane z pól formularza, waliduje je, a następnie wywołuje metodę
	 * kontrolera do aktualizacji wydarzenia. W przypadku błędów wyświetla
	 * odpowiednie ostrzeżenia.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonEvent_UpdateSelectedEvent_Click(ActionEvent actionEvent)
	{
		String eventName = textFieldEvent_Name.getText();
		LocalDate eventDate = datePickerEvent_Date.getValue();
		String eventTime = textFieldEvent_Time.getText();
		String eventNotifyOffset = textFieldEvent_NotifyOffset.getText();
		String eventLocation = textFieldEvent_Location.getText();
		Category eventCategory = comboBoxEvent_Category.getValue();
		String eventDescription = textAreaEvent_Description.getText();
		List<Contact> eventContacts = new ArrayList<Contact>(listViewEvent_ContactsSelected.getItems());

		if (eventName.isBlank() || eventDate == null || eventTime.isBlank())
		{
			alert.setHeaderText("Empty fields");
			alert.setContentText("Name, Date and Time cannot be empty!");
			alert.showAndWait();
			return;
		}

		if (eventNotifyOffset.isBlank())
		{
			eventNotifyOffset = "00:00";
		}

		if (!this.controller.isTimeValid(eventTime) || !this.controller.isTimeValid(eventNotifyOffset))
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Bad time format");
			alert.setContentText("Time must be in [HH:mm] format!");
			alert.showAndWait();
			return;
		}

		LocalTime parsedEventTime = this.controller.parseStringToLocalTime(eventTime);
		LocalDateTime eventDateTime = this.controller.mergeDateTime(eventDate, parsedEventTime);

		int eventId = this.event.getId();

		if (this.controller.isDateTimeOccupied(eventId, eventDateTime))
		{
			alert.setHeaderText("Event date and time duplicate");
			alert.setContentText("An event with the same date and time [" + eventDate + " " + parsedEventTime + "] already exists!");
			alert.showAndWait();
			return;
		}

		LocalTime notifyOffset = this.controller.parseStringToLocalTime(eventNotifyOffset);

		try
		{
			this.controller.updateEvent(this.event, eventName, eventDateTime, notifyOffset, eventLocation, eventCategory, eventDescription,
					eventContacts);
			closeWindow(actionEvent);
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.ERROR);
			alert.setHeaderText("Something goes wrong");
			alert.setContentText(ex.getMessage());
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
	private void comboBoxEvent_ContactsSortBy_Select(ActionEvent actionEvent)
	{
		String selectedSortBy = comboBoxEvent_ContactsSortBy.getValue();
		refreshContactList(selectedSortBy);
	}

	/**
	 * Odświeża listę kontaktów w interfejsie użytkownika, używając domyślnego
	 * sortowania.
	 */
	private void refreshContactList()
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
		listViewEvent_Contacts.getItems().clear();

		this.controller.sortContacts(sortBy);

		listViewEvent_Contacts.getItems().addAll(this.contactList);
	}

	/**
	 * Zamyka aktualne okno, w którym wydarzenie jest aktualizowane.
	 *
	 * @param actionEvent Zdarzenie akcji, które wywołało zamknięcie okna.
	 */
	private void closeWindow(ActionEvent actionEvent)
	{
		Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
		stage.close();
	}

	/**
	 * Ustawia dane wybranego wydarzenia w formularzu aktualizacyjnym, wcześniej
	 * wybranego z listy w {@code view.EventsTabView}.
	 */
	private void setEventData()
	{
		textFieldEvent_Name.setText(this.event.getName());
		datePickerEvent_Date.setValue(this.event.getDate().toLocalDate());
		textFieldEvent_Time.setText(this.event.getDate().toLocalTime().toString());
		textFieldEvent_NotifyOffset.setText(this.event.getNotifyOffset().toString());
		textFieldEvent_Location.setText(this.event.getLocation());
		comboBoxEvent_Category.setValue(this.event.getCategory());
		textAreaEvent_Description.setText(this.event.getDescription());
		listViewEvent_ContactsSelected.getItems().addAll(this.event.getContacts());
	}

	/**
	 * Inicjalizuje widok wyskakującego okna aktualizacji wydarzenia ustawiając
	 * domyślne wartości i ustawienia oraz referencję do kontrolera i wczytuje dane
	 * o kategoriach i kontaktach.
	 *
	 * @param controller Kontroler aplikacji.
	 * @param event      Aktualizowane wydarzenie.
	 */
	public void init(Controller controller, Event event)
	{
		this.controller = controller;
		this.event = event;
		this.contactList = this.controller.getContacts();
		this.categoryList = this.controller.getCategories();
		this.contactSortByMethods = this.controller.getContactSortBy();
		this.alert = new Alert(AlertType.NONE);

		setEventData();
		refreshContactList();
		comboBoxEvent_Category.getItems().addAll(this.categoryList);
		comboBoxEvent_ContactsSortBy.getItems().addAll(this.contactSortByMethods);
		comboBoxEvent_ContactsSortBy.setValue(this.contactSortByMethods[0]);

		listViewEvent_Contacts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
}
