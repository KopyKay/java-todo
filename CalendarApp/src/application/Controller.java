package application;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;
import model.Category;
import model.Contact;
import model.Event;
import postgresql.Database;
import serializer.XMLDataWrapper;
import serializer.XMLDeserializer;
import serializer.XMLSerializer;
import sorter.SortContactByFirstName;
import sorter.SortContactByLastName;
import sorter.SortContactByPhoneNumber;
import sorter.SortEventByCategory;
import sorter.SortEventByDate;
import sorter.SortEventByDescription;
import sorter.SortEventByLocation;
import sorter.SortEventByName;

/**
 * Główny kontroler aplikacji, pełniący rolę pośrednika między interfejsem
 * użytkownika, modelem danych, a bazą danych. Zarządza synchronizacją,
 * sortowaniem, aktualizacją i usuwaniem danych, a także oferuje funkcje
 * dodawania i walidacji. Dodatkowo, obsługuje operacje związane z formatem XML,
 * datą, czasem, oraz numerami telefonów. Klasa ta izoluje główną logikę
 * aplikacji, tworząc most między interfejsem a ukrytym modelem danych.
 */
public class Controller
{
	private List<Category> categories;
	private List<Event> events;
	private List<Contact> contacts;
	private Database database;
	private XMLSerializer xmlSerializer;
	private XMLDeserializer xmlDeserializer;
	private XMLDataWrapper xmlData;

	private SortEventByName byName;
	private SortEventByDate byDate;
	private SortEventByLocation byLocation;
	private SortEventByCategory byCategory;
	private SortEventByDescription byDescription;
	private SortContactByFirstName byFirstName;
	private SortContactByLastName byLastName;
	private SortContactByPhoneNumber byPhoneNumber;

	private String[] eventsSortBy;
	private String[] contactSortBy;

	private boolean databaseSynchronized;

	/**
	 * Kontroler inicjalizuje struktury danych, w tym listy kategorii, wydarzeń i
	 * kontaktów, a także instancje operacji na bazie danych oraz
	 * serializacji/deserializacji XML. Ponadto inicjalizuje kryteria sortowania dla
	 * wydarzeń i kontaktów. Kontroler udostępnia domyślne opcje sortowania dla
	 * wydarzeń i kontaktów, obejmujące sortowanie wydarzeń według nazwy, daty,
	 * lokalizacji, opisu i kategorii, a także kontaktów według imienia, nazwiska i
	 * numeru telefonu. Ustawia również początkowe wartości dla tablic
	 * reprezentujących dostępne opcje sortowania dla wydarzeń i kontaktów.
	 * Konstruktor klasy {@code Controller} ustawia flagę
	 * {@code databaseSynchronized} na {@code false}, wskazując, że baza danych nie
	 * jest początkowo zsynchronizowana.
	 */
	public Controller()
	{
		this.categories = new ArrayList<Category>();
		this.events = new ArrayList<>();
		this.contacts = new ArrayList<>();
		this.database = new Database();
		this.xmlSerializer = new XMLSerializer();
		this.xmlDeserializer = new XMLDeserializer();

		this.byName = new SortEventByName();
		this.byDate = new SortEventByDate();
		this.byLocation = new SortEventByLocation();
		this.byCategory = new SortEventByCategory();
		this.byDescription = new SortEventByDescription();
		this.byFirstName = new SortContactByFirstName();
		this.byLastName = new SortContactByLastName();
		this.byPhoneNumber = new SortContactByPhoneNumber();

		this.eventsSortBy = new String[] { "Name", "Date", "Location", "Description", "Category" };
		this.contactSortBy = new String[] { "First name", "Last name", "Phone number" };

		this.databaseSynchronized = false;
	}

