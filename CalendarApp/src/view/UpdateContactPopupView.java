package view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import application.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Contact;
import model.Event;

/**
 * Ta klasa reprezentuje widok do aktualizacji kontaktów w wyskakującym oknie.
 */
public class UpdateContactPopupView
{
	private Controller controller;
	private Alert alert;

	private Contact contact;
	private List<Event> eventList;

	@FXML
	private DatePicker datePickerContact_FilterBy;

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
	 * Obsługuje zdarzenie anulowania aktualizacji kontaktu.
	 *
	 * @param actionEvent Zdarzenie akcji.
	 */
	@FXML
	private void buttonContact_Cancel_Click(ActionEvent actionEvent)
	{
		closeWindow(actionEvent);
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
	 * Obsługuje zdarzenie kliknięcia przycisku "Select Events". Dodaje zaznaczone
	 * wydarzenia z listy dostępnych wydarzeń do listy wybranych wydarzeń.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonContact_SelectEvents_Click(ActionEvent actionEvent)
	{
		List<Event> eventsSelectedList = listViewContact_EventsSelected.getItems();
		List<Event> chosenEvents = listViewContact_Events.getSelectionModel().getSelectedItems();

		for (Event e : chosenEvents)
		{
			if (!eventsSelectedList.contains(e))
				eventsSelectedList.add(e);
		}
	}

	/**
	 * Obsługuje zdarzenie przycisku aktualizacji kontaktu w widoku edycji. Pobiera
	 * dane z pól formularza, waliduje je, a następnie wywołuje metodę kontrolera do
	 * aktualizacji wydarzenia. W przypadku błędów wyświetla odpowiednie
	 * ostrzeżenia.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonContact_UpdateSelectedContact_Click(ActionEvent actionEvent)
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

		int contactId = this.contact.getId();

		if (this.controller.isPhoneNumberExists(contactId, contactPhoneNumber))
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Phone number already exists");
			alert.setContentText("A contact with the same phone number [" + contactPhoneNumber + "] already exists!");
			alert.showAndWait();
			return;
		}

		try
		{
			this.controller.updateContact(this.contact, contactFirstName, contactLastName, contactPhoneNumber, contactEvents);
			closeWindow(actionEvent);
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
	 * Zamyka aktualne okno, w którym kontakt jest aktualizowany.
	 *
	 * @param actionEvent Zdarzenie akcji, które wywołało zamknięcie okna.
	 */
	private void closeWindow(ActionEvent actionEvent)
	{
		Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
		stage.close();
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
	 * Ustawia dane wybranego kontaktu w formularzu aktualizacyjnym, wcześniej
	 * wybranego z listy w {@code view.ContactsTabView}.
	 */
	private void setContactData()
	{
		textFieldContact_FirstName.setText(this.contact.getFirstName());
		textFieldContact_LastName.setText(this.contact.getLastName());
		textFieldContact_PhoneNumber.setText(this.contact.getPhoneNumber().replaceAll("\\s", ""));
		listViewContact_EventsSelected.getItems().addAll(this.contact.getEvents());
	}

	/**
	 * Inicjalizuje widok wyskakującego okna aktualizacji kontaktu ustawiając
	 * domyślne wartości i ustawienia oraz referencję do kontrolera i wczytuje dane
	 * o wydarzeniach.
	 *
	 * @param controller Kontroler aplikacji.
	 * @param contact    Aktualizowany kontakt.
	 */
	public void init(Controller controller, Contact contact)
	{
		this.controller = controller;
		this.contact = contact;
		this.eventList = this.controller.getEvents();
		this.alert = new Alert(AlertType.NONE);

		setContactData();
		refreshEventList();
		datePickerContact_FilterBy.setValue(LocalDate.now());
		listViewContact_Events.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
}
