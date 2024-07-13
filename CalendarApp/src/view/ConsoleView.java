package view;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import application.Controller;
import model.Category;
import model.Contact;
import model.Event;

/**
 * Klasa reprezentująca widok konsolowy aplikacji do zarządzania wydarzeniami,
 * kontaktami i kategoriami. Umożliwia interakcję z użytkownikiem poprzez
 * konsolę tekstową, pozwalając na dodawanie, przeglądanie oraz usuwanie
 * wydarzeń, kontaktów i kategorii.
 */
public class ConsoleView
{
	private Controller controller;
	private List<Category> categoryList;
	private List<Contact> contactList;
	private List<Event> eventList;

	private Scanner scanner;

	/**
	 * Inicjalizuje widok konsolowy. Tworzy obiekt kontrolera, wczytuje dane, tworzy
	 * listy kategorii, kontaktów i wydarzeń oraz inicjalizuje skaner.
	 */
	public void init()
	{
		try
		{
			this.controller = new Controller();
			this.controller.init();
		}
		catch (Exception e)
		{
			System.out.println("Failed to synchronize with the database.");
			System.out.println("Your data will be saved locally, and it will be synchronized with the database upon the next connection.\n");
		}

		this.categoryList = this.controller.getCategories();
		this.contactList = this.controller.getContacts();
		this.eventList = this.controller.getEvents();

		this.scanner = new Scanner(System.in);

		refreshConsoleView();
	}

	/**
	 * Główna metoda obsługująca interakcję z użytkownikiem. W pętli while oczekuje
	 * na wybór opcji od użytkownika i wykonuje odpowiednie akcje.
	 */
	private void refreshConsoleView()
	{
		int option;

		while (true)
		{
			System.out.println("Choose option:");
			System.out.println("1. Show events.");
			System.out.println("2. Show events detailed.");
			System.out.println("3. Add new event.");
			System.out.println("4. Delete event.");
			System.out.println("5. Show contacts.");
			System.out.println("6. Show contacts detailed.");
			System.out.println("7. Add new contact.");
			System.out.println("8. Delete contact.");
			System.out.println("9. Show categories.");
			System.out.println("10. Add new category.");
			System.out.println("11. Delete category.");
			System.out.println("12. Exit\n");

			System.out.print("Option: ");

			while (!this.scanner.hasNextInt())
			{
				System.err.println("Incorrect option. Please enter a correct option.");
				System.out.print("Option: ");
				this.scanner.next();
			}

			option = this.scanner.nextInt();
			this.scanner.nextLine();

			switch (option)
			{
				case 1:
					showEvents();
					break;
				case 2:
					showEventsDetailed();
					break;
				case 3:
					addEvent();
					break;
				case 4:
					deleteEvent();
					break;
				case 5:
					showContacts();
					break;
				case 6:
					showContactsDetailed();
					break;
				case 7:
					addContact();
					break;
				case 8:
					deleteContact();
					break;
				case 9:
					showCategories();
					break;
				case 10:
					addCategory();
					break;
				case 11:
					deleteCategory();
					break;
				case 12:
					this.controller.saveToXML();
					this.scanner.close();
					System.exit(0);
					break;
				default:
					System.out.println("Incorrect option. Try again.");
					break;
			}
		}
	}

	/**
	 * Metoda wyświetlająca informacje o wydarzeniach w konsoli.
	 */
	private void showEvents()
	{
		for (Event e : this.eventList)
		{
			System.out.println(e.getId() + " " + e);
			System.out.println();
		}
	}

	/**
	 * Metoda wyświetlająca szczegółowe informacje o wydarzeniach w konsoli.
	 */
	private void showEventsDetailed()
	{
		for (Event e : this.eventList)
		{
			System.out.println(e);

			for (Contact c : e.getContacts())
			{
				System.out.println(c);
			}

			System.out.println();
		}
	}

	/**
	 * Metoda dodająca nowe wydarzenie na podstawie danych wprowadzonych przez
	 * użytkownika. Obsługuje błędy wprowadzanych danych i komunikuje je w konsoli.
	 */
	private void addEvent()
	{
		boolean addNotCompleted;

		do
		{
			String eventName = readInput("Enter event name: ", false);
			LocalDateTime eventDateTime = readDateTimeInput();
			LocalTime eventNotifyOffset = readTimeInput();
			String eventLocation = readInput("Enter event location: ", true);
			String eventDescription = readInput("Enter event description: ", true);
			Category category = selectCategory();
			List<Contact> selectedContactsList = selectContacts();

			try
			{
				this.controller.addNewEvent(eventName, eventDateTime, eventNotifyOffset, eventLocation, category, eventDescription,
						selectedContactsList);
				System.out.println("Event added successfully!\n");
			}
			catch (Exception ex)
			{
				System.err.println("Error: " + ex.getMessage());
				System.err.println("Please try again.\n");
			}

			addNotCompleted = false;
		}
		while (addNotCompleted);
	}

