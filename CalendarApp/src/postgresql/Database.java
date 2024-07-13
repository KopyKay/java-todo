package postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import model.Category;
import model.Contact;
import model.Event;

/**
 * Klasa reprezentująca bazę danych postgresql do zarządzania kategoriami,
 * wydarzeniami i kontaktami.
 */
public class Database
{
	private final String url = "jdbc:postgresql://localhost:5432/calendar";
	private final String user = "postgres";
	private final String password = "root";

	/**
	 * Pobiera kategorię z bazy danych na podstawie jej identyfikatora.
	 *
	 * @param  categoryId   Identyfikator kategorii.
	 * @param  connection   Połączenie z bazą danych.
	 * @return              Zwraca kategorię z konkretnym wyszukiwanym
	 *                      indentyfikatorem.
	 * @throws SQLException Jeśli wystąpi błąd w wykonywanym zapytaniu SQL.
	 */
	private Category getCategoryById(int categoryId, Connection connection) throws SQLException
	{
		String selectCategoryQuery = "SELECT * FROM categories WHERE id = ?";
		Category category = null;

		try (PreparedStatement pstmt = connection.prepareStatement(selectCategoryQuery))
		{
			pstmt.setInt(1, categoryId);
			ResultSet resultSet = pstmt.executeQuery();

			if (resultSet.next())
			{
				category = new Category();
				category.setId(resultSet.getInt("id"));
				category.setName(resultSet.getString("category_name"));
			}

			resultSet.close();
		}

		return category;
	}

	/**
	 * Pobiera kontakty powiązane z określonym wydarzeniem z bazy danych.
	 *
	 * @param  eventId      Identyfikator wydarzenia.
	 * @param  connection   Połączenie z bazą danych.
	 * @return              Lista kontaktów powiązanych z wydarzeniem.
	 * @throws SQLException Jeśli wystąpi błąd w wykonywanym zapytaniu SQL.
	 */
	private List<Contact> getEventContacts(int eventId, Connection connection) throws SQLException
	{
		List<Contact> contacts = new ArrayList<>();

		String selectContactsQuery = "SELECT c.* FROM contacts c JOIN events_contacts ec ON c.id = ec.contact_id WHERE ec.event_id = ? AND is_active = true";

		try (PreparedStatement pstmt = connection.prepareStatement(selectContactsQuery))
		{
			pstmt.setInt(1, eventId);
			ResultSet resultSet = pstmt.executeQuery();

			while (resultSet.next())
			{
				Contact contact = new Contact();
				contact.setId(resultSet.getInt("id"));
				contact.setFirstName(resultSet.getString("first_name"));
				contact.setLastName(resultSet.getString("last_name"));
				contact.setPhoneNumber(resultSet.getString("phone_number"));

				contacts.add(contact);
			}

			resultSet.close();
		}

		return contacts;
	}

	/**
	 * Pobiera wydarzenia powiązane z określonym kontaktem z bazy danych.
	 *
	 * @param  contactId    Identyfikator kontaktu.
	 * @param  connection   Połączenie z bazą danych.
	 * @return              Lista wydarzeń powiązanych z kontaktem.
	 * @throws SQLException Jeśli wystąpi błąd w wykonywanym zapytaniu SQL.
	 */
	private List<Event> getContactEvents(int contactId, Connection connection) throws SQLException
	{
		List<Event> events = new ArrayList<>();

		String selectEventsQuery = "SELECT e.* FROM events e JOIN events_contacts ec ON e.id = ec.event_id WHERE ec.contact_id = ? AND is_active = true";

		try (PreparedStatement pstmt = connection.prepareStatement(selectEventsQuery))
		{
			pstmt.setInt(1, contactId);
			ResultSet resultSet = pstmt.executeQuery();

			while (resultSet.next())
			{
				Event event = new Event();
				event.setId(resultSet.getInt("id"));
				event.setName(resultSet.getString("event_name"));
				event.setDate(resultSet.getTimestamp("event_date").toLocalDateTime());
				event.setLocation(resultSet.getString("event_location"));
				event.setDescription(resultSet.getString("event_description"));

				int categoryId = resultSet.getInt("category_id");
				Category category = getCategoryById(categoryId, connection);
				event.setCategory(category);

				events.add(event);
			}

			resultSet.close();
		}

		return events;
	}

	/**
	 * Sprawdza, czy dane kategorii zostały zmienione w stosunku do aktualnej w
	 * bazie danych.
	 *
	 * @param  updatedCategory Zaktualizowana kategoria.
	 * @param  name            Aktualna nazwa kategorii w bazie danych.
	 * @param  colorHex        Aktualny kolor kategorii w formie szesnastkowej w
	 *                         bazie danych.
	 * @return                 True jeśli, któraś z danych kategorii została
	 *                         zmieniona, w przeciwnym razie false.
	 */
	private boolean isCategoryDataChanged(Category updatedCategory, String name, String colorHex)
	{
		if (!name.equals(updatedCategory.getName()))
			return true;

		if (!colorHex.equals(updatedCategory.getColorHex()))
			return true;

		return false;
	}

