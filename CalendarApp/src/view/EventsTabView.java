package view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Category;
import model.Contact;
import model.Event;

/**
 * Klasa obsługująca widok zakładki z wydarzeniami w aplikacji. Umożliwia
 * dodawanie, przeglądanie, aktualizowanie oraz usuwanie wydarzeń. Implementuje
 * graficzny interfejs użytkownika przy użyciu JavaFX.
 */
public class EventsTabView
{
	private Controller controller;
	private CalendarView calendarView;
	private Alert alert;

	private List<Event> eventList;
	private List<Contact> contactList;
	private List<Category> categoryList;
	private String[] eventSortByMethods;
	private String[] contactSortByMethods;

	@FXML
	private ComboBox<Category> comboBoxEvent_Category;

	@FXML
	private ComboBox<String> comboBoxEvent_ContactsSortBy;

	@FXML
	private ComboBox<String> comboBoxEvent_SortBy;

	@FXML
	private DatePicker datePickerEvent_Date;

	@FXML
	private DatePicker datePickerEvent_FilterBy;

	@FXML
	private ListView<Contact> listViewEvent_Contacts;

	@FXML
	private ListView<Contact> listViewEvent_ContactsSelected;

	@FXML
	private ListView<Event> listViewEvent_EventList;

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
	 * Obsługuję zdarzenie kliknięcia przycisku "Add New Event". Dodaje nowe
	 * wydarzenie na podstawie wprowadzonych danych i odświeża listę wydarzeń oraz
	 * kalendarz.
	 */
	@FXML
	private void buttonEvent_AddNewEvent_Click(ActionEvent actionEvent)
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
			alert.setAlertType(AlertType.WARNING);
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