	/**
	 * Metoda usuwająca wydarzenie na podstawie numeru wprowadzonego przez
	 * użytkownika. Wyświetla dostępne wydarzenia przed usunięciem i obsługuje
	 * błędne dane wejściowe.
	 */
	private void deleteEvent()
	{
		System.out.println("Select event you want to delete by number: ");
		showEvents();

		boolean deleteNotCompleted = true;

		do
		{
			System.out.print("Select event: ");
			String input = this.scanner.nextLine();

			if (input.isBlank() || !input.matches("\\d+"))
			{
				System.err.println("Incorrect input! Please enter a valid number.");
				continue;
			}

			int eventId = Integer.parseInt(input);

			Event eventToDelete = this.controller.getEventById(eventId);

			if (eventToDelete == null)
			{
				System.err.println("Event not found! Please enter a valid number.");
				continue;
			}

			try
			{
				this.controller.deleteEvent(eventToDelete);
				System.out.println("Event deleted successfully!\n");
			}
			catch (Exception ex)
			{
				System.err.println("Error: " + ex.getMessage());
				System.err.println("Please try again.\n");
				continue;
			}

			deleteNotCompleted = false;
		}
		while (deleteNotCompleted);
	}

	/**
	 * Metoda wyświetlająca informacje o kontaktach w konsoli.
	 */
	private void showContacts()
	{
		for (Contact c : this.contactList)
		{
			System.out.println(c.getId() + " " + c);
			System.out.println();
		}
	}

	/**
	 * Metoda wyświetlająca szczegółowe informacje o kontaktach w konsoli.
	 */
	private void showContactsDetailed()
	{
		for (Contact c : this.contactList)
		{
			System.out.println(c);

			for (Event e : c.getEvents())
			{
				System.out.println(e);
			}

			System.out.println();
		}
	}

	/**
	 * Metoda dodająca nowy kontakt na podstawie danych wprowadzonych przez
	 * użytkownika. Obsługuje błędy wprowadzanych danych i komunikuje je w konsoli.
	 */
	private void addContact()
	{
		boolean addNotCompleted;

		do
		{
			String contactFirstName = readInput("Enter first name: ", false);
			String contactLastName = readInput("Enter last name: ", false);
			String contactPhoneNumber = readPhoneNumberInput();
			List<Event> selectedEventList = selectEvents();

			try
			{
				this.controller.addNewContact(contactFirstName, contactLastName, contactPhoneNumber, selectedEventList);
				System.out.println("Contact added successfully!\n");
			}
			catch (Exception ex)
			{
				System.err.println("Error: " + ex.getMessage());
				System.err.println("Please try again.\n");
			}

			addNotCompleted = false;
		}
		while (addNotCompleted);
	}

	/**
	 * Metoda usuwająca kontakt na podstawie numeru wprowadzonego przez użytkownika.
	 * Wyświetla dostępne kontakty przed usunięciem i obsługuje błędne dane
	 * wejściowe.
	 */
	private void deleteContact()
	{
		System.out.println("Select contact you want to delete by number: ");
		showContacts();

		boolean deleteNotCompleted = true;

		do
		{
			System.out.print("Select contact: ");
			String input = this.scanner.nextLine();

			if (input.isBlank() || !input.matches("\\d+"))
			{
				System.err.println("Incorrect input! Please enter a valid number.");
				continue;
			}

			int contactId = Integer.parseInt(input);

			Contact contactToDelete = this.controller.getContactById(contactId);

			if (contactToDelete == null)
			{
				System.err.println("Contact not found! Please enter a valid number.");
				continue;
			}

			try
			{
				this.controller.deleteContact(contactToDelete);
				System.out.println("Contact deleted successfully!\n");
			}
			catch (Exception ex)
			{
				System.err.println("Error: " + ex.getMessage());
				System.err.println("Please try again.\n");
				continue;
			}

			deleteNotCompleted = false;
		}
		while (deleteNotCompleted);
	}

	/**
	 * Metoda wyświetlająca informacje o kategoriach w konsoli.
	 */
	private void showCategories()
	{
		for (Category c : this.categoryList)
		{
			System.out.println(c.getId() + " " + c);
			System.out.println();
		}
	}