	/**
	 * Sprawdza, czy dane wydarzenia zostały zmienione w stosunku do aktualnego w
	 * bazie danych.
	 * 
	 * @param  updatedEvent Zaktualizowane wydarzenie.
	 * @param  name         Aktualna nazwa wydarzenia w bazie danych.
	 * @param  date         Aktualna data wydarzenia w bazie danych.
	 * @param  notifyOffset Aktualny czas powiadomienia w bazie danych.
	 * @param  location     Aktualna lokalizacja wydarzenia w bazie danych.
	 * @param  description  Aktualny opis wydarzenia w bazie danych.
	 * @param  categoryId   Aktualny identyfikator kategorii wydarzenia w bazie
	 *                      danych.
	 * @return              True jeśli, któraś z danych wydarzenia została
	 *                      zmieniona, w przeciwnym razie false.
	 */
	private boolean isEventDataChanged(Event updatedEvent, String name, LocalDateTime date, LocalTime notifyOffset, String location,
			String description, int categoryId)
	{
		if (!name.equals(updatedEvent.getName()))
			return true;

		if (!date.equals(updatedEvent.getDate()))
			return true;

		if (!notifyOffset.equals(updatedEvent.getNotifyOffset()))
			return true;

		if (!location.equals(updatedEvent.getLocation()))
			return true;

		if (!description.equals(updatedEvent.getDescription()))
			return true;

		if (categoryId != (updatedEvent.getCategory() != null ? updatedEvent.getCategory().getId() : 0))
			return true;

		return false;
	}

	/**
	 * Sprawdza, czy dane kontaktu zostały zmienione w stosunku do aktualnego w
	 * bazie danych.
	 *
	 * @param  updatedContact Zaktualizowany kontakt.
	 * @param  firstName      Aktualne imię kontaktu w bazie danych.
	 * @param  lastName       Aktualne nazwisko kontaktu w bazie danych.
	 * @param  phoneNumber    Aktualny numer telefonu kontaktu w bazie danych.
	 * @return                True jeśli, któraś z danych kontaktu została
	 *                        zmieniona, w przeciwnym razie false.
	 */
	private boolean isContactDataChanged(Contact updatedContact, String firstName, String lastName, String phoneNumber)
	{
		if (!firstName.equals(updatedContact.getFirstName()))
			return true;

		if (!lastName.equals(updatedContact.getLastName()))
			return true;

		if (!phoneNumber.equals(updatedContact.getPhoneNumber()))
			return true;

		return false;
	}