	/**
	 * Inicjalizuje kontroler, wczytując dane z pliku XML
	 * {@link #loadDataFromXML()}. Następnie próbuje połączyć się z bazą danych i
	 * zsynchronizować dane z tabel bazy danych do list
	 * {@code List<Category> categories}, {@code List<Event> events},
	 * {@code List<Contact> contacts}. Jeśli wystąpią problemy podczas inicjalizacji
	 * lub synchronizacji z bazą danych. Po udanej synchronizacji, aktualizuje plik
	 * XML i ustawia zmienną {@code boolean databaseSynchronized = true}.
	 * 
	 * @throws SQLException W przypadku nieudanego połączenia z bazą danych, zmienna
	 *                      {@code databaseSynchronized} pozostaje {@code false}, co
	 *                      powoduje, że klasa {@link application.Controller} będzie
	 *                      komunikować się wyłącznie z plikiem XML.
	 */
	public void init() throws SQLException
	{
		loadDataFromXML();

		try
		{
			this.database.synchronize(this.categories, this.events, this.contacts);
			this.databaseSynchronized = true;
			saveToXML();
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * @return Niemodyfikowalna lista kategorii.
	 */
	public List<Category> getCategories()
	{
		return Collections.unmodifiableList(this.categories);
	}

	/**
	 * @param categoryId Identyfikator kategorii.
	 * @return Kategoria o określonym identyfikatorze lub null, jeśli nie istnieje.
	 */
	public Category getCategoryById(int categoryId)
	{
		for (Category category : this.categories)
		{
			if (categoryId == category.getId())
			{
				return category;
			}
		}

		return null;
	}

	/**
	 * @return Niemodyfikowalna lista wydarzeń.
	 */
	public List<Event> getEvents()
	{
		return Collections.unmodifiableList(this.events);
	}

	/**
	 * @param targetDate Data, dla której należy pobrać wydarzenia.
	 * @return Niemodyfikowalna lista wydarzeń dla określonej daty.
	 */
	public List<Event> getEventsByDate(LocalDate targetDate)
	{
		List<Event> matchingEvents = new ArrayList<>();

		for (Event event : this.events)
		{
			if (event.getDate().toLocalDate().isEqual(targetDate))
			{
				matchingEvents.add(event);
			}
		}

		return Collections.unmodifiableList(matchingEvents);
	}

	/**
	 * @param eventId Identyfikator wydarzenia.
	 * @return Wydarzenie o określonym identyfikatorze lub null, jeśli nie istnieje.
	 */
	public Event getEventById(int eventId)
	{
		for (Event event : this.events)
		{
			if (eventId == event.getId())
			{
				return event;
			}
		}

		return null;
	}

	/**
	 * @return Niemodyfikowalna lista kontaktów.
	 */
	public List<Contact> getContacts()
	{
		return Collections.unmodifiableList(this.contacts);
	}

	/**
	 * @param contactId Identyfikator kontaktu.
	 * @return Kontakt o określonym identyfikatorze lub null, jeśli nie istnieje.
	 */
	public Contact getContactById(int contactId)
	{
		for (Contact contact : this.contacts)
		{
			if (contactId == contact.getId())
			{
				return contact;
			}
		}

		return null;
	}

	/**
	 * Sortuje listę wydarzeń alfabetycznie według nazwy.
	 */
	public void sortEventByName()
	{
		Collections.sort(this.events, byName);
	}

	/**
	 * Sortuje listę wydarzeń według daty.
	 */
	public void sortEventByDate()
	{
		Collections.sort(this.events, byDate);
	}

	/**
	 * Sortuje listę wydarzeń alfabetycznie według lokalizacji.
	 */
	public void sortEventByLocation()
	{
		Collections.sort(this.events, byLocation);
	}

	/**
	 * Sortuje listę wydarzeń alfabetycznie według kategorii.
	 */
	public void sortEventByCategory()
	{
		Collections.sort(this.events, byCategory);
	}

	/**
	 * Sortuje listę wydarzeń alfabetycznie według opisu.
	 */
	public void sortEventByDescription()
	{
		Collections.sort(this.events, byDescription);
	}

	/**
	 * Sortuje listę wydarzeń według domyślnego kryterium sortowania.
	 */
	public void sortEventByDefault()
	{
		Collections.sort(this.events);
	}

	/**
	 * Sortuje listę kontaktów alfabetycznie według imienia.
	 */
	public void sortContactByFirstName()
	{
		Collections.sort(this.contacts, byFirstName);
	}

	/**
	 * Sortuje listę kontaktów alfabetycznie według nazwiska.
	 */
	public void sortContactByLastName()
	{
		Collections.sort(this.contacts, byLastName);
	}

	/**
	 * Sortuje listę kontaktów według numeru telefonu.
	 */
	public void sortContactByPhoneNumber()
	{
		Collections.sort(this.contacts, byPhoneNumber);
	}

	/**
	 * Sortuje listę kontaktów według domyślnego kryterium sortowania.
	 */
	public void sortContactByDefault()
	{
		Collections.sort(this.contacts);
	}

	/**
	 * Sortuje listę kategorii według domyślnego kryterium sortowania.
	 */
	public void sortCategoryByDefault()
	{
		Collections.sort(this.categories);
	}

	/**
	 * @return Tablica {@code String} z dostępnymi kategoriami sortowania wydarzeń.
	 */
	public String[] getEventsSortBy()
	{
		return this.eventsSortBy;
	}

	/**
	 * @return Tablica {@code String} z dostępnymi kategoriami sortowania kontaktów.
	 */
	public String[] getContactSortBy()
	{
		return this.contactSortBy;
	}

	/**
	 * Sortuje listę wydarzeń według określonego kryterium sortowania.
	 * 
	 * @param sortBy Kryterium sortowania, dostępne opcje: "Name", "Date",
	 *               "Location", "Category" lub dowolne inne, co spowoduje domyślne
	 *               sortowanie.
	 */
	public void sortEvents(String sortBy)
	{
		switch (sortBy)
		{
			case "Name":
			{
				this.sortEventByName();
				break;
			}
			case "Date":
			{
				this.sortEventByDate();
				break;
			}
			case "Location":
			{
				this.sortEventByLocation();
				break;
			}
			case "Category":
			{
				this.sortEventByCategory();
				break;
			}
			default:
				this.sortEventByDefault();
		}
	}

	/**
	 * Sortuje listę kontaktów według określonego kryterium sortowania.
	 * 
	 * @param sortBy Kryterium sortowania, dostępne opcje: "First name", "Last
	 *               name", "Phone number" lub dowolne inne, co spowoduje domyślne
	 *               sortowanie.
	 */
	public void sortContacts(String sortBy)
	{
		switch (sortBy)
		{
			case "First name":
			{
				this.sortContactByFirstName();
				break;
			}
			case "Last name":
			{
				this.sortContactByLastName();
				break;
			}
			case "Phone number":
			{
				this.sortContactByPhoneNumber();
				break;
			}
			default:
				this.sortContactByDefault();
		}
	}

	/**
	 * Zapisuje aktualny stan list kategorii, wydarzeń i kontaktów do pliku XML.
	 * Wykorzystuje {@link serializer.XMLSerializer} do dokonania serializacji.
	 */
	public void saveToXML()
	{
		this.xmlSerializer.encode(this.categories, this.events, this.contacts);
	}

	/**
	 * Pobiera dane z domyślnego pliku XML do obiektu
	 * {@link serializer.XMLDataWrapper}. Wykorzystuje
	 * {@link serializer.XMLDeserializer} do dokonania deserializacji.
	 * 
	 * @return Obiekt {@link serializer.XMLDataWrapper} zawierający listy kategorii,
	 *         wydarzeń i kontaktów.
	 */
	public XMLDataWrapper loadFromXML()
	{
		return this.xmlDeserializer.decode();
	}

	/**
	 * Pobiera dane z określonego pliku XML do obiektu
	 * {@link serializer.XMLDataWrapper}. Wykorzystuje
	 * {@link serializer.XMLDeserializer} do dokonania deserializacji.
	 * 
	 * @param filePath Ścieżka do pliku XML.
	 * @return Obiekt {@link serializer.XMLDataWrapper} zawierający listy kategorii,
	 *         wydarzeń i kontaktów.
	 */
	public XMLDataWrapper loadFromXML(String filePath)
	{
		return this.xmlDeserializer.decode(filePath);
	}

	/**
	 * Wczytuje dane z pliku XML i aktualizuje listy kategorii, wydarzeń i kontaktów
	 * w tym kontrolerze na podstawie odczytanych informacji. Po wczytaniu pobranych
	 * danych przez listy w kontrolerze będą one połączone z listami z
	 * {@link serializer.XMLDataWrapper} poprzez referencję. Wykorzystuje
	 * {@link #loadFromXML()} do deserializacji danych.
	 * 
	 * @see #loadFromXML()
	 */
	private void loadDataFromXML()
	{
		this.xmlData = this.loadFromXML();

		if (this.xmlData != null)
		{
			this.categories = this.xmlData.getCategories();
			this.events = this.xmlData.getEvents();
			this.contacts = this.xmlData.getContacts();
		}
	}

	/**
	 * Konwertuje obiekt klasy {@code Color} do reprezentacji szesnastkowej jako
	 * {@code String} w formacie HEX.
	 * 
	 * @param color Obiekt klasy {@code Color}, którego kolor ma zostać
	 *              skonwertowany.
	 * @return {@code String} reprezentujący kolor w formacie HEX.
	 */
	public String convertColorToHex(Color color)
	{
		int r = (int) (color.getRed() * 255);
		int g = (int) (color.getGreen() * 255);
		int b = (int) (color.getBlue() * 255);

		return String.format("#%02X%02X%02X", r, g, b);
	}

	/**
	 * Sprawdza, czy istnieje kategoria o podanej nazwie w liście
	 * {@code List<Category> categories}
	 * 
	 * @param name Nazwa kategorii do sprawdzenia.
	 * @return {@code true}, jeśli kategoria istnieje, w przeciwnym razie
	 *         {@code false}.
	 */
	public boolean isCategoryExists(String name)
	{
		for (Category category : this.categories)
		{
			if (category.getName().equals(name))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Sprawdza, czy istnieje kategoria o podanej nazwie w liście
	 * {@code List<Category> categories}, pomijając kategorię o określonym
	 * identyfikatorze.
	 * 
	 * @param categoryId Identyfikator kategorii, który ma zostać pominięty.
	 * @param inputName  Nazwa kategorii do sprawdzenia.
	 * @return {@code true}, jeśli kategoria o danej nazwie istnieje (pomijając
	 *         kategorię o podanym identyfikatorze), w przeciwnym razie
	 *         {@code false}.
	 */
	public boolean isCategoryExists(int categoryId, String inputName)
	{
		for (Category category : this.categories)
		{
			if (category.getName().equals(inputName) && category.getId() != categoryId)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Sprawdza, czy w danym terminie w liście {@code List<Event> events} jest już
	 * zaplanowane wydarzenie.
	 * 
	 * @param scheduledDate Data i czas, który ma zostać sprawdzony.
	 * @return {@code true}, jeśli w danym terminie istnieje już zaplanowane
	 *         wydarzenie, w przeciwnym razie {@code false}.
	 */
	public boolean isDateTimeOccupied(LocalDateTime scheduledDate)
	{
		for (Event event : this.events)
		{
			if (event.getDate().equals(scheduledDate))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Sprawdza, czy w danym terminie w liście {@code List<Event> events} jest już
	 * zaplanowane wydarzenie, pomijając wydarzenie o określonym identyfikatorze.
	 * 
	 * @param eventId       Identyfikator wydarzenia, który ma zostać pominięty.
	 * @param inputDateTime Data i czas, który ma zostać sprawdzony.
	 * @return {@code true}, jeśli w danym terminie istnieje już zaplanowane
	 *         wydarzenie (pomijając wydarzenie o podanym identyfikatorze), w
	 *         przeciwnym razie {@code false}.
	 */
	public boolean isDateTimeOccupied(int eventId, LocalDateTime inputDateTime)
	{
		for (Event event : this.events)
		{
			if (event.getDate().equals(inputDateTime) && event.getId() != eventId)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Sprawdza, czy podany ciąg znaków reprezentuje poprawny format czasu w
	 * formacie "HH:mm". Godzina może być reprezentowana przez jedną cyfrę (0-9),
	 * dwie cyfry (00-09 lub 10-23). Minuta zawsze jest reprezentowana przez dwie
	 * cyfry (00-59).
	 * 
	 * @param time Ciąg znaków do sprawdzenia.
	 * @return {@code true}, jeśli ciąg znaków reprezentuje poprawny format czasu, w
	 *         przeciwnym razie {@code false}.
	 */
	public boolean isTimeValid(String time)
	{
		String timeRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
		Pattern pattern = Pattern.compile(timeRegex);
		Matcher matcher = pattern.matcher(time);
		return matcher.matches();
	}

	/**
	 * Sprawdza, czy istnieje kontakt o podanym numerze telefonu w liście
	 * {@code List<Contact> contacts}.
	 * 
	 * @param phoneNumber Numer telefonu do sprawdzenia.
	 * @return {@code true}, jeśli istnieje kontakt o podanym numerze telefonu, w
	 *         przeciwnym razie {@code false}.
	 */
	public boolean isPhoneNumberExists(String phoneNumber)
	{
		String cleanedPhoneNumber = phoneNumber.replaceAll("\s", "");

		for (Contact contact : this.contacts)
		{
			String storedPhoneNumber = contact.getPhoneNumber().replaceAll("\s", "");

			if (storedPhoneNumber.equals(cleanedPhoneNumber))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Sprawdza, czy istnieje kontakt o podanym numerze telefonu, z wyłączeniem
	 * kontaktu o określonym identyfikatorze w liście
	 * {@code List<Contact> contacts}.
	 * 
	 * @param contactId        Identyfikator kontaktu, który ma zostać pominięty
	 *                         przy sprawdzaniu.
	 * @param inputPhoneNumber Numer telefonu do sprawdzenia.
	 * @return {@code true}, jeśli istnieje kontakt o podanym numerze telefonu (poza
	 *         kontaktem o określonym identyfikatorze), w przeciwnym razie
	 *         {@code false}.
	 */
	public boolean isPhoneNumberExists(int contactId, String inputPhoneNumber)
	{
		String cleanedPhoneNumber = inputPhoneNumber.replaceAll("\s", "");

		for (Contact contact : this.contacts)
		{
			String storedPhoneNumber = contact.getPhoneNumber().replaceAll("\s", "");

			if (storedPhoneNumber.equals(cleanedPhoneNumber) && contact.getId() != contactId)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Sprawdza, czy podany numer telefonu jest poprawny. Numer telefonu jest
	 * uważany za poprawny, jeżeli składa się z dokładnie 9 cyfr i nie zawiera
	 * żadnych innych znaków ani spacji.
	 * 
	 * @param phoneNumber Numer telefonu do sprawdzenia.
	 * @return {@code true}, jeśli numer telefonu jest poprawny, w przeciwnym razie
	 *         {@code false}.
	 */
	public boolean isPhoneNumberValid(String phoneNumber)
	{
		phoneNumber = phoneNumber.replaceAll("\s", "");
		String phoneNumberRegex = "\\d{9}$";
		Pattern pattern = Pattern.compile(phoneNumberRegex);
		Matcher matcher = pattern.matcher(phoneNumber);
		return matcher.matches();
	}

	/**
	 * Łączy podaną datę i godzinę w jedną datę czasową.
	 * 
	 * @param date Data do połączenia.
	 * @param time Godzina do połączenia.
	 * @return Obiekt {@code LocalDateTime} reprezentujący połączoną datę i godzinę.
	 */
	public LocalDateTime mergeDateTime(LocalDate date, LocalTime time)
	{
		LocalDateTime mergedDateTime = LocalDateTime.of(date, time);

		return mergedDateTime;
	}

	/**
	 * Parsuje podany ciąg znaków reprezentujący godzinę w formacie "HH:mm" na
	 * obiekt {@code LocalTime}.
	 * 
	 * @param time Ciąg znaków reprezentujący godzinę w formacie "HH:mm".
	 * @return Obiekt {@code LocalTime} reprezentujący sparsowaną godzinę.
	 */
	public LocalTime parseStringToLocalTime(String time)
	{
		LocalTime parsedLocalTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));

		return parsedLocalTime;
	}

	/**
	 * Dodaje nową kategorię do listy kontrolera {@code List<Category> categories}.
	 * Dodana kategoria zostanie również zsynchronizowana z bazą danych, jeśli
	 * synchronizacja jest włączona.
	 * 
	 * @param name  Nazwa nowej kategorii.
	 * @param color Kolor nowej kategorii w formacie heksadecymalnym.
	 * @throws Exception Jeśli wystąpią problemy podczas dodawania kategorii lub
	 *                   synchronizacji z bazą danych.
	 */
	public void addNewCategory(String name, String color) throws Exception
	{
		Category category = new Category();
		category.setName(name);
		category.setColorHex(color);

		this.categories.add(category);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.insertCategory(category);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Dodaje nowe wydarzenie do listy kontrolera {@code List<Event> events}. Dodane
	 * wydarzenie zostanie również zsynchronizowane z bazą danych, jeśli
	 * synchronizacja jest włączona.
	 * 
	 * @param name         Nazwa nowego wydarzenia.
	 * @param date         Data nowego wydarzenia.
	 * @param notifyOffset Przesunięcie czasowe powiadomienia przed wydarzeniem.
	 * @param location     Lokalizacja nowego wydarzenia.
	 * @param category     Kategoria nowego wydarzenia.
	 * @param description  Opis nowego wydarzenia.
	 * @param contacts     Lista kontaktów powiązanych z nowym wydarzeniem.
	 * @throws Exception Jeśli wystąpią problemy podczas dodawania wydarzenia lub
	 *                   synchronizacji z bazą danych.
	 */
	public void addNewEvent(String name, LocalDateTime date, LocalTime notifyOffset, String location, Category category, String description,
			List<Contact> contacts) throws Exception
	{
		Event event = new Event();
		event.setName(name);
		event.setDate(date);
		event.setNotifyOffset(notifyOffset);
		event.setLocation(location);
		event.setCategory(category);
		event.setDescription(description);
		event.setContacts(contacts);

		this.events.add(event);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.insertEvent(event);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Dodaje nowy kontakt do listy kontrolera {@code List<Contact> contacts}.
	 * Dodany kontakt zostanie również zsynchronizowany z bazą danych, jeśli
	 * synchronizacja jest włączona.
	 * 
	 * @param firstName   Imię nowego kontaktu.
	 * @param lastName    Nazwisko nowego kontaktu.
	 * @param phoneNumber Numer telefonu nowego kontaktu.
	 * @param events      Lista wydarzeń powiązanych z nowym kontaktem.
	 * @throws Exception Jeśli wystąpią problemy podczas dodawania kontaktu lub
	 *                   synchronizacji z bazą danych.
	 */
	public void addNewContact(String firstName, String lastName, String phoneNumber, List<Event> events) throws Exception
	{
		phoneNumber = phoneNumber.replaceAll("\s", "");
		String formattedPhoneNumber = String.format("%s %s %s", phoneNumber.substring(0, 3), phoneNumber.substring(3, 6), phoneNumber.substring(6));

		Contact contact = new Contact();
		contact.setFirstName(firstName);
		contact.setLastName(lastName);
		contact.setPhoneNumber(formattedPhoneNumber);
		contact.setEvents(events);

		this.contacts.add(contact);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.insertContact(contact);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Aktualizuje istniejącą kategorię w liście {@code List<Category> categories} w
	 * kontrolerze z nową nazwą i kolorem. Aktualizacja kategorii zostanie również
	 * zsynchronizowana z bazą danych, jeśli synchronizacja jest włączona.
	 * 
	 * @param category Kategoria do zaktualizowania.
	 * @param name     Nowa nazwa kategorii.
	 * @param colorHex Nowy kolor kategorii w formacie heksadecymalnym.
	 * @throws Exception Jeśli wystąpią problemy podczas aktualizacji kategorii lub
	 *                   synchronizacji z bazą danych.
	 */
	public void updateCategory(Category category, String name, String colorHex) throws Exception
	{
		category.setName(name);
		category.setColorHex(colorHex);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.updateCategory(category);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Aktualizuje istniejące wydarzenie w liście {@code List<Event> events} w
	 * kontrolerze z nowymi danymi. Aktualizacja wydarzenia zostanie również
	 * zsynchronizowana z bazą danych, jeśli synchronizacja jest włączona.
	 * 
	 * @param event         Wydarzenie do zaktualizowania.
	 * @param name          Nowa nazwa wydarzenia.
	 * @param date          Nowa data wydarzenia.
	 * @param notifyOffset  Nowe przesunięcie czasowe powiadomienia przed
	 *                      wydarzeniem.
	 * @param location      Nowa lokalizacja wydarzenia.
	 * @param category      Nowa kategoria wydarzenia.
	 * @param description   Nowy opis wydarzenia.
	 * @param eventContacts Nowa lista kontaktów powiązanych z wydarzeniem.
	 * @throws Exception Jeśli wystąpią problemy podczas aktualizacji wydarzenia lub
	 *                   synchronizacji z bazą danych.
	 */
	public void updateEvent(Event event, String name, LocalDateTime date, LocalTime notifyOffset, String location, Category category,
			String description, List<Contact> eventContacts) throws Exception
	{
		event.setName(name);
		event.setDate(date);
		event.setNotifyOffset(notifyOffset);
		event.setLocation(location);
		event.setCategory(category);
		event.setDescription(description);
		updateEventContacts(event, eventContacts);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.updateEvent(event);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Aktualizuje listę kontaktów w wydarzeniu oraz liste wydarzeń w kontaktach
	 * 
	 * @param event           Wydarzenie do zaktualizowania
	 * @param updatedContacts Nowa lista kontaktów powiązanych z wydarzeniem
	 */
	private void updateEventContacts(Event event, List<Contact> updatedContacts)
	{
		List<Contact> currentContacts = new ArrayList<Contact>(event.getContacts());

		// Usuń kontakty, które nie są już w aktualizowanej liście
		for (Contact contact : currentContacts)
		{
			if (!updatedContacts.contains(contact))
			{
				event.removeContact(contact);
			}
		}

		// Dodaj nowe kontakty, które nie są jeszcze w aktualnej liście
		for (Contact contact : updatedContacts)
		{
			if (!currentContacts.contains(contact))
			{
				event.addContact(contact);
			}
		}
	}

	/**
	 * Aktualizuje istniejący kontakt w liście {@code List<Contact> contacts} w
	 * kontrolerze z nowymi danymi. Aktualizacja kontaktu zostanie również
	 * zsynchronizowana z bazą danych, jeśli synchronizacja jest włączona.
	 * 
	 * @param contact       Kontakt do zaktualizowania.
	 * @param firstName     Nowe imię kontaktu.
	 * @param lastName      Nowe nazwisko kontaktu.
	 * @param phoneNumber   Nowy numer telefonu kontaktu.
	 * @param contactEvents Nowa lista wydarzeń powiązanych z kontaktem.
	 * @throws Exception Jeśli wystąpią problemy podczas aktualizacji kontaktu lub
	 *                   synchronizacji z bazą danych.
	 */
	public void updateContact(Contact contact, String firstName, String lastName, String phoneNumber, List<Event> contactEvents) throws Exception
	{
		contact.setFirstName(firstName);
		contact.setLastName(lastName);
		contact.setPhoneNumber(phoneNumber);
		updateContactEvents(contact, contactEvents);

		contact.setPhoneNumber(String.format("%s %s %s", contact.getPhoneNumber().substring(0, 3), contact.getPhoneNumber().substring(3, 6),
				contact.getPhoneNumber().substring(6)));

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.updateContact(contact);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Aktualizuje listę wydarzeń w kontakcie oraz listę kontaktów w wydarzeniu
	 * 
	 * @param contact       Kontakt do zaktualizowania.
	 * @param updatedEvents Nowa lista wydarzeń powiązanych z kontaktem.
	 */
	private void updateContactEvents(Contact contact, List<Event> updatedEvents)
	{
		List<Event> currentEvents = new ArrayList<Event>(contact.getEvents());

		for (Event event : currentEvents)
		{
			if (!updatedEvents.contains(event))
			{
				contact.removeEvent(event);
			}
		}

		for (Event event : updatedEvents)
		{
			if (!currentEvents.contains(event))
			{
				contact.addEvent(event);
			}
		}
	}

	/**
	 * Usuwa kategorię w liście {@code List<Category> categories} w kontrolerze. W
	 * przypadku usunięcia kategorii, wszystkie powiązane z nią wydarzenia, które
	 * nie mają innej przypisanej kategorii, zostaną odłączone od kategorii.
	 * Usunięcie kategorii zostanie również zsynchronizowane z bazą danych, jeśli
	 * synchronizacja jest włączona.
	 * 
	 * @param category Kategoria do usunięcia.
	 * @throws Exception Jeśli wystąpią problemy podczas usuwania kategorii lub
	 *                   synchronizacji z bazą danych.
	 */
	public void deleteCategory(Category category) throws Exception
	{
		for (Event e : events)
		{
			if (e.getCategory() != null && e.getCategory().equals(category))
			{
				e.setCategory(null);
			}
		}

		this.categories.remove(category);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.deleteCategory(category);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Usuwa wydarzenie z listy wydarzeń {@code List<Event> events} w kontrolerze. W
	 * przypadku usunięcia wydarzenia, zostaje odłączone od wszystkich powiązanych
	 * kontaktów. Usunięcie wydarzenia zostanie również zsynchronizowane z bazą
	 * danych, jeśli synchronizacja jest włączona.
	 * 
	 * @param event Wydarzenie do usunięcia.
	 * @throws Exception Jeśli wystąpią problemy podczas usuwania wydarzenia lub
	 *                   synchronizacji z bazą danych.
	 */
	public void deleteEvent(Event event) throws Exception
	{
		for (Contact c : contacts)
		{
			c.removeEvent(event);
		}

		this.events.remove(event);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.deleteEvent(event);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Usuwa wszystkie przeszłe wydarzenia od podanej daty, z listy wydarzeń
	 * {@code List<Event> events} w kontrolerze. Wszystkie przeszłe wydarzenia
	 * zostaną również odłączone od powiązanych kontaktów. Usunięcie przeszłych
	 * wydarzeń zostanie zsynchronizowane z bazą danych, jeśli synchronizacja jest
	 * włączona.
	 * 
	 * @param targetDate Data, przed którą należy usunąć wydarzenia.
	 * @throws Exception Jeśli wystąpią problemy podczas usuwania wydarzeń lub
	 *                   synchronizacji z bazą danych.
	 */
	public void deleteOldEvents(LocalDate targetDate) throws Exception
	{
		LocalDateTime targetDateTime = targetDate.atStartOfDay();

		List<Event> eventsToRemove = new ArrayList<Event>();

		for (Event event : this.events)
		{
			if (event.getDate().isBefore(targetDateTime))
			{
				eventsToRemove.add(event);

				for (Contact contact : this.contacts)
				{
					contact.removeEvent(event);
				}
			}
		}

		this.events.removeAll(eventsToRemove);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.deleteOldEvents(targetDateTime);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}

	/**
	 * Usuwa kontakt z listy kontaktów {@code List<Contact> contacts} w kontrolerze.
	 * Kontakt zostanie również odłączony od wszystkich powiązanych wydarzeń.
	 * Usunięcie kontaktu zostanie zsynchronizowane z bazą danych, jeśli
	 * synchronizacja jest włączona.
	 * 
	 * @param contact Kontakt do usunięcia.
	 * @throws Exception Jeśli wystąpią problemy podczas usuwania kontaktu lub
	 *                   synchronizacji z bazą danych.
	 */
	public void deleteContact(Contact contact) throws Exception
	{
		for (Event e : events)
		{
			e.removeContact(contact);
		}

		this.contacts.remove(contact);

		if (!this.databaseSynchronized)
		{
			return;
		}

		try
		{
			this.database.deleteContact(contact);
		}
		catch (SQLException ex)
		{
			throw ex;
		}
	}
}
