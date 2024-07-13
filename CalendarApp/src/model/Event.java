package model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Reprezentuje wydarzenie w kalendarzu. Posiada identyfikator, nazwę, datę,
 * czas powiadomienia, lokalizację, opis, kategorię oraz listę kontaktów
 * powiązanych z wydarzeniem. Implementuje interfejs Comparable, umożliwiając
 * sortowanie wydarzeń po dacie.
 */
public class Event implements Comparable<Event>
{
	private int id;
	private String name;
	private LocalDateTime date;
	private LocalTime notifyOffset;
	private String location;
	private String description;
	private Category category;
	private List<Contact> contacts = new ArrayList<Contact>();

	/**
	 * Pusty konstuktor klasy {@link Event}, potrzebny na rzecz serializacji danych.
	 */
	public Event()
	{

	}

	/**
	 * Zwraca tekstową reprezentację obiektu {@link model.Event}, włączając nazwę,
	 * sformatowaną datę, lokalizację, opis i nazwę kategorii.
	 *
	 * @return Tekstowa reprezentacja obiektu wydarzenia.
	 */
	@Override
	public String toString()
	{
		String locationStr = location.isEmpty() ? "No location" : location;
		String descriptionStr = description.isEmpty() ? "No description" : description;
		String categoryName = category != null ? category.getName() : "No category";

		return String.format("%s | %s | %s%n%s%n%s", name, getFormattedDate(), locationStr, descriptionStr, categoryName);
	}

	@Override
	public int compareTo(Event o)
	{
		return this.date.compareTo(o.getDate());
	}

	/**
	 * @return Identyfikator wydarzenia.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Ustawia identyfikator wydarzenia.
	 *
	 * @param id Nowy identyfikator wydarzenia.
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return Nazwa wydarzenia.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Ustawia nazwę wydarzenia.
	 *
	 * @param name Nowa nazwa wydarzenia.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return Data wydarzenia.
	 */
	public LocalDateTime getDate()
	{
		return date;
	}

	/**
	 * Ustawia datę wydarzenia.
	 *
	 * @param date Nowa data wydarzenia.
	 */
	public void setDate(LocalDateTime date)
	{
		this.date = date;
	}

	/**
	 * @return Czas odstępu powiadomienia.
	 */
	public LocalTime getNotifyOffset()
	{
		return notifyOffset;
	}

	/**
	 * Ustawia czas odstępu powiadomienia przed wydarzeniem.
	 *
	 * @param notifyOffset Nowy czas odstępu powiadomienia.
	 */
	public void setNotifyOffset(LocalTime notifyOffset)
	{
		this.notifyOffset = notifyOffset;
	}

	/**
	 * @return Lokalizacja wydarzenia.
	 */
	public String getLocation()
	{
		return location;
	}

	/**
	 * Ustawia lokalizację wydarzenia.
	 *
	 * @param location Nowa lokalizacja wydarzenia.
	 */
	public void setLocation(String location)
	{
		this.location = location;
	}

	/**
	 * @return Opis wydarzenia.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Ustawia opis wydarzenia.
	 *
	 * @param description Nowy opis wydarzenia.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return Kategoria wydarzenia.
	 */
	public Category getCategory()
	{
		return category;
	}

	/**
	 * Ustawia kategorię wydarzenia.
	 *
	 * @param category Nowa kategoria wydarzenia.
	 */
	public void setCategory(Category category)
	{
		this.category = category;
	}

	/**
	 * @return Lista kontaktów powiązanych z wydarzeniem
	 */
	public List<Contact> getContacts()
	{
		return contacts;
		// return new ArrayList<Contact>(this.contacts);
	}

	/**
	 * Ustawia listę kontaktów powiązanych z wydarzeniem. Aktualna lista kontaktów
	 * zostanie zastąpiona nową listą kontaktów, gdzie dodatkowo, aktualizuje także
	 * listę wydarzeń powiązanych z dodawanym kontaktem o te wydarzenie.
	 *
	 * @param contacts Nowa lista kontaktów do ustawienia.
	 */
	public void setContacts(List<Contact> contacts)
	{
		this.contacts = contacts;

		for (Contact contact : contacts)
		{
			contact.addEvent(this);
		}
	}

	/**
	 * Dodaje kontakt do listy powiązanych z wydarzeniem kontaktów, jeśli nie jest
	 * już obecny na liście i dodatkowo, aktualizuje także listę wydarzeń
	 * powiązanych z dodawanym kontaktem o te wydarzenie.
	 *
	 * @param contact Kontakt do dodania.
	 */
	public void addContact(Contact contact)
	{
		if (!this.contacts.contains(contact))
		{
			this.contacts.add(contact);
			contact.addEvent(this);
		}
	}

	/**
	 * Usuwa kontakt z listy powiązanych z wydarzeniem kontaktów, jeśli istnieje na
	 * liście. Dodatkowo, aktualizuje także listę wydarzeń powiązanych z usuwanym
	 * kontaktem usuwając w niej te wydarzenie.
	 *
	 * @param contact Kontakt do usunięcia.
	 */
	public void removeContact(Contact contact)
	{
		if (this.contacts.contains(contact))
		{
			this.contacts.remove(contact);
			contact.removeEvent(this);
		}
	}

	/**
	 * Zwraca sformatowaną datę wydarzenia w formie "dd.MM.yyyy HH:mm".
	 *
	 * @return Sformatowana data wydarzenia.
	 */
	public String getFormattedDate()
	{
		return this.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
	}

	/**
	 * Zwraca sformatowaną datę wydarzenia z uwzględnieniem odstępu czasowego
	 * powiadomienia, w formie "dd.MM.yyyy HH:mm".
	 *
	 * @return Sformatowana data wydarzenia z uwzględnieniem odstępu czasowego
	 *         powiadomienia.
	 */
	public String getFormattedDateWithOffset()
	{
		LocalDateTime offsetDateTime = this.date.minusHours(notifyOffset.getHour()).minusMinutes(notifyOffset.getMinute());
		return offsetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
	}

	/**
	 * Zwraca datę wydarzenia z uwzględnieniem odstępu czasowego powiadomienia.
	 *
	 * @return Data wydarzenia z uwzględnieniem odstępu czasowego powiadomienia.
	 */
	public LocalDateTime getDateWithOffset()
	{
		LocalDateTime offsetDateTime = this.date.minusHours(notifyOffset.getHour()).minusMinutes(notifyOffset.getMinute());
		return offsetDateTime;
	}
}