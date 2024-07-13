package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Reprezentuje kontakt w kalendarzu. Posiada identyfikator, imię, nazwisko,
 * numer telefonu oraz listę wydarzeń powiązanych z kontaktem. Implementuje
 * interfejs Comparable, umożliwiając sortowanie kontaktów po imieniu.
 */
public class Contact implements Comparable<Contact>
{
	private int id;
	private String firstName;
	private String lastName;
	private String phoneNumber;
	private List<Event> events = new ArrayList<Event>();

	/**
	 * Pusty konstuktor klasy {@link Contact}, potrzebny na rzecz serializacji
	 * danych.
	 */
	public Contact()
	{

	}

	/**
	 * Zwraca tekstową reprezentację obiektu {@link model.Contact}, włączając imię,
	 * nazwisko i numer telefonu.
	 *
	 * @return Tekstowa reprezentacja kontaktu.
	 */
	@Override
	public String toString()
	{
		return String.format("%s %s | %s", firstName, lastName, phoneNumber);
	}

	/**
	 * @return Identyfikator kontaktu.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Ustawia identyfikator kontaktu.
	 *
	 * @param id Nowy identyfikator kontaktu.
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return Imię kontaktu.
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * Ustawia imię kontaktu.
	 *
	 * @param firstName Nowe imię kontaktu.
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * @return Nazwisko kontaktu.
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * Ustawia nazwisko kontaktu.
	 *
	 * @param lastName Nowe nazwisko kontaktu.
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	/**
	 * @return Numer telefonu kontaktu.
	 */
	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	/**
	 * Ustawia numer telefonu kontaktu.
	 *
	 * @param phoneNumber Nowy numer telefonu kontaktu.
	 */
	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return Lista wydarzeń powiązanych z kontaktem.
	 */
	public List<Event> getEvents()
	{
		return this.events;
		// return new ArrayList<Event>(this.events);
	}

	/**
	 * Ustawia listę wydarzeń powiązanych z kontaktem. Aktualna lista wydarzeń
	 * zostanie zastąpiona nową listą wydarzeń, gdzie dodatkowo, aktualizuje także
	 * listę kontaktów powiązanych z dodawanym wydarzeniem o ten kontakt.
	 *
	 * @param events Nowa lista wydarzeń do ustawienia.
	 */
	public void setEvents(List<Event> events)
	{
		this.events = events;

		for (Event event : events)
		{
			event.addContact(this);
		}
	}

	/**
	 * Dodaje wydarzenie do listy powiązanych z kontaktem wydarzeń, jeśli nie jest
	 * już obecne na liście i dodatkowo, aktualizuje także listę kontaktów
	 * powiązanych z dodawanym wydarzeniem o ten kontakt.
	 *
	 * @param event Wydarzenie do dodania.
	 */
	public void addEvent(Event event)
	{
		if (!this.events.contains(event))
		{
			this.events.add(event);
			event.addContact(this);
		}
	}

	/**
	 * Usuwa wydarzenie z listy powiązanych z kontaktem wydarzeń, jeśli istnieje na
	 * liście. Dodatkowo, aktualizuje także listę kontaktów powiązanych z usuwanym
	 * wydarzeniem usuwając w niej ten kontakt.
	 *
	 * @param event Wydarzenie do usunięcia.
	 */
	public void removeEvent(Event event)
	{
		if (this.events.contains(event))
		{
			this.events.remove(event);
			event.removeContact(this);
		}
	}

	@Override
	public int compareTo(Contact o)
	{
		return this.firstName.compareTo(o.getFirstName());
	}
}