	/**
	 * Dodaje nową kategorię do bazy danych, nadając przekazanej kategorii
	 * identyfikator, który jest generowany przez bazę danych podczas dodawania
	 * nowego rekordu.
	 * 
	 * @param  category                 Kategoria do dodania.
	 * @throws SQLException             Jeśli wystąpi błąd dostępu do bazy danych
	 *                                  lub podczas wykonywania zapytania SQL.
	 * @throws IllegalArgumentException Jeśli przekazana kategoria ma już nadany
	 *                                  identyfikator (id różne od 0).
	 */
	public void insertCategory(Category category) throws SQLException
	{
		if (category.getId() != 0)
		{
			throw new IllegalArgumentException("Category: [" + category + "] already exist in database!");
		}

		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String insertCategoryQuery = "INSERT INTO categories (category_name, color_hex) VALUES (?, ?)";

			try (PreparedStatement pstmt = connection.prepareStatement(insertCategoryQuery, Statement.RETURN_GENERATED_KEYS))
			{
				pstmt.setString(1, category.getName());
				pstmt.setString(2, category.getColorHex());

				pstmt.executeUpdate();

				try (ResultSet generatedKeys = pstmt.getGeneratedKeys())
				{
					if (generatedKeys.next())
						category.setId(generatedKeys.getInt(1));
				}
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Dodaje nowe wydarzenie do bazy danych, nadając przekazanemu wydarzeniu
	 * identyfikator, który jest generowany przez bazę danych podczas dodawania
	 * nowego rekordu. Jeżeli wydarzenie zawiera listę kontaktów, sprawdza czy każdy
	 * kontakt istnieje w bazie. Jeżeli kontakt nie istnieje, dodaje go poprzez
	 * {@link #insertRelatedContacts(List, Connection)}. Następnie tworzy relacje
	 * między wydarzeniem a kontaktem w tabeli łączącej events_contacts za pomocą
	 * {@link #insertRelationships(int, int, Connection)}.
	 * 
	 * @param  event                    Wydarzenie do dodania.
	 * @throws SQLException             Jeśli wystąpi błąd dostępu do bazy danych
	 *                                  lub podczas wykonywania zapytania SQL.
	 * @throws IllegalArgumentException Jeśli przekazane wydarzenie ma już nadany
	 *                                  identyfikator (id różne od 0).
	 */
	public void insertEvent(Event event) throws SQLException
	{
		if (event.getId() != 0)
		{
			throw new IllegalArgumentException("Event: [" + event + "] already exists in the database!");
		}

		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String insertEventQuery = "INSERT INTO events (event_name, event_date, notification_offset, event_location, event_description, category_id) VALUES (?, ?, ?, ?, ?, ?)";

			try (PreparedStatement pstmt = connection.prepareStatement(insertEventQuery, Statement.RETURN_GENERATED_KEYS))
			{
				pstmt.setString(1, event.getName());
				pstmt.setTimestamp(2, Timestamp.valueOf(event.getDate()));
				pstmt.setObject(3, event.getNotifyOffset() != null ? event.getNotifyOffset() : LocalTime.of(0, 0));
				pstmt.setString(4, event.getLocation());
				pstmt.setString(5, event.getDescription());
				pstmt.setObject(6, event.getCategory() != null ? event.getCategory().getId() : null);

				pstmt.executeUpdate();

				try (ResultSet generatedKeys = pstmt.getGeneratedKeys())
				{
					if (generatedKeys.next())
					{
						event.setId(generatedKeys.getInt(1));

						if (!event.getContacts().isEmpty() || event.getContacts() != null)
						{
							for (Contact contact : event.getContacts())
							{
								if (contact.getId() == 0)
								{
									insertRelatedContacts(event.getContacts(), connection);
								}

								insertRelationships(event.getId(), contact.getId(), connection);
							}
						}
					}
				}
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Dodaje nowy kontakt do bazy danych, nadając przekazanemu kontaktowi
	 * identyfikator, który jest generowany przez bazę danych podczas dodawania
	 * nowego rekordu. Jeżeli kontakt zawiera listę wydarzeń, sprawdza czy każde
	 * wydarzenie istnieje w bazie. Jeżeli wydarzenie nie istnieje, dodaje je
	 * poprzez {@link #insertRelatedEvents(List, Connection)}. Następnie tworzy
	 * relacje między wydarzeniem a kontaktem w tabeli łączącej events_contacts za
	 * pomocą {@link #insertRelationships(int, int, Connection)}.
	 * 
	 * @param  contact                  Kontakt do dodania.
	 * @throws SQLException             Jeśli wystąpi błąd dostępu do bazy danych
	 *                                  lub podczas wykonywania zapytania SQL.
	 * @throws IllegalArgumentException Jeśli przekazany kontakt ma już nadany
	 *                                  identyfikator (id różne od 0).
	 */
	public void insertContact(Contact contact) throws SQLException
	{
		if (contact.getId() != 0)
		{
			throw new IllegalArgumentException("Contact: [" + contact + "] already exist in database!");
		}

		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String insertContactQuery = "INSERT INTO contacts (first_name, last_name, phone_number) VALUES (?, ?, ?)";

			try (PreparedStatement pstmt = connection.prepareStatement(insertContactQuery, Statement.RETURN_GENERATED_KEYS))
			{
				pstmt.setString(1, contact.getFirstName());
				pstmt.setString(2, contact.getLastName());
				pstmt.setString(3, contact.getPhoneNumber());

				pstmt.executeUpdate();

				try (ResultSet generatedKeys = pstmt.getGeneratedKeys())
				{
					if (generatedKeys.next())
					{
						contact.setId(generatedKeys.getInt(1));

						if (!contact.getEvents().isEmpty() || contact.getEvents() != null)
						{
							for (Event event : contact.getEvents())
							{
								if (event.getId() == 0)
									insertRelatedEvents(contact.getEvents(), connection);

								insertRelationships(event.getId(), contact.getId(), connection);
							}
						}
					}
				}
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Dodaje powiązane wydarzenia, które zawiera dany kontakt, do bazy danych.
	 * Metoda przyjmuje listę wydarzeń, pobiera ich dane i wykonuje wsadowe dodanie
	 * ich do tabeli events. Dla każdego dodanego wydarzenia generowany jest
	 * identyfikator, który jest ustawiany w obiekcie wydarzenia.
	 *
	 * @param  events       Lista wydarzeń do dodania.
	 * @param  connection   Połączenie do bazy danych.
	 * @throws SQLException Jeśli wystąpi błąd podczas wykonywania zapytania SQL.
	 */
	private void insertRelatedEvents(List<Event> events, Connection connection) throws SQLException
	{
		String query = "INSERT INTO events (event_name, event_date, notification_offset, event_location, event_description, category_id) VALUES (?, ?, ?, ?, ?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			for (Event event : events)
			{
				pstmt.setString(1, event.getName());
				pstmt.setTimestamp(2, Timestamp.valueOf(event.getDate()));
				pstmt.setObject(3, event.getNotifyOffset() != null ? event.getNotifyOffset() : LocalTime.of(0, 0));
				pstmt.setString(4, event.getLocation());
				pstmt.setString(5, event.getDescription());
				pstmt.setObject(6, event.getCategory() != null ? event.getCategory().getId() : null);
				pstmt.addBatch();
			}

			pstmt.executeBatch();

			try (ResultSet generatedKeys = pstmt.getGeneratedKeys())
			{
				for (Event event : events)
				{
					if (generatedKeys.next())
						event.setId(generatedKeys.getInt(1));
				}
			}
		}
	}

	/**
	 * Dodaje powiązane kontakty, które zawiera dane wydarzenie, do bazy danych.
	 * Metoda przyjmuje listę kontaktów, pobiera ich dane i wykonuje wsadowe dodanie
	 * ich do tabeli contacts. Dla każdego dodanego kontaktu generowany jest
	 * identyfikator, który jest ustawiany w obiekcie kontaktu.
	 *
	 * @param  contacts     Lista kontaktów do dodania.
	 * @param  connection   Połączenie do bazy danych.
	 * @throws SQLException Jeśli wystąpi błąd podczas wykonywania zapytania SQL.
	 */
	private void insertRelatedContacts(List<Contact> contacts, Connection connection) throws SQLException
	{
		String query = "INSERT INTO contacts (first_name, last_name, phone_number) VALUES (?, ?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			for (Contact contact : contacts)
			{
				pstmt.setString(1, contact.getFirstName());
				pstmt.setString(2, contact.getLastName());
				pstmt.setString(3, contact.getPhoneNumber());

				pstmt.addBatch();
			}

			pstmt.executeBatch();

			try (ResultSet generatedKeys = pstmt.getGeneratedKeys())
			{
				for (Contact contact : contacts)
				{
					if (generatedKeys.next())
						contact.setId(generatedKeys.getInt(1));
				}
			}
		}
	}

	/**
	 * Dodaje relację między wydarzeniem a kontaktem do bazy danych. Metoda
	 * przyjmuje identyfikatory wydarzenia i kontaktu oraz wykonuje dodanie
	 * odpowiedniego wpisu do tabeli łączącej events_contacts, tworząc powiązanie
	 * między danym wydarzeniem, a kontaktem.
	 *
	 * @param  eventId      Identyfikator wydarzenia.
	 * @param  contactId    Identyfikator kontaktu.
	 * @param  connection   Połączenie do bazy danych.
	 * @throws SQLException Jeśli wystąpi błąd podczas wykonywania zapytania SQL.
	 */
	private void insertRelationships(int eventId, int contactId, Connection connection) throws SQLException
	{
		try (Statement stmt = connection.createStatement())
		{
			stmt.executeUpdate(String.format("INSERT INTO events_contacts (event_id, contact_id) VALUES (%d, %d)", eventId, contactId));
		}
	}

	/**
	 * Aktualizuje istniejącą kategorię w bazie danych na podstawie przekazanej
	 * zaktualizowanej kategorii. Metoda sprawdza istnienie kategorii o
	 * identyfikatorze zaktualizowanej kategorii w bazie danych. Jeśli kategoria
	 * istnieje, porównuje jej dane z danymi zaktualizowanej kategorii za pomocą
	 * {@link #isCategoryDataChanged(Category, String, String)}. Jeżeli dane są
	 * różne, dokonuje aktualizacji nazwy kategorii i koloru w bazie danych.
	 *
	 * @param  updatedCategory Zaktualizowana kategoria do zapisania w bazie danych.
	 * @throws SQLException    Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                         wykonywania zapytania SQL.
	 */
	public void updateCategory(Category updatedCategory) throws SQLException
	{
		if (updatedCategory.getId() == 0)
		{
			throw new SQLException("Category: [" + updatedCategory + "] is not exist in database!");
		}

		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String selectCategoryQuery = "SELECT * FROM categories WHERE id = ?";
			String updateCategoryQuery = "UPDATE categories SET category_name = ?, color_hex = ? WHERE id = ?";

			try (PreparedStatement selectStmt = connection.prepareStatement(selectCategoryQuery))
			{
				selectStmt.setInt(1, updatedCategory.getId());
				ResultSet resultSet = selectStmt.executeQuery();

				if (resultSet.next())
				{
					String categoryName = resultSet.getString("category_name");
					String colorHex = resultSet.getString("color_hex");

					if (isCategoryDataChanged(updatedCategory, categoryName, colorHex))
					{
						try (PreparedStatement updateStmt = connection.prepareStatement(updateCategoryQuery))
						{
							updateStmt.setString(1, updatedCategory.getName());
							updateStmt.setString(2, updatedCategory.getColorHex());
							updateStmt.setInt(3, updatedCategory.getId());

							updateStmt.executeUpdate();
						}
					}
				}

				resultSet.close();
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Aktualizuje istniejące wydarzenie w bazie danych na podstawie przekazanego
	 * zaktualizowanego wydarzenia. Metoda sprawdza istnienie wydarzenia o
	 * identyfikatorze zaktualizowanego wydarzenia w bazie danych. Jeśli wydarzenie
	 * istnieje, porównuje jego dane z danymi zaktualizowanego wydarzenia za pomocą
	 * {@link #isEventDataChanged(Event, String, LocalDateTime, LocalTime, String, String, int)}.
	 * Jeżeli dane są różne, dokonuje aktualizacji nazwy, daty, przesunięcia
	 * powiadomienia, lokalizacji, opisu oraz kategorii w bazie danych. Dodatkowo,
	 * aktualizuje powiązane kontakty, usuwając stare relacje i dodając nowe, jeśli
	 * lista kontaktów została zaktualizowana.
	 *
	 * @param  updatedEvent Zaktualizowane wydarzenie do zapisania w bazie danych.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL.
	 */
	public void updateEvent(Event updatedEvent) throws SQLException
	{
		if (updatedEvent.getId() == 0)
		{
			throw new SQLException("Event: [" + updatedEvent + "] is not exist in the database!");
		}

		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String selectEventQuery = "SELECT * FROM events WHERE id = ?";
			String updateEventQuery = "UPDATE events SET event_name = ?, event_date = ?, notification_offset = ?, event_location = ?, event_description = ?, category_id = ? WHERE id = ?";

			try (PreparedStatement selectStmt = connection.prepareStatement(selectEventQuery))
			{
				selectStmt.setInt(1, updatedEvent.getId());
				ResultSet resultSet = selectStmt.executeQuery();

				if (resultSet.next())
				{
					String eventName = resultSet.getString("event_name");
					LocalDateTime eventDate = resultSet.getTimestamp("event_date").toLocalDateTime();
					LocalTime notifyOffset = resultSet.getTime("notification_offset").toLocalTime();
					String eventLocation = resultSet.getString("event_location");
					String eventDescription = resultSet.getString("event_description");
					int categoryId = resultSet.getInt("category_id");
					List<Contact> currentContacts = getEventContacts(updatedEvent.getId(), connection);

					if (isEventDataChanged(updatedEvent, eventName, eventDate, notifyOffset, eventLocation, eventDescription, categoryId))
					{
						try (PreparedStatement updateStmt = connection.prepareStatement(updateEventQuery))
						{
							updateStmt.setString(1, updatedEvent.getName());
							updateStmt.setTimestamp(2, Timestamp.valueOf(updatedEvent.getDate()));
							updateStmt.setObject(3, updatedEvent.getNotifyOffset() != null ? updatedEvent.getNotifyOffset() : LocalTime.of(0, 0));
							updateStmt.setString(4, updatedEvent.getLocation());
							updateStmt.setString(5, updatedEvent.getDescription());
							updateStmt.setObject(6, updatedEvent.getCategory() != null ? updatedEvent.getCategory().getId() : null);
							updateStmt.setInt(7, updatedEvent.getId());

							updateStmt.executeUpdate();
						}
					}

					if (!currentContacts.equals(updatedEvent.getContacts()))
					{
						String deleteContactsQuery = "DELETE FROM events_contacts WHERE event_id = ?";

						try (PreparedStatement pstmt = connection.prepareStatement(deleteContactsQuery))
						{
							pstmt.setInt(1, updatedEvent.getId());
							pstmt.executeUpdate();
						}

						for (Contact contact : updatedEvent.getContacts())
						{
							insertRelationships(updatedEvent.getId(), contact.getId(), connection);
						}
					}
				}

				resultSet.close();
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Aktualizuje istniejący kontakt w bazie danych na podstawie przekazanego
	 * zaktualizowanego kontaktu. Metoda sprawdza istnienie kontaktu o
	 * identyfikatorze zaktualizowanego kontaktu w bazie danych. Jeśli kontakt
	 * istnieje, porównuje jego dane z danymi zaktualizowanego kontaktu za pomocą
	 * {@link #isContactDataChanged(Contact, String, String, String)}. Jeżeli dane
	 * są różne, dokonuje aktualizacji imienia, nazwiska oraz numeru telefonu w
	 * bazie danych. Dodatkowo, aktualizuje powiązane wydarzenia, usuwając stare
	 * relacje i dodając nowe, jeśli lista wydarzeń została zaktualizowana.
	 *
	 * @param  updatedContact Zaktualizowany kontakt do zapisania w bazie danych.
	 * @throws SQLException   Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                        wykonywania zapytania SQL.
	 */
	public void updateContact(Contact updatedContact) throws SQLException
	{
		if (updatedContact.getId() == 0)
		{
			throw new SQLException("Contact: [" + updatedContact + "] is not exist in database!");
		}

		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String selectContactQuery = "SELECT * FROM contacts WHERE id = ?";
			String updateContactQuery = "UPDATE contacts SET first_name = ?, last_name = ?, phone_number = ? WHERE id = ?";

			try (PreparedStatement selectStmt = connection.prepareStatement(selectContactQuery))
			{
				selectStmt.setInt(1, updatedContact.getId());
				ResultSet resultSet = selectStmt.executeQuery();

				if (resultSet.next())
				{
					String firstName = resultSet.getString("first_name");
					String lastName = resultSet.getString("last_name");
					String phoneNumber = resultSet.getString("phone_number");
					List<Event> currentEvents = getContactEvents(updatedContact.getId(), connection);

					if (isContactDataChanged(updatedContact, firstName, lastName, phoneNumber))
					{
						try (PreparedStatement updateStmt = connection.prepareStatement(updateContactQuery))
						{
							updateStmt.setString(1, updatedContact.getFirstName());
							updateStmt.setString(2, updatedContact.getLastName());
							updateStmt.setString(3, updatedContact.getPhoneNumber());
							updateStmt.setInt(4, updatedContact.getId());

							updateStmt.executeUpdate();
						}
					}

					if (!currentEvents.equals(updatedContact.getEvents()))
					{
						String deleteEventsQuery = "DELETE FROM events_contacts WHERE contact_id = ?";

						try (PreparedStatement pstmt = connection.prepareStatement(deleteEventsQuery))
						{
							pstmt.setInt(1, updatedContact.getId());
							pstmt.executeUpdate();
						}

						for (Event event : updatedContact.getEvents())
						{
							insertRelationships(event.getId(), updatedContact.getId(), connection);
						}
					}
				}

				resultSet.close();
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Dezaktywuje kategorię w bazie danych, ustawiając atrybut is_active na false
	 * dla kategorii o podanym identyfikatorze. Metoda nie usuwa fizycznie rekordu,
	 * lecz deaktywuje go, co pozwala na zachowanie historii kategorii w przypadku
	 * potrzeby przywrócenia.
	 *
	 * @param  category     Kategoria do dezaktywacji.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL.
	 */
	public void deleteCategory(Category category) throws SQLException
	{
		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String updateCategoryQuery = "UPDATE categories SET is_active = false WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(updateCategoryQuery))
			{
				pstmt.setInt(1, category.getId());
				pstmt.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Dezaktywuje wydarzenie w bazie danych, ustawiając atrybut is_active na false
	 * dla wydarzenia o podanym identyfikatorze. Metoda nie usuwa fizycznie rekordu,
	 * lecz deaktywuje go, co pozwala na zachowanie historii wydarzeń w przypadku
	 * potrzeby przywrócenia.
	 *
	 * @param  event        Wydarzenie do dezaktywacji.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL.
	 */
	public void deleteEvent(Event event) throws SQLException
	{
		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String updateEventQuery = "UPDATE events SET is_active = false WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(updateEventQuery))
			{
				pstmt.setInt(1, event.getId());
				pstmt.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Dezaktywuje kontakt w bazie danych, ustawiając atrybut is_active na false dla
	 * kontaktu o podanym identyfikatorze. Metoda nie usuwa fizycznie rekordu, lecz
	 * deaktywuje go, co pozwala na zachowanie historii kontaktów w przypadku
	 * potrzeby przywrócenia.
	 *
	 * @param  contact      Kontakt do dezaktywacji.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL.
	 */
	public void deleteContact(Contact contact) throws SQLException
	{
		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String updateContactQuery = "UPDATE contacts SET is_active = false WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(updateContactQuery))
			{
				pstmt.setInt(1, contact.getId());
				pstmt.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Usuwa z bazy danych wszystkie wydarzenia, których data jest starsza niż
	 * podana docelowa data. Dodatkowo, usuwa powiązania z tabeli łączącej
	 * events_contacts dla usuniętych wydarzeń. Metoda wykonuje te operacje w jednej
	 * transakcji, co zapewnia spójność danych.
	 *
	 * @param  targetDate   Docelowa data, wydarzenia wcześniejsze niż ta zostaną
	 *                      usunięte.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL.
	 */
	public void deleteOldEvents(LocalDateTime targetDate) throws SQLException
	{
		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String deleteEventsQuery = "DELETE FROM events WHERE event_date < ?";
			String deleteEventsContactsQuery = "DELETE FROM events_contacts WHERE event_id IN (SELECT id FROM events WHERE event_date < ?)";

			try (PreparedStatement deleteEventsStmt = connection.prepareStatement(deleteEventsQuery);
					PreparedStatement deleteEventsContactsStmt = connection.prepareStatement(deleteEventsContactsQuery))
			{
				connection.setAutoCommit(false);

				try
				{
					deleteEventsContactsStmt.setTimestamp(1, Timestamp.valueOf(targetDate));
					deleteEventsContactsStmt.executeUpdate();

					deleteEventsStmt.setTimestamp(1, Timestamp.valueOf(targetDate));
					deleteEventsStmt.executeUpdate();

					connection.commit();
				}
				catch (SQLException ex)
				{
					connection.rollback();
					throw ex;
				}
				finally
				{
					connection.setAutoCommit(true);
				}
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Wybiera i wyświetla aktywne rekordy z podanej tabeli w bazie danych.
	 *
	 * @param tableName Nazwa tabeli, z której mają być pobrane rekordy.
	 */
	public void select(String tableName)
	{
		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			String selectQuery = "SELECT * FROM " + tableName + " WHERE is_active = true";
			try (PreparedStatement pstmt = connection.prepareStatement(selectQuery))
			{
				ResultSet resultSet = pstmt.executeQuery();

				ResultSetMetaData metaData = resultSet.getMetaData();
				int columnCount = metaData.getColumnCount();

				while (resultSet.next())
				{
					StringBuilder row = new StringBuilder();
					for (int i = 1; i <= columnCount; i++)
					{
						row.append(metaData.getColumnName(i)).append(": ").append(resultSet.getString(i)).append("\t");
					}
					System.out.println(row.toString());
				}

				resultSet.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Synchronizuje lokalne dane z bazą danych, uzupełniając listy kategorii,
	 * wydarzeń i kontaktów. Przed główną synchronizacją sprawdza czy przekazane
	 * listy zawierają dane, jeżeli tak to sprawdza je w funkcji
	 * {@link #checkXMLData(List, List, List)} i następnie analizuje czy te dane
	 * należy wstawić lub zaktualizować, na koniec czyści te listy i wykonuje
	 * synchronizację między lokalnymi danymi, a bazą danych.
	 *
	 * @param  categories   Lista kategorii, która zostanie zaktualizowana danymi z
	 *                      bazy.
	 * @param  events       Lista wydarzeń, która zostanie zaktualizowana danymi z
	 *                      bazy.
	 * @param  contacts     Lista kontaktów, która zostanie zaktualizowana danymi z
	 *                      bazy.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL.
	 */
	public void synchronize(List<Category> categories, List<Event> events, List<Contact> contacts) throws SQLException
	{
		try (Connection connection = DriverManager.getConnection(url, user, password))
		{
			checkXMLData(categories, events, contacts);

			categories.clear();
			events.clear();
			contacts.clear();

			String selectCategoriesQuery = "SELECT * FROM categories WHERE is_active = true";
			String selectEventsQuery = "SELECT * FROM events WHERE is_active = true";
			String selectContactsQuery = "SELECT * FROM contacts WHERE is_active = true";
			String selectEventsContactsQuery = "SELECT * FROM events_contacts";

			try (PreparedStatement pstmtCategories = connection.prepareStatement(selectCategoriesQuery))
			{
				ResultSet rsCategories = pstmtCategories.executeQuery();

				while (rsCategories.next())
				{
					Category category = new Category();
					category.setId(rsCategories.getInt("id"));
					category.setName(rsCategories.getString("category_name"));
					category.setColorHex(rsCategories.getString("color_hex"));
					categories.add(category);
				}

				rsCategories.close();
			}

			try (PreparedStatement pstmtEvents = connection.prepareStatement(selectEventsQuery))
			{
				ResultSet rsEvents = pstmtEvents.executeQuery();

				while (rsEvents.next())
				{
					Event event = new Event();
					event.setId(rsEvents.getInt("id"));
					event.setName(rsEvents.getString("event_name"));
					event.setDate(rsEvents.getTimestamp("event_date").toLocalDateTime());
					event.setNotifyOffset(rsEvents.getTime("notification_offset").toLocalTime());
					event.setLocation(rsEvents.getString("event_location"));
					event.setDescription(rsEvents.getString("event_description"));

					int categoryId = rsEvents.getInt("category_id");
					Category category = categories.stream().filter(c -> c.getId() == categoryId).findFirst().orElse(null);
					event.setCategory(category);

					events.add(event);
				}

				rsEvents.close();
			}

			try (PreparedStatement pstmtContacts = connection.prepareStatement(selectContactsQuery))
			{
				ResultSet rsContacts = pstmtContacts.executeQuery();

				while (rsContacts.next())
				{
					Contact contact = new Contact();
					contact.setId(rsContacts.getInt("id"));
					contact.setFirstName(rsContacts.getString("first_name"));
					contact.setLastName(rsContacts.getString("last_name"));
					contact.setPhoneNumber(rsContacts.getString("phone_number"));
					contacts.add(contact);
				}

				rsContacts.close();
			}

			try (PreparedStatement pstmtEventsContacts = connection.prepareStatement(selectEventsContactsQuery))
			{
				ResultSet rsEventsContacts = pstmtEventsContacts.executeQuery();

				while (rsEventsContacts.next())
				{
					int eventId = rsEventsContacts.getInt("event_id");
					int contactId = rsEventsContacts.getInt("contact_id");

					Event event = events.stream().filter(e -> e.getId() == eventId).findFirst().orElse(null);
					Contact contact = contacts.stream().filter(c -> c.getId() == contactId).findFirst().orElse(null);

					if (event != null && contact != null)
					{
						event.addContact(contact);
						contact.addEvent(event);
					}
				}

				rsEventsContacts.close();
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
	}

	/**
	 * Wywołuje następujące funkcje: {@link #checkXMLCategories(List)}
	 * {@link #checkXMLEvent(List)} {@link #checkXMLContact(List)}
	 *
	 * @param  categories   Lista kategorii do sprawdzenia i aktualizacji.
	 * @param  events       Lista wydarzeń do sprawdzenia i aktualizacji.
	 * @param  contacts     Lista kontaktów do sprawdzenia i aktualizacji.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL.
	 */
	private void checkXMLData(List<Category> categories, List<Event> events, List<Contact> contacts) throws SQLException
	{
		checkXMLCategories(categories);
		checkXMLEvent(events);
		checkXMLContact(contacts);
	}

	/**
	 * Sprawdza i aktualizuje dane z listy {@code List<Category>} dla kategorii.
	 * Jeśli przekazana lista zawiera dane, sprawdza je pod kątem konieczności
	 * wstawienia lub zaktualizowania w bazie danych.
	 *
	 * @param  categories   Lista kategorii do sprawdzenia i aktualizacji.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL w
	 *                      {@link #insertCategory(Category)} i
	 *                      {@link #updateCategory(Category)}
	 */
	private void checkXMLCategories(List<Category> categories) throws SQLException
	{
		if (categories == null)
			return;

		for (Category category : categories)
		{
			if (category.getId() == 0)
			{
				insertCategory(category);
			}
			else
			{
				updateCategory(category);
			}
		}
	}

	/**
	 * Sprawdza i aktualizuje dane z listy {@code List<Event>} dla wydarzeń. Jeśli
	 * przekazana lista zawiera dane, sprawdza je pod kątem konieczności wstawienia
	 * lub zaktualizowania w bazie danych.
	 *
	 * @param  events       Lista wydarzeń do sprawdzenia i aktualizacji.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL w {@link #insertEvent(Event)}
	 *                      i {@link #updateEvent(Event)}
	 */
	private void checkXMLEvent(List<Event> events) throws SQLException
	{
		if (events == null)
			return;

		for (Event event : events)
		{
			if (event.getId() == 0)
			{
				insertEvent(event);
			}
			else
			{
				updateEvent(event);
			}
		}
	}

	/**
	 * Sprawdza i aktualizuje dane z listy {@code List<Contact>} dla kontaktów.
	 * Jeśli przekazana lista zawiera dane, sprawdza je pod kątem konieczności
	 * wstawienia lub zaktualizowania w bazie danych.
	 *
	 * @param  contacts     Lista kontaktów do sprawdzenia i aktualizacji.
	 * @throws SQLException Jeśli wystąpi błąd dostępu do bazy danych lub podczas
	 *                      wykonywania zapytania SQL w
	 *                      {@link #insertContact(Contact)} i
	 *                      {@link #updateContact(Contact)}
	 */
	private void checkXMLContact(List<Contact> contacts) throws SQLException
	{
		if (contacts == null)
			return;

		for (Contact contact : contacts)
		{
			if (contact.getId() == 0)
			{
				insertContact(contact);
			}
			else
			{
				updateContact(contact);
			}
		}
	}
}