	/**
	 * Metoda dodająca nową kategorię na podstawie nazwy wprowadzonej przez
	 * użytkownika. Obsługuje błędy wprowadzanych danych i komunikuje je w konsoli.
	 */
	private void addCategory()
	{
		boolean addNotCompleted;

		do
		{
			String name = readInput("Enter category name: ", false);

			try
			{
				this.controller.addNewCategory(name, null);
				System.out.println("Category added successfully!\n");
			}
			catch (Exception ex)
			{
				System.err.println("Error: " + ex.getMessage());
				System.err.println("Please try again.\n");
			}

			addNotCompleted = false;
		}
		while (addNotCompleted);
	}

	/**
	 * Metoda usuwająca kategorię na podstawie numeru wprowadzonego przez
	 * użytkownika. Wyświetla dostępne kategorie przed usunięciem i obsługuje błędne
	 * dane wejściowe.
	 */
	private void deleteCategory()
	{
		System.out.println("Select category you want to delete by number: ");
		showCategories();

		boolean deleteNotCompleted = true;

		do
		{
			System.out.print("Select category: ");
			String input = this.scanner.nextLine();

			if (input.isBlank() || !input.matches("\\d+"))
			{
				System.err.println("Incorrect input! Please enter a valid number.");
				continue;
			}

			int categoryId = Integer.parseInt(input);

			Category categoryToDelete = this.controller.getCategoryById(categoryId);

			if (categoryToDelete == null)
			{
				System.err.println("Category not found! Please enter a valid number.");
				continue;
			}

			try
			{
				this.controller.deleteCategory(categoryToDelete);
				System.out.println("Category deleted successfully!\n");
			}
			catch (Exception ex)
			{
				System.err.println("Error: " + ex.getMessage());
				System.err.println("Please try again.\n");
				continue;
			}

			deleteNotCompleted = false;
		}
		while (deleteNotCompleted);
	}

	/**
	 * Metoda odczytująca dane od użytkownika, sprawdzająca, czy są poprawne i
	 * zwracająca wprowadzone dane.
	 *
	 * @param  prompt     Komunikat do wprowadzenia danych.
	 * @param  allowEmpty Określa, czy puste dane są akceptowalne.
	 * @return            Wprowadzone dane od użytkownika.
	 */
	private String readInput(String prompt, boolean allowEmpty)
	{
		String input = null;

		do
		{
			System.out.print(prompt);
			input = this.scanner.nextLine().trim();

			if (!allowEmpty && input.isBlank())
			{
				System.err.println("Input cannot be empty. Please try again.");
			}
		}
		while (!allowEmpty && input.isBlank());

		return input;
	}

	/**
	 * Metoda odczytująca od użytkownika datę i czas wydarzenia w określonym
	 * formacie {@code dd.MM.yyyy HH:mm}.
	 *
	 * @return LocalDateTime reprezentujące datę i czas wydarzenia.
	 */
	private LocalDateTime readDateTimeInput()
	{
		boolean correctDateTime = false;
		LocalDateTime dateTime = null;

		do
		{
			System.out.print("Enter event date and time (dd.MM.yyyy HH:mm): ");
			String input = this.scanner.nextLine();

			try
			{
				dateTime = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

				if (this.controller.isDateTimeOccupied(dateTime))
				{
					System.err.println("The chosen date and time is already occupied. Please choose another one.");
				}
				else
				{
					correctDateTime = true;
				}
			}
			catch (DateTimeParseException e)
			{
				System.err.println("Invalid date and time format. Please use dd.MM.yyyy HH:mm format. Try again.");
			}
		}
		while (!correctDateTime);

		return dateTime;
	}

	/**
	 * Metoda odczytująca od użytkownika jaka ma być różnica czasu do powiadomienia
	 * o wydarzeniu w określonym formacie {@code HH:mm}.
	 *
	 * @return LocalTime reprezentujące czas powiadomienia.
	 */
	private LocalTime readTimeInput()
	{
		boolean correctTime = false;
		LocalTime time = null;

		do
		{
			System.out.print("Enter event notification offset time (HH:mm): ");
			String input = this.scanner.nextLine();

			if (input.isBlank())
			{
				input = "00:00";
			}

			try
			{
				time = this.controller.parseStringToLocalTime(input);
				correctTime = true;
			}
			catch (DateTimeParseException e)
			{
				System.err.println("Invalid time format. Please use HH:mm format. Try again.");
			}
		}
		while (!correctTime);

		return time;
	}