		if (this.controller.isDateTimeOccupied(eventDateTime))
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Event date and time is occupied");
			alert.setContentText("An event with the same date and time [" + eventDate + " " + parsedEventTime + "] already exists!");
			alert.showAndWait();
			return;
		}

		LocalTime notifyOffset = this.controller.parseStringToLocalTime(eventNotifyOffset);

		try
		{
			this.controller.addNewEvent(eventName, eventDateTime, notifyOffset, eventLocation, eventCategory, eventDescription, eventContacts);
			clearFields();
			refreshEventList();
			this.calendarView.refreshCalendar();
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
	private void buttonEvent_ClearFields_Click(ActionEvent actionEvent)
	{
		clearFields();
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
	 * Obsługuje zdarzenie kliknięcia przycisku "Delete Older Events". Usuwa
	 * wydarzenia starsze niż data wybrana w polu datePickerEvent_FilterBy.
	 * Wyświetla potwierdzenie przed usunięciem.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonEvent_DeleteOlderEvents_Click(ActionEvent actionEvent)
	{
		LocalDate selectedDate = datePickerEvent_FilterBy.getValue();

		if (selectedDate == null)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("No data selected");
			alert.setContentText("Choose data in date picker to delete older events!");
			alert.showAndWait();
			return;
		}

		alert.setAlertType(AlertType.CONFIRMATION);
		alert.setHeaderText("Delete older events");
		alert.setContentText("Are you sure you want to delete events older than " + selectedDate + " ?");

		alert.showAndWait().ifPresent(response ->
		{
			if (response == ButtonType.OK)
			{
				try
				{
					this.controller.deleteOldEvents(selectedDate);
					refreshEventList();
					this.calendarView.refreshCalendar();
				}
				catch (Exception ex)
				{
					alert.setAlertType(AlertType.WARNING);
					alert.setHeaderText("Something goes wrong");
					alert.setContentText("Can't delete old events.\n" + ex.getMessage());
					alert.showAndWait();
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Delete". Usuwa zaznaczone
	 * wydarzenie z listy. Wyświetla ostrzeżenie, jeśli nie wybrano żadnego
	 * wydarzenia.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonEvent_Delete_Click(ActionEvent actionEvent)
	{
		Event e = listViewEvent_EventList.getSelectionModel().getSelectedItem();

		if (e == null)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("No selected event");
			alert.setContentText("Select event from list to delete it!");
			alert.showAndWait();
			return;
		}

		try
		{
			this.controller.deleteEvent(e);
			refreshEventList();
			this.calendarView.refreshCalendar();
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Cannot delete this event [" + e.getName() + "]");
			alert.setContentText(ex.getMessage());
			alert.showAndWait();
			ex.printStackTrace();
		}
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Reset". Przywraca domyślne
	 * ustawienia filtrów i sortowania oraz odświeża listę wydarzeń.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonEvent_Reset_Click(ActionEvent actionEvent)
	{
		datePickerEvent_FilterBy.setValue(LocalDate.now());
		comboBoxEvent_SortBy.setValue(this.eventSortByMethods[1]);
		refreshEventList();
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
		// ObservableList<Contact> contactsSelectedList =
		// listViewEvent_ContactsSelected.getItems();
		List<Contact> contactsSelectedList = listViewEvent_ContactsSelected.getItems();
		List<Contact> chosenContacts = listViewEvent_Contacts.getSelectionModel().getSelectedItems();

		for (Contact c : chosenContacts)
		{
			if (!contactsSelectedList.contains(c))
				contactsSelectedList.add(c);
		}
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Update". Otwiera okno aktualizacji
	 * wybranego wydarzenia, jeżeli jakieś wydarzenie jest zaznaczone na liście,
	 * przekazując dalej referencję głównego kontrolera oraz danych wybranego
	 * obiektu wydarzenia. Dodatkowo dodaje ustawienie obsługi zdarzenia podczas
	 * zamknięcia okna, aby odświeżyło listę wydarzeń i kalendarza.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonEvent_Update_Click(ActionEvent actionEvent)
	{
		if (listViewEvent_EventList.getSelectionModel().getSelectedItem() == null)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("No selected event");
			alert.setContentText("Select event from list to update it!");
			alert.showAndWait();
			return;
		}

		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UpdateEventPopupView.fxml"));
			Parent root = fxmlLoader.load();

			UpdateEventPopupView updateEventPopup = fxmlLoader.getController();
			Event selectedEvent = listViewEvent_EventList.getSelectionModel().getSelectedItem();
			updateEventPopup.init(this.controller, selectedEvent);

			Stage stage = new Stage();
			stage.initStyle(StageStyle.UTILITY);
			stage.setScene(new Scene(root));

			stage.setOnHidden(e ->
			{
				refreshEventList();
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
	private void comboBoxEvent_ContactsSortBy_Select(ActionEvent actionEvent)
	{
		String selectedSortBy = comboBoxEvent_ContactsSortBy.getValue();
		refreshContactList(selectedSortBy);
	}

	/**
	 * Obsługuje zdarzenie wyboru elementu z ComboBox do sortowania wydarzeń.
	 * Odświeża listę wydarzeń zgodnie z wybranym kryterium sortowania.
	 *
	 * @param actionEvent Zdarzenie akcji wyboru z ComboBox.
	 */
	@FXML
	private void comboBoxEvent_SortBy_Select(ActionEvent actionEvent)
	{
		String selectedSortBy = comboBoxEvent_SortBy.getValue();
		refreshEventList(selectedSortBy);
	}

	/**
	 * Obsługuje zdarzenie wyboru daty z DatePicker do filtrowania wydarzeń.
	 * Odświeża listę wydarzeń zgodnie z wybraną datą.
	 *
	 * @param actionEvent Zdarzenie akcji wyboru daty z DatePicker.
	 */
	@FXML
	private void datePickerEvent_FilterBy_Select(ActionEvent actionEvent)
	{
		LocalDate selectedDate = datePickerEvent_FilterBy.getValue();
		List<Event> filteredEvents = this.controller.getEventsByDate(selectedDate);

		listViewEvent_EventList.getItems().clear();
		listViewEvent_EventList.getItems().addAll(filteredEvents);
	}

	/**
	 * @return Obiekt ComboBox kategorii wydarzenia.
	 */
	public ComboBox<Category> getComboBoxEvent_Category()
	{
		return comboBoxEvent_Category;
	}

	/**
	 * Czyści pola formularza dodawania nowego wydarzenia.
	 */
	private void clearFields()
	{
		textFieldEvent_Name.clear();
		datePickerEvent_Date.setValue(null);
		textFieldEvent_Time.clear();
		textFieldEvent_NotifyOffset.clear();
		textFieldEvent_Location.clear();
		comboBoxEvent_Category.setValue(null);
		textAreaEvent_Description.clear();
		listViewEvent_ContactsSelected.getItems().clear();
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
		listViewEvent_EventList.getItems().clear();

		this.controller.sortEvents(sortBy);

		listViewEvent_EventList.getItems().addAll(this.eventList);
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
		listViewEvent_Contacts.getItems().clear();

		this.controller.sortContacts(sortBy);

		listViewEvent_Contacts.getItems().addAll(this.contactList);
	}

	/**
	 * Inicjalizuje widok zakładki z wydarzeniami, ustawiając referencje do
	 * kontrolera, widoku kalendarza oraz wczytując dane o kategoriach,
	 * wydarzeniach, i kontaktach. Dodatkowo, inicjalizuje interfejs użytkownika,
	 * ustawiając domyślne wartości i ustawienia.
	 *
	 * @param controller   Obiekt kontrolera, który zarządza logiką biznesową.
	 * @param calendarView Widok kalendarza, z którym ta klasa współpracuje.
	 */
	public void init(Controller controller, CalendarView calendarView)
	{
		this.controller = controller;
		this.calendarView = calendarView;

		this.categoryList = this.controller.getCategories();
		this.eventList = this.controller.getEvents();
		this.contactList = this.controller.getContacts();
		this.eventSortByMethods = this.controller.getEventsSortBy();
		this.contactSortByMethods = this.controller.getContactSortBy();
		this.alert = new Alert(AlertType.NONE);

		refreshEventList();
		refreshContactList();

		datePickerEvent_FilterBy.setValue(LocalDate.now());

		comboBoxEvent_SortBy.getItems().addAll(this.eventSortByMethods);
		comboBoxEvent_SortBy.setValue(this.eventSortByMethods[1]);

		comboBoxEvent_ContactsSortBy.getItems().addAll(this.contactSortByMethods);
		comboBoxEvent_ContactsSortBy.setValue(this.contactSortByMethods[0]);

		listViewEvent_Contacts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		comboBoxEvent_Category.getItems().addAll(this.categoryList);
	}
}
