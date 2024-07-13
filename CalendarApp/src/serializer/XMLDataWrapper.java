package serializer;

import java.util.List;

import model.Category;
import model.Contact;
import model.Event;

/**
 * Klasa {@link serializer.XMLDataWrapper} reprezentuje opakowanie danych, które
 * zawiera trzy listy obiektów: kategorie {@link model.Category}, wydarzenia
 * {@link model.Event} i kontakty {@link model.Contact}. Jest używana podczas
 * procesu deserializacji, aby grupować te listy i ułatwiać operacje na danych w
 * ramach aplikacji. Klasa ta przechowuje referencje do list kategorii, wydarzeń
 * i kontaktów. Udostępnia również metody dostępu do tych list, takie jak
 * {@link #getCategories()}, {@link #getEvents()} i {@link #getContacts()}.
 * Obiekty tej klasy są tworzone podczas procesu deserializacji danych z pliku
 * XML przy użyciu {@link serializer.XMLDeserializer}. Wspomaga ona strukturę
 * danych w aplikacji, ułatwiając dostęp do kategorii, wydarzeń i kontaktów po
 * deserializacji.
 */
public class XMLDataWrapper
{
	private final List<Category> categoryList;
	private final List<Event> eventList;
	private final List<Contact> contactList;

	/**
	 * Ten konstruktor inicjalizuje instancję XMLDataWrapper przy użyciu
	 * dostarczonych list obiektów kategorii, zdarzeń i kontaktów. Przechowuje
	 * referencję przekazanych list, dzięki czemu umożliwia tym synchronizację z
	 * opakowywanymi danymi, które zawierają przekazane listy, dla efektywnego
	 * zapisu i odczytu ich w formacie XML.
	 *
	 * @param categoryList Lista obiektów kategorii, które zostaną opakowane.
	 * @param eventList    Lista obiektów zdarzeń, które zostaną opakowane.
	 * @param contactList  Lista obiektów kontaktów, które zostaną opakowane.
	 */
	public XMLDataWrapper(List<Category> categoryList, List<Event> eventList, List<Contact> contactList)
	{
		this.categoryList = categoryList;
		this.eventList = eventList;
		this.contactList = contactList;
	}

	/**
	 * @return Lista kategorii.
	 */
	public List<Category> getCategories()
	{
		return this.categoryList;
	}

	/**
	 * @return Lista wydarzeń.
	 */
	public List<Event> getEvents()
	{
		return this.eventList;
	}

	/**
	 * @return Lista kontaktów.
	 */
	public List<Contact> getContacts()
	{
		return this.contactList;
	}
}