	/**
	 * Metoda odczytująca od użytkownika numer telefonu, sprawdzająca jego
	 * poprawność i unikalność oraz zwracająca wprowadzony numer telefonu.
	 *
	 * @return Wprowadzony numer telefonu od użytkownika.
	 */
	private String readPhoneNumberInput()
	{
		String phoneNumber = null;

		do
		{
			phoneNumber = readInput("Enter phone number: ", false);

			if (!this.controller.isPhoneNumberValid(phoneNumber))
			{
				System.err.println("Invalid phone number format! Please enter a valid number.");
				phoneNumber = null;
			}
			else if (this.controller.isPhoneNumberExists(phoneNumber))
			{
				System.err.println("This phone number is already in use! Please enter a different one.");
				phoneNumber = null;
			}
		}
		while (phoneNumber == null);

		return phoneNumber;
	}

	/**
	 * Metoda pozwalająca użytkownikowi wybrać kategorię z listy dostępnych
	 * kategorii.
	 *
	 * @return Wybrana kategoria lub null, jeśli użytkownik wybierze opcję "skip".
	 */
	private Category selectCategory()
	{
		Category selectedCategory = null;

		System.out.println("Select the category you want to assign to the event by number: ");
		showCategories();
		System.out.println("Type \"skip\" if you do not want to assign category.");

		do
		{
			System.out.println("Select category: ");
			String input = this.scanner.nextLine();

			if (input.equals("skip"))
			{
				return null;
			}

			if (input.isBlank() || !input.matches("\\d+"))
			{
				System.err.println("Incorrect input! Please enter a valid number.");
				continue;
			}

			int categoryId = Integer.parseInt(input);
			selectedCategory = this.controller.getCategoryById(categoryId);

			if (selectedCategory == null)
			{
				System.err.println("Incorrect input! Please enter a valid number.");
			}
		}
		while (selectedCategory == null);

		return selectedCategory;
	}

	/**
	 * Metoda pozwalająca użytkownikowi wybrać wydarzenia z listy dostępnych
	 * wydarzeń.
	 *
	 * @return Lista wybranych wydarzeń lub pusta lista, jeśli użytkownik wybierze
	 *         opcję "skip".
	 */
	private List<Event> selectEvents()
	{
		List<Event> selectedEventsList = new ArrayList<>();

		System.out.println("Select events you want to assign to the contact by number:");
		showEvents();
		System.out.println("Type \"skip\" if you do not want to assign any event.");

		boolean selectionEnded = false;
		do
		{
			System.out.print("Select event: ");
			String input = this.scanner.nextLine();

			if (input.equals("skip"))
			{
				selectionEnded = true;
				continue;
			}

			if (input.isBlank() || !input.matches("\\d+"))
			{
				System.err.println("Incorrect input! Please enter a valid number.");
				continue;
			}

			int eventId = Integer.parseInt(input);
			Event selectedEvent = this.controller.getEventById(eventId);

			if (selectedEvent != null && !selectedEventsList.contains(selectedEvent))
			{
				selectedEventsList.add(selectedEvent);
			}
			else
			{
				System.err.println("Incorrect input! Please enter a valid number.");
			}
		}
		while (!selectionEnded);

		return selectedEventsList;
	}

	/**
	 * Metoda pozwalająca użytkownikowi wybrać kontakty z listy dostępnych
	 * kontaktów.
	 *
	 * @return Lista wybranych kontaktów lub pusta lista, jeśli użytkownik wybierze
	 *         opcję "skip".
	 */
	private List<Contact> selectContacts()
	{
		List<Contact> selectedContactsList = new ArrayList<>();

		System.out.println("Select contacts you want to assign to the event by number:");
		showContacts();
		System.out.println("Type \"skip\" if you do not want to assign any contact.");

		boolean selectionEnded = false;
		do
		{
			System.out.print("Select contact: ");
			String input = this.scanner.nextLine();

			if (input.equals("skip"))
			{
				selectionEnded = true;
				continue;
			}

			if (input.isBlank() || !input.matches("\\d+"))
			{
				System.err.println("Incorrect input! Please enter a valid number.");
				continue;
			}

			int contactId = Integer.parseInt(input);
			Contact selectedContact = this.controller.getContactById(contactId);

			if (selectedContact != null && !selectedContactsList.contains(selectedContact))
			{
				selectedContactsList.add(selectedContact);
			}
			else
			{
				System.err.println("Incorrect input! Please enter a valid number.");
			}
		}
		while (!selectionEnded);

		return selectedContactsList;
	}
}
